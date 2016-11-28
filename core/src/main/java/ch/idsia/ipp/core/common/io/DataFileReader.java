package ch.idsia.ipp.core.common.io;


import ch.idsia.ipp.core.utils.data.array.TIntArrayList;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static ch.idsia.ipp.core.utils.RandomStuff.f;


/**
 * Reads the content of a datapoints file in Cussen's format.
 */
public class DataFileReader implements Closeable {

    /**
     * Logger.
     */
    private static final Logger log = Logger.getLogger(
            DataFileReader.class.getName());

    public String path;

    /**
     * Number of variables in BN
     */
    public int n_var;

    // Datafile metadata
    /**
     * List of names of variables in BN
     */
    public String[] l_s_names;

    /**
     * List of arities of variables in BN
     */
    public int[] l_n_arity;

    /**
     * Number of datapoints in sample
     */
    public int n_datapoints;

    /**
     * For each variable, and for each value, an array of all the row where that variable appears with that value. The last index is for missing values.
     */
    public int[][][] row_values;

    /**
     * Matrix n_var * n_datapoints, containing all the row_values in sample
     */
    public short[][] sample;

    /**
     * Concluded sample reading
     */
    public boolean concluded;

    /**
     * Reader structure.
     */
    private BufferedReader rd_dat;

    private boolean doneValues = false;
    private boolean doneMetadata = false;

    public DataFileReader(String s) throws FileNotFoundException {
        this.rd_dat = new BufferedReader(new FileReader(s));
        this.path = s;
    }

    static public int[][] clone(int[][] a) {
        int[][] b = new int[a.length][];

        for (int i = 0; i < a.length; i++) {
            b[i] = Arrays.copyOf(a[i], a[i].length);
        }
        return b;
    }

    /**
     * Read the content of the .dat file (list of variable assignment) into the structures.
     *
     * @throws IOException if there is a problem in the reading.
     */
    public void readValuesCache() throws IOException {

        if (doneValues) {
            return;
        }

        sample = new short[n_var][n_datapoints];

        String line;
        int j = 0; // row index

        // First pass, fill raw sample
        while ((line = rd_dat.readLine()) != null) {

            int i = 0; // variable index
            int v; // value index

            for (String aux : line.split(" ")) {

                if ("?".equals(aux.trim())) {
                    v = -1;
                } else {
                    v = Integer.parseInt(aux);
                }

                sample[i][j] = (short) v;

                if ((v+1) >= l_n_arity[i])
                    l_n_arity[i] = v+1;
                i++;
            }
            j++;
        }

        // Second pass, complete row values
        TIntArrayList[][] v_aux = new TIntArrayList[n_var][];

        for (int n = 0; n < n_var; n++) {
            TIntArrayList[] v1_aux = new TIntArrayList[l_n_arity[n] + 1];

            for (int v = 0; v < (l_n_arity[n] + 1); v++) {
                v1_aux[v] = new TIntArrayList();
            }
            v_aux[n] = v1_aux;
        }

        // For each variable
        for (int i = 0; i < n_var; i++) {
            // For each row
            for (j = 0; j < n_datapoints; j++) {
                int v = sample[i][j];
                if (v < 0)
                    v = l_n_arity[i];
                v_aux[i][v].add(j);
            }
        }


        row_values = new int[n_var][][];
        for (int n = 0; n < n_var; n++) {
            row_values[n] = new int[l_n_arity[n] + 1][];
            for (int v = 0; v < (l_n_arity[n] + 1); v++) {
                row_values[n][v] = v_aux[n][v].toArray();
            }
        }

        rd_dat.close();

        doneValues = true;

    }

    /**
     * Copy values from list of Integer to array of int.
     *
     * @param l input values
     * @return array of values
     */
    private int[] cvtList(List<Integer> l) {
        int[] aux = new int[l.size()];
        int i = 0;

        for (int e : l) {
            aux[i++] = e;
        }
        return aux;
    }

    /**
     * Read the metadata of the .dat file.
     *
     * @throws IOException if there is a problem with the reading.
     */
    public void readMetaData() throws IOException {
        if (doneMetadata) {
            return;
        }

        // Number of variables (necessary)
        n_var = Integer.parseInt(rd_dat.readLine().trim());

        // Read names, if they are here
        String ln = rd_dat.readLine();
        String [] sp = ln.split(" ");
        if (sp.length > 1) {
            l_s_names = sp;
            ln = rd_dat.readLine();
        } else {
            l_s_names = new String[n_var];
            for (int i = 0; i < n_var; i++)
                l_s_names[i] = f("N%d", i);
        }

        // Read arities, if they are here
        sp = ln.split(" ");
        l_n_arity = new int[n_var];
        if (sp.length > 1) {
            for (int i = 0; i < n_var; i++) {
                l_n_arity[i] = Integer.parseInt(sp[i]);
            }
            ln = rd_dat.readLine();
        } else {
            for (int i = 0; i < n_var; i++)
                l_n_arity[i] = 0;
        }

        // Number of datapoints (necessary)
        n_datapoints = Integer.parseInt(ln.trim());

        concluded = false;

        doneMetadata = true;
    }

    /**
     * Get next line from file
     *
     * @return readed line, ready for analysis
     */
    public short[] next() {

        short[] samp = new short[n_var];

        try {
            String line = rd_dat.readLine();

            if (line == null) {
                concluded = true;
                return samp;
            }

            int i = 0; // variable index

            for (String aux : line.split(" ")) {
                samp[i] = (short) Integer.parseInt(aux);
                i++;
            }

        } catch (IOException e) {
            log.severe(
                    String.format("Error while accessing data file: %s",
                    e.getMessage()));
        }

        return samp;
    }

    /**
     * Close structure.
     *
     * @throws IOException error concerning reader
     */
    public void close() throws IOException {
        if (rd_dat != null) {
            rd_dat.close();
        }
    }

    public void read() throws IOException {
        readMetaData();
        readValuesCache();
    }

    /*
     private TIntSet[] getvaluesSets(int n, int arity) {
     // For each value of the variable, create a set of rows where it appears
     TIntSet[] row_values = new TIntSet[arity];

     // For each row, get value of variable on row s; add this row to the set for that value
     for (int s = 0; s < n_datapoints; s++) {
     row_values[sample[n][s]].add(s);
     }
     // Debug
     for (int v = 0; v < arity; v++)
     log.conclude(row_values[v].toString());

     return row_values;
     }*/
}
