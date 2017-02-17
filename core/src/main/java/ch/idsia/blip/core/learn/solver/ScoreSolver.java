package ch.idsia.blip.core.learn.solver;

import ch.idsia.blip.core.common.io.ScoreReader;
import ch.idsia.blip.core.learn.solver.ps.Provider;
import ch.idsia.blip.core.learn.solver.ps.ScoreProvider;
import ch.idsia.blip.core.learn.solver.samp.Sampler;
import ch.idsia.blip.core.learn.solver.samp.SimpleSampler;

public abstract class ScoreSolver extends BaseSolver {

    public String dat_path;

    protected ScoreReader sc;

    @Override
    protected Provider getProvider() {
        return new ScoreProvider(sc);
    }

    @Override
    protected Sampler getSampler() {
        return new SimpleSampler(sc.n_var);
    }


    public void init(ScoreReader sc, int time, int threads) {
        this.thread_pool_size = threads;
        init(sc, time);
    }

    public void init(ScoreReader sc, int time) {
        this.max_exec_time = time;
        init(sc);
    }

     public void init(ScoreReader sc) {
        this.sc = sc;
         this.n_var = sc.n_var;
    }

}
