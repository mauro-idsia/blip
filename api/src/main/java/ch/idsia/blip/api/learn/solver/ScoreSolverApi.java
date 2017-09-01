package ch.idsia.blip.api.learn.solver;


import ch.idsia.blip.api.Api;
import ch.idsia.blip.core.learn.solver.ScoreSolver;
import ch.idsia.blip.core.utils.other.ParentSet;
import ch.idsia.blip.core.utils.other.RandomStuff;
import org.kohsuke.args4j.Option;

import java.util.logging.Logger;


public abstract class ScoreSolverApi extends Api {

    private static final Logger log = Logger.getLogger(
            ScoreSolverApi.class.getName());

    protected ScoreSolver solver;

    @Option(name = "-j", required = true, usage = "Scores input file (in jkl format)")
    protected String ph_scores;

    @Option(name = "-r", required = true, usage = "result output file. If not supplied, the scores are printed on screen")
    protected String ph_result;

    @Option(name = "-t", usage = "maximum time limit (seconds)")
    protected int max_exec_time = 10;

    @Option(name = "-e", usage = "improvement delta")
    protected int delta = 0;

    @Option(name = "-p", usage = "max_parents")
    protected int max_parents = 0;

    @Option(name = "-o", usage = "number of solutions to output")
    protected int out_solutions = 1;

    @Option(name = "-l", usage = "log file (if not given, to output)")
    protected String logPath;

    public ScoreSolverApi() {
        this.solver = getSolver();
    }

    protected abstract ScoreSolver getSolver();

    public void exec() throws Exception {
        ParentSet[][] sc = RandomStuff.getScoreReader(this.ph_scores, this.verbose);
        this.solver.init(options());
        this.solver.init(sc);
        if (log != null) {
            this.solver.logWr = RandomStuff.getWriter(this.logPath);
        }
        this.solver.go(this.ph_result);
    }
}

