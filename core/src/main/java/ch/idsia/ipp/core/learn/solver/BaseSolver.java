package ch.idsia.ipp.core.learn.solver;

import ch.idsia.ipp.core.common.BayesianNetwork;
import ch.idsia.ipp.core.common.arcs.Directed;
import ch.idsia.ipp.core.learn.solver.ps.Provider;
import ch.idsia.ipp.core.learn.solver.samp.Sampler;
import ch.idsia.ipp.core.learn.solver.src.Searcher;
import ch.idsia.ipp.core.utils.ParentSet;
import ch.idsia.ipp.core.utils.data.array.TDoubleArrayList;

import java.io.IOException;
import java.io.Writer;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static ch.idsia.ipp.core.utils.RandomStuff.*;
import static ch.idsia.ipp.core.utils.data.ArrayUtils.sameArray;

public abstract class BaseSolver {

    private final Logger log = Logger.getLogger(BaseSolver.class.getName());

    /**
     * Best structure found yet
     */
    public double best_sk;

    public boolean testAcycility = false;

    /**
     * Best structure found yet
     */
    public ParentSet[] best_str;

    /**
     * Flag verbosity
     */
    public int verbose;

    public Writer logWr;

    /**
     * Maximum execution time
     */
    public Integer max_exec_time = 10;

    /**
     * Maximum number of threads to use
     */
    public Integer thread_pool_size = 0;

    // Lock for concurrent searchers
    protected final Object lock = new Object();

    protected long start;

    public int n_var;

    protected boolean atLeastOne = false;

    public String res_path;

    private TDoubleArrayList sk_proposed;

    protected int numIter;

    public int out_solutions = 1;

    TreeSet<Solution> open;

    private double worstSolScore;

    private Provider pro;

    public ParentSet[][] scores;

    private Sampler smp;

    protected abstract String name();

    protected void prepare() {

        if (thread_pool_size == 0) {
            thread_pool_size = Runtime.getRuntime().availableProcessors();
        }

        start = System.currentTimeMillis();

        if (verbose > 0) {
            log("Starting " + name() + "\n");
            log("max_exec_time: " + max_exec_time + "\n");
            log("thread_pool_size: " + thread_pool_size + "\n");

            sk_proposed = new TDoubleArrayList();
        }

        best_sk = -Double.MAX_VALUE;
        best_str = new ParentSet[n_var];
        for (int i = 0; i < n_var; i++) {
            best_str[i] = new ParentSet(0, new int[0]);
        }

        if (out_solutions > 1)
            open = new TreeSet<Solution>();

        counter = System.currentTimeMillis();

    }

    public void go(String path) throws Exception {
        this.res_path = path;

        go();

        writeStructure(res_path);
    }

    protected void writeStructure(String res_path) {
        writeStructure(res_path, best_sk, best_str);
        if (out_solutions > 1) {
            int i = 0;
            for (Solution s: open) {
                writeStructure(f("%s-%d", res_path, i++), s.sk, s.str);
            }
        }
    }

    public void go()  {

        prepare();

        pro = getProvider();

        smp = getSampler();
        smp.init();

        // Obtains the parent set to optimize
        if (verbose > 0)
            log("Read scores... \n");
        scores = pro.getParentSets();

        almost();

        try {
            ExecutorService es = Executors.newCachedThreadPool();

            for (int i = 0; i < thread_pool_size; i++) {
                if (verbose > 0)
                    logf("Starting %d searcher \n", i);
                es.execute(getNewSearcher(i));
            }

            es.shutdown();
            es.awaitTermination(Integer.MAX_VALUE, TimeUnit.MINUTES);

            conclude();
        } catch (InterruptedException e) {
            logExp(log, e);
        }


    }

    protected void almost() {
    }

    protected abstract Sampler getSampler();

    protected abstract Searcher getSearcher();

    protected abstract Provider getProvider();

    public boolean still_time = true;

    public double elapsed;

    protected long counter;

    public void checkTime() {

        elapsed = (System.currentTimeMillis() - start) / 1000.0;

        if (max_exec_time > 0 && elapsed > max_exec_time) {
            still_time = false;
        }
    }

    protected void conclude() {

        if (verbose > 0) {
            log("... Ended! \n");
            logf("Num Iterations total: %d \n", numIter);
        }

        if (verbose > 1) {
            logf("Num scores found: %d \n", sk_proposed.size());
            logf("Mean scores found: %.2f \n", sk_proposed.mean());
            logf("Median scores found: %.2f \n", sk_proposed.median());
        }
    }

    public void logf(String format, Object... args) {
        log(String.format(format, args));
    }

