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

import static java.lang.Math.*;
import static jdistlib.math.Constants.*;
import static jdistlib.math.MathFunctions.*;


public class Gamma extends GenericDistribution {
    private static final double M_cutoff = M_LN2 * DBL_MAX_EXP / DBL_EPSILON;

    public static double density(double x, double shape, double scale, boolean give_log) {
        double pr;

        if (Double.isNaN(x) || Double.isNaN(shape) || Double.isNaN(scale)) {
            return x + shape + scale;
        }
        if (shape < 0 || scale <= 0) {
            return Double.NaN;
        }
        if (x < 0) {
            return (give_log ? Double.NEGATIVE_INFINITY : 0.);
        }
        if (shape == 0) { /* point mass at 0 */
            return (x == 0)
                    ? Double.POSITIVE_INFINITY
                    : (give_log ? Double.NEGATIVE_INFINITY : 0.);
        }
        if (x == 0) {
            if (shape < 1) {
                return Double.POSITIVE_INFINITY;
            }
            if (shape > 1) {
                return (give_log ? Double.NEGATIVE_INFINITY : 0.);
            }
            return give_log ? -log(scale) : 1 / scale;
        }
        if (shape < 1) {
            pr = Poisson.density_raw(shape, x / scale, give_log);
            return give_log ? pr + log(shape / x) : pr * shape / x;
        }

        /* else  shape >= 1 */
        pr = Poisson.density_raw(shape - 1, x / scale, give_log);
        return give_log ? pr - log(scale) : pr / scale;
    }

    private static double dpois_wrap(double x_plus_1, double lambda, boolean give_log) {
        if (MathFunctions.isInfinite(lambda)) {
            return (give_log ? Double.NEGATIVE_INFINITY : 0.);
        }
        if (x_plus_1 > 1) {
            return Poisson.density_raw(x_plus_1 - 1, lambda, give_log);
        }
        if (lambda > abs(x_plus_1 - 1) * M_cutoff) {
            lambda = -lambda - lgammafn(x_plus_1);
            return give_log ? (lambda) : exp(lambda);
        }
        double d = Poisson.density_raw(x_plus_1, lambda, give_log);

        return give_log ? d + log(x_plus_1 / lambda) : d * (x_plus_1 / lambda);
    }

    /*
     * Abramowitz and Stegun 6.5.29 [right]
     */
    private static double pgamma_smallx(double x, double alph, boolean lower_tail, boolean log_p) {
        double sum = 0, c = alph, n = 0, term;

        /*
         * Relative to 6.5.29 all terms have been multiplied by alph
         * and the first, thus being 1, is omitted.
         */

        do {
            n++;
            c *= -x / n;
            term = c / (alph + n);
            sum += term;
        } while (abs(term) > DBL_EPSILON * abs(sum));

        if (lower_tail) {
            double f1 = log_p ? log1p(sum) : 1 + sum;
            double f2;

            if (alph > 1) {
                f2 = Poisson.density_raw(alph, x, log_p);
                f2 = log_p ? f2 + x : f2 * exp(x);
            } else if (log_p) {
                f2 = alph * log(x) - lgamma1p(alph);
            } else {
                f2 = pow(x, alph) / exp(lgamma1p(alph));
            }
            return log_p ? f1 + f2 : f1 * f2;
        } else {
            double lf2 = alph * log(x) - lgamma1p(alph);

            if (log_p) {
                lf2 = log1p(sum) + lf2;
                return lf2 > -M_LN2 ? log(-expm1(lf2)) : log1p(-exp(lf2));
            } else {
                double f1m1 = sum;
                double f2m1 = expm1(lf2);

                return -(f1m1 + f2m1 + f1m1 * f2m1);
            }
        }
    } /* pgamma_smallx() */

    /*
     * Compute the following ratio with higher accuracy that would be had
     * from doing it directly.
     *
     *		 dnorm (x, 0, 1, FALSE)
     *	   ----------------------------------
     *	   pnorm (x, 0, 1, lower_tail, FALSE)
     *
     * Abramowitz & Stegun 26.2.12
     */
    private static double dpnorm(double x, boolean lower_tail, double lp) {

        /*
         * So as not to repeat a pnorm call, we expect
         *
         *	 lp == pnorm (x, 0, 1, lower_tail, TRUE)
         *
         * but use it only in the non-critical case where either x is small
         * or p==exp(lp) is close to 1.
         */

        if (x < 0) {
            x = -x;
            lower_tail = !lower_tail;
        }

        if (x > 10 && !lower_tail) {
            double term = 1 / x;
            double sum = term;
            double x2 = x * x;
            double i = 1;

            do {
                term *= -i / x2;
                sum += term;
                i += 2;
            } while (abs(term) > DBL_EPSILON * sum);

            return 1 / sum;
        }
        return Normal.density(x, 0, 1, false) / exp(lp);
    }

