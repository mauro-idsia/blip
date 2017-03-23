package ch.idsia.blip.core.common.io.dat;

import ch.idsia.blip.core.common.DataSet;

import java.io.IOException;
import java.io.Writer;

public class DataFileWriter extends DatFileWriter {

    public DataFileWriter() {
        separator = ",";
    }

    public static void ex(DataSet dat, String s) throws IOException {
        new DataFileWriter().go(dat, s);
    }


    @Override
    protected void writeMetaData(DataSet dat, Writer wr) throws IOException {
    }
}
