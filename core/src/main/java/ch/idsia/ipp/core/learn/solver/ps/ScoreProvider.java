package ch.idsia.ipp.core.learn.solver.ps;

import ch.idsia.ipp.core.common.io.ScoreReader;
import ch.idsia.ipp.core.utils.ParentSet;

import java.io.IOException;

import static ch.idsia.ipp.core.utils.RandomStuff.logExp;
import static ch.idsia.ipp.core.utils.RandomStuff.pf;

public class ScoreProvider implements Provider {

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
            logExp(e);
            return null;
        }

        return sc.m_scores;
    }
}
