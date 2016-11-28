package ch.idsia.ipp.core.learn.constraints.oracle;


import ch.idsia.ipp.core.common.analyze.MutualInformation;
import ch.idsia.ipp.core.common.io.DataFileReader;


public class MiOracle extends Oracle {

    private final double eps;
    private final MutualInformation mutualInf;

    public MiOracle(double eps, DataFileReader dat) {
        super(dat);
        this.eps = eps;
        mutualInf = new MutualInformation(dat);
    }

    @Override
    public boolean condInd(int x, int y, int[] z) {
        double mi;

        if (z.length == 0) {
            mi = mutualInf.computeMi(x, y);
        } else {
            mi = mutualInf.computeCMI(x, y, z);
        }

        // System.out.printf("%.3f - eps %.3f \n", mi, eps);

        return mi < eps;
    }
}
