package ch.idsia.blip.api.learn.solver;


import ch.idsia.blip.core.learn.solver.ObsSolver;
import ch.idsia.blip.core.learn.solver.ScoreSolver;
import ch.idsia.blip.core.utils.other.IncorrectCallException;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.util.logging.Logger;

import static ch.idsia.blip.core.utils.other.RandomStuff.getWriter;
import static ch.idsia.blip.core.utils.other.RandomStuff.p;


public class ObsAdvSolverApi extends ObsSolverApi {

    @Option(name = "-d", usage = "Datafile path (.dat format)")
    protected String dat_path;

    @Option(name = "-smp", usage = "Advanced sampler (possible values: std, mi, ent, r_mi, r_ent)")
    protected String sampler;

    @Option(name = "-src", usage = "Advanced searcher (possible values: std, adv)")
    protected String searcher;

    public static void main(String[] args) {
        defaultMain(args, new ObsAdvSolverApi());
    }

    @Override
    protected ScoreSolver getSolver() {
        return new ObsSolver();
    }

    @Override
    protected void check() throws IncorrectCallException {
        super.check();

        if ( dat_path!= null && !dat_path.equals("")  && new File(dat_path).exists()) {
            throw new IncorrectCallException("Data input file ("+dat_path +") does not exists.");
        }
    }

}
