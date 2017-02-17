package ch.idsia.blip.core.common.graph;

import ch.idsia.blip.core.common.BayesianNetwork;
import ch.idsia.blip.core.utils.RandomStuff;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class NetToGraph {


    public int max_time;

    public Method meth = Method.Png;

    public String path_highlith;

    public void go(BayesianNetwork bn, String s) {
        go(bn, s, 1000);
    }

    public enum Method {
        Png, Plain
    }

    TreeSet<String> highligth;

    public void go(BayesianNetwork bn, String s, int max_time) {
        this.max_time = max_time;

        File index = new File(s);
        if (!index.exists()) {
            index.mkdir();
        }

        try {
        if (meth == Method.Png)
            png(bn, s);
        else if (meth == Method.Plain)
            plain(bn, s);
        } catch (Exception e) {
            RandomStuff.logExp(e);
        }
    }

    private void plain(BayesianNetwork bn, String s) throws IOException {
        printBn(bn, s);

        String h = RandomStuff.f("dot -Tplain %s.dot -o %s.plain", s, s);

        exec(h);
    }

    private void png(BayesianNetwork bn, String s) throws IOException {

        // read highlight
        if (path_highlith != null)
            highligth = readHigh();

        List<BayesianNetwork> ls = BnSeparator.go(bn);

        PrintWriter w = new PrintWriter(s + "/excluded", "UTF-8");

        Map<BayesianNetwork, Integer> sized = new HashMap<BayesianNetwork, Integer>();
        for (BayesianNetwork b: ls) {
            sized.put(b, b.n_var);
        }
        sized = RandomStuff.sortInvByValues(sized);
        // pngSingle(bn, s);

        int i = 0;
        // Print each bn separated
        for (BayesianNetwork b: sized.keySet()) {

            if (b.n_var <= 1) {
                RandomStuff.wf(w, twoIsBetter(b));
                continue;
            }

            String s1 = RandomStuff.f("%s/%d", s, i);

            pngSingle(b, s1);
            i++;
        }

        w.flush();
        w.close();
    }

    private TreeSet<String> readHigh() throws IOException {
        BufferedReader b = new BufferedReader(new FileReader(path_highlith));
        TreeSet<String> highligth = new TreeSet<String>();
        String line;
        while ((line = b.readLine()) != null) {
            highligth.add(line.trim());
        }
        return highligth;

    }

    private String twoIsBetter(BayesianNetwork b) {

        if (b.n_var == 1)
            return RandomStuff.f("%s \n", b.name( 0));

        if (b.l_parent_var.length > 0)
            return RandomStuff.f("%s -> %s \n", b.name( 1), b.name( 0));

        else return RandomStuff.f("%s -> %s \n", b.name( 0), b.name( 1));
    }

    private void pngSingle(BayesianNetwork bn, String s) throws IOException {
        printBn(bn, s);
        String h = RandomStuff.f("dot -Tpng %s.dot -o %s.png", s, s);
        exec(h);
    }

    private void exec(String h) throws IOException {
        Process proc = Runtime.getRuntime().exec(h, new String[0]);
        int exitVal = RandomStuff.waitForProc(proc, max_time * 1000);
    }

    private void printBn(BayesianNetwork bn, String s) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter w = new PrintWriter(s + ".dot", "UTF-8");
        bn.toGraph(w, highligth);
        w.flush();
        w.close();
    }
}
