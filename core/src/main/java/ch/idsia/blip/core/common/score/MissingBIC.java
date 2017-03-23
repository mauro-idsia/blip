package ch.idsia.blip.core.common.score;

import ch.idsia.blip.core.common.DataSet;
import ch.idsia.blip.core.utils.data.ArrayUtils;

import java.util.Arrays;


/**
 * Computes the BIC
 */
public class MissingBIC extends BIC {

    public MissingBIC(DataSet dat) {
        super(dat);
    }

    @Override
    public double computeScore(int n, int[] set_p) {

        Arrays.sort(set_p);

        if (check(set_p))
            return -Double.MAX_VALUE;

        int[][] p_values = computeParentSetValues(set_p);

        numEvaluated++;

        Arrays.sort(set_p);

        double skore = 0;

        int arity = dat.l_n_arity[n];

        int p_arity = 1;

        for (int p : set_p) {
            p_arity *= dat.l_n_arity[p];
        }

        double alpha = 1;
        double alpha_i = alpha / arity;
        double alpha_ij = alpha / (arity * p_arity);

        for (int p_v = 0; p_v < p_values.length; p_v++) {

            // Check if it contains a missing value; in case, don't consider it
            // if (containsMissing(z_i, z)) {
            // continue;
            // }
            int valcount;

            for (int v = 0; v < arity; v++) {
                valcount = ArrayUtils.intersectN(dat.row_values[n][v], p_values[p_v]);

                if (valcount == 0) {
                    continue;
                }

                // System.out.printf("%.4f, %d - %d, %.3f \n", skore, valcount[v], p_values[p_v].length,  log((valcount[v] * 1.0) / p_values[p_v].length));

                skore += valcount
                        * (log(valcount + alpha_ij)
                                - log(p_values[p_v].length + alpha_i));

                // System.out.printf("%d- %.2f, ", valcount[v], p);

                // System.out.println(valcount[v] + "   " + log(p) + "   " + p + "   " + skore);
            }

        }
        // Penalization term
        skore -= getPenalization(arity, p_arity);

        return skore;
    }

    @Override
    public String descr() {
        return "BIC with missing values";
    }

}
