package ch.idsia.blip.core.common.io;


import ch.idsia.blip.core.common.BayesianNetwork;
import ch.idsia.blip.core.utils.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;

import static ch.idsia.blip.core.utils.RandomStuff.wf;


/**
 * Write the content of a datapoints file in arff format.
 */
public class ArffFileWriter extends DataFileWriter {

    private final String name;

    /**
     * Default constructor
     *
     * @param in_bn        network to graph about
     * @param in_rd_writer writer for data
     */
    public ArffFileWriter(BayesianNetwork in_bn, BufferedWriter in_rd_writer, String name) {
        super(in_bn, in_rd_writer);
        this.name = name;
    }

    /**
     * Write metadata in the file.
     *
     * @param n_sample number of datapoints
     * @throws IOException if there is a problem in writing.
     */
    @Override
    public void writeMetaData(int n_sample) throws IOException {

        wf(wr, "@relation '%s'\n\n", name);

        for (int i = 0; i < bn.n_var; i++) {
            wf(wr, "@attribute %s {", bn.l_nm_var[i]);
            wf(wr, "%s}\n", StringUtils.join(bn.l_values_var[i], ","));
        }

        wf(wr, "\n@data\n");
    }

    /**
     * Write the next line of sample
     *
     * @param n      counter
     * @param sample sample to graph (value for each variable)
     * @throws IOException if there is a problem writing
     */
    @Override
    public void next(int n, short[] sample) throws IOException {

        String l = null;

        for (int i = 0; i < bn.n_var; i++) {
            if (l == null)
                l = "";
            else
                l += ",";

            l += bn.values(i)[sample[i]];
        }
        l += "\n";

        // Replace missing values
        wf(wr, l);
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
