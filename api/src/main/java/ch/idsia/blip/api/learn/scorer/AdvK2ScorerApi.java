package ch.idsia.blip.api.learn.scorer;

import ch.idsia.blip.core.learn.scorer.AdvK2;
import ch.idsia.blip.core.learn.scorer.BaseScorer;

import java.util.logging.Logger;

public class AdvK2ScorerApi extends ScorerApi {

    private static final Logger log = Logger.getLogger(
            AdvK2ScorerApi.class.getName());

    public static void main(String[] args) {
        defaultMain(args, new AdvK2ScorerApi(), log);
    }

    @Override
    protected BaseScorer getScorer() {
        return new AdvK2();
    }
}
