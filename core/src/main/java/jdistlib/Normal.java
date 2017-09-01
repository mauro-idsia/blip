/*
 *  Mathlib : A C Library of Special Functions
 *  Copyright (C) 1998   Ross Ihaka
 *  Copyright (C) 2000-9 The R Development Core Team
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
 */
package jdistlib;

import jdistlib.generic.GenericDistribution;
import jdistlib.math.MathFunctions;
import jdistlib.rng.RandomEngine;

import static java.lang.Double.*;
import static java.lang.Math.*;
import static jdistlib.math.Constants.*;
import static jdistlib.math.MathFunctions.isInfinite;
import static jdistlib.math.MathFunctions.*;

/**
 * Manually translated from R's Distlib by Roby Joehanes
 */
public class Normal extends GenericDistribution {
    public static double density(double x, double mu, double sigma, boolean give_log) {
        if (isNaN(x) || isNaN(mu) || isNaN(sigma))
            return x + mu + sigma;
        if (isInfinite(sigma))
            return (give_log ? NEGATIVE_INFINITY : 0.);
        if (isInfinite(x) && x == mu)
            return NaN;
        if (sigma <= 0) {
            if (sigma == 0)
                return x == mu ? POSITIVE_INFINITY : (give_log ? NEGATIVE_INFINITY : 0.);
            return NaN;
        }

        x = (x - mu) / sigma;
        if (MathFunctions.isInfinite(x)) return (give_log ? NEGATIVE_INFINITY : 0.);
        if (give_log)
            return -(M_LN_SQRT_2PI + 0.5 * x * x + log(sigma));
        if (x < 5) return M_1_SQRT_2PI * exp(-0.5 * x * x) / sigma;
        /* ELSE:

	     * x*x  may lose upto about two digits accuracy for "large" x
	     * Morten Welinder's proposal for PR#15620
	     * https://bugs.r-project.org/bugzilla/show_bug.cgi?id=15620

	     * -- 1 --  No hoop jumping when we underflow to zero anyway:

	     *  -x^2/2 <         log(2)*.Machine$double.min.exp  <==>
	     *     x   > sqrt(-2*log(2)*.Machine$double.min.exp) =IEEE= 37.64031
	     * but "thanks" to denormalized numbers, underflow happens a bit later,
	     *  effective.D.MIN.EXP <- with(.Machine, double.min.exp + double.ulp.digits)
	     * for IEEE, DBL_MIN_EXP is -1022 but "effective" is -1074
	     * ==> boundary = sqrt(-2*log(2)*(.Machine$double.min.exp + .Machine$double.ulp.digits))
	     *              =IEEE=  38.58601
	     * [on one x86_64 platform, effective boundary a bit lower: 38.56804]
	     */
        if (x > sqrt(-2 * M_LN2 * (DBL_MIN_EXP + 1 - DBL_MANT_DIG))) return 0.;

	    /* Now, to get full accuracy, split x into two parts,
         *  x = x1+x2, such that |x2| <= 2^-16.
	     * Assuming that we are using IEEE doubles, that means that
	     * x1*x1 is error free for x<1024 (but we have x < 38.6 anyway).
	     * If we do not have IEEE this is still an improvement over the naive formula.
	     */
        double x1 = //  R_forceint(x * 65536) / 65536 =
                ldexp(rint(ldexp(x, 16)), -16);
        double x2 = x - x1;
        return M_1_SQRT_2PI / sigma *
                (exp(-0.5 * x1 * x1) * exp((-0.5 * x2 - x1) * x2));
    }

    public static double cumulative_standard(double x) {
        return cumulative(x, 0, 1, true, false);
    }

    public static double cumulative(double x, double mu, double sigma) {
        return cumulative(x, mu, sigma, true, false);
    }

