package ch.idsia.blip.api.measure;


import ch.idsia.blip.api.Api;
import ch.idsia.blip.core.common.BayesianNetwork;
import ch.idsia.blip.core.common.io.dat.BaseFileLineReader;
import ch.idsia.blip.core.utils.IncorrectCallException;
import ch.idsia.blip.core.utils.LLEval;
import org.kohsuke.args4j.Option;

import java.io.FileNotFoundException;
import java.util.logging.Logger;

import static ch.idsia.blip.core.utils.RandomStuff.*;


/**
 * Log-likelihood Evaluator
 */

public class LLEvalApi extends Api {

    private static final Logger log = Logger.getLogger(LLEvalApi.class.getName());

    @Option(name = "-n", required = true, usage = "Bayesian network file path")
    private String ph;

    @Option(name = "-d", required = true, usage = "Datafile path (.dat format)")
    private String s_datafile;

    @Option(name = "-l", required = false, usage = "List of log-likelihood instead of the average")
    public boolean list;

    /**
     * Command line execution
     *
     * @param args parameters provided
     */
    public static void main(String[] args) {
        defaultMain(args, new LLEvalApi(), log);
    }

    @Override
    public void exec() throws IncorrectCallException, FileNotFoundException {

        BayesianNetwork bn = getBayesianNetwork(ph);
        BaseFileLineReader dr = getDataSetReader(s_datafile);
        LLEval l = new LLEval();
        l.go(bn, dr);

        if (list) {
            for (int i = 0; i < l.ls.size(); i++)
                p(l.ls.get(i));
        } else
            p(l.ll);
    }

}

