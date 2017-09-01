package ch.idsia.blip.api.common;


import ch.idsia.blip.api.Api;
import ch.idsia.blip.core.common.BayesianNetwork;
import ch.idsia.blip.core.common.SamGe;
import org.kohsuke.args4j.Option;

import java.util.logging.Logger;

import static ch.idsia.blip.core.utils.other.RandomStuff.getBayesianNetwork;


/**
 * Sample Generator
 */

public class SamGeApi extends Api {

    private static final Logger log = Logger.getLogger(SamGeApi.class.getName());

    @Option(name = "-n", required = true, usage = "Bayesian network file path")
    private String s_bn;

    @Option(name = "-d", required = true, usage = "Datafile path (.dat format)")
    private String s_datafile;

    @Option(name = "-set", usage = "Number of samples")
    private Integer n_sample = 10000;

    @Option(name = "-f", usage = "Output format (dat, arff)")
    private String format = "dat";


    /**
     * Command line execution
     *
     * @param args parameters provided
     */
    public static void main(String[] args) {
        defaultMain(args, new SamGeApi(), log);
    }

    public void exec() throws Exception {
        BayesianNetwork bn = getBayesianNetwork(s_bn);
        if (bn == null)
            return;
        SamGe samGe = new SamGe();
        samGe.seed = seed;
        samGe.go(bn, s_datafile, n_sample, format);
    }

    /*
         @Option(name="m", samGe.missing, false, false,
                "missing values options");

         @Option(name="v", samGe.perc_var, 0.2, false,
                "Percentage of variables that will contain missing values");

         @Option(name="l", samGe.perc_values, 0.2, false,
                "Percentage of missing values");
    } */
}
