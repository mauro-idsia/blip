package ch.idsia.blip.api.utils;


import ch.idsia.blip.api.Api;
import ch.idsia.blip.core.common.io.ScoreReader;
import ch.idsia.blip.core.common.io.ScoreWriter;
import ch.idsia.blip.core.common.tw.KTree;
import ch.idsia.blip.core.utils.ParentSet;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.TreeSet;
import java.util.logging.Logger;


/**
 * Creates n-version of the given file score, each one guaranteed to give at maximum tw treewidth
 */
public class KTreeScore extends Api {

    private static final Logger log = Logger.getLogger(
            KTreeScore.class.getName());

    @Option(name="-s", required = true, usage="Scores input file (in jkl format)")
    private String ph_scores;

    @Option(name="-w", required = true, usage="maximum treewidth")
    private int max_tw;

    @Option(name="-o", required = true, usage="path of reduced scores")
    private String ph_output;

    @Option(name="-m", required = true, usage="number of reduced scores to graph")
    private int num_outputs;

    public static void main(String[] args) {
        defaultMain(args, new KTreeScore(), log);
    }

    @Override
    public void exec() throws IOException {

        ScoreReader sc = new ScoreReader(ph_scores.toString(), 0);
        sc.readScores();

        ParentSet[][] orig = sc.m_scores.clone();

        TreeSet<OpenScores> open = new TreeSet<OpenScores>();
        double worst_score = 0;
        int last_improvement = 0;

        while (last_improvement < 100) {

            last_improvement++;
            System.out.println(last_improvement);

            // sample a tree
            KTree k = KTree.sample(sc.n_var, max_tw);
            // get the parent sets
            ParentSet[][] sel = k.selectScores(orig);
            // get the number of selected scores
            double sk = k.score(sel);

            boolean toDropWorst = false;

            // If there is no space in the queue
            if (open.size() >= num_outputs) {
                // and it is terrible
                if (sk < worst_score) {
                    continue;
                }
                toDropWorst = true;
            }

            if (toDropWorst) {
                open.pollLast();
            }

            open.add(new OpenScores(sel, sk));
            last_improvement = 0;
            worst_score = open.last().sk;
        }

        for (int i = 0; i < num_outputs; i++) {
            ParentSet[][] sel = open.pollFirst().p;
            ScoreWriter sw = new ScoreWriter(ph_output + "-" + i + ".jkl");

            System.out.println(sw.path);

            sw.go(sel);
            sw.close();
        }
    }


    private class OpenScores implements Comparable<OpenScores> {
        private final ParentSet[][] p;
        private final double sk;
        public int n;

        public OpenScores(ParentSet[][] p, double sk) {
            this.p = p;
            this.sk = sk;

            n = 0;
            for (ParentSet[] p1 : p) {
                n += p1.length;
            }
        }


        public int compareTo(OpenScores other) {
            if (sk < other.sk) {
                return 1;
            }
            return -1;
        }
    }
}
