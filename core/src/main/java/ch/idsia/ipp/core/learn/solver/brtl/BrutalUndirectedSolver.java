package ch.idsia.ipp.core.learn.solver.brtl;

import ch.idsia.ipp.core.common.arcs.Und;
import ch.idsia.ipp.core.learn.solver.BaseSolver;
import ch.idsia.ipp.core.learn.solver.ps.NullProvider;
import ch.idsia.ipp.core.learn.solver.ps.Provider;
import ch.idsia.ipp.core.learn.solver.samp.Sampler;
import ch.idsia.ipp.core.learn.solver.samp.SimpleSampler;
import ch.idsia.ipp.core.learn.solver.src.BrutalUndirectedSearcher;
import ch.idsia.ipp.core.learn.solver.src.Searcher;
import ch.idsia.ipp.core.utils.data.hash.TIntIntHashMap;

import java.io.File;
import java.io.PrintWriter;
import java.util.TreeSet;

import static ch.idsia.ipp.core.utils.RandomStuff.f;
import static ch.idsia.ipp.core.utils.RandomStuff.wf;
import static ch.idsia.ipp.core.utils.data.ArrayUtils.sameArray;

/**
 * BRTL approach, Greedy
 */
public class BrutalUndirectedSolver extends BaseSolver {

    public int tw;

    private Und und;

    private Und best_und;

    public int thread = 0;

    private TIntIntHashMap sols;

    TreeSet<Solution2> open;

    private double worstSk;

    public int behaviour;

    public void init(int time, Und und, int threads, int maxTw) {
        this.tw = maxTw;
        this.thread_pool_size = threads;
        this.max_exec_time = time;
        this.und = und;
        this.n_var = und.n;

        this.sols = new TIntIntHashMap();

        this.open=new TreeSet<Solution2>();
    }


    @Override
    protected String name() {
        return "Brutal Undirected";
    }

    @Override
    protected Sampler getSampler() {
        return new SimpleSampler(n_var);
    }

    @Override
    protected Searcher getSearcher() {
        return new BrutalUndirectedSearcher(this, tw, und);
    }

    @Override
    protected Provider getProvider() {
        return new NullProvider();
    }

    public void newUndirected(int new_sk, Und new_und) {

        if (new_und == null)
            return;

        synchronized (lock) {

            numIter++;

            if (new_sk > best_sk) {

                checkTime();

                // checkUnd(und, new_und);

                if (verbose > 0) {
                    logf("New improvement! %d (after %.1f s.)\n", new_sk,
                            elapsed);
                }

                best_sk = new_sk;

                best_und = new_und.clone();

                atLeastOne = true;

                // best_order = vars.clone();
            }

            propose(new_sk, new_und);


            if (verbose > 1)
                if ((System.currentTimeMillis() - counter) / 1000.0 > 1) {
                    logf("%.5f %.1f \n", best_sk, elapsed);
                    counter = System.currentTimeMillis();
                }

            // logf("%d \n", numIter);
        }
    }

    /*
    private void checkUnd(Undirected und, Undirected new_und) {
        // Check that in new_und there are no arcs not present in und
        for (int i1 = 0; i1 < und.n; i1++) {
            for (int i2 = i1 + 1; i2 < und.n; i2++) {
                if (new_und.check(i1, i2) && !und.check(i1, i2))
                    p("WARNIIIIIINGGGG!");
            }
        }
    } */

    private void propose(int new_sk, Und new_str) {

        boolean toDropWorst = false;

        if (open.size() > 20) {

            if (new_sk < worstSk) {
                // log.conclude("pruned");
                return;
            }

            toDropWorst = true;
        }

        for (Solution2 s: open) {
            if (s.same(new_str))
                return;
        }

        // Drop worst element in queue, to make room!
        if (toDropWorst) {
            open.pollLast();
            worstSk = open.last().sk;
        } else // If we didn't drop any element, check if we have to update the current
            // worst score!
            if (new_sk < worstSk) {
                worstSk = new_sk;
            }

        open.add(new Solution2(new_sk, new_str));

        writeUndirected(res_path, new_sk, new_str);

    }


    public void writeUndirected(String s, int sk, Und str) {
        if (!sols.containsKey(sk)) {
            sols.put(sk, 0);
            new File(f("%s/%d/", s, sk)).mkdir();
        }

        int v = sols.get(sk);
        String y = f("%s/%d/%d", s, sk, v);
        str.write(y);
        write(str, y);
        sols.put(sk, v + 1);
    }

    private void write(Und str, String y) {
        try {
            PrintWriter w = new PrintWriter(y, "UTF-8");
            wf(w, "%d\n", str.n );
            for (int v1 = 0; v1 < str.n; v1++) {
                for (int v2: str.neigh[v1])
                    if (v2 > v1)
                        wf(w, "%d %d\n", (v1 + 1), (v2 + 1));
            }
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Solution2 implements Comparable<Solution2>{
        private final Und str;
        private final double sk;

        public Solution2(double sk, Und str) {
            this.sk = sk;
            this.str = str;
        }

        @Override
        public int compareTo(Solution2 other) {
            if (sk < other.sk) {
                return 1;
            }
            return -1;
        }

        public boolean same(Und o_str) {

            if (str.n != o_str.n)
                return  false;

            for (int i = 0; i < str.n; i++) {
                if (!sameArray(str.neigh[i], o_str.neigh[i]))
                    return false;
            }

            return true;
        }
    }
}
