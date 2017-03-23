package ch.idsia.blip.core.learn.solver.src.obs;


import ch.idsia.blip.core.learn.solver.BaseSolver;
import ch.idsia.blip.core.learn.solver.src.ScoreSearcher;
import ch.idsia.blip.core.utils.ParentSet;

import java.util.BitSet;


/**
 * (given an order, for each variable select the best parent set that doesn't contain any preceding variable)
 */
public class ObsSearcher extends ScoreSearcher {

    public ObsSearcher(BaseSolver solver) {
        super(solver);
    }

    /**
         * Find the best combination given the order (second way, koller's)
         *
         * @param vars     order of the variable
         */
    @Override
        public ParentSet[] search(int[] vars) {
            prepare();
            last_sk = 0;

            BitSet forbidden = new BitSet(n_var);

            for (int v : vars) {

                for (ParentSet pSet : m_scores[v]) {
                    if (acceptable(pSet.parents, forbidden)) {
                        last_str[v] = pSet;
                        last_sk += pSet.sk;
                        break;
                    }
                }

                forbidden.set(v);
            }

        return last_str;
        }

    /**
     * Clear structure
     */
    protected void prepare() {
        last_str = new ParentSet[n_var];
        for (int i = 0; i < n_var; i++) {
            last_str[i] = null;
        }
    }

    /**
     * Check if the given parent set contains any of the denied elements
     *
     * @param parents   parent set to evaluate
     * @param forbidden denied variables
     * @return if the parent set is acceptable (doesn't contains forbidden variables)
     */
    protected boolean acceptable(int[] parents, BitSet forbidden) {
        for (int p : parents) {
            if (forbidden.get(p)) {
                return false;
            }
        }
        return true;
    }


}
