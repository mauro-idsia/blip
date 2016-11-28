package ch.idsia.ipp.api.learn.scorer;


import ch.idsia.ipp.core.learn.scorer.BaseScorer;
import ch.idsia.ipp.core.learn.scorer.SeqAdvScorer;

import java.util.logging.Logger;


public class SeqAdvScorerApi extends ScorerApi {

    private static final Logger log = Logger.getLogger(
            SeqAdvScorerApi.class.getName());

    public static void main(String[] args) {
        defaultMain(args, new SeqAdvScorerApi(), log);
    }

    @Override
    protected BaseScorer getScorer() {
        return new SeqAdvScorer();
    }
}
