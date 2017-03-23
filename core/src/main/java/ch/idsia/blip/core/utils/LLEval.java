package ch.idsia.blip.core.utils;

import ch.idsia.blip.core.common.BayesianNetwork;
import ch.idsia.blip.core.common.io.dat.BaseFileLineReader;
import ch.idsia.blip.core.utils.data.array.TDoubleArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static ch.idsia.blip.core.utils.RandomStuff.getDataSetReader;

public class LLEval {

    private static final Logger log = Logger.getLogger(LLEval.class.getName());

    public double ll;

    public TDoubleArrayList ls;

    public void go(BayesianNetwork bn, String s) {
        go(bn, getDataSetReader(s));
    }

    /**
     * @param bn
     * @param dat_rd
     * @return computed log-likelihood of the given Bayesian network over the given data
     */
    public void go(BayesianNetwork bn, BaseFileLineReader dat_rd) {

         ll = 0.0;

         ls = new TDoubleArrayList();

        short[] samp = new short[0];

        int cnt = 0;

        try {
            dat_rd.readMetaData();

            samp = dat_rd.next();

            while (!dat_rd.concluded) {
                double l = bn.getLogLik(samp);
                ll += l;
                ls.add(l);
                samp = dat_rd.next();
                cnt++;
            }

        } catch (Exception e) {
            System.out.println("Sample: " + Arrays.toString(samp));
            RandomStuff.logExp(log, e);
        }

        ll /= cnt;

    }

    /**
     * Reorder sample, from data order to network order
     *
     * @param samp       original sample
     * @param data_names names of variables (with order found in data)
     * @param bn_names   names of variables (with order found in network)
     * @return reordered sample
     */
    private short[] reorder(short[] samp, String[] data_names, List<String> bn_names) throws Exception {
        short[] n_samp = new short[samp.length];

        for (int ix = 0; ix < samp.length; ix++) {
            int n_ix = bn_names.indexOf(data_names[ix]);

            if (n_ix < 0) {
                throw new Exception("Not found variable: " + data_names[ix]);
            }
            n_samp[n_ix] = samp[ix];
        }

        return n_samp;
    }

}
