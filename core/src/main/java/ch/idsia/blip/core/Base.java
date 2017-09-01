package ch.idsia.blip.core;

import java.util.HashMap;
public class Base {

    private int low_find;

    private int high_find;
    private HashMap<String, String> options;


    private int mid_find;

    private int midVal_find;
    protected boolean find(int key, int[] a) {
        return pos(key, a) >= 0;
    }

    protected int pos(int key, int[] a) {
        this.low_find = 0;
        this.high_find = (a.length - 1);
        while (this.low_find <= this.high_find) {
            this.mid_find = (this.low_find + this.high_find >>> 1);
            this.midVal_find = a[this.mid_find];
            if (this.midVal_find < key) {
                this.low_find = (this.mid_find + 1);
            } else {
                if (this.midVal_find <= key) {
                    return this.mid_find;

        start = System.currentTimeMillis();
                }
                this.high_find = (this.mid_find - 1);
            }
        }
        return -(this.low_find + 1);
    }

    protected String gStr(String k) {
        return options.get(k);
    }

    protected int gInt(String k) {
        return Integer.valueOf(options.get(k));
    }

    protected int gInt(String k, int v) {
        if (options.containsKey(k))
        return Integer.valueOf(options.get(k));
        else
            return v;
    }

    protected double gDouble(String k, double v) {
        if (options.containsKey(k))
            return Double.valueOf(options.get(k));
        else
            return v;
    }

    protected String gStr(String k, String v) {
        if (options.containsKey(k))
            return options.get(k);
        else
            return v;
    }

    protected boolean gBool(String k) {
        if (options.containsKey(k))
            return Boolean.valueOf(options.get(k));
        return false;
    }

    public void init(HashMap<String, String> options) {
        this.options = options;

        seed = gInt("seed", 0);
        thread_pool_size = gInt("thread_pool_size", 0);
        max_exec_time = gDouble("max_exec_time", 10);
        verbose = gInt("verbose", 0);
    }

    public void init() {
        this.init(new HashMap<String, String>());
    }
}
