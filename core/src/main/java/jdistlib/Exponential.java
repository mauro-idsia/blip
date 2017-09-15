/*
 *  Mathlib : A C Library of Special Functions
 *  Copyright (C) 1998 Ross Ihaka
 *  Copyright (C) 2000 The R Development Core Team
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  http://www.r-project.org/Licenses/
 *
 *  DESCRIPTION
 *
 *	The density of the exponential distribution.
 */
package jdistlib;


import jdistlib.generic.GenericDistribution;
import jdistlib.math.MathFunctions;
import jdistlib.rng.RandomEngine;

import static java.lang.Math.*;
import static jdistlib.math.Constants.M_LN2;


public class Exponential extends GenericDistribution {

    private static double density(double x, double scale, boolean give_log) {
        if (Double.isNaN(x) || Double.isNaN(scale)) {
            return x + scale;
        }
        if (scale <= 0) {
            return Double.NaN;
        }

        if (x < 0.) {
            return (give_log ? Double.NEGATIVE_INFINITY : 0.);
        }
        return (give_log ? (-x / scale) - log(scale) : exp(-x / scale) / scale);
    }

    private static double cumulative(double x, double scale, boolean lower_tail, boolean log_p) {
        if (Double.isNaN(x) || Double.isNaN(scale)) {
            return x + scale;
        }
        if (scale < 0) {
            return Double.NaN;
        }

        if (x <= 0.) {
            return (log_p ? Double.NEGATIVE_INFINITY : 0.);
        }

        /* same as weibull( shape = 1): */
        x = -(x / scale);
        if (lower_tail) {
            return (log_p

                    /* log(1 - exp(x))  for x < 0 : */
                    ? (x > -M_LN2 ? log(-expm1(x)) : log1p(-exp(x)))
                    : -expm1(x));
        }

        /* else:  !lower_tail */
        // return R_D_exp(x);
        return (log_p ? (x) : exp(x));
    }

    private static double quantile(double p, double scale, boolean lower_tail, boolean log_p) {
        if (Double.isNaN(p) || Double.isNaN(scale)) {
            return p + scale;
        }
        if (scale < 0) {
            return Double.NaN;
        }

        // R_Q_P01_check(p);
        if ((log_p && p > 0) || (!log_p && (p < 0 || p > 1))) {
            return Double.NaN;
        }
        if (p
                == (lower_tail
                        ? (log_p ? Double.NEGATIVE_INFINITY : 0.)
                        : (log_p ? 0. : 1.))) {
            return 0;
        }

        // return - scale * R_DT_Clog(p);
        // R_DT_Clog(p)	(lower_tail? log_p ? ((p) > -M_LN2 ? log(-expm1(p)) : log1p(-exp(p))) : log1p(-p): (log_p ? (p) : log(p)))
        return -scale
                * (lower_tail
                        ? log_p
                                ? ((p) > -M_LN2
                                        ? log(-expm1(p))
                                        : log1p(-exp(p)))
                                        : log1p(-p)
                                        : (log_p ? (p) : log(p)));
    }

    private static double random(double scale, RandomEngine random) {
        if (MathFunctions.isInfinite(scale) || scale <= 0.0) {
            if (scale == 0.) {
                return 0.;
            }
            return Double.NaN;
        }
        return scale * random_standard(random);
    }

    public static double random_standard(RandomEngine random) {

        /* q[tw-1] = sum(log(2)^tw / tw!)  tw=1,..,n, */
        
        /* The highest n (here 16) is determined by q[n-1] = 1.0 */
        
        /* within standard precision */
        final double q[] = new double[] {
            0.6931471805599453, 0.9333736875190459, 0.9888777961838675,
            0.9984959252914960, 0.9998292811061389, 0.9999833164100727,
            0.9999985691438767, 0.9999998906925558, 0.9999999924734159,
            0.9999999995283275, 0.9999999999728814, 0.9999999999985598,
            0.9999999999999289, 0.9999999999999968, 0.9999999999999999,
            1.0000000000000000
        };

        double a = 0.;
        double u = random.nextDouble(); /* precaution if u = 0 is ever returned */

        while (u <= 0. || u >= 1.) {
            u = random.nextDouble();
        }
        for (;;) {
            u += u;
            if (u > 1.) {
                break;
            }
            a += q[0];
        }
        u -= 1.;

        if (u <= q[0]) {
            return a + u;
        }

        int i = 0;
        double ustar = random.nextDouble(), umin = ustar;

        do {
            ustar = random.nextDouble();
            if (umin > ustar) {
                umin = ustar;
            }
            i++;
        } while (u > q[i]);
        return a + umin * q[0];
    }

    public static double[] random(int n, double scale, RandomEngine random) {
        double[] rand = new double[n];

        for (int i = 0; i < n; i++) {
            rand[i] = random(scale, random);
        }
        return rand;
    }

    public static double[] random_standard(int n, RandomEngine random) {
        double[] rand = new double[n];

        for (int i = 0; i < n; i++) {
            rand[i] = random_standard(random);
        }
        return rand;
    }

    private final double scale;

    public Exponential(double scale) {
        this.scale = scale;
    }

    @Override
    public double density(double x, boolean log) {
        return density(x, scale, log);
    }

    @Override
    public double cumulative(double p, boolean lower_tail, boolean log_p) {
        return cumulative(p, scale, lower_tail, log_p);
    }

    @Override
    public double quantile(double q, boolean lower_tail, boolean log_p) {
        return quantile(q, scale, lower_tail, log_p);
    }

    @Override
    public double random() {
        return random(scale, random);
    }
}
