package ch.idsia.ipp.core.utils;

import ch.idsia.ipp.core.common.io.DataFileReader;
import ch.idsia.ipp.core.utils.data.hash.TIntIntHashMap;

import java.io.IOException;
import java.util.logging.Logger;

import static ch.idsia.ipp.core.utils.RandomStuff.logExp;
import static ch.idsia.ipp.core.utils.RandomStuff.pf;
import static ch.idsia.ipp.core.utils.data.ArrayUtils.intersect;

public class Query {

    private static final Logger log = Logger.getLogger(Query.class.getName());

    public static double ex(DataFileReader dat_rd, TIntIntHashMap q) {
        try {
            return new Query().go(dat_rd, q);
        } catch (IOException e) {
            logExp(log, e);
        }

        return 0;
    }

    private double go(DataFileReader dat_rd, TIntIntHashMap q) throws IOException {
        dat_rd.read();
        int[] rows = null;
        for (int var: q.keys()) {
            int val =  q.get(var);
            int[] aux = dat_rd.row_values[var][val];

            if (rows == null)
                rows = aux;
            else
                rows = intersect(rows, aux);

            pf("%d %d %d \n", var, val, rows.length);
        }

        if (rows == null || rows.length == 0)
            return 0;

        return Math.log10(rows.length * 1.0) - Math.log10(dat_rd.n_datapoints);
    }

}
