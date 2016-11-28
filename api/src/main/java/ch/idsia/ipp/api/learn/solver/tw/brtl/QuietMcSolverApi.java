package ch.idsia.ipp.api.learn.solver.tw.brtl;


import ch.idsia.ipp.core.learn.solver.ScoreSolver;
import ch.idsia.ipp.core.learn.solver.brtl.QuietGreedySolver;

import java.util.logging.Logger;

public class QuietMcSolverApi extends TwSolverApi {

    private static final Logger log = Logger.getLogger(
            QuietMcSolverApi.class.getName());

    public static void main(String[] args) {
        defaultMain(args, new QuietMcSolverApi(), log);
    }

    @Override
    protected ScoreSolver getSolver() {
        return new QuietGreedySolver();
    }
}
