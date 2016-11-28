package ch.idsia.ipp.core.common.analyze;


import ch.idsia.ipp.core.common.io.DataFileReader;
import ch.idsia.ipp.core.utils.data.ArrayUtils;
import ch.idsia.ipp.core.utils.math.FastMath;


public class Entropy extends Analyzer {

    /**
     * Alpha for counts
     */
    double alpha = 1.0;

    /**
     * Num of variables
     */
    private int r;

    public Entropy(DataFileReader dat) {
        super(dat);
    }

    public double computeH(int x) {
        double h = 0;

        double p;
        int k;

        for (int v = 0; v < dat.l_n_arity[x]; v++) {
            k = dat.row_values[x][v].length;
            p = getFreq(k, dat.l_n_arity[x]);

            h += p * FastMath.log(p);
        }

        return -h;
    }

    public double computeHCond(int x, int y) {

        double h = 0;

        int x_ar = dat.l_n_arity[x];
        int y_ar = dat.l_n_arity[y];

        for (int x_i = 0; x_i < x_ar; x_i++) {
            // P(x)
            int[] r_x = dat.row_values[x][x_i];
            double p_x = getFreq(r_x.length, x_ar);

            for (int y_i = 0; y_i < y_ar; y_i++) {
                // P(y)
                int[] r_y = dat.row_values[y][y_i];
                double p_y = getFreq(r_y.length, y_ar);

                // P(x, y)
                double p_xy = getFreq(ArrayUtils.intersectN(r_x, r_y),
                        x_ar * y_ar);

                h += p_xy * FastMath.log(p_xy / p_y);

                // System.out.printf(" %.5f * log ( %.5f / %.5f) - %.5f * %.5f \n", p_xy, p_xy, p_x * p_y, p_xy, FastFastMath.log(p_xy / (p_x * p_y)));
            }
        }

        // System.out.printf("mi: x %d, y %d -> %.5f\n", x, y, mi);

        return -h;
    }

    public double computeHCond(int x, int[] y) {

        int[][] y_rows;

        if (y.length > 1) {
            y_rows = computeParentSetValues(y);
        } else if (y.length == 1) {
            y_rows = dat.row_values[y[0]];
        } else {
            return computeH(x);
        }

        int y_ar = 1;

        for (int e_y : y) {
            y_ar *= dat.l_n_arity[e_y];
        }

        double h = 0;

        int x_ar = dat.l_n_arity[x];

        for (int x_i = 0; x_i < x_ar; x_i++) {
            // P(x)
            int[] r_x = dat.row_values[x][x_i];
            double p_x = getFreq(r_x.length, x_ar);

            for (int y_i = 0; y_i < y_rows.length; y_i++) {
                if (containsMissing(y_i, y)) {
                    continue;
                }

                // P(y)
                int[] r_y = y_rows[y_i];
                double p_y = getFreq(r_y.length, y_ar);

                // P(x, y)
                double p_xy = getFreq(ArrayUtils.intersectN(r_x, r_y),
                        x_ar * y_ar);

                h += p_xy * FastMath.log(p_xy / p_y);

                // System.out.printf(" %.5f * log ( %.5f / %.5f) - %.5f * %.5f \n", p_xy, p_xy, p_x * p_y, p_xy, FastFastMath.log(p_xy / (p_x * p_y)));
            }
        }

        // System.out.printf("mi: x %d, y %d -> %.5f\n", x, y, mi);

        return -h;
    }

    public double computeH(int x, int y) {

        double h = 0;

        int x_ar = dat.l_n_arity[x];
        int y_ar = dat.l_n_arity[y];

        for (int x_i = 0; x_i < x_ar; x_i++) {
            int[] r_x = dat.row_values[x][x_i];

            for (int y_i = 0; y_i < y_ar; y_i++) {

                int[] r_y = dat.row_values[y][y_i];

                double p_xy = getFreq(ArrayUtils.intersectN(r_x, r_y),
                        x_ar * y_ar);

                h += p_xy * FastMath.log(p_xy);

                // System.out.printf(" %.5f * log ( %.5f / %.5f) - %.5f * %.5f \n", p_xy, p_xy, p_x * p_y, p_xy, FastFastMath.log(p_xy / (p_x * p_y)));
            }
        }

        // System.out.printf("mi: x %d, y %d -> %.5f\n", x, y, mi);

        return -h;
    }

    public double computeH(int[] x) {
        double h = 0;

        int[][] x_rows;

        if (x.length > 1) {
            x_rows = computeParentSetValues(x);
        } else {
            x_rows = dat.row_values[x[0]];
        }
        int x_ar = 1;

        for (int e_x : x) {
            x_ar *= dat.l_n_arity[e_x];
        }

        for (int x_i = 0; x_i < x_rows.length; x_i++) {
            if (containsMissing(x_i, x)) {
                continue;
            }
            // P(x)
            int[] r_x = x_rows[x_i];
            double p_x = getFreq(r_x.length, x_ar);

            h += p_x * FastMath.log(p_x);
        }

        return -h;
    }

    public double computeH(int x, int[] y) {

        double h = 0;

        int x_ar = dat.l_n_arity[x];

        int[][] y_rows;

        if (y.length > 1) {
            y_rows = computeParentSetValues(y);
        } else {
            y_rows = dat.row_values[y[0]];
        }
        int y_ar = 1;

        for (int e_y : y) {
            y_ar *= dat.l_n_arity[e_y];
        }

        for (int y_i = 0; y_i < y_rows.length; y_i++) {
            if (containsMissing(y_i, y)) {
                continue;
            }
            int[] r_y = y_rows[y_i];

            for (int x_i = 0; x_i < x_ar; x_i++) {
                int[] r_x = dat.row_values[x][x_i];

                double p_xy = getFreq(ArrayUtils.intersectN(r_x, r_y),
                        x_ar * y_ar);

                h += p_xy * FastMath.log(p_xy);

                // System.out.printf(" %.5f * log ( %.5f / %.5f) - %.5f * %.5f \n", p_xy, p_xy, p_x * p_y, p_xy, FastFastMath.log(p_xy / (p_x * p_y)));
            }
        }

        // System.out.printf("mi: x %d, y %d -> %.5f\n", x, y, mi);

        return -h;
    }
}
