package ch.idsia.ipp.core.learn.solver.src;

import ch.idsia.ipp.core.learn.solver.BaseSolver;
import ch.idsia.ipp.core.utils.ParentSet;

import java.util.BitSet;

public class ObsSearcher implements Searcher {

    protected final BaseSolver solver;

    protected ParentSet[][] m_scores;

    public double last_sk;

    protected ParentSet[] last_str;

    protected int[] variables;

    protected int n_var;

    protected BitSet[] descendant;

    protected BitSet[] ancestor;

    public ObsSearcher(BaseSolver solver) {
        this.solver = solver;
    }

    @Override
    public void init(ParentSet[][] scores) {
        m_scores = scores;
        this.n_var = scores.length;

        variables = new int[n_var];
        for (int i = 0; i < n_var; i++) {
            variables[i] = i;
        }
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
                if (acceptable(v, pSet.parents, forbidden)) {
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
     * @param v         variable interested
     * @param parents   parent set to evaluate
     * @param forbidden denied variables
     * @return if the parent set is acceptable (doesn't contains forbidden variables)
     */
    protected boolean acceptable(int v, int[] parents, BitSet forbidden) {
        for (int p : parents) {
            if (forbidden.get(p)) {
                return false;
            }
        }
        return true;
    }


}
