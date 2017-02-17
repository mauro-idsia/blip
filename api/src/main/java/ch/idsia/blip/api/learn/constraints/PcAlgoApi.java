package ch.idsia.blip.api.learn.constraints;


import ch.idsia.blip.api.Api;
import ch.idsia.blip.core.common.DataSet;
import ch.idsia.blip.core.learn.constraints.PcAlgo;
import org.kohsuke.args4j.Option;

import java.util.logging.Logger;

import static ch.idsia.blip.core.utils.RandomStuff.getDataFromFile;

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
        DataSet dat = getDataFromFile(ph_dat);
        pc.verbose = verbose;
        pc.execute(dat);
    }

}
