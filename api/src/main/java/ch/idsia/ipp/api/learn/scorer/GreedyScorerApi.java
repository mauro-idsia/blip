package ch.idsia.ipp.api.learn.scorer;


import ch.idsia.ipp.core.learn.scorer.BaseScorer;
import ch.idsia.ipp.core.learn.scorer.GreedyScorer;

import java.util.logging.Logger;


public class GreedyScorerApi extends ScorerApi {

    private static final Logger log = Logger.getLogger(
            GreedyScorerApi.class.getName());

    public static void main(String[] args) {
        defaultMain(args, new GreedyScorerApi(), log);
    }

    @Override
    protected BaseScorer getScorer() {
        return new GreedyScorer();
    }
}