    /*
     * Asymptotic expansion to calculate the probability that Poisson variate
     * has value <= x.
     * Various assertions about this are made (without proof) at
     * http://members.aol.com/iandjmsmith/PoissonApprox.htm
     */
    private static double ppois_asymp(double x, double lambda, boolean lower_tail, boolean log_p) {
        final double coefs_a[] = {
            -1e99, /* placeholder used for 1-indexing */ 2 / 3., -4 / 135.,
            8 / 2835., 16 / 8505., -8992 / 12629925., -334144 / 492567075.,
            698752 / 1477701225.
        };

        final double coefs_b[] = {
            -1e99, /* placeholder */ 1 / 12., 1 / 288., -139 / 51840.,
            -571 / 2488320., 163879 / 209018880., 5246819 / 75246796800.,
            -534703531 / 902961561600.
        };

        double elfb, elfb_term;
        double res12, res1_term, res1_ig, res2_term, res2_ig;
        double dfm, pt_, s2pt, f, np;
        int i;

        dfm = lambda - x;

        /* If lambda is large, the distribution is highly concentrated
         about lambda.  So representation error in x or lambda can lead
         to arbitrarily large values of pt_ and hence divergence of the
         coefficients of this approximation.
         */
        pt_ = -log1pmx(dfm / x);
        s2pt = sqrt(2 * x * pt_);
        if (dfm < 0) {
            s2pt = -s2pt;
        }

        res12 = 0;
        res1_ig = res1_term = sqrt(x);
        res2_ig = res2_term = s2pt;
        for (i = 1; i < 8; i++) {
            res12 += res1_ig * coefs_a[i];
            res12 += res2_ig * coefs_b[i];
            res1_term *= pt_ / i;
            res2_term *= 2 * pt_ / (2 * i + 1);
            res1_ig = res1_ig / x + res1_term;
            res2_ig = res2_ig / x + res2_term;
        }

        elfb = x;
        elfb_term = 1;
        for (i = 1; i < 8; i++) {
            elfb += elfb_term * coefs_b[i];
            elfb_term /= x;
        }
        if (!lower_tail) {
            elfb = -elfb;
        }

        f = res12 / elfb;

        np = Normal.cumulative(s2pt, 0.0, 1.0, !lower_tail, log_p);

        if (log_p) {
            double n_d_over_p = dpnorm(s2pt, !lower_tail, np);

            return np + log1p(f * n_d_over_p);
        } else {
            double nd = Normal.density(s2pt, 0., 1., log_p);

            return np + f * nd;
        }
    } /* ppois_asymp() */

