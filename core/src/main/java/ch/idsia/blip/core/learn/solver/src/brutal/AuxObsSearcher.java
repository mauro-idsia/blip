package ch.idsia.blip.core.learn.solver.src.brutal;

import ch.idsia.blip.core.learn.solver.BaseSolver;
import ch.idsia.blip.core.learn.solver.src.obs.ObsSearcher;

import static ch.idsia.blip.core.utils.data.ArrayUtils.find;


public class AuxObsSearcher extends ObsSearcher {

    private int[] initCl;

    public AuxObsSearcher(BaseSolver solver, int tw) {
        super(solver);
    }

    @Override
    protected boolean acceptable(int[] ps, boolean[] fb) {
        for (int p : ps) {
            if (!find(p, initCl))
                return false;
        }

        return super.acceptable(ps, fb);
    }

    public void setClique(int[] initCl) {
        this.initCl = initCl;
    }
}
