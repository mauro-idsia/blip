package ch.idsia.ipp.api.learn.scorer;


import ch.idsia.ipp.api.Api;
import ch.idsia.ipp.core.common.io.ScoreReader;
import ch.idsia.ipp.core.learn.scorer.RankerScores;
import org.kohsuke.args4j.Option;

import java.util.logging.Logger;


/**
 * Comparison between different scores
 */
public class RankScores extends Api {

    private static final Logger log = Logger.getLogger(
            RankScores.class.getName());

    @Option(name="-j1", required = true, usage="First parent set scores output file (jkl format)")
    private static String ph_scores_f = "";

    @Option(name="-j2", required = true, usage="Second parent set scores output file (jkl format)")
    private static String ph_scores_s = "";

    private final RankerScores ranker;

    public RankScores() {
        ranker = new RankerScores();
    }

    /**
     * Default command line execution
     */
    public static void main(String[] args) {
        defaultMain(args, new RankScores(), log);
    }

    @Override
    public void exec() throws Exception {

        ScoreReader sc1 = null;
        ScoreReader sc2 = null;

        sc1 = new ScoreReader(ph_scores_f, verbose);
        sc2 = new ScoreReader(ph_scores_s, verbose);

        ranker.debug = verbose;

        System.out.println(ranker.execute(sc1, sc2));
    }

}