    private static double pgamma_raw(double x, double alph, boolean lower_tail, boolean log_p) {

        /* Here, assume that  (x,alph) are not NA  &  alph > 0 . */
        double res;

        // R_P_bounds_01(x, 0., ML_POSINF);
        if (x <= 0) {
            return lower_tail
                    ? (log_p ? Double.NEGATIVE_INFINITY : 0)
                    : (log_p ? 0 : 1);
        }
        if (x >= Double.POSITIVE_INFINITY) {
            return lower_tail
                    ? (log_p ? 0 : 1)
                    : (log_p ? Double.NEGATIVE_INFINITY : 0);
        }

        if (x < 1) {
            res = pgamma_smallx(x, alph, lower_tail, log_p);
        } else if (x <= alph - 1 && x < 0.8 * (alph + 50)) {

            /* incl. large alph compared to x */
            double sum = MathFunctions.pd_upper_series(x, alph, log_p); /* = x/alph + o(x/alph) */
            double d = dpois_wrap(alph, x, log_p);

            if (!lower_tail) {
                if (log_p) {
                    res = d + sum;
                    res = res > -M_LN2 ? log(-expm1(res)) : log1p(-exp(res));
                } else {
                    res = 1 - d * sum;
                }
            } else {
                res = log_p ? sum + d : sum * d;
            }
        } else if (alph - 1 < x && alph < 0.8 * (x + 50)) {

            /* incl. large x compared to alph */
            double sum;
            double d = dpois_wrap(alph, x, log_p);

            if (alph < 1) {
                if (x * DBL_EPSILON > 1 - alph) {
                    sum = (log_p ? 0. : 1.);
                } else {
                    double f = MathFunctions.pd_lower_cf(alph, x - (alph - 1))
                            * x / alph;

                    /* = [alph/(x - alph+1) + o(alph/(x-alph+1))] * x/alph = 1 + o(1) */
                    sum = log_p
                            ? log(f)
                            : f;
                }
            } else {
                sum = MathFunctions.pd_lower_series(x, alph - 1); /* = (alph-1)/x + o((alph-1)/x) */
                sum = log_p
                        ? log1p(sum)
                        : 1 + sum;
            }
            if (!lower_tail) {
                res = log_p ? sum + d : sum * d;
            } else {
                if (log_p) {
                    res = d + sum;
                    res = res > -M_LN2 ? log(-expm1(res)) : log1p(-exp(res));
                } else {
                    res = 1 - d * sum;
                }
            }
        } else { /* x >= 1 and x fairly near alph. */
            res = ppois_asymp(alph - 1, x, !lower_tail, log_p);
        }

        /*
         * We lose a fair amount of accuracy to underflow in the cases
         * where the final result is very close to DBL_MIN.	 In those
         * cases, simply redo via log space.
         */
        if (!log_p && res < DBL_MIN / DBL_EPSILON) {

            /* with(.Machine, double.xmin / double.eps) #|-> 1.002084e-292 */
            return exp(pgamma_raw(x, alph, lower_tail, true));
        } else {
            return res;
        }
    }

    public static double cumulative(double x, double alph, double scale, boolean lower_tail, boolean log_p) {
        if (Double.isNaN(x) || Double.isNaN(alph) || Double.isNaN(scale)) {
            return x + alph + scale;
        }
        if (alph < 0. || scale <= 0.) {
            return Double.NaN;
        }
        x /= scale;
        if (Double.isNaN(x)) { /* eg. original x = scale = +Inf */
            return x;
        }
        if (alph == 0.) { /* limit case; useful e.g. in pnchisq() */
            return (x <= 0)
                    ? (lower_tail
                            ? (log_p ? Double.NEGATIVE_INFINITY : 0.)
                            : (log_p ? 0. : 1.))
                            : (lower_tail
                                    ? (log_p ? 0. : 1.)
                                    : (log_p ? Double.NEGATIVE_INFINITY : 0.));
        } /* <= assert  pgamma(0,0) ==> 0 */
        return pgamma_raw(x, alph, lower_tail, log_p);
    }

