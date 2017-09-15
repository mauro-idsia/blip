/*
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 3 of the License.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  http://www.r-project.org/Licenses/
 */
package jdistlib.generic;


import jdistlib.rng.MersenneTwister;
import jdistlib.rng.RandomEngine;


/**
 * An interface for a generic distribution. All parameters have to be encoded (either as fields or otherwise).
 * Treat this interface as an adapter to the other distributions.
 *
 * @author Roby Joehanes
 */
public abstract class GenericDistribution {
    protected RandomEngine random = new MersenneTwister();

    protected abstract double density(double x, boolean log);

    protected abstract double cumulative(double p, boolean lower_tail, boolean log_p);

    protected abstract double quantile(double q, boolean lower_tail, boolean log_p);

    protected abstract double random();

    private double[] density(double[] x, boolean log) {
        int n = x.length;
        double[] v = new double[n];

        for (int i = 0; i < n; i++) {
            v[i] = density(x[i], log);
        }
        return v;
    }

    /**
     * Assume non-log
     *
     * @param x
     * @return density
     */
    public double[] density(double[] x) {
        return density(x, false);
    }

    /**
     * Assume lower tail and non-log
     *
     * @param p
     * @return cdf
     */
    public double cumulative(double p) {
        return cumulative(p, true, false);
    }

    private double[] cumulative(double[] p, boolean lower_tail, boolean log_p) {
        int n = p.length;
        double[] v = new double[n];

        for (int i = 0; i < n; i++) {
            v[i] = cumulative(p[i], lower_tail, log_p);
        }
        return v;
    }

    /**
     * Assume lower tail and non-log
     *
     * @param p
     * @return cdf
     */
    public double[] cumulative(double[] p) {
        return cumulative(p, true, false);
    }

    private double[] quantile(double[] q, boolean lower_tail, boolean log_p) {
        int n = q.length;
        double[] v = new double[n];

        for (int i = 0; i < n; i++) {
            v[i] = quantile(q[i], lower_tail, log_p);
        }
        return v;
    }

    /**
     * Assume lower tail and non-log
     *
     * @param q
     * @return quantile
     */
    public double[] quantile(double[] q) {
        return quantile(q, true, false);
    }

    /**
     * Assume lower tail and non-log
     *
     * @param q
     * @return quantile
     */
    public double quantile(double q) {
        return quantile(q, true, false);
    }

    public double[] random(int n) {
        double[] rand = new double[n];

        for (int i = 0; i < n; i++) {
            rand[i] = random();
        }
        return rand;
    }

    /**
     * Hazard function of a distribution. Defined as: pdf / (1-cdf)
     *
     * @param t
     * @param give_log
     * @return hazard value
     */
    private double hazard(double t, boolean give_log) {
        double pdf = density(t, true);
        double cdf = cumulative(t, false, true);

        return give_log ? pdf - cdf : Math.exp(pdf - cdf);
    }

    public double[] hazard(double[] t, boolean give_log) {
        int n = t.length;
        double[] v = new double[n];

        for (int i = 0; i < n; i++) {
            v[i] = hazard(t[i], give_log);
        }
        return v;
    }

    /**
     * Cumulative hazard function, which is basically -ln(1-CDF).
     *
     * @param p
     * @return survival function
     */
    public double cumulative_hazard(double p) {
        return -cumulative(p, false, true);
    }

    public double[] cumulative_hazard(double[] p) {
        int n = p.length;
        double[] v = new double[n];

        for (int i = 0; i < n; i++) {
            v[i] = cumulative(p[i], false, true);
        }
        return v;
    }

    /**
     * Survival function, which is basically 1-CDF.
     *
     * @param p
     * @return survival function
     */
    public double survival(double p, boolean log_p) {
        return cumulative(p, false, log_p);
    }

    public double[] survival(double[] p, boolean log_p) {
        int n = p.length;
        double[] v = new double[n];

        for (int i = 0; i < n; i++) {
            v[i] = cumulative(p[i], false, log_p);
        }
        return v;
    }

    /**
     * Survival function, which is basically 1-CDF. Assume non-log.
     *
     * @param p
     * @return survival function
     */
    public double[] survival(double[] p) {
        return cumulative(p, false, false);
    }

    /**
     * Inverse survival function, which is basically quantile(1-p).
     *
     * @param p
     * @param log_p true if the p-value is in log scale
     * @return Inverse survival function
     */
    public double inverse_survival(double p, boolean log_p) {
        return quantile(p, false, log_p);
    }

    public double[] inverse_survival(double[] p, boolean log_p) {
        int n = p.length;
        double[] v = new double[n];

        for (int i = 0; i < n; i++) {
            v[i] = quantile(p[i], false, log_p);
        }
        return v;
    }

    public void setRandomEngine(RandomEngine r) {
        random = r;
    }

    public RandomEngine getRandomEngine() {
        return random;
    }

    /**
     * Old RNG API
     *
     * @param r random number generator
     * @return Random number for the distribution
     * @deprecated
     */
    public double random(RandomEngine r) {
        RandomEngine temp = random;

        random = r;
        double v = random();

        random = temp;
        return v;
    }
}
