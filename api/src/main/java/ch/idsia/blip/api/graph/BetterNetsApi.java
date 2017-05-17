package ch.idsia.blip.api.graph;


import ch.idsia.blip.core.common.graph.BetterNets;

import java.util.logging.Logger;

/**
 * Update pa network file with position conclude (requires graphviz)
 */

public class BetterNetsApi extends NetToGraphApi {

    private static final Logger log = Logger.getLogger(NetToGraphApi.class.getName());

    public BetterNetsApi() {
        ntg = new BetterNets();
    }

    public static void main(String[] args) {
        defaultMain(args, new BetterNetsApi(), log);
    }
}
