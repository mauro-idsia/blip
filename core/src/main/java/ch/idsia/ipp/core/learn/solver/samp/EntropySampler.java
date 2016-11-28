package ch.idsia.ipp.core.learn.solver.samp;

import ch.idsia.ipp.core.common.analyze.Entropy;
import ch.idsia.ipp.core.common.io.DataFileReader;

import java.io.FileNotFoundException;
import java.util.Random;

public class EntropySampler implements Sampler {

    protected  int n;
    protected Random r;

    protected DataFileReader dat;

    protected double[] weight;

    private Object lock = new Object();

    @Override
    public int[] sample() {
        return sampleWeighted(n, r, weight);
    }

    public EntropySampler(String ph_dat, int n) {
        try {
            dat = new DataFileReader(ph_dat);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        this.n = n;
        r = new Random();
    }

    @Override
    public void init() {

            weight = new double[n];
            Entropy e = new Entropy(dat);

            for (int i = 0; i < n; i++) {
                weight[i] = e.computeH(i);
            }
    }

    public int[] sampleWeighted(int n, Random r, double[] weights) {

        int[] new_ord = new int[n];

        synchronized (lock) {

            boolean[] selected = new boolean[n];
            for (int j = 0; j < n; j++) {

                double tot = 0;
                for (int i = 0; i < n; i++) {
                    if (!selected[i])
                        tot += weights[i];
                }
                double v = r.nextDouble() - Math.pow(2, -10);
                int sel = -1;
                for (int i = 0; i < n && sel == -1; i++) {
                    if (!selected[i]) {
                        double s = weights[i] / tot;
                        if (v < s)
                            sel = i;
                        v -= s;
                    }
                }

                selected[sel] = true;
                new_ord[j] = sel;
            }
        }

        return new_ord;
    }
}
