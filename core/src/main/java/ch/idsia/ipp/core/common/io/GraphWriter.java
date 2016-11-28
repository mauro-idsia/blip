package ch.idsia.ipp.core.common.io;

import ch.idsia.ipp.core.common.BayesianNetwork;
import ch.idsia.ipp.core.common.arcs.NamedDirected;
import ch.idsia.ipp.core.utils.IncorrectCallException;

import java.io.IOException;

import static ch.idsia.ipp.core.utils.RandomStuff.getDataFileReader;

public class GraphWriter {

    public static void go(String path_res, String path_dat, String out) throws IOException, IncorrectCallException {
        go(BnResReader.ex(path_res), getDataFileReader(path_dat), out);
    }

    private static void go(BayesianNetwork f, DataFileReader dat, String out) throws IOException {
        dat.readMetaData();

        NamedDirected d = f.directed();
        d.names = dat.l_s_names;
        d.graph(out);
    }
}
