package ch.idsia.blip.api.learn.scorer;


import ch.idsia.blip.api.Api;
import ch.idsia.blip.core.common.DataSet;
import ch.idsia.blip.core.learn.scorer.BaseScorer;
import org.kohsuke.args4j.Option;

import static ch.idsia.blip.core.utils.RandomStuff.getDataFromFile;


abstract class ScorerApi extends Api {

    @Option(name="-d", required = true, usage="Datafile path (.dat format)")
    private static String ph_dat;

    @Option(name="-j", required = true, usage="Parent set scores output file (jkl format)")
    private static String ph_jkl;

    @Option(name="-n", usage="Maximum learned in-degree (if 0, no constraint is applied)")
    private static int max_pset_size = 0;

    @Option(name="-t", usage="Maximum time (if 0, default 60 seconds for variable)")
    private static int max_time = 0;

    @Option(name="-c", usage="Chosen score function. Possible choices: BIC, BDeu")
    private static String scoreNm = "bic";

    @Option(name="-a", usage="(if BDeu is chosen) equivalent sample size parameter")
    private static Double alpha = 1.0;

    @Option(name="-b", usage="Number of machine cores to use (if 0, all are used)")
    private static int thread_pool_size = 1;

    @Option(name="-u", usage="Search only the selected variable (ex: '3' or '1-10')")
    private static String choice_variables;


    private final BaseScorer scorer;

    public ScorerApi() {
        scorer = getScorer();
    }

    @Override
    public void exec() throws Exception {
        DataSet dat = getDataFromFile(ph_dat);
        scorer.verbose = verbose;
        scorer.init(ph_jkl, max_pset_size, max_time, scoreNm, alpha, thread_pool_size, choice_variables);
        scorer.go(dat);
    }

    protected abstract BaseScorer getScorer();
}

