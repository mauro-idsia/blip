package ch.idsia.ipp.core.common.score;


import ch.idsia.ipp.core.common.io.DataFileReader;
import ch.idsia.ipp.core.utils.Gamma;

import java.util.Arrays;


/**
 * Computes the BDeu.
 */
public class BDeu extends Score {

    /**
     * Equivalent sample size
     */
    private double alpha = 10;

    /**
     * Default constructor.
     */

    public BDeu(double alpha, DataFileReader dat) {
        super(dat);
        if (alpha != 0) {
            this.alpha = alpha;
        }
    }

    @Override
    public double computeScore(int[][] values) {

        numEvaluated++;

        int arity = values.length - 1;

        double a_ij = alpha;
        double a_ijk = alpha / arity;

        double skore = Gamma.lgamma(a_ij)
                - Gamma.lgamma(a_ij + dat.n_datapoints);

        for (int v = 0; v < arity; v++) {
            skore += Gamma.lgamma(a_ijk + values[v].length)
                    - Gamma.lgamma(a_ijk);
        }
        return skore;
    }

    @Override
    public double computeScore(short[] values, int n, int[][] p_values, int[] set_p) {

        numEvaluated++;

        Arrays.sort(set_p);

        int arity = dat.l_n_arity[n];

        int p_arity = 1;

        for (int p : set_p) {
            p_arity *= dat.l_n_arity[p];
        }

        double a_ij = alpha / p_arity;
        double a_ijk = a_ij / arity;

        double skore = 0;

        skore += (Gamma.lgamma(a_ij) * p_arity)
                - (Gamma.lgamma(a_ijk) * p_arity * arity);

        int i = 0;

        for (int p_v = 0; p_v < p_values.length; p_v++) {

            // Check if it contains a missing value; in case, don't consider it
            if (containsMissing(p_v, set_p)) {
                continue;
            }

            skore -= Gamma.lgamma(a_ij + p_values[p_v].length);

            int[] valcount = new int[arity + 1];

            for (int v = 0; v < arity; v++) {
                valcount[v] = 0;
            }

            // For every value of var, keep track of the number of times
            // it appears in the p_values[p_v] rows
            for (int r : p_values[p_v]) {
                valcount[values[r]] += 1;
            }

            for (int v = 0; v < arity; v++) {
                skore += Gamma.lgamma(a_ijk + valcount[v]);
            }

            i++;
        }

        // System.out.println(p_arity + " " + thread + " " + p_values.length);

        return skore;
    }

    @Override
    public double inter(int n, int[] set, int p2) {
        return 0;
    }
}
