package ch.idsia.blip.core.utils.exp;


import ch.idsia.blip.core.common.BayesianNetwork;


public class CyclicGraphException extends Exception {

    private final BayesianNetwork bn;

    public CyclicGraphException(BayesianNetwork bn) {
        super("Found an cyclic bayesian network!");
        this.bn = bn;
    }
}
