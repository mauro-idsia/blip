package ch.idsia.ipp.core.learn.solver.samp;

import ch.idsia.ipp.core.common.analyze.Entropy;

public class EntropyRSampler extends EntropySampler {

    public EntropyRSampler(String ph_dat, int n) {
        super(ph_dat, n);
    }

    @Override
    public void init() {
        weight = new double[n];
        Entropy e = new Entropy(dat);

        for (int i = 0; i < n; i++) {
            weight[i] = 1.0 / e.computeH(i);
        }
    }

}
