package ch.idsia.blip.core.learn.solver.samp;

import java.util.Random;

public class EntropyBSampler extends EntropySampler {

    protected double[] weight_r;

    Random r;

    @Override
    public int[] sample() {
        double v = r.nextDouble();

        if (v > 0.66) {
            // p("direct");
            return sampleWeighted(n, r, weight);

        }

        if (v > 0.33) {
            // p("inverse");
            return  sampleWeighted(n, r, weight_r);
        }

        // p("normal");
        return sample();
    }

    public EntropyBSampler(String ph_dat, int n) {
        super(ph_dat, n);
        r = new Random();
    }

    @Override
    public void init() {
        super.init();

        weight_r = new double[n];
        for (int i = 0; i < n; i++)
            weight_r[i] = 1.0 / weight[i];
    }
}
