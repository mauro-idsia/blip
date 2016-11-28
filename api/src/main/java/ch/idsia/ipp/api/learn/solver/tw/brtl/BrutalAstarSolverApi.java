package ch.idsia.ipp.api.learn.solver.tw.brtl;


import ch.idsia.ipp.core.learn.solver.ScoreSolver;
import ch.idsia.ipp.core.learn.solver.brtl.BrutalAstarSolver;

import java.util.logging.Logger;


public class BrutalAstarSolverApi extends TwSolverApi {

    private static final Logger log = Logger.getLogger(
            BrutalAstarSolverApi.class.getName());

    public static void main(String[] args) {
        defaultMain(args, new BrutalAstarSolverApi(), log);
    }

    @Override
    protected ScoreSolver getSolver() {
        return new BrutalAstarSolver();
    }
}