    private static double qchisq_appr(double p, double nu, double g/* = log Gamma(nu/2) */,
            boolean lower_tail, boolean log_p, double tol/* EPS1 */) {
        final double C7 = 4.67, C8 = 6.66, C9 = 6.73, C10 = 13.32;
        double alpha, a, c, ch, p1;
        double p2, q, t, x;

        /* test arguments and initialise */

        if (Double.isNaN(p) || Double.isNaN(nu)) {
            return p + nu;
        }
        // R_Q_P01_check(p);
        if ((log_p && p > 0) || (!log_p && (p < 0 || p > 1))) {
            return Double.NaN;
        }

        if (nu <= 0) {
            return Double.NaN;
        }

        alpha = 0.5 * nu; /* = [pq]gamma() shape */
        c = alpha - 1;

        // R_DT_log(p)	(lower_tail? (log_p	? (p) : log(p)) : (log_p ? ((p) > -M_LN2 ? log(-expm1(p)) : log1p(-exp(p)))) : log1p(-p)))
        p1 = (lower_tail
                ? (log_p ? (p) : log(p))
                : (log_p
                        ? ((p) > -M_LN2 ? log(-expm1(p)) : log1p(-exp(p)))
                        : log1p(-p)));
        if (nu < -1.24 * p1) { /* for small chi-squared */

            /* log(alpha) + g = log(alpha) + log(gamma(alpha)) =
             *        = log(alpha*gamma(alpha)) = lgamma(alpha+1) suffers from
             *  catastrophic cancellation when alpha << 1
             */
            double lgam1pa = (alpha < 0.5) ? lgamma1p(alpha) : (log(alpha) + g);

            ch = exp((lgam1pa + p1) / alpha + M_LN2);

        } else if (nu > 0.32) { /* using Wilson and Hilferty PR */

            x = Normal.quantile(p, 0, 1, lower_tail, log_p);
            p1 = 2. / (9 * nu);
            ch = nu * pow(x * sqrt(p1) + 1 - p1, 3);

            /* approximation for p tending to 1: */
            if (ch > 2.2 * nu + 6) {
                // R_DT_Clog(p) == (lower_tail? (log_p ? (p) > -M_LN2 ? log(-expm1(p)) : log1p(-exp(p)) : log1p(-p)): (log_p ? (p) : log(p)))
                // ch = -2*(R_DT_Clog(p) - c*log(0.5*ch) + g);
                ch = -2
                        * ((lower_tail
                                ? (log_p
                                        ? (p) > -M_LN2
                                                ? log(-expm1(p))
                                                : log1p(-exp(p))
                                                : log1p(-p))
                                                : (log_p ? (p) : log(p)))
                                                        - c * log(0.5 * ch)
                                                        + g);
            }

        } else { /* "small nu" : 1.24*(-log(p)) <= nu <= 0.32 */

            ch = 0.4;
            // a = R_DT_Clog(p) + g + c*M_LN2;
            a = (lower_tail
                    ? (log_p
                            ? (p) > -M_LN2 ? log(-expm1(p)) : log1p(-exp(p))
                            : log1p(-p))
                            : (log_p ? (p) : log(p)))
                                    + g
                                    + c * M_LN2;
            do {
                q = ch;
                p1 = 1. / (1 + ch * (C7 + ch));
                p2 = ch * (C9 + ch * (C8 + ch));
                t = -0.5 + (C7 + 2 * ch) * p1 - (C9 + ch * (C10 + 3 * ch)) / p2;
                ch -= (1 - exp(a + 0.5 * ch) * p2 * p1) / t;
            } while (abs(q - ch) > tol * abs(ch));
        }

        return ch;
    }

