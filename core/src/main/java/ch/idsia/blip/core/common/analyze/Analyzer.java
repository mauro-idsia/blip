package ch.idsia.blip.core.common.analyze;


import ch.idsia.blip.core.common.DataSet;
import ch.idsia.blip.core.utils.data.ArrayUtils;
import ch.idsia.blip.core.utils.data.SIntSet;
import ch.idsia.blip.core.utils.math.FastMath;

import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;


public class Analyzer {

    private static final Logger log = Logger.getLogger(Analyzer.class.getName());

    // Data file
    public  DataSet dat;

    // Alpha for counts
    public double alpha = 1.0;

    public boolean base2 = false;

    protected HashMap<SIntSet, int[][]> cache_z_rows;

    public Analyzer(DataSet dat) {
        this.dat = dat;
    }

    protected double getFreq(int v, int i) {

       // return ((v + (alpha / thread)) / (dat.n_datapoints + alpha));
        return (v * 1.0 / dat.n_datapoints) ;
        // double d = ((v + (alpha / i)) / (dat.n_datapoints + alpha));
        // double d1 = d+ d * (new Random().nextDouble() - 0.5) / 10.0;
//        return d;
    }

    public int[][] computeParentSetValues(int[] set_p) {
        return computeParentSetValues(set_p, dat.row_values);
    }

    /**
     * Compute the set of datapoints indexes for each parent configuration.
     * <p/>
     * (Please remember that for each variable, the position equal to (arity of that variable + 1)
     * is for missing data
     *
     * @param set_p parents to consider
     * @return for each configurations of the parents, array of datapoints indexes where that configuration appears
     */
    public int[][] computeParentSetValues(int[] set_p, int[][][] rows) {

        int p_arity = 1;

        Arrays.sort(set_p);

        for (int p : set_p) {
            p_arity *= dat.l_n_arity[p];
        }

        int[][] p_values = new int[p_arity][];

        for (int i = 0; i < p_arity; i++) {
            int[] values = null;

            int n = i;

            // Get value for the parent in this configuration
            for (int p : set_p) {
                int ar = dat.l_n_arity[p];
                short val = (short) (n % ar);

                n /= ar;

                // System.out.print("p: " + p + " v: " + val + ", ");

                // Update set containing sample rows for the chosen configuration
                // System.out.printf("%d (%d) - %d - %d\n", p,  dat.l_n_arity[p], val, dat.row_values[p].length);
                int[] par_var = rows[p][val];

                if (values == null) {
                    values = par_var;
                } else {
                    values = ArrayUtils.intersect(values, par_var);
                }
            }

            p_values[i] = values;
        }

        return p_values;
    }

    /*
    protected boolean containsMissing(int n, int[] set_p) {
        for (int p : set_p) {
            int ar = dat.l_n_arity[p] + 1;
            short val = (short) (n % ar);

            n /= ar;

            if (val == dat.l_n_arity[p]) {
                return true;
            }
        }
        return false;
    }*/

    public void resetCache() {
        cache_z_rows = new HashMap<SIntSet, int[][]>();
    }

    protected double log(double p) {
        if (base2)
            return FastMath.log(p) / FastMath.log(2);
        return FastMath.log(p);
    }
}
