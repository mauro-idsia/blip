package ch.idsia.ipp.api.measure;


import ch.idsia.ipp.api.Api;
import ch.idsia.ipp.core.common.BayesianNetwork;
import ch.idsia.ipp.core.common.io.DataFileReader;
import ch.idsia.ipp.core.utils.IncorrectCallException;
import ch.idsia.ipp.core.utils.RandomStuff;
import org.kohsuke.args4j.Option;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static ch.idsia.ipp.core.utils.RandomStuff.getBayesianNetwork;
import static ch.idsia.ipp.core.utils.RandomStuff.getDataFileReader;


/**
 * Log-likelihood Evaluator
 */

public class LLEval extends Api {

    /**
     * Logger
     */
    private static final Logger log = Logger.getLogger(LLEval.class.getName());

    @Option(name="-n", required = true, usage="Bayesian network file path")
    private String ph;

    @Option(name="-d", required = true, usage="Datafile path (.dat format)")
    private String s_datafile;

    /**
     * Command line execution
     *
     * @param args parameters provided
     */
    public static void main(String[] args) {
        defaultMain(args, new LLEval(), log);
    }

    @Override
    public void exec() throws IncorrectCallException, FileNotFoundException {

        BayesianNetwork bn = getBayesianNetwork(ph);
        DataFileReader dat = getDataFileReader(s_datafile);

        double sum_ll = computeLL(bn, dat);

        System.out.println(sum_ll);
    }

    /**
     * @param bn
     * @param dat_rd
     * @return computed log-likelihood of the given Bayesian network over the given data
     */
    public double computeLL(BayesianNetwork bn, DataFileReader dat_rd) {

        double sum_ll = 0.0;

        short[] samp = new short[0];

        try {
            dat_rd.readMetaData();

            samp = dat_rd.next();

            while (!dat_rd.concluded) {
                sum_ll += bn.getLogLik(samp);
                samp = dat_rd.next();
            }

        } catch (Exception e) {
            System.out.println("Sample: " + Arrays.toString(samp));
            RandomStuff.logExp(log, e);
        }

        return sum_ll / dat_rd.n_datapoints;
    }

    /**
     * Reorder sample, from data order to network order
     *
     * @param samp       original sample
     * @param data_names names of variables (with order found in data)
     * @param bn_names   names of variables (with order found in network)
     * @return reordered sample
     */
    private short[] reorder(short[] samp, String[] data_names, List<String> bn_names) throws Exception {
        short[] n_samp = new short[samp.length];

        for (int ix = 0; ix < samp.length; ix++) {
            int n_ix = bn_names.indexOf(data_names[ix]);

            if (n_ix < 0) {
                throw new Exception("Not found variable: " + data_names[ix]);
            }
            n_samp[n_ix] = samp[ix];
        }

        return n_samp;
    }
}