    public static double quantile(double p, double alpha, double scale, boolean lower_tail, boolean log_p) {
        final double EPS1 = 1e-2, EPS2 = 5e-7, EPS_N = 1e-15, /* LN_EPS = -36.043653389117156, */pMIN = 1e-100, pMAX = (1
                - 1e-14);
        final int MAXIT = 1000;

        final double i420 = 1. / 420.,
                i2520 = 1. / 2520.,
                i5040 = 1. / 5040;

        double p_, a, b, c, g, ch, ch0, p1;
        double p2, q, s1, s2, s3, s4, s5, s6, t, x;
        int i, max_it_Newton = 1;

        /* test arguments and initialise */

        if (Double.isNaN(p) || Double.isNaN(alpha) || Double.isNaN(scale)) {
            return p + alpha + scale;
        }

        // R_Q_P01_boundaries(p, 0., ML_POSINF);
        if (log_p) {
            if (p > 0) {
                return Double.NaN;
            }
            if (p == 0) { /* upper bound*/
                return lower_tail ? Double.POSITIVE_INFINITY : 0;
            }
            if (p == Double.NEGATIVE_INFINITY) {
                return lower_tail ? 0 : Double.POSITIVE_INFINITY;
            }
        } else { /* !log_p */
            if (p < 0 || p > 1) {
                return Double.NaN;
            }
            if (p == 0) {
                return lower_tail ? 0 : Double.POSITIVE_INFINITY;
            }
            if (p == 1) {
                return lower_tail ? Double.POSITIVE_INFINITY : 0;
            }
        }

        if (alpha < 0 || scale <= 0) {
            return Double.NaN;
        }

        if (alpha == 0) { /* all mass at 0 : */
            return 0.;
        }

        if (alpha < 1e-10) {

            /* Warning seems unnecessary now: */
            max_it_Newton = 7; /* may still be increased below */
        }

        // p_ = R_DT_qIv(p);/* lower_tail prob (in any case) */
        // R_DT_qIv(p)	(log_p ? (lower_tail ? exp(p) : - expm1(p)) : (lower_tail ? (p) : (0.5 - (p) + 0.5)))
        p_ = (log_p
                ? (lower_tail ? exp(p) : -expm1(p))
                : (lower_tail ? (p) : (0.5 - (p) + 0.5))); /* lower_tail prob (in any case) */

        g = lgammafn(
                alpha); /* log Gamma(v/2) */

        /* ----- Phase I : Starting Approximation */
        ch = qchisq_appr(p, /* nu= 'df' =  */2 * alpha, /* lgamma(nu/2)= */g,
                lower_tail, log_p, /* tol= */EPS1);
        if (MathFunctions.isInfinite(ch)) {

            /* forget about all iterations! */
            max_it_Newton = 0; // goto END;
        } else {
            if (ch < EPS2) { /* Corrected according to AS 91; MM, May 25, 1999 */
                max_it_Newton = 20;
                // goto END;/* and do Newton steps */
            } else {

                /* FIXME: This (cutoff to {0, +Inf}) is far from optimal
                 * -----  when log_p or !lower_tail, but NOT doing it can be even worse */
                if (p_ > pMAX || p_ < pMIN) {

                    /* did return ML_POSINF or 0.;	much better: */
                    max_it_Newton = 20;
                    // goto END;/* and do Newton steps */
                } else {

                    /* ----- Phase II: Iteration
                     *	Call pgamma() [AS 239]	and calculate seven term taylor series
                     */
                    c = alpha - 1;
                    s6 = (120 + c * (346 + 127 * c)) * i5040; /* used below, is "const" */

                    ch0 = ch; /* save initial approx. */
                    for (i = 1; i <= MAXIT; i++) {
                        q = ch;
                        p1 = 0.5 * ch;
                        p2 = p_
                                - pgamma_raw(p1, alpha, /* lower_tail*/true, /* log_p*/
                                false);
                        if (MathFunctions.isInfinite(p2) || ch <= 0) {
                            ch = ch0;
                            max_it_Newton = 27;
                            break;
                        }/* was  return ML_NAN;*/

                        t = p2 * exp(alpha * M_LN2 + g + p1 - c * log(ch));
                        b = t / ch;
                        a = 0.5 * t - b * c;
                        s1 = (210
                                + a
                                        * (140
                                                + a
                                                        * (105
                                                                + a
                                                                        * (84
                                                                                + a
                                                                                        * (70
                                                                                                + 60
                                                                                                        * a)))))
                                                                                                                * i420;
                        s2 = (420
                                + a * (735 + a * (966 + a * (1141 + 1278 * a))))
                                        * i2520;
                        s3 = (210 + a * (462 + a * (707 + 932 * a))) * i2520;
                        s4 = (252 + a * (672 + 1182 * a)
                                + c * (294 + a * (889 + 1740 * a)))
                                * i5040;
                        s5 = (84 + 2264 * a + c * (1175 + 606 * a)) * i2520;

                        ch += t
                                * (1 + 0.5 * t * s1
                                - b * c
                                * (s1
                                        - b
                                                * (s2
                                                        - b
                                                                * (s3
                                                                        - b
                                                                                * (s4
                                                                                        - b
                                                                                                * (s5
                                                                                                        - b
                                                                                                                * s6))))));
                        if (abs(q - ch) < EPS2 * ch) {
                            break;
                        }
                        if (abs(q - ch) > 0.1 * ch) { /* diverging? -- also forces ch > 0 */
                            if (ch < q) {
                                ch = 0.9 * q;
                            } else {
                                ch = 1.1 * q;
                            }
                        }
                    }
                }
            }
        }

        /* no convergence in MAXIT iterations -- but we add Newton now... */
        
        /* was
         *    ML_ERROR(ME_PRECISION, "qgamma");
         * does nothing in R !*/

        // END:
        /*
         * PR# 2214 :	 From: Morten Welinder <terra@diku.dk>, Fri, 25 Oct 2002 16:50
         --------	 To: R-bugs@biostat.ku.dk     Subject: qgamma precision

         * With a final Newton step, double accuracy, e.g. for (p= 7e-4; nu= 0.9)
         *
         * Improved (MM): - only if rel.Err > EPS_N (= 1e-15);
         *		    - also for lower_tail = FALSE	 or log_p = TRUE
         * 		    - optionally *iterate* Newton
         */
        x = 0.5 * scale * ch;
        if (max_it_Newton != 0) {

            /* always use log scale */
            if (!log_p) {
                p = log(p);
                log_p = true;
            }
            if (x == 0) {
                final double _1_p = 1. + 1e-7;
                final double _1_m = 1. - 1e-7;

                x = DBL_MIN;
                p_ = cumulative(x, alpha, scale, lower_tail, log_p);
                if ((lower_tail && p_ > p * _1_p)
                        || (!lower_tail && p_ < p * _1_m)) {
                    return (0.);
                }

                /* else:  continue, using x = DBL_MIN instead of  0  */
            } else {
                p_ = cumulative(x, alpha, scale, lower_tail, log_p);
            }
            if (p_ == Double.NEGATIVE_INFINITY) {
                return 0;
            } /* PR#14710 */
            for (i = 1; i <= max_it_Newton; i++) {
                p1 = p_ - p;
                if (abs(p1) < abs(EPS_N * p)) {
                    break;
                }

                /* else */
                if ((g = density(x, alpha, scale, log_p)) == 0) {
                    break;
                }

                /* else :
                 * delta x = f(x)/f'(x);
                 * if(log_p) f(x) := log P(x) - p; f'(x) = d/dx log P(x) = P' / P
                 * ==> f(x)/f'(x) = f*P / P' = f*exp(p_) / P' (since p_ = log P(x))
                 */
                t = log_p ? p1 * exp(p_ - g) : p1 / g; /* = "delta x" */
                t = lower_tail ? x - t : x + t;
                p_ = cumulative(t, alpha, scale, lower_tail, log_p);
                if (abs(p_ - p) > abs(p1) || (i > 1 && abs(p_ - p) == abs(p1))/* <- against flip-flop */) {

                    /* no improvement */
                    break;
                } /* else : */
                x = t;
            }
        }

        return x;
    }

