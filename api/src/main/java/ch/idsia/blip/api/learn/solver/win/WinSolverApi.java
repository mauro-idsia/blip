package ch.idsia.blip.api.learn.solver.win;


import ch.idsia.blip.api.learn.solver.ScoreSolverApi;
import ch.idsia.blip.core.learn.solver.ScoreSolver;
import ch.idsia.blip.core.learn.solver.WinAsobsSolver;
import ch.idsia.blip.core.learn.solver.WinSolver;
import org.kohsuke.args4j.Option;

import java.util.logging.Logger;

public class WinSolverApi extends ScoreSolverApi {

    @Option(name = "-win", usage = "Maximum window size")
    protected int win = 5;

    private static final Logger log = Logger.getLogger(
            WinSolverApi.class.getName());

    public static void main(String[] args) {
        defaultMain(args, new WinSolverApi(), log);
    }

    @Override
    protected ScoreSolver getSolver() {
        return new WinSolver();
    }

    @Override
    public void exec() throws Exception {
        ((WinSolver) solver).init(win);
        super.exec();
    }

}
