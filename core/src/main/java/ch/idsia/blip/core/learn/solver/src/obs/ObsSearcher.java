package ch.idsia.blip.core.learn.solver.src.obs;


import ch.idsia.blip.core.learn.solver.BaseSolver;
import ch.idsia.blip.core.learn.solver.src.ScoreSearcher;
import ch.idsia.blip.core.utils.ParentSet;

import static ch.idsia.blip.core.utils.data.ArrayUtils.cloneArray;


/**
 * (given an order, for each variable select the best parent set that doesn't contain any preceding variable)
 */
public class ObsSearcher extends ScoreSearcher {

    protected boolean[] voidB;
    protected boolean[] fullB;

    protected boolean[] forbidden;

    public ObsSearcher(BaseSolver solver) {
        super(solver);
    }

    // Find the best combination given the order (second way, koller's)
    @Override
    public ParentSet[] search() {
        vars = smp.sample();

        return obs(vars);
    }

    public ParentSet[] obs(int[] vars) {
        last_sk = 0;

        cloneArray(voidB, forbidden);

        for (int v : vars) {

            for (ParentSet pSet: m_scores[v]) {
                if (acceptable(pSet.parents, forbidden)) {
                    last_str[v] = pSet;
                    last_sk += pSet.sk;
                    break;
                }
            }

            forbidden[v] = true;
        }

        return last_str;
    }

    @Override
    public void init(ParentSet[][] scores, int thread) {
        super.init(scores, thread);

        last_str = new ParentSet[n_var];

        forbidden = new boolean[n_var];

        voidB = new boolean[n_var];
        fullB = new boolean[n_var];
        for (int i = 0; i < n_var; i++) {
            voidB[i] = false;
            fullB[i] = true;
        }
    }

    /**
     * Check if the given parent set contains any of the denied elements
     *
     * @param parents   parent set to evaluate
     * @param forbidden denied variables
     * @return if the parent set is acceptable (doesn't contains forbidden variables)
     */
    protected boolean acceptable(int[] parents, boolean[] forbidden) {
        for (int p : parents) {
            if (forbidden[p]) {
                return false;
            }
        }
        return true;
    }


}
