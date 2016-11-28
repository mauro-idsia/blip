package ch.idsia.ipp.core.learn.solver.src;

import ch.idsia.ipp.core.utils.ParentSet;

/**
 * New search following the given order
 */
public interface Searcher {

    ParentSet[] search(int[] vars) ;

    void init(ParentSet[][] scores);
}
