package ch.idsia.ipp.core.learn.constraints.oracle;


import ch.idsia.ipp.core.common.analyze.Analyzer;
import ch.idsia.ipp.core.common.io.DataFileReader;


public abstract class Oracle extends Analyzer {

    protected Oracle(DataFileReader dat) {
        super(dat);
    }

    public abstract boolean condInd(int x, int y, int[] z);
}
