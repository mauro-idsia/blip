package ch.idsia.blip.core.learn.scorer;


import ch.idsia.blip.core.utils.data.ArrayUtils;
import ch.idsia.blip.core.utils.data.SIntSet;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.logging.Logger;


public class GreedyScorer extends BaseScorer {

    private static final Logger log = Logger.getLogger(
            GreedyScorer.class.getName());

    /**
     * Queue size limit (for memory!)
     */
    private final long max_queue_size = (long) Math.pow(2, 20);

    /**
     * Maximum size for queue
     */
    private long queue_size;

    @Override
    protected String getName() {
        return "Greedy scoring";
    }

    public GreedyScorer() {
        super();
    }

    @Override
    public void prepare() {
        super.prepare();

        queue_size = (long) Math.pow(dat.n_var, 3);
        if (queue_size > max_queue_size) {
            queue_size = max_queue_size;
        }
    }

    @Override
    public GreedySearcher getNewSearcher(int n) {
        return new GreedySearcher(n);
    }

    /**
     * Entry of a parent set in the linked-list queue.
     */
    public static class ParentSetEntry implements Comparable<ParentSetEntry> {

        private final int[] pset;

        public final double sk;

        /**
         * Default constructor.
         *
         * @param pset hash of the parent set base
         * @param sk   score of the parent set
         */
        public ParentSetEntry(int[] pset, double sk) {
            this.pset = pset;
            this.sk = sk;
        }

        @Override
        public int compareTo(ParentSetEntry other) {
            if (sk > other.sk) {
                return 1;
            }
            return -1;
        }

        public String toString() {
            return String.format("(%s %.3f)", Arrays.toString(pset), sk);
        }

    }


    private class GreedySearcher extends BaseSearcher {

        /**
         * Queue for parent set to examine
         */
        private TreeSet<ParentSetEntry> open;

        /**
         * Set of already considered parent sets
         */
        private HashSet<SIntSet> closed;

        /**
         * Holder of the currently worst score saved in queue for evaluation
         */
        private double worstQueueScore;

        public GreedySearcher(int in_n) {
            super(in_n);
        }

        /**
         * Evaluate the parent sets of the variable in the available time, following an heuristic ordering.
         */
        @Override
        public void run() {

            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            double start = bean.getCurrentThreadCpuTime();
            double elapsed = 0;

            prepare();

            if (verbose > 2) {
                log.info(
                        String.format("Starting with: %d, max time: %.2f", n,
                        max_exec_time));
            }

            int arity = dat.l_n_arity[n];

            // Initialize everything
            closed = new HashSet<SIntSet>(); // Parent set already seen
            open = new TreeSet<ParentSetEntry>(); // Parent set to evaluate

            // Compute one-scores
            for (int p = 0; p < dat.n_var; p++) {

                if (p == n) {
                    continue;
                }

                double sk;

                sk = oneScores.get(p);

                addScore(p, sk);

                addParentSetToEvaluate(new int[] { p}, sk);
            }

            if (max_exec_time == 0) {
                max_exec_time = Integer.MAX_VALUE;
            }

            int i = 0;

            // Consider all the parent set for evaluation!
            while (!open.isEmpty() && (elapsed < max_exec_time)) {

                i += 1;

                ParentSetEntry pset = open.pollLast();

                for (int p2 = 0; (p2 < dat.n_var) && (elapsed < max_exec_time); p2++) {

                    if (p2 == n) {
                        continue;
                    }

                    evaluteParentSet(n, pset.pset, p2);

                    elapsed = (bean.getCurrentThreadCpuTime() - start)
                            / 1000000000;
                }
            }

            if (verbose > 2) {
                log.info(
                        String.format(
                                "ending with: %d, elapsed: %.2f, num evaluated %d",
                                n, elapsed, score.numEvaluated));
            }

            // synchronized (scorer) {
            if (verbose > 0)
                System.out.println("... finishing " + n);

            conclude();
        }

        private void evaluteParentSet(int n, int[] old, int p2) {

            if (Arrays.binarySearch(old, p2) >= 0) {
                return;
            }

            int[] pars = ArrayUtils.expandArray(old, p2);

            if (max_pset_size > 0 && pars.length >= max_pset_size) {
                return;
            }

            SIntSet cl = new SIntSet(pars);

            if (closed.contains(cl)) {
                return;
            }

            closed.add(cl);

            double sk = score.computeScore(n, pars);

            // System.out.println(Arrays.toString(pars));

            double bestScore;

            bestScore = voidSk;

            if ((sk > bestScore)) {

                addScore(pars, sk);

                addParentSetToEvaluate(cl.set, sk);
            }

        }

        private void addParentSetToEvaluate(int[] p, double sk) {

            boolean toDropWorst = false;

            if (open.size() > queue_size) {

                if (sk < worstQueueScore) {
                    // log.conclude("pruned");
                    return;
                }

                toDropWorst = true;
            }

            // Drop worst element in queue, to make room!
            if (toDropWorst) {
                ParentSetEntry worst = open.pollLast();

                worstQueueScore = open.last().sk;
            } else // If we didn't drop any element, check if we have to update the current
            // worst score!
            if (sk < worstQueueScore) {
                worstQueueScore = sk;
            }

            open.add(new ParentSetEntry(p, sk));
        }
    }
}
