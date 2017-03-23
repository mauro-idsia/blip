package ch.idsia.blip.core.common.io.dat;


import java.io.*;
import java.util.logging.Logger;


/**
 * Write a datapoints file
 */
public abstract class BaseFileLineReader implements Closeable {

    private static final Logger log = Logger.getLogger(
            BaseFileLineReader.class.getName());


    public String path;

    // Concluded sample reading
    public boolean concluded;

    // Reader structure.
    protected BufferedReader rd_dat;

    public int n_var;

    public String[] l_s_names;

    public int n_datapoints;

    protected String nextLine;

    public BaseFileLineReader(String s) throws FileNotFoundException {
        this.rd_dat = new BufferedReader(new FileReader(s));
        this.path = s;
    }


    public abstract short[] next();

    public abstract boolean readMetaData() throws IOException;

    public void close() throws IOException {
        if (rd_dat != null) {
            rd_dat.close();
        }
    }

}
