package ch.idsia.blip.core.common.io.dat;


import ch.idsia.blip.core.common.BayesianNetwork;

import java.io.BufferedWriter;
import java.io.IOException;


/**
 * Write a datapoints file
 */
public abstract class BaseFileLineWriter {

    // Network to graph about
    protected final BayesianNetwork bn;

    // Writer for data
    protected BufferedWriter wr;

    /**
     * Default constructor
     *
     * @param in_bn        network to graph about
     * @param in_rd_writer writer for data
     */
    public BaseFileLineWriter(BayesianNetwork in_bn, BufferedWriter in_rd_writer) {
        wr = in_rd_writer;
        bn = in_bn;
    }

    /**
     * Write metadata in the file.
     *
     * @param n_sample number of datapoints
     * @throws IOException if there is a problem in writing.
     */
    public abstract void writeMetaData(int n_sample) throws IOException;
    /**
     * Write the next line of sample
     *
     * @param n      counter
     * @param sample sample to graph (value for each variable)
     * @throws IOException if there is a problem writing
     */
    public abstract void next(int n, short[] sample) throws IOException;

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
