package ch.idsia.blip.core.common.arcs;

import ch.idsia.blip.core.utils.data.ArrayUtils;

import java.io.PrintWriter;
import java.util.Formatter;

import static ch.idsia.blip.core.utils.data.ArrayUtils.cloneArray;

public class Und extends Arcs {

    public int[][] neigh;

    public Und(Integer n) {
        this.n = n;
        neigh = new int[n][];
        for (int i = 0; i < n; i++)
            neigh[i] =  new int[0];
    }

    public void mark(int i1, int i2) {
            neigh[i1] = ArrayUtils.expandArray(neigh[i1], i2);
            neigh[i2] = ArrayUtils.expandArray(neigh[i2], i1);
    }

    @Override
    protected int getSize() {
        return n;
    }

    @Override
    protected int index(int v1, int v2) {
        return 0;
    }

    @Override
    public int[] r_index(int i) {
        return new int[0];
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        Formatter fm = new Formatter(s);

        fm.format("graph G {\n");
        for (int v1 = 0; v1 < n; v1++) {
            fm.format("%s \n", name(v1));
            for (int v2: neigh[v1])
            if (v2 > v1)
                    fm.format("%s -- %s \n", name(v1), name(v2));
        }

        s.append("}");
        return s.toString();
    }

    public Und clone() {
        Und nw = new Und(n);

        nw.neigh = new int[n][];
        for (int i = 0; i < n; i++) {
            nw.neigh[i] = new int[neigh[i].length];
            ArrayUtils.cloneArray(neigh[i], nw.neigh[i]);
        }

        return nw;
    }

    public void write(String s) {
        try {
            PrintWriter w = new PrintWriter(s + ".dot", "UTF-8");
            w.print(this);
            w.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}