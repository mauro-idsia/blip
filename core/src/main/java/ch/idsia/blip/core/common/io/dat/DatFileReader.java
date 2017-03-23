package ch.idsia.blip.core.common.io.dat;


import ch.idsia.blip.core.common.DataSet;
import ch.idsia.blip.core.utils.data.array.TIntArrayList;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static ch.idsia.blip.core.utils.data.ArrayUtils.index;


/**
 * Reads the content of a datapoints file
 */
public class DatFileReader implements Closeable {

    // logger
    private static final Logger log = Logger.getLogger(
            DatFileReader.class.getName());

    // Reader structure.
    protected BufferedReader rd_dat;

    public String path;

    private boolean done = false;
    
    protected DataSet dSet;

    public boolean readMissing = false;

    protected String nextLine;

    public DatFileReader(String s) throws FileNotFoundException {
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

    public void readValuesCache() throws IOException {

        // Collect row values
        List<List<TIntArrayList>> v_aux = new ArrayList<List<TIntArrayList>>(dSet.n_var);
        for (int n = 0; n < dSet.n_var; n++) {
            ArrayList<TIntArrayList> aux = new ArrayList<TIntArrayList>();
            for (int v = 0; v < dSet.l_n_arity[n]; v++)
                aux.add(new TIntArrayList());
                v_aux.add(aux);
        }


        List<TIntArrayList> missing_aux_v = new ArrayList<TIntArrayList>();
        TIntArrayList missing_aux_l = new TIntArrayList();

        String [] sp;
        short v;
        List<TIntArrayList> lu;
        dSet.n_datapoints = 0;
        while (nextLine != null) {
            String line = nextLine.trim();
            if ("".equals(line))
                continue;

            if (readMissing && line.contains("?"))
                continue;

            sp = getSplit(line);
            if (sp.length != dSet.n_var) {
                notifyError(dSet.n_datapoints + 1, sp.length);
                return;
            }

            // For each variable
            for (int i = 0; i < dSet.n_var; i++) {
                if ("?".equals(sp[i])) {
                    int pos = index(i, missing_aux_l);
                    if (pos < 0) {
                        pos = missing_aux_l.size();
                        missing_aux_v.add(new TIntArrayList());
                        missing_aux_l.add(i);
                    }

                    missing_aux_v.get(pos).add(dSet.n_datapoints);
                } else {
                    lu = v_aux.get(i);
                    v = Short.valueOf(sp[i]);
                    lu.get(v).add(dSet.n_datapoints);
                }
            }

            dSet.n_datapoints++;

            nextLine = rd_dat.readLine();
        }

        dSet.row_values = new int[dSet.n_var][][];
        for (int n = 0; n < dSet.n_var; n++) {
            dSet.row_values[n] = new int[dSet.l_n_arity[n]][];
            lu = v_aux.get(n);
            for (v = 0; v < dSet.l_n_arity[n]; v++) {
                dSet.row_values[n][v] = lu.get(v).toArray();
            }
        }

        dSet.missing_l = new int[dSet.n_var][];
        for (int n = 0; n < dSet.n_var; n++) {
            int pos = index(n, missing_aux_l);
            if (pos >= 0) {
                dSet.missing_l[n] = missing_aux_v.get(pos).toArray();
            }
        }

        rd_dat.close();

    }


    protected void notifyError(int line, int found) {
        log.severe(String.format("Problem in file at line %d: found %d cardinalities, %d variables.", line, dSet.n_var, found));
    }

    protected String[] getSplit(String ln) {
        return ln.split("\\s+");
    }

    public void close() throws IOException {
        if (rd_dat != null) {
            rd_dat.close();
        }
    }

    public DataSet read() throws IOException {
        
            if (done) {
                return dSet;
            }
            
            dSet = new DataSet();

            readMetaData();
        
        readValuesCache();

        done = true;
        
        return dSet;
    }

    protected void readMetaData() throws IOException {

        // Read names
        nextLine = rd_dat.readLine();
        dSet.l_s_names = getSplit(nextLine);
        dSet.n_var = dSet.l_s_names.length;

        // Read arities
        nextLine = rd_dat.readLine();
        String[] sp = getSplit(nextLine);
        dSet.l_n_arity = new int[dSet.n_var];
        if (sp.length != dSet.n_var) {
            notifyError(2, sp.length);
            return;
        }
        for (int i = 0; i < dSet.n_var; i++) {
            dSet.l_n_arity[i] = Integer.parseInt(sp[i]);
        }

        nextLine = rd_dat.readLine();

        dSet.n_datapoints = 0;
    }
}
