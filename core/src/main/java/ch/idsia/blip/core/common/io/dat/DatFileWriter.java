package ch.idsia.blip.core.common.io.dat;

import ch.idsia.blip.core.common.DataSet;
import ch.idsia.blip.core.utils.StringUtils;

import java.io.IOException;
import java.io.Writer;

import static ch.idsia.blip.core.utils.RandomStuff.getWriter;
import static ch.idsia.blip.core.utils.RandomStuff.wf;
import static ch.idsia.blip.core.utils.data.ArrayUtils.find;

public class DatFileWriter {

    protected String separator = " ";

    public static void ex(DataSet dat, String s) throws IOException {
        new DatFileWriter().go(dat, s);
    }

    public void go(DataSet dat, String s) throws IOException {
        Writer wr = getWriter(s);
        writeMetaData(dat, wr);

        String g;

        for (int d = 0; d < dat.n_datapoints; d++) {
            for (int n = 0; n < dat.n_var; n++) {
                int v = -1;
                for (int k = 0; k < dat.l_n_arity[n]; k++) {
                    if (find(d, dat.row_values[n][k]))
                        v = k;
                }
                if (v != -1)
                    g = String.valueOf(v);
                else
                    g = "?";
                if (n > 0)
                    wf(wr, "%s", separator);
                wf(wr, "%s", g);
            }

            wf(wr, "\n");
            wr.flush();
        }
    }

    protected void writeMetaData(DataSet dat, Writer wr) throws IOException {
        wf(wr, "%s\n", StringUtils.join(dat.l_s_names, separator));
        wf(wr, "%s\n", StringUtils.join(dat.l_n_arity, separator));
    }
}
