package ch.idsia.blip.core.common.io.dat;


import ch.idsia.blip.core.common.DataSet;
import ch.idsia.blip.core.utils.data.array.TIntArrayList;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static ch.idsia.blip.core.utils.RandomStuff.p;
import static ch.idsia.blip.core.utils.data.ArrayUtils.index;


/**
 * Reads the content of a datapoints file
 */
public class ArffFileReader implements Closeable {

    // logger
    private static final Logger log = Logger.getLogger(
            ArffFileReader.class.getName());

    // Reader structure.
    private BufferedReader rd_dat;

    public String path;

    private boolean done = false;

    private DataSet dSet;

    public boolean readMissing = false;
    
    private String relation;

    ArrayList<String[]> values;

    public ArffFileReader(String s) throws FileNotFoundException {
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

        // Collect row values
        TIntArrayList[][] v_aux = new TIntArrayList[dSet.n_var][];
        for (int n = 0; n < dSet.n_var; n++) {
            TIntArrayList[] vv_aux = new TIntArrayList[dSet.l_n_arity[n]];
            for (int v = 0; v < dSet.l_n_arity[n]; v++)
                vv_aux[v] = new TIntArrayList();
            v_aux[n] = vv_aux;
        }

        List<TIntArrayList> missing_aux_v = new ArrayList<TIntArrayList>();
        TIntArrayList missing_aux_l = new TIntArrayList();

        String line;
        String [] sp;
        Integer v;
        List<TIntArrayList> lu;
        HashMap<String, Integer> lv;
        dSet.n_datapoints = 0;
        while ((line = readLn()) != null) {
            line = line.trim();
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
                    v = Arrays.binarySearch(values.get(i), sp[i]);
                    if (v < 0)
                        p("ciao");
                    v_aux[i][v].add(dSet.n_datapoints);
                }
            }

            dSet.n_datapoints++;

        }

        dSet.row_values = new int[dSet.n_var][][];
        for (int n = 0; n < dSet.n_var; n++) {
            dSet.row_values[n] = new int[dSet.l_n_arity[n]][];
            for (v = 0; v < dSet.l_n_arity[n]; v++) {
                dSet.row_values[n][v] = v_aux[n][v].toArray();
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

    private void notifyError(int line, int found) {
        log.severe(String.format("Problem in file at line %d: found %d cardinalities, %d variables.", line, dSet.n_var, found));
    }

    private String[] getSplit(String ln) {
        return ln.split(",");
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

            // Read relation name
            String ln = readLn();
            if (!ln.startsWith("@relation"))
                p("ERROR! Arff file does not start with relation");
            relation = splitSpace(ln)[1];
        ln = readLn();

            int n_var = 0;
            ArrayList<String> names = new ArrayList<String>();
            values = new ArrayList<String[]>();

            // Read names 
            while (!ln.equals("@data")) {
                if (!ln.startsWith("@attribute "))
                    p("ERROR! Arff file does not start with relation");

                ln = ln.replace("@attribute ", "");
                String name;
                String value;

                if (ln.startsWith("'")) {
                    ln = ln.substring(1);
                    int g = ln.indexOf("'");
                    name = ln.substring(0, g);
                    value = ln.substring(g +1);
                } else {
                    int g = ln.indexOf(" ");
                    name = ln.substring(0, g);
                    value = ln.substring(g);
                }

                names.add(name);
                String[] v = getSplit(value.replace("{", "").replace("}", "").trim());
                Arrays.sort(v);
                String[] va = new String[v.length];
                for (int j = 0;j  < v.length; j++)
                    va[j] = v[j].trim();

                values.add(va);

                n_var++;

                ln = readLn();
            }

            // Read names
            dSet.n_var = n_var;
            dSet.l_s_names = new String[n_var];
            dSet.l_n_arity = new int[n_var];

            for (int i = 0; i < n_var; i++) {
                dSet.l_s_names[i] = names.get(i);
                dSet.l_n_arity[i] = values.get(i).length;
            }

            dSet.n_datapoints = 0;
        
        readValuesCache();

        done = true;
        
        return dSet;
    }

    private String[] splitSpace(String ln) {
        return ln.split("\\s+");
    }

    private String readLn() throws IOException {
        String s = readL();
        if (s == null) return null;
        while (s.equals("")) {
            s = readL();
            if (s == null) return null;
        }
        return s;
    }

    private String readL() throws IOException {
        String s = rd_dat.readLine();
        if (s == null)
            return null;
        s = s.trim();
        return s;
    }
}