    public static double cumulative(double x, double mu, double sigma, boolean lower_tail, boolean log_p) {
        final double SIXTEN = 16; /* Cutoff allowing exact "*" and "/" */
        final double a[] = new double[]{
                2.2352520354606839287,
                161.02823106855587881,
                1067.6894854603709582,
                18154.981253343561249,
                0.065682337918207449113
        };
        final double b[] = new double[]{
                47.20258190468824187,
                976.09855173777669322,
                10260.932208618978205,
                45507.789335026729956
        };
        final double c[] = new double[]{
                0.39894151208813466764,
                8.8831497943883759412,
                93.506656132177855979,
                597.27027639480026226,
                2494.5375852903726711,
                6848.1904505362823326,
                11602.651437647350124,
                9842.7148383839780218,
                1.0765576773720192317e-8
        };
        final double d[] = new double[]{
                22.266688044328115691,
                235.38790178262499861,
                1519.377599407554805,
                6485.558298266760755,
                18615.571640885098091,
                34900.952721145977266,
                38912.003286093271411,
                19685.429676859990727
        };
        final double p[] = new double[]{
                0.21589853405795699,
                0.1274011611602473639,
                0.022235277870649807,
                0.001421619193227893466,
                2.9112874951168792e-5,
                0.02307344176494017303
        };
        final double q[] = new double[]{
                1.28426009614491121,
                0.468238212480865118,
                0.0659881378689285515,
                0.00378239633202758244,
                7.29751555083966205e-5
        };

        double xden, temp, xnum, result, ccum;
        double del, min, eps, xsq;
        double y;
        int i;

		/* Note: The structure of these checks has been */
		/* carefully thought through.  For example, if x == mu */
		/* and sigma == 0, we still get the correct answer. */

        if (isNaN(x) || isNaN(mu) || isNaN(sigma))
            return x + mu + sigma;
        if (sigma <= 0) {
            if (sigma < 0) return NaN;
            // return (x < mu) ? R_DT_0 : R_DT_1;
            return x < mu ? (lower_tail ? (log_p ? Double.NEGATIVE_INFINITY : 0.) : (log_p ? 0. : 1.)) : (lower_tail ? (log_p ? 0. : 1.) : (log_p ? Double.NEGATIVE_INFINITY : 0.));
        }

        result = (x - mu) / sigma;
        ccum = 0;
        // lower == lower_tail, upper == !lower_tail
        // Entering pnorm_both

        if (isInfinite(result)) // return (x < mu) ? R_DT_0 : R_DT_1;
            return x < mu ? (lower_tail ? (log_p ? Double.NEGATIVE_INFINITY : 0.) : (log_p ? 0. : 1.)) : (lower_tail ? (log_p ? 0. : 1.) : (log_p ? Double.NEGATIVE_INFINITY : 0.));
        x = result;

        eps = DBL_EPSILON * 0.5;
        min = DBL_MIN;
		/*!*     y = fabs(x); *!*/
        y = abs(x);
        if (y <= 0.67448975) { /* qnorm(3/4) = .6744.... -- earlier had 0.66291 */
            if (y > eps) {
                xsq = x * x;
                xnum = a[4] * xsq;
                xden = xsq;
                for (i = 0; i < 3; ++i) {
                    xnum = (xnum + a[i]) * xsq;
                    xden = (xden + b[i]) * xsq;
                }
            } else xnum = xden = 0.0;
            temp = x * (xnum + a[3]) / (xden + b[3]);
            if (lower_tail) {
                result = log_p ? log(0.5 + temp) : 0.5 + temp;
            } else {
                ccum = log_p ? log(0.5 - temp) : 0.5 - temp;
            }
        } else if (y <= M_SQRT_32) {
			/* Evaluate pnorm for 0.67448975 <= |z| <= sqrt(32) */
            xnum = c[8] * y;
            xden = y;
            for (i = 0; i < 7; ++i) {
                xnum = (xnum + c[i]) * y;
                xden = (xden + d[i]) * y;
            }
            temp = (xnum + c[7]) / (xden + d[7]);
            xsq = trunc(y * SIXTEN) / SIXTEN;
            del = (y - xsq) * (y + xsq);
            if (log_p) {
                result = (-xsq * xsq * 0.5) + (-del * 0.5) + log(temp);
                if ((lower_tail && x > 0.) || (!lower_tail && x <= 0.))
                    ccum = log1p(-exp(-xsq * xsq * 0.5) * exp(-del * 0.5) * temp);
            } else {
                result = exp(-xsq * xsq * 0.5) * exp(-del * 0.5) * temp;
                ccum = 1.0 - result;
            }
            if (x > 0.0) {
                temp = result;
                if (lower_tail)
                    result = ccum;
                ccum = temp;
            }
        } else if ((log_p && y < 1e170) || (lower_tail && -37.5193 < x && x < 8.2924) || (!lower_tail && -8.2924 < x && x < 37.5193)) {

            xsq = (1.0 / x); /* (1./x)*(1./x) might be better */
            xsq = xsq * xsq;
            xnum = p[5] * xsq;
            xden = xsq;
            for (i = 0; i < 4; ++i) {
                xnum = (xnum + p[i]) * xsq;
                xden = (xden + q[i]) * xsq;
            }
            temp = xsq * (xnum + p[4]) / (xden + q[4]);
            temp = (M_1_SQRT_2PI - temp) / y;
			/*!* 	xsq = trunc(x * SIXTEN) / SIXTEN; *!*/
            xsq = trunc(x * SIXTEN) / SIXTEN;
            del = (x - xsq) * (x + xsq);
            if (log_p) {
                result = (-xsq * xsq * 0.5) + (-del * 0.5) + log(temp);
                if ((lower_tail && x > 0.) || (!lower_tail && x <= 0.))
                    ccum = log1p(-exp(-xsq * xsq * 0.5) * exp(-del * 0.5) * temp);
            } else {
                result = exp(-xsq * xsq * 0.5) * exp(-del * 0.5) * temp;
                ccum = 1.0 - result;
            }
            if (x > 0.0) {
                temp = result;
                if (lower_tail)
                    result = ccum;
                ccum = temp;
            }
        } else {
            if (x > 0) {
                result = (log_p ? 0. : 1.);
                ccum = (log_p ? Double.NEGATIVE_INFINITY : 0.);
            } else {
                result = (log_p ? Double.NEGATIVE_INFINITY : 0.);
                ccum = (log_p ? 0. : 1.);
            }
        }

        if (log_p) {
            if (result > -min) result = -0.;
            if (ccum > -min) ccum = -0.;
        } else {
            if (result < min) result = 0.0;
            if (ccum < min) ccum = 0.0;
        }
        return lower_tail ? result : ccum;
    }

