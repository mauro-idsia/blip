package ch.idsia.ipp.core.common.score;


import ch.idsia.ipp.core.common.analyze.Analyzer;
import ch.idsia.ipp.core.common.io.DataFileReader;


public abstract class Score extends Analyzer {

    /**
     * Num of evaluations made
     */
    public int numEvaluated = 0;
    public boolean debug = false;

    Score(DataFileReader dat) {
        super(dat);
    }

    /**
     * Compute the score with no parent set
     *
     * @param values set of row for each value of variable
     * @return computed BDeu score
     */
    public abstract double computeScore(int[][] values);

    /**
     * Compute the score for a parent set
     *
     * @param samples  set of row for each value of the variables
     * @param p_values set of row for each configuration of parents
     * @param set_p    parent set
     * @return computed BDeu score
     */
    public abstract double computeScore(short[] samples, int n, int[][] p_values, int[] set_p);

    public double computeScore(short[] samples, int n, int[][] p_values, int p) {
        return computeScore(samples, n, p_values, new int[] { p});
    }

    public abstract double inter(int n, int[] set, int p2);

    public double computeScore(int n, int[] p) {

        // Check to protect from time-consuming operations
        int arity = 1;
        for (int ps: p)
        arity *= dat.l_n_arity[ps];
        if (arity > 2000)
            return -Double.MAX_VALUE;

        int[][] p_values = computeParentSetValues(p);
        return computeScore(dat.sample[n], n, p_values, p);
    }

    public double computeScore(int n) {
        return computeScore(dat.row_values[n]);
    }

}
