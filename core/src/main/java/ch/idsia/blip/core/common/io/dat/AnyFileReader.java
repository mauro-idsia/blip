package ch.idsia.blip.core.common.io.dat;


import ch.idsia.blip.core.common.DataSet;
import ch.idsia.blip.core.utils.data.array.TIntArrayList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import static ch.idsia.blip.core.utils.data.ArrayUtils.index;


/**
 * Reads the content of a datapoints file
 */
public class AnyFileReader extends DatFileReader {

    // logger
    private static final Logger log = Logger.getLogger(
            AnyFileReader.class.getName());

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
    @Override
    public void readValuesCache() throws IOException {

        /*
         sample = new short[dSet.n_var][dSet.n_datapoints];

         String line;
         int j = 0; // row index

         // First pass, fill raw sample
         while ((line = rd_dat.readLine()) != null) {

         int i = 0; // variable index
         int v; // value index

         for (String aux : getSplit(line)) {

         if ("?".equals(aux.trim())) {
         v = -1;
         } else {
         v = Integer.parseInt(aux);
         }

         sample[i][j] = (short) v;

         if ((v+1) >= dSet.l_n_arity[i])
         dSet.l_n_arity[i] = v+1;
         i++;
         }
         j++;
         }

         for (String aux :) {

         // For each variable
         for (int i = 0; i < dSet.n_var; i++) {
         // For each row
         for (j = 0; j < dSet.n_datapoints; j++) {
         int v = sample[i][j];
         if (v < 0)
         v = dSet.l_n_arity[i];
         v_aux[i][v].add(j);
         }
         }

         */

        // Collect row values
        List<List<TIntArrayList>> v_aux = new ArrayList<List<TIntArrayList>>(
                dSet.n_var);
        List<HashMap<String, Integer>> values = new ArrayList<HashMap<String, Integer>>();

        for (int n = 0; n < dSet.n_var; n++) {
            v_aux.add(new ArrayList<TIntArrayList>());
            values.add(new HashMap<String, Integer>());
        }

        List<TIntArrayList> missing_aux_v = new ArrayList<TIntArrayList>();
        TIntArrayList missing_aux_l = new TIntArrayList();

        String line;
        String[] sp;
        Integer v;
        List<TIntArrayList> lu;
        HashMap<String, Integer> lv;

        dSet.n_datapoints = 0;
        while ((line = rd_dat.readLine()) != null) {
            line = line.trim();
            if ("".equals(line)) {
                continue;
            }

            if (!readMissing && line.contains("?")) {
                continue;
            }

            sp = getSplit(line);
            if (sp.length != dSet.n_var) {
                notifyError(dSet.n_datapoints + 1, sp.length);

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
                    lv = values.get(i);
                    v = lv.get(sp[i]);
                    if (v == null) {
                        v = dSet.l_n_arity[i];
                        dSet.l_n_arity[i]++;
                        lv.put(sp[i], v);
                        lu.add(new TIntArrayList());
                    }
                    lu.get(v).add(dSet.n_datapoints);
                }
            }

            dSet.n_datapoints++;

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

    @Override
    protected String[] getSplit(String ln) {
        return ln.split("\\s+");
    }

    public void close() throws IOException {
        if (rd_dat != null) {
            rd_dat.close();
        }
    }

    public DataSet read(boolean readMissing) throws Exception {

        if (done) {
            return dSet;
        }

        dSet = new DataSet();

        // Number of variables (necessary)
        // dSet.n_var = Integer.parseInt(rd_dat.readLine().trim());

        // Read names
        String ln = rd_dat.readLine();

        dSet.l_nm_var = getSplit(ln);
        dSet.n_var = dSet.l_nm_var.length;

        // Read arities
        /*
         ln = rd_dat.readLine();
         String[] sp = getSplit(ln);
         dSet.l_n_arity = new int[dSet.n_var];
         if (sp.length != dSet.n_var) {
         notifyError(2, sp.length);
         return;
         }
         for (int i = 0; i < dSet.n_var; i++) {
         dSet.l_n_arity[i] = Integer.parseInt(sp[i]);
         }
         */

        dSet.n_datapoints = 0;
        dSet.l_n_arity = new int[dSet.n_var];

        readValuesCache();

        done = true;

        return dSet;
    }

    /*
     private TIntSet[] getvaluesSets(int n, int arity) {
     // For each value of the variable, create a set of rows where it appears
     TIntSet[] dSet.row_values = new TIntSet[arity];

     // For each row, get value of variable on row s; add this row to the set for that value
     for (int s = 0; s < dSet.n_datapoints; s++) {
     dSet.row_values[sample[n][s]].add(s);
     }
     // Debug
     for (int v = 0; v < arity; v++)
     log.conclude(dSet.row_values[v].toString());

     return dSet.row_values;
     }*/
}
