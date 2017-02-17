package ch.idsia.blip.core.learn.solver;

import ch.idsia.blip.core.learn.solver.samp.Sampler;
import ch.idsia.blip.core.learn.solver.src.ObsSearcher;
import ch.idsia.blip.core.learn.solver.src.Searcher;

import static ch.idsia.blip.core.learn.solver.samp.SamplerUtils.getAdvSampler;
import static ch.idsia.blip.core.utils.RandomStuff.f;

;


/**
 * (given an order, for each variable select the best parent set that doesn't contain any preceding variable)
 */
public class ObsSolver  extends ScoreSolver {


    public String sampler;

    public String searcher;

    public ObsSolver(String search) {
        this.searcher = search;
    }

    public ObsSolver() {
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
    protected Searcher getSearcher() {
        return new ObsSearcher(this);
    }

    @Override
    protected String name() {
        return f("Obs %s", searcher);
    }

    public void initAdv(String searcher) {
        this.searcher = searcher;
    }

    public void initAdv(String dat_path, String sampler, String searcher) {
        this.dat_path = dat_path;
        this.sampler = sampler;
        initAdv(searcher);
    }
}
