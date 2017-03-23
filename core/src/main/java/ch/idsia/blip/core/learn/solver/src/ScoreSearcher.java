package ch.idsia.blip.core.learn.solver.src;

import ch.idsia.blip.core.learn.solver.BaseSolver;
import ch.idsia.blip.core.utils.ParentSet;

public abstract class ScoreSearcher implements Searcher {

    protected final BaseSolver solver;

    protected ParentSet[][] m_scores;

    protected int n_var;

    protected int[] variables;

    protected int thread;

    public double last_sk;

    protected ParentSet[] last_str;

    public ScoreSearcher(BaseSolver solver) {
        this.solver = solver;
    }

    @Override
    public ParentSet[] search(int[] vars) {
        return new ParentSet[0];
    }

    public void init(ParentSet[][] scores) {
        init(scores, 0);
    }

    @Override
    public void init(ParentSet[][] scores, int thread) {
        m_scores = scores;
        this.n_var = scores.length;

        variables = new int[n_var];
        for (int i = 0; i < n_var; i++) {
            variables[i] = i;
        }

        this.thread = thread;
    }



    protected double checkSk(ParentSet[] new_str) {
        double check = 0.0;

        for (ParentSet p : new_str) {
            if (p!= null)
                check += p.sk;
        }
        return check;
    }

    public double checkSk() {
        return checkSk(last_str);
    }

    protected int randInt(int a, int b) {
        return solver.randInt(a, b);
    }

}