    public final double random(double a, double scale) {
        return random(a, scale, random);
    }

    public static double random(double a, double scale, RandomEngine random) {

        /* Constants : */
        final double sqrt32 = 5.656854;
        final double exp_m1 = 0.36787944117144232159; /* exp(-1) = 1/e */

        /* Coefficients q[tw] - for q0 = sum(q[tw]*a^(-tw))
         * Coefficients a[tw] - for q = q0+(t*t/2)*sum(a[tw]*v^tw)
         * Coefficients e[tw] - for exp(q)-1 = sum(e[tw]*q^tw)
         */
        final double q1 = 0.04166669;
        final double q2 = 0.02083148;
        final double q3 = 0.00801191;
        final double q4 = 0.00144121;
        final double q5 = -7.388e-5;
        final double q6 = 2.4511e-4;
        final double q7 = 2.424e-4;

        final double a1 = 0.3333333;
        final double a2 = -0.250003;
        final double a3 = 0.2000062;
        final double a4 = -0.1662921;
        final double a5 = 0.1423657;
        final double a6 = -0.1367177;
        final double a7 = 0.1233795;

        // RJ's modification: The paltry saving isn't worth it since we much prefer threading
        double e, p, q, r, t, u, v, w, x, ret_val;
        // static double aa = 0.;
        // static double aaa = 0.;
        double s, s2, d; /* no. 1 (step 1) */
        double q0, b, si, c; /* no. 2 (step 4) */

        if (MathFunctions.isInfinite(a) || MathFunctions.isInfinite(scale)
                || a < 0.0 || scale <= 0.0) {
            if (scale == 0.) {
                return 0.;
            }
            return Double.NaN;
        }

        if (a < 1.) { /* GS algorithm for parameters a < 1 */
            if (a == 0) {
                return 0.;
            }
            e = 1.0 + exp_m1 * a;
            for (;;) {
                p = e * random.nextDouble();
                if (p >= 1.0) {
                    x = -log((e - p) / a);
                    if (Exponential.random_standard(random)
                            >= (1.0 - a) * log(x)) {
                        break;
                    }
                } else {
                    x = exp(log(p) / a);
                    if (Exponential.random_standard(random) >= x) {
                        break;
                    }
                }
            }
            return scale * x;
        } /* --- a >= 1 : GD algorithm --- */ /* Step 1: Recalculations of s2, s, d if a has changed */ { // if (a != aa) {
            // aa = a;
            s2 = a
                    - 0.5;
            s = sqrt(s2);
            d = sqrt32 - s * 12.0;
        }

        /* Step 2: t = standard normal deviate,
         x = (s,1/2) -normal deviate. */

        /* immediate acceptance (thread) */
        t = Normal.random_standard(random);
        x = s + 0.5 * t;
        ret_val = x * x;
        if (t >= 0.0) {
            return scale * ret_val;
        }

        /* Step 3: u = 0,1 - uniform sample. squeeze acceptance (s) */
        u = random.nextDouble();
        if (d * u <= t * t * t) {
            return scale * ret_val;
        } /* Step 4: recalculations of q0, b, si, c if necessary */ { // if (a != aaa) {
            // aaa = a;
            r = 1.0 / a;
            q0 = ((((((q7 * r + q6) * r + q5) * r + q4) * r + q3) * r + q2) * r
                    + q1)
                            * r;

            /* Approximation depending on size of parameter a */
            
            /* The constants in the expressions for b, si and c */
            
            /* were established by numerical experiments */

            if (a <= 3.686) {
                b = 0.463 + s + 0.178 * s2;
                si = 1.235;
                c = 0.195 / s - 0.079 + 0.16 * s;
            } else if (a <= 13.022) {
                b = 1.654 + 0.0076 * s2;
                si = 1.68 / s + 0.275;
                c = 0.062 / s + 0.024;
            } else {
                b = 1.77;
                si = 0.75;
                c = 0.1515 / s;
            }
        }

        /* Step 5: no quotient test if x not positive */

        if (x > 0.0) {

            /* Step 6: calculation of v and quotient q */
            v = t / (s + s);
            if (abs(v) <= 0.25) {
                q = q0
                        + 0.5 * t * t
                        * ((((((a7 * v + a6) * v + a5) * v + a4) * v + a3) * v
                                + a2)
                                        * v
                                                + a1)
                                                * v;
            } else {
                q = q0 - s * t + 0.25 * t * t + (s2 + s2) * log(1.0 + v);
            }

            /* Step 7: quotient acceptance (q) */
            if (log(1.0 - u) <= q) {
                return scale * ret_val;
            }
        }

        for (;;) {

            /* Step 8: e = standard exponential deviate
             *	u =  0,1 -uniform deviate
             *	t = (b,si)-double exponential (laplace) sample */
            e = Exponential.random_standard(random);
            u = random.nextDouble();
            u = u + u - 1.0;
            if (u < 0.0) {
                t = b - si * e;
            } else {
                t = b + si * e;
            }

            /* Step	 9:  rejection if t < tau(1) = -0.71874483771719 */
            if (t >= -0.71874483771719) {

                /* Step 10:	 calculation of v and quotient q */
                v = t / (s + s);
                if (abs(v) <= 0.25) {
                    q = q0
                            + 0.5 * t * t
                            * ((((((a7 * v + a6) * v + a5) * v + a4) * v + a3)
                                    * v
                                            + a2)
                                                    * v
                                                            + a1)
                                                            * v;
                } else {
                    q = q0 - s * t + 0.25 * t * t + (s2 + s2) * log(1.0 + v);
                }

                /* Step 11:	 hat acceptance (h) */
                
                /* (if q not positive ex to step 8) */
                if (q > 0.0) {
                    w = expm1(q);

                    /* ^^^^^ original code had approximation with rel.err < 2e-7 */
                    
                    /* if t is rejected sample again at step 8 */
                    if (c * abs(u) <= w * exp(e - 0.5 * t * t)) {
                        break;
                    }
                }
            }
        } /* repeat .. until  `t' is accepted */
        x = s + 0.5 * t;
        return scale * x * x;
    }

    public static double[] random(int n, double a, double scale, RandomEngine random) {
        double[] rand = new double[n];

        for (int i = 0; i < n; i++) {
            rand[i] = random(a, scale, random);
        }
        return rand;
    }

    private final double shape;
    private final double scale;

    public Gamma() {
        this.shape = 1;
        this.scale = 1;
    }

    public Gamma(double shape, double scale) {
        this.shape = shape;
        this.scale = scale;
    }

    @Override
    public double density(double x, boolean log) {
        return density(x, shape, scale, log);
    }

    @Override
    public double cumulative(double p, boolean lower_tail, boolean log_p) {
        return cumulative(p, shape, scale, lower_tail, log_p);
    }

    @Override
    public double quantile(double q, boolean lower_tail, boolean log_p) {
        return quantile(q, shape, scale, lower_tail, log_p);
    }

    @Override
    public double random() {
        return random(shape, scale, random);
    }
}
