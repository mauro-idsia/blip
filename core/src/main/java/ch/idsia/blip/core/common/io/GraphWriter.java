package ch.idsia.blip.core.common.io;

import ch.idsia.blip.core.common.BayesianNetwork;
import ch.idsia.blip.core.common.arcs.NamedDirected;
import ch.idsia.blip.core.utils.IncorrectCallException;

import java.io.IOException;

public class GraphWriter {

    public static void go(String path_res, String path_dat, String out) throws IOException, IncorrectCallException {
        go(BnResReader.ex(path_res), new DataFileLineReader(path_dat), out);
    }

    private static void go(BayesianNetwork f, DataFileLineReader dr, String out) throws IOException {
        dr.readMetaData();

        NamedDirected d = f.directed();
        d.names = dr.l_s_names;
        d.graph(out);
    }
}
