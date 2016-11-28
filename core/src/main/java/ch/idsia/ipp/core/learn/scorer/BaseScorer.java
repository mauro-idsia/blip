package ch.idsia.ipp.core.learn.scorer;


import ch.idsia.ipp.core.common.io.DataFileReader;
import ch.idsia.ipp.core.common.score.BDeu;
import ch.idsia.ipp.core.common.score.BIC;
import ch.idsia.ipp.core.common.score.MIT;
import ch.idsia.ipp.core.common.score.Score;
import ch.idsia.ipp.core.learn.scorer.concurrency.Executor;
import ch.idsia.ipp.core.learn.scorer.utils.OpenParentSet;
import ch.idsia.ipp.core.utils.RandomStuff;
import ch.idsia.ipp.core.utils.data.SIntSet;
import ch.idsia.ipp.core.utils.data.hash.TIntDoubleHashMap;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.idsia.ipp.core.utils.RandomStuff.*;
import static ch.idsia.ipp.core.utils.data.ArrayUtils.reduceArray;


public abstract class BaseScorer {

    private static final Logger log = Logger.getLogger(
            BaseScorer.class.getName());

    private static long start;

    /**
     * Input datapoint file
     */
    public DataFileReader dat;

    public String ph_scores;

    /**
     * BDeu scorer
     */
    public Score score;

    /**
     * Parents size limit (for memory!)
     */
    private final long max_parents = (long) Math.pow(2, 10);

    /**
     * Equivalent sample size
     */
    public double alpha = 10.0;

    /* Parameters */

    /**
     * Maximum execution time
     */
    public Integer max_exec_time = 10;

    /**
     * Maximum in-degree for variable
     */
    public int max_pset_size = 0;

    /**
     * Maximum number of threads
     */
    public int thread_pool_size;

    /**
     * Flag for verbose operations
     */
    public int verbose = 0;

    public String scoreNm = "bdeu";

    /**
     * Flag to graph name of variables, instead of index
     */
    public boolean write_names;

    public String choice_variables;

    protected int n_var;

    private ScoreWriter scoreWriter;

    BaseScorer() {
        this(10);
    }

    BaseScorer(int maxExec) {
        this.max_exec_time = maxExec;
    }

    protected abstract String getName();

    public void go(DataFileReader in_dat, int x) throws Exception {
        dat = in_dat;
        start = System.currentTimeMillis();
        prepareSearch();
        searchOne(x);
    }

    void prepareSearch() throws Exception {

        start = System.currentTimeMillis();

        if (verbose > 0) {
            pf("Reading from datafile '%s'... \n", dat.path);
        }

        if (score == null) {
            scoreNm = scoreNm.toLowerCase().trim();

            if ("bdeu".equals(scoreNm)) {
                score = new BDeu(alpha, dat);
            } else if ("bic".equals(scoreNm)) {
                score = new BIC(alpha, dat);
            } else if ("mit".equals(scoreNm)) {
                score = new MIT(alpha, dat);
            } else {
                throw new Exception("Chosen score not known!");
            }
        }

        dat.readMetaData();
        dat.readValuesCache();

        n_var = dat.n_var;

        if (thread_pool_size == 0) {
            thread_pool_size = Runtime.getRuntime().availableProcessors();
        }
    }

    public void go(DataFileReader in_dat) throws Exception {

        dat = in_dat;

        prepareSearch();

        if (choice_variables != null) {
            searchChoice();
        } else {
            searchAll();
        }

        if (verbose > 0) {
            pf("... done - eval: %d (el: %.2f) \n",
                    score.numEvaluated,
                    (System.currentTimeMillis() - start) / 1000.0);
        }
    }

    private void searchChoice() throws Exception {

        int s = 0, e = 0;

        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(choice_variables);
        if (m.matches()) {
            s = Integer.valueOf(choice_variables);
            e = s + 1;
        }

        p = Pattern.compile("(\\d+)-(\\d+)");
        m = p.matcher(choice_variables);
        if (m.matches()) {
            s = Integer.valueOf(m.group(1));
            e = Math.min(n_var, Integer.valueOf(m.group(2)) + 1);
        }

        Writer writer = getWriter(ph_scores);
            Thread t1 = new Thread(new Executor(thread_pool_size, s, e, this));
            scoreWriter = new ScoreWriter(this, writer, s, e);
            Thread t2 = new Thread(scoreWriter);

            t1.start();
            t2.start();

            t1.join();
            t2.join();

    }


    private void searchOne(int n) throws InterruptedException {

        Runnable r = getNewSearcher(n);
        Thread t = new Thread(r);

        t.start();
        t.join();
    }

