package ch.idsia.blip.api.learn.solver;


import ch.idsia.blip.api.Api;
import ch.idsia.blip.core.common.io.ScoreReader;
import ch.idsia.blip.core.learn.solver.ScoreSolver;
import ch.idsia.blip.core.utils.RandomStuff;
import org.kohsuke.args4j.Option;

import java.util.logging.Logger;

import static ch.idsia.blip.core.utils.RandomStuff.getWriter;


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

    @Option(name="-e", usage="improvement delta")
    protected int delta = 0;

    @Option(name="-b", usage="number of machine cores to use")
    protected int thread_pool_size = 1;

    @Option(name="-o", usage="number of solutions to output")
    protected int out_solutions = 1;

    @Option(name="-l", usage="log file (if not given, to output)")
    protected String logPath;

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
        solver.delta = delta;
        solver. out_solutions = out_solutions;
        solver.verbose = verbose;
        if (log != null)
            solver.logWr = getWriter(logPath);
        solver.go(ph_result);
    }
}

