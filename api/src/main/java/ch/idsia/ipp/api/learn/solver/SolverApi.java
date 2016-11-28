package ch.idsia.ipp.api.learn.solver;


import ch.idsia.ipp.api.Api;
import ch.idsia.ipp.core.common.io.ScoreReader;
import ch.idsia.ipp.core.learn.solver.ScoreSolver;
import ch.idsia.ipp.core.utils.RandomStuff;
import org.kohsuke.args4j.Option;

import java.util.logging.Logger;


public abstract class SolverApi extends Api {

    private static final Logger log = Logger.getLogger(
            SolverApi.class.getName());

    protected ScoreSolver solver;

    @Option(name="-j", required = true, usage="Scores input file (in jkl format)")
    protected String ph_scores;

    @Option(name="-r", required = true, usage="result output file. If not supplied, the scores are printed on screen")
    protected String ph_result;

    @Option(name="-t", usage="maximum time limit (seconds)")
    protected int max_exec_time = 10;

    @Option(name="-b", usage="number of machine cores to use")
    protected int thread_pool_size = 1;

    @Option(name="-o", usage="number of solutions to output")
    protected int out_solutions = 1;

    public SolverApi() {
        solver = getSolver();
    }

    protected abstract ScoreSolver getSolver();

    /**
     * Default command line execution
     */
    public void exec() throws Exception {
        ScoreReader sc = RandomStuff.getScoreReader(ph_scores, verbose);
        solver.init(sc, max_exec_time, thread_pool_size);
        solver. out_solutions = out_solutions;
        solver.verbose = verbose;
        solver.go(ph_result);
    }
}

