package ch.idsia.ipp.core.common.score;


import ch.idsia.ipp.core.common.io.DataFileReader;


/**
 * Computes the DyBIC.
 * <p/>
 * Remenber: last index in values is for missing data (let's simply ignore them!)
 */
class DyBIC extends BIC {

    private final double threshold;

    public DyBIC(DataFileReader dat, double threshold) {
        super(dat);
        this.threshold = threshold;
    }

    @Override
    protected double getPenalization(int arity) {
        return Math.log(dat.n_datapoints) * (arity - 1) * threshold;
    }

    @Override
    protected double getPenalization(int arity, int p_arity) {
        return Math.log(dat.n_datapoints) * (arity - 1) * p_arity * threshold;
    }
}