    public static double quantile(double p, double mu, double sigma, boolean lower_tail, boolean log_p) {
        double q, r, val;

		/*!* #ifdef IEEE_754 /*4!*/
        if (isNaN(p) || isNaN(mu) || isNaN(sigma))
            return p + mu + sigma;
		/*!* #endif /*4!*/
        // R_Q_P01_boundaries(p, ML_NEGINF, ML_POSINF);
        if (log_p) {
            if (p > 0) return NaN;
            if (p == 0) return lower_tail ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
            if (p == Double.NEGATIVE_INFINITY) return lower_tail ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        } else {
            if (p < 0 || p > 1) return NaN;
            if (p == 0) return lower_tail ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            if (p == 1) return lower_tail ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
        }

        if (sigma < 0)
            return NaN;
        if (sigma == 0)
            return mu;
        double p_ = log_p ? (lower_tail ? exp(p) : -expm1(p)) : (lower_tail ? (p) : (0.5 - (p) + 0.5));
        q = p_ - 0.5;

		/*!*     if (fabs(q) <= 0.42) { *!*/
        if (abs(q) <= 0.425) {
            r = .180625 - q * q;
            val =
                    q * (((((((r * 2509.0809287301226727 +
                            33430.575583588128105) * r + 67265.770927008700853) * r +
                            45921.953931549871457) * r + 13731.693765509461125) * r +
                            1971.5909503065514427) * r + 133.14166789178437745) * r +
                            3.387132872796366608)
                            / (((((((r * 5226.495278852854561 +
                            28729.085735721942674) * r + 39307.89580009271061) * r +
                            21213.794301586595867) * r + 5394.1960214247511077) * r +
                            687.1870074920579083) * r + 42.313330701600911252) * r + 1.);
        } else {
            if (q > 0)
                r = (log_p ? (lower_tail ? -expm1(p) : exp(p)) : (lower_tail ? (0.5 - (p) + 0.5) : (p)));
            else
                r = p_;/* = R_DT_Iv(p) ^=  p */
            r = sqrt(-((log_p &&
                    ((lower_tail && q <= 0) || (!lower_tail && q > 0))) ?
                    p : /* else */ log(r)));
            if (r <= 5.) { /* <==> Math.min(p,1-p) >= exp(-25) ~= 1.3888e-11 */
                r += -1.6;
                val = (((((((r * 7.7454501427834140764e-4 +
                        .0227238449892691845833) * r + .24178072517745061177) *
                        r + 1.27045825245236838258) * r +
                        3.64784832476320460504) * r + 5.7694972214606914055) *
                        r + 4.6303378461565452959) * r +
                        1.42343711074968357734)
                        / (((((((r *
                        1.05075007164441684324e-9 + 5.475938084995344946e-4) *
                        r + .0151986665636164571966) * r +
                        .14810397642748007459) * r + .68976733498510000455) *
                        r + 1.6763848301838038494) * r +
                        2.05319162663775882187) * r + 1.);
            } else { /* very close to  0 or 1 */
                r += -5.;
                val = (((((((r * 2.01033439929228813265e-7 +
                        2.71155556874348757815e-5) * r +
                        .0012426609473880784386) * r + .026532189526576123093) *
                        r + .29656057182850489123) * r +
                        1.7848265399172913358) * r + 5.4637849111641143699) *
                        r + 6.6579046435011037772)
                        / (((((((r *
                        2.04426310338993978564e-15 + 1.4215117583164458887e-7) *
                        r + 1.8463183175100546818e-5) * r +
                        7.868691311456132591e-4) * r + .0148753612908506148525)
                        * r + .13692988092273580531) * r +
                        .59983220655588793769) * r + 1.);
            }
            if (q < 0.0)
                val = -val;
			/* return (q >= 0.)? r : -r ;*/
        }
        return mu + sigma * val;
    }

