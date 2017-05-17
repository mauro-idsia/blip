package ch.idsia.blip.core.common.io.dat;


import ch.idsia.blip.core.common.BayesianNetwork;
import ch.idsia.blip.core.utils.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;

import static ch.idsia.blip.core.utils.RandomStuff.wf;


/**
 * Write the content of a datapoints file in a custom format.
 */
public class DatFileLineWriter extends BaseFileLineWriter {

    /**
     * Default constructor
     *
     * @param in_bn        network to graph about
     * @param in_rd_writer writer for data
     */
    public DatFileLineWriter(BayesianNetwork in_bn, BufferedWriter in_rd_writer) {
        super(in_bn, in_rd_writer);
    }

    /**
     * Write metadata in the file.
     *
     * @throws IOException if there is a problem in writing.
     */
    @Override
    public void writeMetaData() throws IOException {
           wf(wr, "%s\n", StringUtils.join(bn.l_nm_var, " "));
        wf(wr, "%s\n", StringUtils.join(bn.l_ar_var, " "));
    }

    /**
     * Write the next line of sample
     *
     * @param sample sample to graph (value for each variable)
     * @throws IOException if there is a problem writing
     */
    @Override
    public void next(short[] sample) throws IOException {

        String l = "";

            l += sample[0];
            for (int i = 1; i < bn.n_var; i++) {
                l += " " + sample[i];
            }
            l += "\n";

        // Replace missing values
        wr.write(l.replace("-1", "?"));

        wr.flush();
    }

    /**
     * Close the writer structure
     *
     * @throws IOException if there is a problem
     */
    public void close() throws IOException {
        if (wr != null) {
            wr.close();
            wr = null;
        }
    }
}
