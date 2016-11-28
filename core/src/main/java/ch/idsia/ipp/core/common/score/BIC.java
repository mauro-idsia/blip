package ch.idsia.ipp.core.common.score;


import ch.idsia.ipp.core.common.io.DataFileReader;

import java.util.Arrays;


/**
 * Computes the BDeu.
 * <p/>
 * Remenber: last index in values is for missing data (let's simply ignore them!)
 */
public class BIC extends Score {

    public BIC(DataFileReader dat) {
        this(2, dat);
    }

    public BIC(double alpha, DataFileReader dat) {
        super(dat);
        this.alpha = alpha;

    }

    @Override
    public double computeScore(int[][] values) {

        numEvaluated++;

        int arity = values.length - 1;

        double skore = 0;

        // for(int[] v: values)
        // System.out.println(Arrays.toString(v));

        double alpha = 1;
        double alpha_i = (alpha / arity);

        for (int v = 0; v < arity; v++) {
            double p = (values[v].length + alpha_i) / (dat.n_datapoints + alpha);

            skore += values[v].length * Math.log(p);
            // System.out.printf("%d - %.2f, ", values[v].length, p);
            // System.out.println((values[v].length * 1.0) + (alpha / arity));
            // System.out.println((n_datapoints + alpha));

            // System.out.println(values[v].length + " & " + Math.log(p) + " & " + p + " % " + skore);
        }

        skore -= getPenalization(arity);

        return skore;
    }

    double getPenalization(int arity) {
        return Math.log(dat.n_datapoints) * (arity - 1) / alpha;
    }

    @Override
    public double computeScore(short[] values, int n, int[][] p_values, int[] set_p) {

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
            if (containsMissing(p_v, set_p)) {
                continue;
            }

            int[] valcount = new int[arity + 1];

            for (int v = 0; v < arity; v++) {
                valcount[v] = 0;
            }

            for (int r : p_values[p_v]) {
                valcount[values[r]] += 1;
            }

            for (int v = 0; v < arity; v++) {

                if (valcount[v] == 0) {
                    continue;
                }

                // System.out.printf("%.4f, %d - %d, %.3f \n", skore, valcount[v], p_values[p_v].length,  Math.log((valcount[v] * 1.0) / p_values[p_v].length));

                skore += valcount[v]
                        * (Math.log(valcount[v] + alpha_ij)
                                - Math.log(p_values[p_v].length + alpha_i));

                // System.out.printf("%d- %.2f, ", valcount[v], p);

                // System.out.println(valcount[v] + "   " + Math.log(p) + "   " + p + "   " + skore);
            }

        }
        // Penalization term
        skore -= getPenalization(arity, p_arity);

        return skore;
    }

    double getPenalization(int arity, int p_arity) {
        return Math.log(dat.n_datapoints) * (arity - 1) * p_arity / 2;
    }

    @Override
    public double inter(int n, int[] set, int p2) {

        // Compute interaction
        int p1_ar = 0;

        for (int v1 : set) {
            p1_ar = dat.l_n_arity[v1];
        }

        int p2_ar = dat.l_n_arity[p2];

        return getPenalization(dat.l_n_arity[n], (p1_ar + p2_ar - p1_ar * p2_ar));
    }

}
