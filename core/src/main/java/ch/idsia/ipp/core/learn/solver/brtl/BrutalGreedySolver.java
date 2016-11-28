package ch.idsia.ipp.core.learn.solver.brtl;

import ch.idsia.ipp.core.common.io.ScoreReader;
import ch.idsia.ipp.core.learn.solver.ScoreSolver;
import ch.idsia.ipp.core.learn.solver.ps.Provider;
import ch.idsia.ipp.core.learn.solver.ps.TreewidthScoreProvider;
import ch.idsia.ipp.core.learn.solver.samp.*;
import ch.idsia.ipp.core.learn.solver.src.BrutalGreedySearcher;
import ch.idsia.ipp.core.learn.solver.src.BrutalNewGreedySearcher;
import ch.idsia.ipp.core.learn.solver.src.BrutalOldGreedySearcher;
import ch.idsia.ipp.core.learn.solver.src.Searcher;

import static ch.idsia.ipp.core.learn.solver.samp.SamplerUtils.getAdvSampler;

/**
 * BRTL approach, Greedy
 */
public class BrutalGreedySolver extends ScoreSolver {

    public int tw;

    public String dat_path;

    public String sampler;

    public String searcher;

    @Override
    protected String name() {
        return "BRUTAL Greedy";
    }

    @Override
    public void prepare() {
        super.prepare();

        if (tw == 0)
            tw = 3;

        if (verbose > 0) {
            log("tw: " + tw + "\n");
            log("sampler: " + sampler + "\n");
            log("sercher: " + searcher + "\n");
        }
    }

    public void initAdv(String dat_path, String sampler, String searcher) {
        this.dat_path = dat_path;
        this.sampler = sampler;
        this.searcher = searcher;
    }

    @Override
    protected Provider getProvider() {
        return new TreewidthScoreProvider(sc, tw);
    }

    @Override
    protected Sampler getSampler() {
        return getAdvSampler(sampler, dat_path, sc.n_var);
    }

    @Override
    protected Searcher getSearcher() {
        if ("old".equals(searcher))
            return new BrutalOldGreedySearcher(this, tw);
            else if ("new".equals(searcher))
            return new BrutalNewGreedySearcher(this, tw);
            else
        return new BrutalGreedySearcher(this, tw);
    }

    public void init(int time, ScoreReader sc, int threads, int maxTw) {
        this.tw = maxTw;
        init(sc, time, threads);
    }


}
