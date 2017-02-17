package ch.idsia.blip.core.learn.solver.brtl;

import ch.idsia.blip.core.learn.solver.src.Searcher;
import ch.idsia.blip.core.learn.solver.src.brutal.BrutalMaxDirectedSearcher;
import ch.idsia.blip.core.learn.solver.src.brutal.BrutalMaxDirectedSearcherWeight;

/**
 * BRTL approach, Greedy
 */
public class BrutalMaxSolver extends BrutalGreedySolver {

    // public List<Clique> bestJuncTree;

    @Override
    protected String name() {
        return "k-MAX";
    }

    @Override
    public void prepare() {
        super.prepare();

        if (tw == 0)
            tw = 3;

        if (verbose > 0) {
            log("tw: " + tw + "\n");
            log("sampler: " + sampler + "\n");
        }
    }

    @Override
    protected Searcher getSearcher() {
        if ("weight".equals(searcher))
            return new BrutalMaxDirectedSearcherWeight(this, tw);
        return new BrutalMaxDirectedSearcher(this, tw);
    }

}
