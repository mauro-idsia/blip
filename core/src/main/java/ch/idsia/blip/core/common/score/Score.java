package ch.idsia.blip.core.common.score;


import ch.idsia.blip.core.common.DataSet;
import ch.idsia.blip.core.common.analyze.Analyzer;


public abstract class Score extends Analyzer {

    /**
     * Num of evaluations made
     */
    public int numEvaluated = 0;
    public boolean debug = false;

    Score(DataSet dat) {
        super(dat);
    }

    /**
     * Compute the score with no parent set
     *
     * @return computed BDeu score
     */
    public abstract double computeScore(int n);

    /**
     * Compute the score for a parent set
     *
     * @param set_p    parent set
     * @return computed BDeu score
     */
    public abstract double computeScore(int n, int[] set_p);

    public double computeScore(int n, int p) {
        return computeScore(n, new int[] {p});
    }

    public abstract double inter(int n, int[] set, int p2);

    public abstract String descr();

    protected boolean check(int[] p) {
        // Check to protect from time-consuming operations
        int arity = 1;
        for (int ps: p)
            arity *= dat.l_n_arity[ps];
        return  arity > 2000 || arity == 1;
    }

}
