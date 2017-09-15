package ch.idsia.blip.core.learn.solver.ps;


import ch.idsia.blip.core.utils.other.ParentSet;


/**
 * Provides the parent set guiding the exploration
 */
public interface Provider {

    public ParentSet[][] getParentSets();
}
