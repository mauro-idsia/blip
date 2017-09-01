package ch.idsia.blip.core.learn.solver.ps;

import ch.idsia.blip.core.utils.other.ParentSet;

public class SimpleProvider implements Provider {

    protected ParentSet[][] sc;

    public SimpleProvider(ParentSet[][] sc) {
        this.sc = sc;
    }

    @Override
    public ParentSet[][] getParentSets() {
        return sc;
    }
}
