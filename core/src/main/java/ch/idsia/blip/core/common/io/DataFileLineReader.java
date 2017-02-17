package ch.idsia.blip.core.common.io;


import java.io.*;
import java.util.logging.Logger;


/**
 * Reads the content of a datapoints file, line by line
 */
public class DataFileLineReader implements Closeable {

    // logger
    private static final Logger log = Logger.getLogger(
            DataFileLineReader.class.getName());

    public String path;

    // Concluded sample reading
    public boolean concluded;

    // Reader structure.
    private BufferedReader rd_dat;

    protected int n_var;

    protected String[] l_s_names;

    public int n_datapoints;

    private String nextLine;

    public DataFileLineReader(String s) throws FileNotFoundException {
        this.rd_dat = new BufferedReader(new FileReader(s));
        this.path = s;
    }

    private void notifyError(int line, int found) {
        log.severe(String.format("Problem in file at line %d: found %d cardinalities, %d variables.", line, n_var, found));
    }

    private String[] getSplit(String ln) {
        return ln.split("\\s+");
    }

    /**
     * Get next line from file
     *
     * @return readed line, ready for analysis
     */
    public short[] next() {

        short[] samp = new short[n_var];

        try {

            int i = 0; // variable index
            for (String aux : getSplit(nextLine)) {
                samp[i] = (short) Integer.parseInt(aux);
                i++;
            }

            n_datapoints++;

            nextLine = rd_dat.readLine();
            if (nextLine == null) {
                concluded = true;
            }

        } catch (IOException e) {
            log.severe(
                    String.format("Error while accessing data file: %s",
                    e.getMessage()));
        }

        return samp;
    }

    public void close() throws IOException {
        if (rd_dat != null) {
            rd_dat.close();
        }
    }

    public void readMetaData() throws IOException {
        String ln = rd_dat.readLine();
        l_s_names = getSplit(ln);
        n_var = l_s_names.length;

        nextLine = rd_dat.readLine();
    }
}
