package ch.idsia.blip.core.learn.solver;

import ch.idsia.blip.core.learn.solver.ps.MaxScoreProvider;
import ch.idsia.blip.core.learn.solver.ps.Provider;
import ch.idsia.blip.core.learn.solver.ps.SimpleProvider;
import ch.idsia.blip.core.learn.solver.samp.Sampler;
import ch.idsia.blip.core.learn.solver.samp.SimpleSampler;
import ch.idsia.blip.core.utils.ParentSet;

public abstract class ScoreSolver extends BaseSolver {

    public String dat_path;

    public int max_parents;

    @Override
    protected Sampler getSampler() {
        return new SimpleSampler(sc.length, this.rand);
    }

    @Override
    protected Provider getProvider() {
        if (max_parents == 0) return new SimpleProvider(sc);
        else return new MaxScoreProvider(sc, max_parents);
    }

    public void init(long start, ParentSet[][] sc, int max_exec_time, int thread_pool_size) {
        this.start = start;
        init(sc, max_exec_time, thread_pool_size);
    }

    public void init(ParentSet[][] sc, int time, int threads) {
        this.thread_pool_size = threads;
        init(sc, time);
    }

    public void init(ParentSet[][] sc, int time) {
        this.max_exec_time = time;
        init(sc);
    }

     public void init(ParentSet[][] sc) {
        super.init();
        this.sc = sc;
         this.n_var = sc.length;
    }

}