    /**
     * Random normal by quantile inversion -- the default in R
     *
     * @param mu
     * @param sigma
     * @param random
     * @return random variate
     */
    private static double random(double mu, double sigma, RandomEngine random) {
        return mu + sigma * random_standard(random);
    }

    public static double random_standard(RandomEngine random) {
        double u1 = random.nextDouble();
        u1 = (int) (134217728 * u1) + random.nextDouble();
        u1 = quantile(u1 / 134217728, 0, 1, true, false);
        return u1;
    }

    public static double[] random(int n, double mu, double sigma, RandomEngine random) {
        double[] rand = new double[n];
        for (int i = 0; i < n; i++)
            rand[i] = random(mu, sigma, random);
        return rand;
    }

    public static double[] random_standard(int n, RandomEngine random) {
        double[] rand = new double[n];
        for (int i = 0; i < n; i++)
            rand[i] = random_standard(random);
        return rand;
    }

    public static double random_ahrens_dieter(double mu, double sigma, RandomEngine random) {
        final double a[] = new double[]
                {
                        0.0000000, 0.03917609, 0.07841241, 0.1177699,
                        0.1573107, 0.19709910, 0.23720210, 0.2776904,
                        0.3186394, 0.36012990, 0.40225010, 0.4450965,
                        0.4887764, 0.53340970, 0.57913220, 0.6260990,
                        0.6744898, 0.72451440, 0.77642180, 0.8305109,
                        0.8871466, 0.94678180, 1.00999000, 1.0775160,
                        1.1503490, 1.22985900, 1.31801100, 1.4177970,
                        1.5341210, 1.67594000, 1.86273200, 2.1538750
                };

        final double d[] = new double[]
                {
                        0.0000000, 0.0000000, 0.0000000, 0.0000000,
                        0.0000000, 0.2636843, 0.2425085, 0.2255674,
                        0.2116342, 0.1999243, 0.1899108, 0.1812252,
                        0.1736014, 0.1668419, 0.1607967, 0.1553497,
                        0.1504094, 0.1459026, 0.1417700, 0.1379632,
                        0.1344418, 0.1311722, 0.1281260, 0.1252791,
                        0.1226109, 0.1201036, 0.1177417, 0.1155119,
                        0.1134023, 0.1114027, 0.1095039
                };

        final double t[] = new double[]
                {
                        7.673828e-4, 0.002306870, 0.003860618, 0.005438454,
                        0.007050699, 0.008708396, 0.010423570, 0.012209530,
                        0.014081250, 0.016055790, 0.018152900, 0.020395730,
                        0.022811770, 0.025434070, 0.028302960, 0.031468220,
                        0.034992330, 0.038954830, 0.043458780, 0.048640350,
                        0.054683340, 0.061842220, 0.070479830, 0.081131950,
                        0.094624440, 0.112300100, 0.136498000, 0.171688600,
                        0.227624100, 0.330498000, 0.584703100
                };

        final double h[] = new double[]
                {
                        0.03920617, 0.03932705, 0.03950999, 0.03975703,
                        0.04007093, 0.04045533, 0.04091481, 0.04145507,
                        0.04208311, 0.04280748, 0.04363863, 0.04458932,
                        0.04567523, 0.04691571, 0.04833487, 0.04996298,
                        0.05183859, 0.05401138, 0.05654656, 0.05953130,
                        0.06308489, 0.06737503, 0.07264544, 0.07926471,
                        0.08781922, 0.09930398, 0.11555990, 0.14043440,
                        0.18361420, 0.27900160, 0.70104740
                };

        double s, u1, w, y, u2, aa, tt;
        int i;
        u1 = random.nextDouble();
        s = 0.0;
        if (u1 > 0.5)
            s = 1.0;
        u1 = u1 + u1 - s;
        u1 *= 32.0;
        i = (int) u1;
        if (i == 32)
            i = 31;
        if (i != 0) {
            u2 = u1 - i;
            aa = a[i - 1];
            while (u2 <= t[i - 1]) {
                u1 = random.nextDouble();
                w = u1 * (a[i] - aa);
                tt = (w * 0.5 + aa) * w;
                while (true) {
                    if (u2 > tt) {
                        y = aa + w;
                        return mu + sigma * ((s == 1.0) ? -y : y);
                    }
                    u1 = random.nextDouble();
                    if (u2 < u1)
                        break;
                    tt = u1;
                    u2 = random.nextDouble();
                }
                u2 = random.nextDouble();
            }
            w = (u2 - t[i - 1]) * h[i - 1];
        } else {
            i = 6;
            aa = a[31];
            while (true) {
                u1 = u1 + u1;
                if (u1 >= 1.0)
                    break;
                aa = aa + d[i - 1];
                i = i + 1;
            }
            u1 = u1 - 1.0;
            while (true) {
                w = u1 * d[i - 1];
                tt = (w * 0.5 + aa) * w;
                while (true) {
                    u2 = random.nextDouble();
                    if (u2 > tt) {
                        y = aa + w;
                        return mu + sigma * ((s == 1.0) ? -y : y);
                    }
                    u1 = random.nextDouble();
                    if (u2 < u1) break;
                    tt = u1;
                }
                u1 = random.nextDouble();
            }
        }

        y = aa + w;
        return (s == 1.0) ? -y : y;
    }

