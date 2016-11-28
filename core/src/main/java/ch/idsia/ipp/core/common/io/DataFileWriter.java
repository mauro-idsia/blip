package ch.idsia.ipp.core.common.io;


import ch.idsia.ipp.core.common.BayesianNetwork;
import ch.idsia.ipp.core.utils.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Logger;


/**
 * Write the content of a datapoints file in a custom format.
 */
public class DataFileWriter {

    /**
     * Logger.
     */
    private static final Logger log = Logger.getLogger(
            DataFileWriter.class.getName());

    /**
     * Network to graph about
     */
    private final BayesianNetwork bn;

    /**
     * Format to graph the file
     */
    private final Format format;

    /**
     * Writer for data
     */
    private BufferedWriter rd_writer;

    /**
     * Default constructor
     *
     * @param in_bn        network to graph about
     * @param in_rd_writer writer for data
     * @param in_format    format for the written file
     */
    public DataFileWriter(BayesianNetwork in_bn, BufferedWriter in_rd_writer, Format in_format) {
        rd_writer = in_rd_writer;
        bn = in_bn;
        format = in_format;
    }

    /**
     * Write metadata in the file.
     *
     * @param n_sample number of datapoints
     * @throws IOException if there is a problem in writing.
     */
    public void writeMetaData(int n_sample) throws IOException {

        if (format == Format.Cussens) {

            rd_writer.write(String.format("%d\n", bn.n_var));

            rd_writer.write(
                    String.format("%s\n", StringUtils.join(bn.l_nm_var, " ")));

            rd_writer.write(
                    String.format("%s\n",
                    StringUtils.join(bn.l_ar_var, " ").replace("{", "").replace("}", "").replace(",", "").trim()));

            rd_writer.write(String.format("%d\n", n_sample));
        }

        if (format == Format.Dataframe) {
            rd_writer.write(String.format("\"%s\" ", bn.name( 0)));
            for (int i = 1; i < bn.n_var; i++) {
                rd_writer.write(String.format(" \"%s\"", bn.name( i)));
            }
            rd_writer.write("\n");
        }
    }

    /**
     * Write the next line of sample
     *
     * @param n      counter
     * @param sample sample to graph (value for each variable)
     * @throws IOException if there is a problem writing
     */
    public void next(int n, short[] sample) throws IOException {

        String l = "";

        if (format == Format.Cussens) {
            l += sample[0];
            for (int i = 1; i < bn.n_var; i++) {
                l += " " + sample[i];
            }
            l += "\n";
        }

        if (format == Format.Dataframe) {
            l += String.format("\"%d\" ", n);
            for (int i = 0; i < bn.n_var; i++) {
                l += String.format(" \"%s\"", bn.values(i)[sample[i]]);
            }
            l += "\n";
        }

        // Replace missing values
        rd_writer.write(l.replace("-1", "?"));
    }

    /**
     * Close the writer structure
     *
     * @throws IOException if there is a problem
     */
    public void close() throws IOException {
        if (rd_writer != null) {
            rd_writer.close();
            rd_writer = null;
        }
    }

    /**
     * Format to graph. For now, available Cussen's and Dataframe.
     */
    public enum Format {

        /**
         * Cussen's format (used in ph_gobnilp)
         */
        Cussens, /**
         * Dataframe format
         */Dataframe
    }
}
