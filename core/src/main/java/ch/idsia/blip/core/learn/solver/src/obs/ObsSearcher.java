package ch.idsia.blip.core.learn.solver.src.obs;


import ch.idsia.blip.core.common.BayesianNetwork;
import ch.idsia.blip.core.learn.solver.BaseSolver;
import ch.idsia.blip.core.learn.solver.src.ScoreSearcher;
import ch.idsia.blip.core.utils.data.ArrayUtils;
import ch.idsia.blip.core.utils.exp.CyclicGraphException;
import ch.idsia.blip.core.utils.other.ParentSet;


/**
 * (given an order, for each variable select the best parent set that doesn't contain any preceding variable)
 */
public class ObsSearcher extends ScoreSearcher {

    protected boolean[] voidB;

    protected boolean[] fullB;

    protected boolean[] forbidden;

    protected double eps = -Math.pow(2.0D, -18.0D);

    protected boolean[][] cand;

    protected double old_sk;

    private ParentSet pSet;

    public ObsSearcher(BaseSolver solver) {
        super(solver);
    }

    // Find the best combination given the order (second way, koller's)
    @Override
    public ParentSet[] search() {
        this.vars = this.smp.sample();

        return obs(vars);
    }

    public ParentSet[] obs(int[] vars) {

        obs(this.vars);

        boolean gain = true;
        while (gain) {
            gain = greedy(this.vars);
        }
        return this.last_str;
    }

    public boolean greedy(int[] vars) {
        int best_ix = -1;

        double best_gain = -this.eps;

        ArrayUtils.cloneArray(this.voidB, this.forbidden);
        for (int ix = 0; ix < this.n_var - 1; ix++) {
            int v = vars[ix];
            int next = vars[(ix + 1)];

            this.forbidden[v] = false;
            double gain = -this.last_str[next].sk + best(next).sk;

            this.forbidden[next] = true;
            gain += -this.last_str[v].sk + best(v).sk;
            if (gain > best_gain) {
                best_ix = ix;
                best_gain = gain;
            }
            this.forbidden[v] = true;
        }
        if (best_ix == -1) {
            return false;
        }
        ArrayUtils.cloneArray(this.voidB, this.forbidden);
        for (int ix = 0; ix < best_ix; ix++) {
            this.forbidden[vars[ix]] = true;
        }
        int v = vars[best_ix];
        int next = vars[(best_ix + 1)];

        this.forbidden[v] = false;
        this.last_str[next] = best(next);

        this.forbidden[next] = true;
        this.last_str[v] = best(v);

        ArrayUtils.swapArray(vars, best_ix, best_ix + 1);

        this.last_sk += best_gain;
        try {
            new BayesianNetwork(this.last_str).checkAcyclic();
        } catch (CyclicGraphException e) {
            e.printStackTrace();
        }
        this.solver.newStructure(this.last_str);

        return true;
    }

    public ParentSet[] obs(int[] vars) {
        this.last_sk = 0.0D;

        ArrayUtils.cloneArray(this.voidB, this.forbidden);
        for (int v : vars) {
            this.pSet = best(v);
            this.last_str[v] = this.pSet;
            this.last_sk += this.pSet.sk;

            this.forbidden[v] = true;
            fullB[i] = true;
        }
        return this.last_str;
    }

    protected ParentSet best(int v) {
        for (ParentSet pSet : this.m_scores[v]) {
            if (acceptable(pSet.parents, this.forbidden)) {
                return pSet;
            }
        }
        return null;
    }

    public void init(ParentSet[][] scores, int thread) {
        super.init(scores, thread);

        this.last_str = new ParentSet[this.n_var];

        this.forbidden = new boolean[this.n_var];

        this.voidB = new boolean[this.n_var];
        this.fullB = new boolean[this.n_var];
        for (int i = 0; i < this.n_var; i++) {
            this.voidB[i] = false;
            this.fullB[i] = true;
        }
        this.cand = new boolean[this.n_var][];
        for (int i = 0; i < this.n_var; i++) {
            this.cand[i] = new boolean[this.n_var];
            for (ParentSet ps : this.m_scores[i]) {
                for (int p : ps.parents) {
                    this.cand[i][p] = true;
                }
            }
        }
    }

    protected boolean acceptable(int[] parents, boolean[] forbidden) {
        for (int p : parents) {
            if (forbidden[p]) {
                return false;
            }
        }
        return true;
    }

}
