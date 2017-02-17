package ch.idsia.blip.api.measure;


import ch.idsia.blip.api.Api;
import ch.idsia.blip.core.common.BayesianNetwork;
import ch.idsia.blip.core.common.io.DataFileLineReader;
import ch.idsia.blip.core.utils.IncorrectCallException;
import ch.idsia.blip.core.utils.RandomStuff;
import org.kohsuke.args4j.Option;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static ch.idsia.blip.core.utils.RandomStuff.getBayesianNetwork;
import static ch.idsia.blip.core.utils.RandomStuff.p;


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

    @Option(name="-l", required = false, usage="List of log-likelihood instead of the average")
    public boolean list;

    public double sum_ll;

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
        DataFileLineReader dr = new DataFileLineReader(s_datafile);

        computeLL(bn, dr);
    }

    /**
     * @param bn
     * @param dat_rd
     * @return computed log-likelihood of the given Bayesian network over the given data
     */
    public void computeLL(BayesianNetwork bn, DataFileLineReader dat_rd) {

        sum_ll = 0.0;

        short[] samp = new short[0];

        try {
            dat_rd.readMetaData();

            samp = dat_rd.next();

            while (!dat_rd.concluded) {
                double l = bn.getLogLik(samp);
                sum_ll += l;
                samp = dat_rd.next();
                if (list)
                    p(l);
            }

        } catch (Exception e) {
            System.out.println("Sample: " + Arrays.toString(samp));
            RandomStuff.logExp(log, e);
        }

        if (!list)
            p(sum_ll / dat_rd.n_datapoints);
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

