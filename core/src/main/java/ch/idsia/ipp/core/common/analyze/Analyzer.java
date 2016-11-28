package ch.idsia.ipp.core.common.analyze;


import ch.idsia.ipp.core.common.io.DataFileReader;
import ch.idsia.ipp.core.utils.data.ArrayUtils;
import ch.idsia.ipp.core.utils.data.SIntSet;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import static ch.idsia.ipp.core.utils.RandomStuff.logExp;


public class Analyzer {

    private static final Logger log = Logger.getLogger(Analyzer.class.getName());

    /**
     * Data file
     */
    protected final DataFileReader dat;

    /**
     * Alpha for counts
     */
    public double alpha = 1.0;

    protected HashMap<SIntSet, int[][]> cache_z_rows;

    protected Analyzer(DataFileReader dat) {
        this.dat = dat;
        try {
            dat.read();
        } catch (IOException e) {
            logExp(log, e);
        }
    }

    protected double getFreq(double v, int i) {

       // return ((v + (alpha / thread)) / (dat.n_datapoints + alpha));
        // return (v / dat.n_datapoints) * 1.0;
        double d = ((v + (alpha / i)) / (dat.n_datapoints + alpha));
        // double d1 = d+ d * (new Random().nextDouble() - 0.5) / 10.0;
       return d;
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
    public int[][] computeParentSetValues(int[] set_p) {

        int p_arity = 1;

        Arrays.sort(set_p);

        for (int p : set_p) {
            p_arity *= (dat.l_n_arity[p] + 1);
        }

        int[][] p_values = new int[p_arity][];

        for (int i = 0; i < p_arity; i++) {
            int[] values = null;

            int n = i;

            // Get value for the parent in this configuration
            for (int p : set_p) {
                int ar = dat.l_n_arity[p] + 1;
                short val = (short) (n % ar);

                n /= ar;

                // System.out.print("p: " + p + " v: " + val + ", ");

                // Update set containing sample rows for the chosen configuration
                // System.out.printf("%d (%d) - %d - %d\n", p,  dat.l_n_arity[p], val, dat.row_values[p].length);
                int[] par_var = dat.row_values[p][val];

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
    }

    public void resetCache() {
        cache_z_rows = new HashMap<SIntSet, int[][]>();
    }
}
