package ch.idsia.ipp.api.old;


import ch.idsia.ipp.api.Api;
import ch.idsia.ipp.core.common.BayesianNetwork;
import ch.idsia.ipp.core.common.io.DataFileReader;
import ch.idsia.ipp.core.common.score.BDeu;
import ch.idsia.ipp.core.utils.IncorrectCallException;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.util.logging.Logger;

import static ch.idsia.ipp.core.utils.RandomStuff.getBayesianNetwork;
import static ch.idsia.ipp.core.utils.RandomStuff.getDataFileReader;


/**
 * BDeu Accountant
 */

public class Bda extends Api {

    private static final Logger log = Logger.getLogger(Bda.class.getName());

    @Option(name="-n", required = true, usage="Bayesian network file path")
    private String ph;

    @Option(name="-d", required = true, usage="Datafile path (.dat format)")
    private String s_datafile;

    @Option(name="-a", required = true, usage="Equivalent sample size")
    private double alpha = 1;


    /**
     * Command line execution
     *
     * @param args parameters provided
     * @throws ch.idsia.ipp.core.utils.IncorrectCallException if there is a problem with parameters
     */
    public static void main(String[] args) throws IncorrectCallException {
        defaultMain(args, new Bda(), log);
    }

    @Override
    public void exec() throws Exception {

        BayesianNetwork bn = getBayesianNetwork(ph);
        DataFileReader dat = getDataFileReader(s_datafile);

        double sum_ll = computeBDeu(bn, dat, alpha);

        System.out.println(sum_ll);
    }

    /**
     * @param bn
     * @param dat_rd
     * @param alpha
     * @return computed BDeu score of the given Bayesian network over the given data
     */
    private double computeBDeu(BayesianNetwork bn, DataFileReader dat_rd, double alpha) {

        try {
            dat_rd.readMetaData();
            dat_rd.readValuesCache();

        } catch (IOException ex) {
            log.severe(
                    String.format("Error reading / writing: %s", ex.getMessage()));
            return 0;
        }

        BDeu bDeu = new BDeu(this.alpha, dat_rd);

        double tot_sk = 0;

        for (int n = 0; n < dat_rd.n_var; n++) {

            int[] pars = bn.parents(n);

            double sk;

            if (pars.length > 0) {
                int[][] p_values = bDeu.computeParentSetValues(pars);

                sk = bDeu.computeScore(dat_rd.sample[n], n, p_values, pars);
            } else {
                sk = bDeu.computeScore(dat_rd.row_values[n]);
            }

            System.out.printf("%d # %s%n", n, sk);

            tot_sk += sk;

        }

        return tot_sk;
    }

}
