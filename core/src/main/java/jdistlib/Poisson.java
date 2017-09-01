/*
 *  AUTHOR
 *    Catherine Loader, catherine@research.bell-labs.com.
 *    October 23, 2000.
 *
 *  Merge in to R:
 *	Copyright (C) 2000, The R Core Development Team
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
import static java.lang.Math.round;
import static jdistlib.math.Constants.*;
import static jdistlib.math.MathFunctions.*;

public class Poisson extends GenericDistribution {
    public static class RandomState {
        /* These are static --- persistent between calls for same mu : */
        int l, m;

        double b1, b2, c, c0, c1, c2, c3;
        final double[] pp = new double[36];
        double p0;
        double p;
        double q;
        double s;
        double d;
        double omega;
        double big_l;/* integer "w/o overflow" */
        double muprev = 0., muprev2 = 0.;/*, muold	 = 0.*/
    }

    private static RandomState create_random_state() {
        return new RandomState();
    }

    public static double density_raw(double x, double lambda, boolean give_log) {
        if (lambda == 0) return ((x == 0) ? (give_log ? 0. : 1.) : (give_log ? Double.NEGATIVE_INFINITY : 0.));
        if (MathFunctions.isInfinite(lambda)) return (give_log ? Double.NEGATIVE_INFINITY : 0.);
        if (x < 0) return (give_log ? Double.NEGATIVE_INFINITY : 0.);
        if (x <= lambda * DBL_MIN) return (give_log ? -lambda : exp(-lambda));
        if (lambda < x * DBL_MIN) {
            x = -lambda + x * log(lambda) - lgammafn(x + 1);
            return (give_log ? x : exp(x));
        }
        // return(R_D_fexp( M_2PI*x, -stirlerr(x)-bd0(x,lambda) ));
        lambda = -stirlerr(x) - bd0(x, lambda);
        x = M_2PI * x;
        return give_log ? -0.5 * log(x) + lambda : exp(lambda - 0.5 * log(x));
    }

    private static double density(double x, double lambda, boolean give_log) {
        if (Double.isNaN(x) || Double.isNaN(lambda)) return x + lambda;
        if (lambda < 0) return Double.NaN;
        if (isNonInt(x)) return (give_log ? Double.NEGATIVE_INFINITY : 0.); // Non integer
        if (x < 0 || MathFunctions.isInfinite(x)) return (give_log ? Double.NEGATIVE_INFINITY : 0.);
        return density_raw(round(x), lambda, give_log);
    }

    private static double cumulative(double x, double lambda, boolean lower_tail, boolean log_p) {
        if (Double.isNaN(x) || Double.isNaN(lambda))
            return x + lambda;
        if (lambda < 0.) return Double.NaN;
        if (x < 0) return (lower_tail ? (log_p ? Double.NEGATIVE_INFINITY : 0.) : (log_p ? 0. : 1.));
        if (lambda == 0.) return (lower_tail ? (log_p ? 0. : 1.) : (log_p ? Double.NEGATIVE_INFINITY : 0.));
        if (MathFunctions.isInfinite(x))
            return (lower_tail ? (log_p ? 0. : 1.) : (log_p ? Double.NEGATIVE_INFINITY : 0.));
        x = floor(x + 1e-7);

        return Gamma.cumulative(lambda, x + 1, 1., !lower_tail, log_p);
    }

    /*	Uses the Cornish-Fisher Expansion to include a skewness
     *	correction to a normal approximation.  This gives an
     *	initial value which never seems to be off by more than
     *	1 or 2.	 A search is then conducted of values close to
     *	this initial start point.
     */
    private static double do_search(double y, double[] z, double p, double lambda, double incr) {
        if (z[0] >= p) {
            /* search to the left */
            for (; ; ) {
                if (y == 0 ||
                        (z[0] = cumulative(y - incr, lambda, /*l._t.*/true, /*log_p*/false)) < p)
                    return y;
                y = max(0, y - incr);
            }
        } else {		/* search to the right */

            for (; ; ) {
                y = y + incr;
                if ((z[0] = cumulative(y, lambda, /*l._t.*/true, /*log_p*/false)) >= p)
                    return y;
            }
        }
    }

    private static double quantile(double p, double lambda, boolean lower_tail, boolean log_p) {
        double mu, sigma, gamma, z[] = new double[1], y;
        if (Double.isNaN(p) || Double.isNaN(lambda))
            return p + lambda;
        if (MathFunctions.isInfinite(lambda))
            return Double.NaN;
        if (lambda < 0) return Double.NaN;
        if (lambda == 0) return 0;

        //R_Q_P01_boundaries(p, 0, ML_POSINF);
        if (log_p) {
            if (p > 0)
                return Double.NaN;
            if (p == 0) /* upper bound*/
                return lower_tail ? Double.POSITIVE_INFINITY : 0;
            if (p == Double.NEGATIVE_INFINITY)
                return lower_tail ? 0 : Double.POSITIVE_INFINITY;
        } else { /* !log_p */
            if (p < 0 || p > 1)
                return Double.NaN;
            if (p == 0)
                return lower_tail ? 0 : Double.POSITIVE_INFINITY;
            if (p == 1)
                return lower_tail ? Double.POSITIVE_INFINITY : 0;
        }

        mu = lambda;
        sigma = sqrt(lambda);
        /* gamma = sigma; PR#8058 should be kurtosis which is mu^-0.5 */
        gamma = 1.0 / sigma;

		/* Note : "same" code in qpois.c, qbinom.c, qnbinom.c --
		 * FIXME: This is far from optimal [cancellation for p ~= 1, etc]: */
        if (!lower_tail || log_p) {
            //p = R_DT_qIv(p); /* need check again (cancellation!): */
            // R_DT_qIv(p)	(log_p ? (lower_tail ? exp(p) : - expm1(p)) : (lower_tail ? (p) : (0.5 - (p) + 0.5)))
            p = (log_p ? (lower_tail ? exp(p) : -expm1(p)) : (lower_tail ? (p) : (0.5 - (p) + 0.5))); /* need check again (cancellation!): */
            if (p == 0.) return 0;
            if (p == 1.) return Double.POSITIVE_INFINITY;
        }
		/* temporary hack --- FIXME --- */
        if (p + 1.01 * DBL_EPSILON >= 1.) return Double.POSITIVE_INFINITY;

		/* y := approx.value (Cornish-Fisher expansion) :  */
        z[0] = Normal.quantile(p, 0., 1., /*lower_tail*/true, /*log_p*/false);
        //y = floor(mu + sigma * (z[0] + gamma * (z[0]*z[0] - 1) / 6) + 0.5);
        y = rint(mu + sigma * (z[0] + gamma * (z[0] * z[0] - 1) / 6));

        z[0] = cumulative(y, lambda, /*lower_tail*/true, /*log_p*/false);

		/* fuzz to ensure left continuity; 1 - 1e-7 may lose too much : */
        p *= 1 - 64 * DBL_EPSILON;

		/* If the mean is not too large a simple search is OK */
        if (lambda < 1e5) return do_search(y, z, p, lambda, 1);
		/* Otherwise be a bit cleverer in the search */
        {
            double incr = floor(y * 0.001), oldincr;
            do {
                oldincr = incr;
                y = do_search(y, z, p, lambda, incr);
                incr = max(1, floor(incr / 100));
            } while (oldincr > 1 && incr > lambda * 1e-15);
            return y;
        }
    }

    public static double random(double mu, RandomEngine random) {
        return random(mu, random, null);
    }

    public static double[] random(int n, double mu, RandomEngine random) {
        return random(n, mu, random, create_random_state());
    }

    private static double[] random(int n, double mu, RandomEngine random, RandomState state) {
        if (state == null) state = create_random_state();
        double[] result = new double[n];
        for (int i = 0; i < n; i++)
            result[i] = random(mu, random, state);
        return result;
    }

    private static double random(double mu, RandomEngine random, RandomState state) {
        final double
                a0 = -0.5,
                a1 = 0.3333333,
                a2 = -0.2500068,
                a3 = 0.2000118,
                a4 = -0.1661269,
                a5 = 0.1421878,
                a6 = -0.1384794,
                a7 = 0.1250060,
                one_7 = 0.1428571428571428571,
                one_12 = 0.0833333333333333333,
                one_24 = 0.0416666666666666667,
                fact[] = new double[]{1., 1., 2., 6., 24., 120., 720., 5040., 40320., 362880.};
        //
        double del, difmuk = 0., E = 0., fk = 0., fx, fy, g, px, py, t, u = 0., v, x;
        double pois = -1.;
        int k;
        boolean kflag = true, big_mu, new_big_mu = false;

        if (MathFunctions.isInfinite(mu) || mu < 0)
            return Double.NaN;

        if (mu <= 0.)
            return 0.;

        if (state == null)
            state = new RandomState();
        big_mu = mu >= 10.;
        if (big_mu)
            new_big_mu = false;

        if (!(big_mu && mu == state.muprev)) {/* maybe compute new persistent par.s */

            if (big_mu) {
                new_big_mu = true;
				/* Case A. (recalculation of s,d,l	because mu has changed):
				 * The poisson probabilities pk exceed the discrete normal
				 * probabilities fk whenever tw >= m(mu).
				 */
                state.muprev = mu;
                state.s = sqrt(mu);
                state.d = 6. * mu * mu;
                state.big_l = floor(mu - 1.1484);
				/* = an upper bound to m(mu) for all mu >= 10.*/
            } else { /* Small mu ( < 10) -- not using normal approx. */

				/* Case B. (start new table and calculate p0 if necessary) */

				/*muprev = 0.;-* such that next time, mu != muprev ..*/
                if (mu != state.muprev) {
                    state.muprev = mu;
                    state.m = max(1, (int) mu);
                    state.l = 0; /* pp[] is already ok up to pp[l] */
                    state.q = state.p0 = state.p = exp(-mu);
                }

                for (; ; ) {
					/*	Step U. uniform sample for inversion method */
                    u = random.nextDouble();
                    if (u <= state.p0)
                        return 0.;

					/*	Step T. table comparison until the end pp[l] of the
						pp-table of cumulative poisson probabilities
						(0.458 > ~= pp[9](= 0.45792971447) for mu=10 ) */
                    if (state.l != 0) {
                        for (k = (u <= 0.458) ? 1 : min(state.l, state.m); k <= state.l; k++)
                            if (u <= state.pp[k])
                                return (double) k;
                        if (state.l == 35) /* u > pp[35] */
                            continue;
                    }
					/*	Step C. creation of new poisson
						probabilities p[l..] and their cumulatives q =: pp[tw] */
                    state.l++;
                    for (k = state.l; k <= 35; k++) {
                        state.p *= mu / k;
                        state.q += state.p;
                        state.pp[k] = state.q;
                        if (u <= state.q) {
                            state.l = k;
                            return (double) k;
                        }
                    }
                    state.l = 35;
                } /* end(repeat) */
            }/* mu < 10 */

        } /* end {initialize persistent vars} */

		/* Only if mu >= 10 : ----------------------- */

		/* Step N. normal sample */
        g = mu + state.s * Normal.random_standard(random);/* norm_rand() ~ N(0,1), standard normal */

        if (g >= 0.) {
            pois = floor(g);
			/* Step I. immediate acceptance if pois is large enough */
            if (pois >= state.big_l)
                return pois;
			/* Step S. squeeze acceptance */
            fk = pois;
            difmuk = mu - fk;
            u = random.nextDouble(); /* ~ U(0,1) - sample */
            if (state.d * u >= difmuk * difmuk * difmuk)
                return pois;
        }

		/* Step P. preparations for steps Q and H.
		       (recalculations of parameters if necessary) */

        if (new_big_mu || mu != state.muprev2) {
			/* Careful! muprev2 is not always == muprev
			   because one might have exited in step I or S
			 */
            state.muprev2 = mu;
            state.omega = M_1_SQRT_2PI / state.s;
			/* The quantities b1, b2, c3, c2, c1, c0 are for the Hermite
			 * approximations to the discrete normal probabilities fk. */

            state.b1 = one_24 / mu;
            state.b2 = 0.3 * state.b1 * state.b1;
            state.c3 = one_7 * state.b1 * state.b2;
            state.c2 = state.b2 - 15. * state.c3;
            state.c1 = state.b1 - 6. * state.b2 + 45. * state.c3;
            state.c0 = 1. - state.b1 + 3. * state.b2 - 15. * state.c3;
            state.c = 0.1069 / mu; /* guarantees majorization by the 'hat'-function. */
        }

        boolean skip_to_stepf = false;
        if (g >= 0.) {
			/* 'Subroutine' F is called (kflag=0 for correct return) */
            //kflag = 0;
            kflag = false;
            skip_to_stepf = true;
            //goto Step_F;
        }

        for (; ; ) {
            if (!skip_to_stepf) {
				/* Step E. Exponential Sample */
                E = Exponential.random_standard(random);	/* ~ Exp(1) (standard exponential) */
				/*  sample t from the laplace 'hat'
				    (if t <= -0.6744 then pk < fk for all mu >= 10.) */
                u = 2 * random.nextDouble() - 1.;
                //t = 1.8 + fsign(E, u);
                t = 1.8 + abs(E) * signum(u);

                if (t <= -0.6744) continue;

				/* t > -.67.. */
                pois = floor(mu + state.s * t);
                fk = pois;
                difmuk = mu - fk;
				/* 'subroutine' F is called (kflag=1 for correct return) */
                //kflag = 1;
                kflag = true;
            }

            //Step_F: /* 'subroutine' F : calculation of px,py,fx,fy. */
            skip_to_stepf = false;
            if (pois < 10) { /* use factorials from table fact[] */
                px = -mu;
                py = pow(mu, pois) / fact[(int) pois]; // FIXME pois can be < 0!
            } else {
				/* Case pois >= 10 uses polynomial approximation
				   a0-a7 for accuracy when advisable */
                del = one_12 / fk;
                del = del * (1. - 4.8 * del * del);
                v = difmuk / fk;
                if (abs(v) <= 0.25)
                    px = fk * v * v * (((((((a7 * v + a6) * v + a5) * v + a4) *
                            v + a3) * v + a2) * v + a1) * v + a0) - del;
                else /* |v| > 1/4 */
                    px = fk * log(1. + v) - difmuk - del;
                py = M_1_SQRT_2PI / sqrt(fk);
            }
            x = (0.5 - difmuk) / state.s;
            x *= x;/* x^2 */
            fx = -0.5 * x;
            fy = state.omega * (((state.c3 * x + state.c2) * x + state.c1) * x + state.c0);
            if (kflag) {
				/* Step H. Hat acceptance (E is repeated on rejection) */
                if (state.c * abs(u) <= py * exp(px + E) - fy * exp(fx + E))
                    break;
            } else
				/* Step Q. Quotient acceptance (rare case) */
                if (fy - u * fy <= py * exp(px - fx))
                    break;
        }
        return pois;
    }

    private final double lambda;
    private final RandomState state;

    public Poisson(double lambda) {
        this.lambda = lambda;
        state = create_random_state();
    }

    @Override
    public double density(double x, boolean log) {
        return density(x, lambda, log);
    }

    @Override
    public double cumulative(double p, boolean lower_tail, boolean log_p) {
        return cumulative(p, lambda, lower_tail, log_p);
    }

    @Override
    public double quantile(double q, boolean lower_tail, boolean log_p) {
        return quantile(q, lambda, lower_tail, log_p);
    }

    @Override
    public double random() {
        return random(lambda, random, state);
    }

    @Override
    public double[] random(int n) {
        return random(n, lambda, random, state);
    }
}
