package ch.idsia.blip.core.common.score;


import ch.idsia.blip.core.common.DataSet;
import ch.idsia.blip.core.utils.Gamma;
import ch.idsia.blip.core.utils.data.ArrayUtils;

import java.util.Arrays;

import static ch.idsia.blip.core.utils.RandomStuff.f;


/**
 * Computes the BDeu - weighted.
 */
public class BDeuWeight extends Score {

    // rows weight
    private final double[] weight;

    private double totWeight;

    // Equivalent sample size
    private double alpha = 10;

    public BDeuWeight(double alpha, DataSet dat, double[] weight) {
        super(dat);
        if (alpha != 0) {
            this.alpha = alpha;
        }
        this.weight = weight;

        totWeight = 0;
        for (double w: weight)
            totWeight += w;
    }

    @Override
    public double computeScore(int n) {

        numEvaluated++;

        int arity = dat.l_n_arity[n];

        double a_ij = alpha;
        double a_ijk = alpha / arity;

        // double skore = Gamma.lgamma(a_ij) - Gamma.lgamma(a_ij + dat.n_datapoints);
        double skore = Gamma.lgamma(a_ij) - Gamma.lgamma(a_ij + totWeight);

        for (int v = 0; v < arity; v++) {

            double sum = 0;
            for (int row: dat.row_values[n][v])
                sum += weight[row];

            // skore += Gamma.lgamma(a_ijk + values[v].length) - Gamma.lgamma(a_ijk);
            skore += Gamma.lgamma(a_ijk + sum) - Gamma.lgamma(a_ijk);
        }
        return skore;
    }

    @Override
    public double computeScore(int n, int[] set_p) {

        Arrays.sort(set_p);

        if (check(set_p))
            return -Double.MAX_VALUE;

        int[][] p_values = computeParentSetValues(set_p);

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
            // if (containsMissing(z_i, z)) {
            // continue;
            // }

            double sum = 0;

            for (int v = 0; v < arity; v++) {
                int valcount = 0;
                int[] vz = ArrayUtils.intersect(dat.row_values[n][v], p_values[p_v]);
                for (int vzu: vz) {
                    valcount += weight[vzu];
                    sum += weight[vzu];
                }

                skore += Gamma.lgamma(a_ijk + valcount);
            }


            skore -= Gamma.lgamma(a_ij + sum);

            i++;
        }

        // System.out.println(p_arity + " " + thread + " " + p_values.length);

        return skore;
    }

    @Override
    public double inter(int n, int[] set, int p2) {
        return 0;
    }

    @Override
    public String descr() {
        return f("BDeu weight (alpha: %.2f)", alpha);
    }
}