    private static final double A = 2.216035867166471;

    private static double g(double x) {
        final double C1 = 0.398942280401433, C2 = 0.180025191068563;
        return (C1 * exp(-x * x / 2.0) - C2 * (A - x));
    }

    public static double random_kinderman_ramage(RandomEngine random) {
        double u1, u2, u3, tt;
        //* corrected version from Josef Leydold
        u1 = random.nextDouble();
        if (u1 < 0.884070402298758) {
            u2 = random.nextDouble();
            return A * (1.131131635444180 * u1 + u2 - 1);
        }

        if (u1 >= 0.973310954173898) { /* tail: */
            for (; ; ) {
                u2 = random.nextDouble();
                u3 = random.nextDouble();
                tt = (A * A - 2 * log(u3));
                if (u2 * u2 < (A * A) / tt)
                    return (u1 < 0.986655477086949) ? sqrt(tt) : -sqrt(tt);
            }
        }

        if (u1 >= 0.958720824790463) { /* region3: */
            for (; ; ) {
                u2 = random.nextDouble();
                u3 = random.nextDouble();
                tt = A - 0.630834801921960 * Math.min(u2, u3);
                if (Math.max(u2, u3) <= 0.755591531667601)
                    return (u2 < u3) ? tt : -tt;
                if (0.034240503750111 * abs(u2 - u3) <= g(tt))
                    return (u2 < u3) ? tt : -tt;
            }
        }

        if (u1 >= 0.911312780288703) { /* region2: */
            for (; ; ) {
                u2 = random.nextDouble();
                u3 = random.nextDouble();
                tt = 0.479727404222441 + 1.105473661022070 * Math.min(u2, u3);
                if (Math.max(u2, u3) <= 0.872834976671790)
                    return (u2 < u3) ? tt : -tt;
                if (0.049264496373128 * abs(u2 - u3) <= g(tt))
                    return (u2 < u3) ? tt : -tt;
            }
        }

		/* ELSE	 region1: */
        for (; ; ) {
            u2 = random.nextDouble();
            u3 = random.nextDouble();
            tt = 0.479727404222441 - 0.595507138015940 * Math.min(u2, u3);
            if (tt < 0.) continue;
            if (Math.max(u2, u3) <= 0.805577924423817)
                return (u2 < u3) ? tt : -tt;
            if (0.053377549506886 * abs(u2 - u3) <= g(tt))
                return (u2 < u3) ? tt : -tt;
        }
    }

    public static double random_box_muller(RandomEngine random) {
        double
                theta = 2 * PI * random.nextDouble(),
                R = sqrt(-2 * log(random.nextDouble())) + 10 * DBL_MIN;
        return random.nextDouble() < 0.5 ? R * cos(theta) : R * sin(theta);
    }

    private final double mu;
    private final double sigma;

    /**
     * Constructor for standard normal (thread.e., mean = 0, sd = 1)
     */
    public Normal() {
        this(0, 1);
    }

    private Normal(double mu, double sigma) {
        this.mu = mu;
        this.sigma = sigma;
        if (sigma <= 0) throw new RuntimeException("Sigma must be positive");
    }

    @Override
    public double density(double x, boolean log) {
        return density(x, mu, sigma, log);
    }

    @Override
    public double cumulative(double p, boolean lower_tail, boolean log_p) {
        return cumulative(p, mu, sigma, lower_tail, log_p);
    }

    @Override
    public double quantile(double q, boolean lower_tail, boolean log_p) {
        return quantile(q, mu, sigma, lower_tail, log_p);
    }

    @Override
    public double random() {
        return random(mu, sigma, random);
    }
}
