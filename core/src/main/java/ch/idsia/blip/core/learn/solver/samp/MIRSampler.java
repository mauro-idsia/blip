package ch.idsia.blip.core.learn.solver.samp;

import ch.idsia.blip.core.common.analyze.MutualInformation;

public class MIRSampler extends EntropySampler {

    public MIRSampler(String ph_dat, int n) {
        super(ph_dat, n);
    }

    @Override
    public void init() {
        weight = new double[n];

        MutualInformation mi = new MutualInformation(dat);

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double m = mi.computeMi(i, j);
                weight[i] += m;
                weight[j] += m;
            }
        }

        for (int i = 0; i < n; i++) {
            weight[i] = 1.0 / weight[i];
        }

    }

}
