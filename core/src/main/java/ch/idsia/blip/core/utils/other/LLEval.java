package ch.idsia.blip.core.utils.other;


import ch.idsia.blip.core.common.BayesianNetwork;
import ch.idsia.blip.core.common.io.dat.BaseFileLineReader;
import ch.idsia.blip.core.utils.data.array.TDoubleArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static ch.idsia.blip.core.utils.other.RandomStuff.getDataSetReader;


public class LLEval {

    private static final Logger log = Logger.getLogger(LLEval.class.getName());

    public double ll;

    public TDoubleArrayList ls;

    public boolean log10 = false;

    public static double ex(BayesianNetwork bn, String s) {
        return new LLEval().go(bn, s);
    }

    public double go(BayesianNetwork bn, String s) {
        return go(bn, getDataSetReader(s));
    }

    /**
     * @param bn
     * @param dat_rd
     * @return computed log-likelihood of the given Bayesian network over the given data
     */
    public double go(BayesianNetwork bn, BaseFileLineReader dat_rd) {
        this.ll = 0.0D;

        this.ls = new TDoubleArrayList();

        short[] samp = new short[0];

        int cnt = 0;

        try {
            dat_rd.readMetaData();
            while (!dat_rd.concluded) {
                samp = dat_rd.next();
                double l;

                if (this.log10) {
                    l = bn.getLogLik10(samp);
                } else {
                    l = bn.getLogLik(samp);
                }
                this.ll += l;
                this.ls.add(l);
                cnt++;
            }
        } catch (Exception e) {
            System.out.println("Sample: " + Arrays.toString(samp));
            RandomStuff.logExp(log, e);
        }
        this.ll /= cnt;

        return this.ll;
    }

    /**
     * Reorder sample, from data order to network order
     *
     * @param samp       original sample
     * @param data_names names of variables (with order found in data)
     * @param bn_names   names of variables (with order found in network)
     * @return reordered sample
     */
    private short[] reorder(short[] samp, String[] data_names, List<String> bn_names)
        throws Exception {
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
