package ch.idsia.ipp.api.learn.solver.tw.brtl;


        import ch.idsia.ipp.core.learn.solver.ScoreSolver;
        import ch.idsia.ipp.core.learn.solver.brtl.BrutalGreedySolver;
        import org.kohsuke.args4j.Option;

        import java.util.logging.Logger;

public class BrutalGreedyAdvSolverApi extends TwSolverApi {

    private static final Logger log = Logger.getLogger(
            BrutalGreedyAdvSolverApi.class.getName());

    @Option(name="-d", usage="Datafile path (.dat format)")
    private String dat_path;

    @Option(name="-smp", usage="Advanced sampler (possible values: std, mi, ent, r_mi, r_ent)")
    private String sampler;

    @Option(name="-src", usage="Advanced searcher (possible values: std, adv)")
    private String searcher;

    public static void main(String[] args) {
        defaultMain(args, new BrutalGreedyAdvSolverApi(), log);
    }

    @Override
    protected ScoreSolver getSolver() {
        return new BrutalGreedySolver();
    }

    @Override
    public void exec() throws Exception {
        ((BrutalGreedySolver) solver).initAdv(dat_path, sampler, searcher);
        super.exec();
    }
}