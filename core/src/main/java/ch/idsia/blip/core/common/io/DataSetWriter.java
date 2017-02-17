package ch.idsia.blip.core.common.io;

import ch.idsia.blip.core.common.DataSet;
import ch.idsia.blip.core.utils.StringUtils;

import java.io.IOException;
import java.io.Writer;

import static ch.idsia.blip.core.utils.RandomStuff.getWriter;
import static ch.idsia.blip.core.utils.RandomStuff.wf;
import static ch.idsia.blip.core.utils.data.ArrayUtils.find;

public class DataSetWriter {

    public static void ex(DataSet dat, String s) throws IOException {
        Writer wr = getWriter(s);
        wf(wr, "%s\n", StringUtils.join(dat.l_s_names, " "));

        for (int d = 0; d < dat.n_datapoints; d++) {
            for (int n = 0; n < dat.n_var; n++) {
                int v = -1;
                for (int k = 0; k < dat.l_n_arity[n]; k++) {
                    if (find(d, dat.row_values[n][k]))
                        v = k;
                }
                wf(wr, "%d ", v);
            }

            wf(wr, "\n");
            wr.flush();
        }
    }
}
