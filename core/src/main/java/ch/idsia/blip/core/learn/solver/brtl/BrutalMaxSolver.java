package ch.idsia.blip.core.learn.solver.brtl;

import ch.idsia.blip.core.learn.solver.src.Searcher;
import ch.idsia.blip.core.learn.solver.src.brutal.BrutalMaxDirectedSearcher;

/**
 * BRTL approach, MAX
 */
public class BrutalMaxSolver extends BrutalSolver {

    @Override
    protected String name() {
        return "BRUTAL MAX";
    }

    @Override
    public void prepare() {
        super.prepare();

        if (tw == 0)
            tw = 3;

        if (verbose > 0) {
            log("tw: " + tw + "\n");
        }
    }


    @Override
    protected Searcher getSearcher() {

      return new BrutalMaxDirectedSearcher(this, tw);

           //  return new BrutalMaxDirectedSearcherWeight(this, tw);
    }
}
