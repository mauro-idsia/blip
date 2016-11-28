package ch.idsia.ipp.core.learn.solver.samp;

import ch.idsia.ipp.core.utils.data.array.TIntArrayList;

import java.util.Random;

public class SimpleSampler implements Sampler {

    private final int n;
    private final Random r;

    private TIntArrayList vars;

    private Object lock = new Object();

    @Override
    public int[] sample() {
        int[] nv;

        synchronized (lock) {
            vars.shuffle(r);
            nv = vars.toArray().clone();
        }

        return  nv;
    }

    public SimpleSampler(int n) {
        this.n = n;
        r = new Random();
    }

    @Override
    public void init() {
        vars = new TIntArrayList();
        for (int i = 0; i < n; i++)
            vars.add(i);
    }
}