    private void searchAll() throws InterruptedException, IOException {

        Writer writer = getWriter(ph_scores);

        if (verbose > 0) {
            pf("Executing with: \n");
            pf("%-12s: %s \n", "code", this.getClass().getName());
            pf("%-12s: %d \n", "threads", thread_pool_size);
            pf("%-12s: %d \n", "max_time", max_exec_time);
            pf("%-12s: %d \n", "max_degree", max_pset_size);
        }

        if (verbose > 1) {
            pf("... searching (el: %.2f) \n",
                    (System.currentTimeMillis() - start) / 1000.0);
        }

        Thread t1 = new Thread(new Executor(thread_pool_size, 0, n_var, this));
        scoreWriter = new ScoreWriter(this, writer, 0, n_var);
        Thread t2 = new Thread(scoreWriter);

        if (verbose > 0) {
            pf("... writing down:");
        }

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        conclude();

    }

    protected void conclude() {
        if (verbose > 0) {
            pf("done! \n");
        }
    }

    public abstract BaseSearcher getNewSearcher(int n);

    /**
     * Get a string that represent the set score
     *
     * @param sk    score
     * @param set_p parent set
     * @return representation
     */
    String strPSetScore(Double sk, int[] set_p) {

        String mex = String.format("%.4f %d", sk, set_p.length);

        for (int p : set_p) {
            mex += String.format(" %s", getVarName(p));
        }
        return mex;
    }

    /**
     * @param n variable of interest
     * @return name of variable to graph (index or actual name, depending on flag)
     */
    private String getVarName(int n) {
        if (write_names) {
            return dat.l_s_names[n];
        } else {
            return String.valueOf(n);
        }
    }