    public void log(String s) {
        try {
            if (logWr != null) {
                logWr.write(s);
                logWr.flush();
            }
            else
                System.out.print(s);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void writeGraph(String s, String[] nm) {
        Directed d = new Directed(best_str);
        d.names = nm;
        d.graph(s);
    }

    public void writeGraph(String s) {
        Directed d = new Directed(best_str);
        d.graph(s);
    }

    /**
     * Write the best structure found so far.
     *
     * @throws IOException if there is an error writing
     */
    public void writeStructure(String s, double sk, ParentSet[] str) {

        if (!atLeastOne) {
            return;
        }

        Writer w;

        try {
            w = getWriter(s);


        // writer.graph("Quick Structure: " + printQuick(best_str));

        // writer.graph("\n\nExpanded Structure: \n\n");
        if (str != null && str.length > 0) {
            for (int i = 0; i < str.length; i++) {

                ParentSet pSet = str[i];

                if (pSet != null)
                wf(w, "%d: %.2f %s\n", i, pSet.sk,
                        (pSet.parents.length > 0
                                ? " (" + combine(pSet.parents, ",") + ")"
                                : ""));
                else
                    wf(w, "%d: 0 0\n", i);
            }
        }

        wf(w, "\nScore: %.3f \n", sk);

        w.flush();
            w.close();
        } catch (IOException e) {
            logExp(log, e);
        }

    }

    /**
     * Combine the parent set for output
     *
     * @param s parent set in array form
     * @param d delimiter
     * @return parent set in mdl form
     */
    private String combine(int[] s, String d) {
        int k = s.length;

        if (k == 0) {
            return "";
        }
        StringBuilder out = new StringBuilder();

        out.append(s[0]);
        for (int x = 1; x < k; ++x) {
            out.append(d).append(s[x]);
        }
        return out.toString();
    }

    public String printQuick(ParentSet[] best_str) {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < n_var; i++) {
            s.append(String.format("[%d", i));
            ParentSet pSet = best_str[i];

            if (pSet.parents.length > 0) {
                s.append("|");
                s.append(combine(pSet.parents, ":"));
            }
            s.append("]");
        }

        return s.toString();
    }

    /**
     * Check that the resulting network is acyclic
     *
     * @return if the network is acyclic
     */
    public boolean testAcyclic(ParentSet[] new_str) {
        BayesianNetwork bn = new BayesianNetwork(new_str.length);

        // System.out.println(Arrays.toString(new_str));
        int i = 0;
        for (ParentSet aStr : new_str) {
            // System.out.println(thread + " " + aStr);
            bn.l_parent_var[i++] = aStr.parents;
        }
        return bn.isAcyclic();
    }


    public void newStructure(ParentSet[] new_str) {

        if (new_str == null)
            return;

        synchronized (lock) {

            numIter++;

            double new_sk = getSk(new_str);

            if (testAcycility && !testAcyclic(new_str)) {
                log.severe("Network CYCLIC!");
            }

            if (testAcycility && !testComplete(new_str)) {
                log.severe("Network NOT COMPLETE!");
            }

            if (verbose > 0)
                sk_proposed.add(new_sk);

            if (new_sk > best_sk) {

                checkTime();

                if (verbose > 0) {
                    logf("New improvement! %.5f (after %.1f s.)\n", new_sk,
                            elapsed);
                }

                best_sk = new_sk;

                best_str = new_str.clone();

                atLeastOne = true;

                if (res_path != null) {
                    writeStructure(res_path, best_sk, best_str);
                }

                // best_order = vars.clone();
            }

            if (verbose > 1)
                if ((System.currentTimeMillis() - counter) / 1000.0 > 1) {
                    logf("%.5f %.1f \n", best_sk, elapsed);
                    counter = System.currentTimeMillis();
                }

            // logf("%d \n", numIter);

            if (out_solutions > 1) {
                propose(new_sk, new_str);
            }
        }
    }

    private boolean testComplete(ParentSet[] new_str) {
        for (int i = 0; i <n_var; i++) {
            ParentSet ps = new_str[i];
            if (ps == null)
                return false;
            if (ps.sk > scores[i][0].sk)
                return false;
        }
        return true;
    }

    private void propose(double new_sk, ParentSet[] new_str) {

        boolean toDropWorst = false;

        if (open.size() > (out_solutions + 1)) {

            if (new_sk < worstSolScore) {
                // log.conclude("pruned");
                return;
            }

            toDropWorst = true;
        }

        for (Solution s: open) {
            if (s.same(new_str))
                return;
        }

        // Drop worst element in queue, to make room!
        if (toDropWorst) {
            open.pollLast();
            worstSolScore = open.last().sk;
        } else // If we didn't drop any element, check if we have to update the current
            // worst score!
            if (new_sk < worstSolScore) {
                worstSolScore = new_sk;
            }

        open.add(new Solution(new_sk, new_str));
    }

    protected double getSk(ParentSet[] new_str) {
        double new_sk = 0;
        for (ParentSet p: new_str) {
            new_sk += p.sk;
        }
        return new_sk;
    }

    protected long availableTime() {
        return max_exec_time * 1000 - (System.currentTimeMillis() - start);
    }

    public BaseSearcher getNewSearcher(int i) {
        return new BaseSearcher(this, i);
    }

    public class BaseSearcher implements Runnable {

        protected final BaseSolver solver;

        protected final int thread;

        public BaseSearcher(BaseSolver solver, int thread) {
            this.solver = solver;
            this.thread = thread;
        }

        private Searcher src;

        @Override
        public void run() {

            src = getSearcher();

            src.init(scores);

            while (still_time) {

                // Get a new ordering
                int[] vars = smp.sample();
                // Obtains a new candidate solution
                ParentSet[] str = src.search(vars);
                // Propose the new solution
                newStructure(str);

                checkTime();
            }

        }
    }

    private static class Solution implements Comparable<Solution>{
        private final ParentSet[] str;
        private final double sk;

        public Solution(double sk, ParentSet[] str) {
            this.sk = sk;
            this.str = str;
        }

        @Override
        public int compareTo(Solution other) {
            if (sk < other.sk) {
                return 1;
            }
            return -1;
        }

        public boolean same(ParentSet[] o_str) {

            if (str.length != o_str.length)
                return  false;

            for (int i = 0; i < str.length; i++) {
                if (!sameArray(str[i].parents, o_str[i].parents))
                    return false;
            }

            return true;
        }
    }
}
