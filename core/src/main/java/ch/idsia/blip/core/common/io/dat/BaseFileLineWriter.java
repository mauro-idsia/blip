package ch.idsia.blip.core.common.io.dat;


import ch.idsia.blip.core.common.BayesianNetwork;

import java.io.IOException;
import java.io.Writer;


/**
 * Write a datapoints file
 */
public abstract class BaseFileLineWriter {

    // Network to graph about
    protected final BayesianNetwork bn;

    // Writer for data
    protected Writer wr;

    /**
     * Default constructor
     *
     * @param bn        network to graph about
     * @param wr writer for data
     */
    public BaseFileLineWriter(BayesianNetwork bn, Writer wr) {
        this.wr = wr;
        this.bn = bn;
    }

    /**
     * Write metadata in the file.
     *
     * @throws IOException if there is a problem in writing.
     */
    public abstract void writeMetaData() throws IOException;
    /**
     * Write the next line of sample
     *
     * @param sample sample to graph (value for each variable)
     * @throws IOException if there is a problem writing
     */
    public abstract void next(short[] sample) throws IOException;

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