    /**
     * Check if the skore has to be pruned: exists a subset with an higher skore
     *
     * @param sk     skore to check
     * @param set_p  parents in the set to check
     * @param scores scores list
     * @return if there is a better skore in the subsets
     */
    protected boolean checkToPrune(double sk, int[] set_p, TreeMap<SIntSet, Double> scores) {
        for (int p : set_p) {
            int[] set_new = reduceArray(set_p, p);
            SIntSet c = new SIntSet(set_new);

            if (scores.containsKey(c) && (sk < scores.get(c))) {
                return true;
            }

            if (set_new.length > 1) {
                if (checkToPrune(sk, set_new, scores)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void init(String ph_jkl, int max_pset_size, int max_time, String scoreNm, Double alpha, int thread_pool_size, String choice_variables) {
        this.ph_scores = ph_jkl;
        this.max_exec_time = max_time;
        this.max_pset_size = max_pset_size;
        this.scoreNm = scoreNm;
        this.alpha = alpha;
        this.thread_pool_size = thread_pool_size;
        this.choice_variables = choice_variables;
    }

    public void init(String ph_jkl, int max_pset_size, int max_time, String scoreNm, Double alpha, int thread_pool_size) {
        init(ph_jkl, max_pset_size, max_time, scoreNm, alpha, thread_pool_size, null);
    }

    public void init(String ph_jkl, int max_pset_size, int max_time, String scoreNm, Double alpha) {
        init(ph_jkl, max_pset_size, max_time, scoreNm, alpha, 1);
    }


    class ScoreWriter implements Runnable {

        private final Writer wr;
        private final int end;
        private final int st;

        private HashMap<Integer, TreeMap<SIntSet, Double>> cache;

        public ScoreWriter(BaseScorer sc, Writer in_writer, int start, int end) {
            wr = in_writer;
            this.st = start;
            this.end = Math.min(end, n_var+1);

            preamble(sc);
        }

        private void preamble(BaseScorer sc) {
            try {
                wf(wr, "%d\n", sc.n_var);
                wf(wr, "# Scores computed using %s (%d seconds per variable) \n", sc.getName(), max_exec_time);
                wf(wr, "# Score function: %s \n", ("bic".equals(scoreNm)) ? "bic" : f("bdeu (%.2f alpha)", alpha));
            } catch (IOException e) {
                logExp(log, e);
            }

        }

        public void run() {

            int i = st;
            boolean cnt;

            cache = new HashMap<Integer, TreeMap<SIntSet, Double>>();

            while (i < end) {

                cnt = cache.containsKey(i);

                if (!cnt) {
                    if (verbose > 1) {
                        p("waiting for... " + i);
                    }
                    waitAsec(1000);
                    continue;
                }

                if (verbose > 1) {
                    pf("%d (el: %.2f), ", i,
                            (System.currentTimeMillis() - start) / 1000.0);
                } else if (verbose > 0) {
                    pf("%d, ", i);
                }

                if (wr != null)
                    writeScores(wr, i, cache.get(i));
                    // else do it yourself
                else if (ph_scores != null)
                    try {
                        Writer writer = getWriter(ph_scores + i + ".jkl");
                        writeScores(writer, i, cache.get(i));
                    } catch (Exception ex) {
                        logExp(log, ex);
                    }

                cache.remove(i);

                i++;

            }
        }

        private void waitAsec(int s) {
            try {
                Thread.sleep(s);
            } catch (InterruptedException e) {
                logExp(log, e);
            }
        }

        // add a new variables scores to graph down
        public void add(int n, TreeMap<SIntSet, Double> scores) {
            cache.put(n, scores);
        }
    }

    /**
     * Write found scores on file
     *
     * @param wr writer object to output data
     */
    private void writeScores(Writer wr, int n, TreeMap<SIntSet, Double> scores) {

        try {

            wf(wr, "%s %d\n", getVarName(n), scores.size());

            for (SIntSet pset : RandomStuff.sortInvByValues(scores).keySet()) {

                wr.write(strPSetScore(scores.get(pset), pset.set));
                wr.write("\n");
            }

            wr.flush();
        } catch (IOException e) {
            log.severe(
                    String.format("Error writing score to file: %s",
                            e.getMessage()));
        }

    }

    public abstract class BaseSearcher implements Runnable {

        /**
         * Variable to work with
         */
        protected final int n;

        /**
         * List of good parents for the variable
         */
        int[] parents;

        protected double voidSk;

        protected TIntDoubleHashMap oneScores;

        protected TreeMap<SIntSet, Double> scores;

        protected double m_elapsed;

        protected double m_start;

        public BaseSearcher(int in_n) {
            n = in_n;
        }

        protected void addScore(double sk) {
            addScore(new SIntSet(), sk);
        }

        protected void addScore(int[] p, double sk) {
            addScore(new SIntSet(p), sk);
        }

        protected void addScore(int p, double sk) {
            addScore(new SIntSet(p), sk);
        }

        protected void addScore(SIntSet p, double sk) {
                scores.put(p, sk);
        }

        void prepare() {

            // Prepare scores
            scores = new TreeMap<SIntSet, Double>();

            // Void score
            voidSk = score.computeScore(dat.row_values[n]);
            addScore(voidSk);

            // One score
            searchSingleParents(n);

            m_start = System.currentTimeMillis();
        }

        private void searchSingleParents(int n) {

            oneScores = new TIntDoubleHashMap();

            double worstQueueScore = 0;

            TreeSet<OpenParentSet> open = new TreeSet<OpenParentSet>();

            for (int n2 = 0; n2 < n_var; n2++) {

                if (n == n2) {
                    continue;
                }
                double oneSk = score.computeScore(dat.sample[n], n,
                        dat.row_values[n2], n2);

                boolean toDropWorst = false;

                if (open.size() > max_parents) {

                    if (oneSk < worstQueueScore) {
                        // log.conclude("pruned");
                        continue;
                    }

                    toDropWorst = true;
                }

                // Drop worst element in queue, to make room!
                if (toDropWorst) {
                    open.pollLast();
                    worstQueueScore = open.last().sk;
                } else // If we didn't drop any element, check if we have to update the current
                    // worst score!
                    if (oneSk < worstQueueScore) {
                        worstQueueScore = oneSk;
                    }

                open.add(new OpenParentSet(null, n2, oneSk));
            }

            for (OpenParentSet p : open) {
                oneScores.put(p.p2, p.sk);
                addScore(p.p2, p.sk);
            }

        }

        protected void conclude() {

            // prune scores
            scores = pruneScores();

            // If parent writer is ready, graph there
            if (scoreWriter != null) {
                scoreWriter.add(n, scores);
            }
        }

        private TreeMap<SIntSet, Double> pruneScores() {

            TreeMap<SIntSet, Double> new_scores = new TreeMap<SIntSet, Double>();

            // Pick only the best scores
            for (SIntSet pset : RandomStuff.sortInvByValues(scores).keySet()) {

                // Check when to limit scores
                double sk = scores.get(pset);

                // log.conclude(set + " " + new_sk);

                // Decide to put it in the final score
                boolean toPrune = sk < voidSk;

                toPrune = toPrune || checkToPrune(sk, pset.set, scores);

                if (!toPrune) {
                    new_scores.put(pset, sk);
                }
            }

            return new_scores;
        }

        protected boolean thereIsTime() {
            if (max_exec_time == 0)
                    return true;
           m_elapsed = ((System.currentTimeMillis() - m_start) / 1000.0);
            return m_elapsed < max_exec_time;
        }


    }
}

