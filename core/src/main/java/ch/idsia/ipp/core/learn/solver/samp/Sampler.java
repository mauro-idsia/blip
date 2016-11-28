package ch.idsia.ipp.core.learn.solver.samp;

/**
 * Samples a new order for exploration
 */
public interface Sampler {

    int[] sample();

    void init();
}
