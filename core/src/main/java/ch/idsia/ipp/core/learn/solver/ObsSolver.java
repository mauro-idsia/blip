package ch.idsia.ipp.core.learn.solver;


import ch.idsia.ipp.core.learn.solver.samp.*;
import ch.idsia.ipp.core.learn.solver.src.*;

import static ch.idsia.ipp.core.learn.solver.samp.SamplerUtils.getAdvSampler;


/**
 * (given an order, for each variable select the best parent set compatible with the previous assignment).
 */
public class ObsSolver extends ScoreSolver {

    public String dat_path;

    public String sampler;

    public String searcher;

    @Override
    protected Searcher getSearcher() {
        return new ObsSearcher(this);
    }

    @Override
    public void prepare() {
        super.prepare();

        if (verbose > 0) {
            log("sampler: " + sampler + "\n");
            log("sercher: " + searcher + "\n");
        }
    }

    @Override
    protected Sampler getSampler() {
        return getAdvSampler(sampler, dat_path, sc.n_var);
    }

    @Override
    protected String name() {
        return "Obs";
    }

    public void initAdv(String dat_path, String sampler, String searcher) {
        this.dat_path = dat_path;
        this.sampler = sampler;
        this.searcher = searcher;
    }
}
