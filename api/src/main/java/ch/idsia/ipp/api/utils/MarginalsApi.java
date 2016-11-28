package ch.idsia.ipp.api.utils;

import ch.idsia.ipp.api.Api;
import ch.idsia.ipp.core.common.io.DataFileReader;
import ch.idsia.ipp.core.utils.Marginals;
import org.kohsuke.args4j.Option;

import java.io.Writer;
import java.util.logging.Logger;

import static ch.idsia.ipp.core.utils.RandomStuff.getDataFileReader;
import static ch.idsia.ipp.core.utils.RandomStuff.getWriter;

public class MarginalsApi extends Api {

    private static final Logger log = Logger.getLogger(MarginalsApi.class.getName());

    @Option(name="-o", required = true, usage="Result output")
    protected String ph_out;

    @Option(name="-d", required = true, usage="Dataset path")
    protected String ph_dat;

    public static void main(String[] args) {
        defaultMain(args, new MarginalsApi(), log);
    }

    @Override
    public void exec() throws Exception {
        DataFileReader dat_rd = getDataFileReader(ph_dat);
        Writer wr = getWriter(ph_out);

        Marginals.ex(dat_rd, wr);
        wr.close();
    }
}
