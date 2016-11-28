package ch.idsia.ipp.core.utils;

import ch.idsia.ipp.core.common.io.DataFileReader;

import java.io.IOException;
import java.io.Writer;
import java.util.logging.Logger;

import static ch.idsia.ipp.core.utils.RandomStuff.logExp;
import static ch.idsia.ipp.core.utils.RandomStuff.wf;

public class Marginals {

    private static final Logger log = Logger.getLogger(Marginals.class.getName());

    public static void ex(DataFileReader dat_rd, Writer wr) {
        try {
            new Marginals().go(dat_rd, wr);
        } catch (IOException e) {
            logExp(log, e);
        }
    }

    private void go(DataFileReader dat_rd, Writer wr) throws IOException {
        dat_rd.read();

        int n= dat_rd.n_var;
        double d = dat_rd.n_datapoints * 1.0;
        wf(wr, "%d\n", n);
        for (int i = 0; i < n; i++) {
            int a = dat_rd.l_n_arity[i];
            wf(wr, "%d %d", i, a);
            for (int v = 0; v < a; v++) {
                wf(wr, " %5.4f", dat_rd.row_values[i][v].length / d);
            }
            wf(wr, "\n");
        }
    }
}
