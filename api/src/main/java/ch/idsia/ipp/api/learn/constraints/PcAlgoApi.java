package ch.idsia.ipp.api.learn.constraints;


import ch.idsia.ipp.api.Api;
import ch.idsia.ipp.core.common.io.DataFileReader;
import ch.idsia.ipp.core.learn.constraints.PcAlgo;
import org.kohsuke.args4j.Option;

import java.util.logging.Logger;

import static ch.idsia.ipp.core.utils.RandomStuff.getDataFileReader;

public class PcAlgoApi extends Api {

    private static final Logger log = Logger.getLogger(PcAlgoApi.class.getName());

    @Option(name="-d", required = true, usage="Datafile path (.dat format)")
    private static String ph_dat;

    @Option(name="-n", required = true, usage="Bayesian network file path")
    private static String ph_net;

    private final PcAlgo pc;

    public PcAlgoApi() {
        pc = new PcAlgo();
    }

    protected static void main(String[] args) {
        defaultMain(args, new PcAlgoApi(), log);
    }

    @Override
    public void exec() throws Exception {
        DataFileReader dat = getDataFileReader(ph_dat);
        System.out.println("ciao");
        pc.verbose = verbose;
        pc.execute(dat);
    }

}
