package ch.idsia.blip.core.learn.solver;


import ch.idsia.blip.core.learn.solver.src.Searcher;
import ch.idsia.blip.core.learn.solver.src.WinSearcher;

import static ch.idsia.blip.core.utils.other.RandomStuff.f;


/**
 * (given an order, for each variable select the best parent set compatible with the previous assignment).
 */
public class WinSolver extends ScoreSolver {

    public int max_windows = 4;

    @Override
    public void prepare() {
        super.prepare();

        if (verbose > 0) {
            logf("max window: %d \n", max_windows);
        }
    }

    @Override
    protected Searcher getSearcher() {
        return new WinSearcher(this);
    }

    @Override
    protected String name() {
        return f("WinAsobs %d", max_windows);
    }

    public void init(int max_windows) {
        this.max_windows = max_windows;
        super.init();
    }
}
