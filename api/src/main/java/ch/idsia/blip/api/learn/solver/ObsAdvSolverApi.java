package ch.idsia.blip.api.learn.solver;


import ch.idsia.blip.core.learn.solver.ObsSolver;
import ch.idsia.blip.core.learn.solver.ScoreSolver;
import org.kohsuke.args4j.Option;

import java.util.logging.Logger;

public class ObsAdvSolverApi extends ObsSolverApi {

    private static final Logger log = Logger.getLogger(
            ObsAdvSolverApi.class.getName());

    @Option(name="-d", required = true, usage="Datafile path (.dat format)")
    protected String dat_path;

    @Option(name="-smp", usage="Advanced sampler (possible values: std, mi, ent, r_mi, r_ent)")
    protected String sampler;

    @Option(name="-src", usage="Advanced searcher (possible values: std, adv)")
    protected String searcher;


    public static void main(String[] args) {
        defaultMain(args, new ObsAdvSolverApi(), log);
    }

    @Override
    protected ScoreSolver getSolver() {
        return new ObsSolver();
    }
}
