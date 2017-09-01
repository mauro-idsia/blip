package ch.idsia.blip.core.learn.solver.ps;

import ch.idsia.blip.core.utils.other.ParentSet;

public class NullProvider implements Provider {

    @Override
    public ParentSet[][] getParentSets() {
        return null;
    }
}
