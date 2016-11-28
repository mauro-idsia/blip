package ch.idsia.ipp.core.learn.solver.ps;

import ch.idsia.ipp.core.utils.ParentSet;

public class NullProvider implements Provider {

    @Override
    public ParentSet[][] getParentSets() {
        return null;
    }
}
