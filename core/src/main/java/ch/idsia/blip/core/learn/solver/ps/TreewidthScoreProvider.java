package ch.idsia.blip.core.learn.solver.ps;

import ch.idsia.blip.core.common.io.ScoreReader;

public class TreewidthScoreProvider extends ScoreProvider {

    public TreewidthScoreProvider(ScoreReader sc, int tw) {
        super(sc);
        sc.max_size = tw - 1;
    }

}
