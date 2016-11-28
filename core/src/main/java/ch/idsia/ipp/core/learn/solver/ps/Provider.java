package ch.idsia.ipp.core.learn.solver.ps;

import ch.idsia.ipp.core.utils.ParentSet;

/**
 * Provides the parent set guiding the exploration
 */
public interface Provider {

    public ParentSet[][] getParentSets();
}
