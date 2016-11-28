package ch.idsia.ipp.core.utils.exp;


import ch.idsia.ipp.core.common.BayesianNetwork;


public class CyclicGraphException extends Exception {

    private final BayesianNetwork bn;

    public CyclicGraphException(BayesianNetwork bn) {
        super("Found an acyclic bayesian network!");
        this.bn = bn;
    }
}
