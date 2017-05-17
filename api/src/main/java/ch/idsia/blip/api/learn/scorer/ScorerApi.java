package ch.idsia.blip.api.learn.scorer;


import ch.idsia.blip.api.Api;
import ch.idsia.blip.core.learn.scorer.BaseScorer;
import org.kohsuke.args4j.Option;

import java.util.logging.Logger;

import static ch.idsia.blip.core.utils.RandomStuff.getDataSet;


abstract class ScorerApi extends Api {

    private static final Logger log = Logger.getLogger(
            ScorerApi.class.getName());

    @Option(name="-d", required = true, usage="Datafile path (.dat format)")
    public String ph_dat;

    @Option(name="-j", required = true, usage="Parent set scores output file (jkl format)")
    public String ph_scores;

    @Option(name="-n", usage="Maximum learned in-degree")
    public int max_pset_size = 6;

    @Option(name="-t", usage="Maximum time (if 0, default 60 seconds for variable)")
    public int max_time = 0;

    @Option(name="-c", usage="Chosen score function. Possible choices: BIC, BDeu")
    public String scoreNm = "bic";

    @Option(name="-a", usage="(if BDeu is chosen) equivalent sample size parameter")
    public Double alpha = 1.0;

    @Option(name="-u", usage="Search only the selected variable (ex: '3' or '1-10')")
    public String choice_variables = "";

    private BaseScorer scorer;

    public ScorerApi() {
        scorer = getScorer();
    }

    @Override
    public void exec() throws Exception {
        scorer.init(options());
        scorer.go(getDataSet(ph_dat));
    }

    protected abstract BaseScorer getScorer();
}

