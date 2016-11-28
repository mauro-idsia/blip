package ch.idsia.ipp.api.utils;


import ch.idsia.ipp.api.Api;
import ch.idsia.ipp.core.common.BayesianNetwork;
import org.kohsuke.args4j.Option;

import java.util.logging.Logger;

import static ch.idsia.ipp.core.utils.RandomStuff.getBayesianNetwork;
import static ch.idsia.ipp.core.utils.RandomStuff.pf;

public class Analyze extends Api {

    private static final Logger log = Logger.getLogger(Analyze.class.getName());

    @Option(name="-n", required = true, usage="Bayesian network file path")
    private String ph;

    public static void main(String[] args) {
        defaultMain(args, new Analyze(), log);
    }

    @Override
    public void exec() throws Exception {

        BayesianNetwork bn = getBayesianNetwork(ph);

        pf("Number of nodes: %d \n", bn.n_var);
        pf("Number of edges: %d \n", bn.numEdges());
        pf("Check: is acyclic? %b \n", bn.isAcyclic());
        pf("Check treediwth: %d \n", bn.treeWidth());
    }
}
