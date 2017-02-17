package ch.idsia.blip.core.learn.solver.ps;

import ch.idsia.blip.core.common.io.ScoreReader;
import ch.idsia.blip.core.utils.ParentSet;

import java.io.IOException;
import java.util.logging.Logger;

import static ch.idsia.blip.core.utils.RandomStuff.logExp;
import static ch.idsia.blip.core.utils.RandomStuff.pf;

public class ScoreProvider implements Provider {

    private final Logger log = Logger.getLogger(ScoreProvider.class.getName());

    protected final ScoreReader sc;

    public ScoreProvider(ScoreReader sc) {
        this.sc = sc;
    }

    @Override
    public ParentSet[][] getParentSets() {

        try {
            sc.readScores();
        } catch (IOException e) {
            pf("Problem reading file: %s. Problem: %s \n\n", sc.filename, e.getMessage());
            logExp(log, e);
            return null;
        }

        return sc.m_scores;
    }
}
