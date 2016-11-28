package ch.idsia.ipp.core.common.io;

import ch.idsia.ipp.core.common.BayesianNetwork;
import ch.idsia.ipp.core.utils.RandomStuff;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.logging.Logger;

import static ch.idsia.ipp.core.utils.RandomStuff.*;

public abstract class BnWriter {

    private static final DecimalFormat fmt = new DecimalFormat("#.######");

    private static final Logger log = Logger.getLogger(
            BnNetWriter.class.getName());

    public void go(String net, BayesianNetwork bn) {
        Writer g = null;
        try {
            g = getWriter(net);
            go(g, bn);
        } catch (Exception e) {
            logExp(log, e);
        } finally {
            closeIt(log, g);
        }
    }

    public abstract void go(Writer wr, BayesianNetwork bn) throws IOException;

    /**
     * Write a single potential
     *
     * @param rd_wr writer for the file
     * @param p     potential
     */
    public void writeP(Writer rd_wr, double p) {
        try {
            wf(rd_wr, " %s", fmt.format(p));
        } catch (Exception e) {
            System.out.printf("p: %.2f", p);
            RandomStuff.logExp(log, e);
        }
    }
}
