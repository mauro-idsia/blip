package ch.idsia.ipp.core.learn.solver.ps;

import ch.idsia.ipp.core.common.io.ScoreReader;

public class TreewidthScoreProvider extends ScoreProvider {

    public TreewidthScoreProvider(ScoreReader sc, int tw) {
        super(sc);
        sc.max_size = tw - 1;
    }

}
