package ch.idsia.blip.core;


import java.io.IOException;
import java.io.Writer;
import java.util.Random;


public class Base {

    protected long start;

    // Maximum execution time
    public double max_exec_time = 10;

    // Maximum number of threads to use
    public Integer thread_pool_size = 0;

    // Lock for concurrent searchers
    protected final Object lock = new Object();

    public int verbose;

    public Writer logWr;

    public long seed;

    public Random rand;

    public void logf(String format, Object... args) {
        log(String.format(format, args));
    }

    public void safeLogf(String format, Object... args) {
        synchronized (lock) {
            logf(format ,args);
        }
    }

    public void safeLogf(int i, String format, Object... args) {
        synchronized (lock) {
            logf(i, format ,args);
        }
    }

    public void logf(int i, String format, Object... args) {
        if (verbose > i)
            log(String.format(format, args));
    }

    public void log(int i, String s) {
        if (verbose > i)
            log(s);
    }

    public void log(String s) {
        try {
            if (logWr != null) {
                logWr.write(s);
                logWr.flush();
            }
            else
                System.out.print(s);
            System.out.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void init() {
        if (seed == 0)
            seed = System.currentTimeMillis();

         rand = new Random(seed);
    }

    public int randInt(int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }

    public double randProb() {
        return randInt(0, 1) * rand.nextDouble();
    }

    public double randDouble() {
        return rand.nextDouble();
    }
}
