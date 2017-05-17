package ch.idsia.blip.core.common.score;


import ch.idsia.blip.core.common.DataSet;
import ch.idsia.blip.core.utils.Gamma;
import ch.idsia.blip.core.utils.data.ArrayUtils;

import java.util.Arrays;

import static ch.idsia.blip.core.utils.RandomStuff.f;


/**
 * Computes the BDeu.
 */
public class BDeu extends Score {

    /**
     * Equivalent sample size
     */
    protected double alpha = 10;

    /**
     * Default constructor.
     */

    public BDeu(double alpha, DataSet dat) {
        super(dat);
        if (alpha != 0) {
            this.alpha = alpha;
        }
    }

    @Override
    public double computeScore(int n) {

        numEvaluated++;

        int arity = dat.l_n_arity[n];

        double a_ij = alpha;
        double a_ijk = alpha / arity;

        double skore = Gamma.lgamma(a_ij)
                - Gamma.lgamma(a_ij + dat.n_datapoints);

        for (int v = 0; v < arity; v++) {
            int weight = dat.row_values[n][v].length;
            skore += Gamma.lgamma(a_ijk + weight)
                    - Gamma.lgamma(a_ijk);
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

        /*
        int i = 0;

        boolean gh = false;
        if (n==6 && Arrays.toString(set_p).equals("[0, 3, 5]"))
            gh = true;
        */


        for (int p_v = 0; p_v < p_values.length; p_v++) {

            // Check if it contains a missing value; in case, don't consider it
            // if (containsMissing(z_i, z)) {
            // continue;
            // }

            skore -= Gamma.lgamma(a_ij + p_values[p_v].length);

            int valcount;

            /*
            if (gh) {
                p("");
                p(p_values[p_v].length);
            }
            */

            for (int v = 0; v < arity; v++) {
                valcount = ArrayUtils.intersectN(dat.row_values[n][v], p_values[p_v]);

                skore += Gamma.lgamma(a_ijk + valcount);

                /*
                if (gh)
                    p(valcount);
                    */
            }


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
        return f("BDeu (alpha: %.2f)", alpha);
    }


}
