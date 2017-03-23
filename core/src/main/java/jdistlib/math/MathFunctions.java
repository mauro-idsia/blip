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
package jdistlib.math;

import static java.lang.Math.*;
import static jdistlib.math.Constants.*;

public class MathFunctions {
	/**
	 * Log of multivariate gamma function
	 * By: Roby Joehanes
	 * @param a
	 * @param p the order (or dimension)
	 * @return log multivariate gamma
	 */
	public static final double lmvgammafn(double a, int p) {
		double sum = 0;
		for (int j = 1; j <= p; j++)
			sum += lgammafn(a + (1-j)/2.0);
		return sum + p*(p-1)/4.0 * M_LOG_PI;
	}

	public static final double trunc(double x)
	{	return x >= 0 ? floor(x) : ceil(x); }

	/**
	 * Rounding to desired num places
	 * @param val
	 * @param places
	 * @return rounded number
	 */
	public static final double round(double val, int places) {
		double factor = pow(10, places);
		return Math.round(val*factor)/factor;
	}

	/**
	 * Mimicking R's signif
	 * @param val
	 * @param places
	 * @return rounded values
	 */
	public static final double signif(double val, int places) {
		if (val != 0)
			places = places - (int) (ceil(log10(val)));
		double factor = pow(10, places);
		return Math.round(val*factor)/factor;
	}

	/**
	 * Find greatest common divisor of m and n
	 * @param m
	 * @param n
	 * @return GCD(m, n)
	 */
	public static final int gcd(int m, int n) {
		while (true) {
			m %= n;
			if (0 == m) return n;
			n %= m;
			if (0 == n) return m;
		}
	}

	/**
	 * Compute sinc(x) = sin(x)/x for x != 0. Return 1 when x == 0.
	 * @param x
	 * @return sinc value
	 */
	public static final double sinc(double x) {
		return x == 0 ? 1 : sin(x) / x;
	}

	// R's finite is not the same as Java's !Double.isInfinite
	public static final boolean isFinite(double x)
	{	return !Double.isNaN(x) && (x != Double.POSITIVE_INFINITY) && (x != Double.NEGATIVE_INFINITY); }

	public static final boolean isInfinite(double x)
	{	return Double.isNaN(x) || (x == Double.POSITIVE_INFINITY) || (x == Double.NEGATIVE_INFINITY); }

	public static final double ldexp(double x, double ex) {
		return exp(log(x) + ex * Constants.M_LN2);
	}

	/**
	 * Determine the number of terms for the
	 * double precision orthogonal Chebyshev series "dos" needed to insure
	 * the error is no larger than "eta".  Ordinarily eta will be
	 * chosen to be one-tenth machine precision.
	 *
	 *    These routines are translations into C of Fortran routines
	 *    by W. Fullerton of Los Alamos Scientific Laboratory.
	 *
	 *    Based on the Fortran routine dcsevl by W. Fullerton.
	 *    Adapted from R. Broucke, Algorithm 446, CACM., 16, 254 (1973).
	 */
	static final int chebyshev_init(double dos[], int nos, double eta) {
		if (nos < 1) return 0;

		double err = 0.0;
		int i = 0;
		for (int ii=1; ii<=nos; ii++) {
			i = nos - ii;
			err += abs(dos[i]);
			if (err > eta) {
				return i;
			}
		}
		return i;
	}

	/**
	 * evaluate the n-term Chebyshev series "a" at "x".
	 * @param x
	 * @param a
	 * @param n
	 * @return Chebyshev function result
	 */
	public static final double chebyshev_eval(double x, double a[], int n) {
		if (n < 1 || n > 1000 || x < -1.1 || x > 1.1)
			return Double.NaN;
		double twox = x * 2, b2 = 0, b1 = 0, b0 = 0;
		for (int i = 1; i <= n; i++) {
			b2 = b1;
			b1 = b0;
			b0 = twox * b1 - b2 + a[n - i];
		}
		return (b0 - b2) * 0.5;
	}

	/*
	 *
	 *  SYNOPSIS
	 *
	 *    #include "DistLib.h"
	 *    double lgammacor(double x);
	 *
	 *  DESCRIPTION
	 *
	 *    Compute the log gamma correction factor for x >= 10 so that
	 *
	 *    log(gamma(x)) = log(sqrt(2*pi))+(x-.5)*log(x)-x+lgammacor(x)
	 *
	 *  NOTES
	 *
	 *    This routine is a translation into C of a Fortran subroutine
	 *    written by W. Fullerton of Los Alamos Scientific Laboratory.
	 */

	/*!* #include "DistLib.h" /*4!*/

	public static final double lgammacor(double x)
	{
		final double algmcs[] = {
				+.1666389480451863247205729650822e+0,
				-.1384948176067563840732986059135e-4,
				+.9810825646924729426157171547487e-8,
				-.1809129475572494194263306266719e-10,
				+.6221098041892605227126015543416e-13,
				-.3399615005417721944303330599666e-15,
				+.2683181998482698748957538846666e-17,
				-.2868042435334643284144622399999e-19,
				+.3962837061046434803679306666666e-21,
				-.6831888753985766870111999999999e-23,
				+.1429227355942498147573333333333e-24,
				-.3547598158101070547199999999999e-26,
				+.1025680058010470912000000000000e-27,
				-.3401102254316748799999999999999e-29,
				+.1276642195630062933333333333333e-30
		};
		int nalgm = 5;
		double xbig = 94906265.62425156;
		double xmax = 3.745194030963158e306;
		double tmp;

		if (x < 10) return Double.NaN;
		if (x >= xmax) return 1 / (x * 12); // Underflow
		if (x < xbig) {
			tmp = 10 / x;
			return chebyshev_eval(tmp * tmp * 2 - 1, algmcs, nalgm) / x;
		}
		return 1 / (x * 12);
	}

	public static final double lgammafn_sign(double x, int []sgn)
	{
		final double xmax = 2.5327372760800758e+305;
		final double dxrel = 1.490116119384765625e-8;
		double ans, y, sinpiy;

		if (sgn != null) sgn[0] = 1;
		if (sgn != null && x < 0 && (floor(-x) % 2.) == 0)
			sgn[0] = -1;

		if (x <= 0 && x == trunc(x)) { /* Negative integer argument */
			//ML_ERROR(ME_RANGE, "lgamma");
			return Double.POSITIVE_INFINITY;/* +Inf, since lgamma(x) = log|gamma(x)| */
		}

		y = abs(x);

		if (y < 1e-306) return -log(y); // denormalized range, R change
		if (y <= 10)
			return log(abs(gammafn(x)));
		if (y > xmax) {
			//ML_ERROR(ME_RANGE, "lgamma");
			return Double.POSITIVE_INFINITY;
		}

		if (x > 0) { /* thread.e. y = x > 10 */
			if(x > 1e17)
				return(x*(log(x) - 1.));
			if(x > 4934720.)
				return(M_LN_SQRT_2PI + (x - 0.5) * log(x) - x);
			return M_LN_SQRT_2PI + (x - 0.5) * log(x) - x + lgammacor(x);
		}
		/* else: x < -10; y = -x */
		sinpiy = abs(sinpi(y));

		if (sinpiy == 0) { /* Negative integer argument ===
	    			  Now UNNECESSARY: caught above */
			//MATHLIB_WARNING(" ** should NEVER happen! *** [lgamma.c: Neg.int, y=%g]\n",y);
			return Double.NaN;
		}

		ans = M_LN_SQRT_PId2 + (x - 0.5) * log(y) - x - log(sinpiy) - lgammacor(y);

		if(abs((x - trunc(x - 0.5)) * ans / x) < dxrel) {
			/* The answer is less than half precision because
			 * the argument is too near a negative integer. */
			//ML_ERROR(ME_PRECISION, "lgamma");
			System.err.println("lgamma precision error!");
		}
		return ans;
	}

	public static final double lgammafn(double x) {
		return lgammafn_sign(x, null);
	}

	/**
	 * Batch call log gamma function
	 * @param x
	 * @return an array of results
	 */
	public static final double[] lgammafn(double[] x) {
		int n = x.length;
		double[] r = new double[n];
		for (int i = 0; i < n; i++)
			r[i] = lgammafn_sign(x[i], null);
		return r;
	}

	public static final double stirlerr(double n)
	{
		final double S0 = 0.083333333333333333333,
				S1 = 0.00277777777777777777778,
				S2 = 0.00079365079365079365079365,
				S3 = 0.000595238095238095238095238,
				S4 = 0.0008417508417508417508417508;
		final double sferr_halves[] = {
			0.0, /* n=0 - wrong, place holder only */
			0.1534264097200273452913848,  /* 0.5 */
			0.0810614667953272582196702,  /* 1.0 */
			0.0548141210519176538961390,  /* 1.5 */
			0.0413406959554092940938221,  /* 2.0 */
			0.03316287351993628748511048, /* 2.5 */
			0.02767792568499833914878929, /* 3.0 */
			0.02374616365629749597132920, /* 3.5 */
			0.02079067210376509311152277, /* 4.0 */
			0.01848845053267318523077934, /* 4.5 */
			0.01664469118982119216319487, /* 5.0 */
			0.01513497322191737887351255, /* 5.5 */
			0.01387612882307074799874573, /* 6.0 */
			0.01281046524292022692424986, /* 6.5 */
			0.01189670994589177009505572, /* 7.0 */
			0.01110455975820691732662991, /* 7.5 */
			0.010411265261972096497478567, /* 8.0 */
			0.009799416126158803298389475, /* 8.5 */
			0.009255462182712732917728637, /* 9.0 */
			0.008768700134139385462952823, /* 9.5 */
			0.008330563433362871256469318, /* 10.0 */
			0.007934114564314020547248100, /* 10.5 */
			0.007573675487951840794972024, /* 11.0 */
			0.007244554301320383179543912, /* 11.5 */
			0.006942840107209529865664152, /* 12.0 */
			0.006665247032707682442354394, /* 12.5 */
			0.006408994188004207068439631, /* 13.0 */
			0.006171712263039457647532867, /* 13.5 */
			0.005951370112758847735624416, /* 14.0 */
			0.005746216513010115682023589, /* 14.5 */
			0.005554733551962801371038690  /* 15.0 */
		};
		double nn;
		if (n <= 15.0) {
			nn = n + n;
			if (nn == (int)nn) return(sferr_halves[(int)nn]);
			return(lgammafn(n + 1.) - (n + 0.5)*log(n) + n - M_LN_SQRT_2PI);
		}
		nn = n*n;
		if (n>500) return((S0-S1/nn)/n);
		if (n> 80) return((S0-(S1-S2/nn)/nn)/n);
		if (n> 35) return((S0-(S1-(S2-S3/nn)/nn)/nn)/n);
		// 15 < n <= 35 :
		return((S0-(S1-(S2-(S3-S4/nn)/nn)/nn)/nn)/n);
	}

	public static final double gammafn(double x)
	{
		final double gamcs[] = {
				+.8571195590989331421920062399942e-2,
				+.4415381324841006757191315771652e-2,
				+.5685043681599363378632664588789e-1,
				-.4219835396418560501012500186624e-2,
				+.1326808181212460220584006796352e-2,
				-.1893024529798880432523947023886e-3,
				+.3606925327441245256578082217225e-4,
				-.6056761904460864218485548290365e-5,
				+.1055829546302283344731823509093e-5,
				-.1811967365542384048291855891166e-6,
				+.3117724964715322277790254593169e-7,
				-.5354219639019687140874081024347e-8,
				+.9193275519859588946887786825940e-9,
				-.1577941280288339761767423273953e-9,
				+.2707980622934954543266540433089e-10,
				-.4646818653825730144081661058933e-11,
				+.7973350192007419656460767175359e-12,
				-.1368078209830916025799499172309e-12,
				+.2347319486563800657233471771688e-13,
				-.4027432614949066932766570534699e-14,
				+.6910051747372100912138336975257e-15,
				-.1185584500221992907052387126192e-15,
				+.2034148542496373955201026051932e-16,
				-.3490054341717405849274012949108e-17,
				+.5987993856485305567135051066026e-18,
				-.1027378057872228074490069778431e-18,
				+.1762702816060529824942759660748e-19,
				-.3024320653735306260958772112042e-20,
				+.5188914660218397839717833550506e-21,
				-.8902770842456576692449251601066e-22,
				+.1527474068493342602274596891306e-22,
				-.2620731256187362900257328332799e-23,
				+.4496464047830538670331046570666e-24,
				-.7714712731336877911703901525333e-25,
				+.1323635453126044036486572714666e-25,
				-.2270999412942928816702313813333e-26,
				+.3896418998003991449320816639999e-27,
				-.6685198115125953327792127999999e-28,
				+.1146998663140024384347613866666e-28,
				-.1967938586345134677295103999999e-29,
				+.3376448816585338090334890666666e-30,
				-.5793070335782135784625493333333e-31
		};
		int i, n;
		double y, value;
		/*
		int ngam = 0;
		double xmin = 0., xmax = 0., xsml = 0., dxrel = 0.;
		if (ngam == 0) {
			ngam = chebyshev_init(gamcs, 42, DBL_EPSILON/20);
			xmin=-170.5674972726612; xmax=171.61447887182298;
			xsml = exp(max(log(DBL_MIN), -log(Double.MAX_VALUE))+0.01);
			dxrel = sqrt(DBL_EPSILON);
		}
		/*/
		final int ngam = 22;
		final double
			xmin = -170.5674972726612,
			xmax = 171.61447887182298,
			xsml = 2.2474362225598545e-308,
			dxrel = 1.490116119384765696e-8;
		//*/

		if(Double.isNaN(x)) return x;
		// If the argument is exactly zero or a negative integer then return NaN.
		if (x == 0 || (x < 0 && x == (long)x)) return Double.NaN;
		y = abs(x);

		if (y <= 10) {
			/* Compute gamma(x) for -10 <= x <= 10. */
			/* Reduce the interval and find gamma(1 + y) for */
			/* 0 <= y < 1 first of all. */
			n = (int) x;
			if(x < 0) --n;
			y = x - n;/* n = floor(x)  ==>	y in [ 0, 1 ) */
			--n;
			value = chebyshev_eval(y * 2 - 1, gamcs, ngam) + .9375;
			if (n == 0)
				return value;/* x = 1.dddd = 1+y */

			if (n < 0) {
				/* compute gamma(x) for -10 <= x < 1 */

				/* The answer is less than half precision */
				/* because x too near a negative integer. */
				if (x < -0.5 && abs(x - (int)(x - 0.5) / x) < dxrel)
					throw new ArithmeticException("Math Error: PRECISION");

				/* The argument is so close to 0 that the result would overflow. */
				if (y < xsml)
					return x > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
				n = -n;
				for (i = 0; i < n; i++)
					value /= (x + i);
				return value;
			} else {
				/* gamma(x) for 2 <= x <= 10 */
				for (i = 1; i <= n; i++)
					value *= (y + i);
				return value;
			}
		} else {
			/* gamma(x) for	 y = |x| > 10. */
			if (x > xmax)			/* Overflow */
				return Double.POSITIVE_INFINITY;

			if (x < xmin)			/* Underflow */
				return 0.;

			if(y <= 50 && y == (int)y) { /* compute (n - 1)! */
				value = 1.;
				for (i = 2; i < y; i++) value *= i;
			} else { /* normal case */
				value = exp((y - 0.5) * log(y) - y + M_LN_SQRT_2PI +
						((2*y == (int)2*y)? stirlerr(y) : lgammacor(y)));
			}

			if (x > 0)
				return value;

			if (abs((x - (int)(x - 0.5))/x) < dxrel)
				/* The answer is less than half precision because */
				/* the argument is too near a negative integer. */
				throw new ArithmeticException("Math Error: PRECISION");

			double sinpiy = sinpi(y);
			if (sinpiy == 0)		/* Negative integer arg - overflow */
				return Double.POSITIVE_INFINITY;

			return -PI / (y * sinpiy * value);
		}
	}

	/**
	 * Batch call gamma function
	 * @param x
	 * @return an array of results
	 */
	public static final double[] gammafn(double[] x) {
		int n = x.length;
		double[] r = new double[n];
		for (int i = 0; i < n; i++)
			r[i] = gammafn(x[i]);
		return r;
	}

	/*
	 * 	Evaluates the "deviance part"
	 *	bd0(x,M) :=  M * D0(x/M) = M*[ x/M * log(x/M) + 1 - (x/M) ] =
	 *		  =  x * log(x/M) + M - x
	 *	where M = E[X] = n*p (or = lambda), for	  x, M > 0
	 *
	 *	in a manner that should be stable (with small relative error)
	 *	for all x and M=np. In particular for x/np close to 1, direct
	 *	evaluation fails, and evaluation is based on the Taylor series
	 *	of log((1+v)/(1-v)) with v = (x-M)/(x+M) = (x-np)/(x+np).
	 */
	public static final double bd0(double x, double np)
	{
		double ej, s, s1, v;
		int j;

		if(isInfinite(x) || isInfinite(np) || np == 0.0) return Double.NaN;

		if (abs(x-np) < 0.1*(x+np)) {
			v = exp(log(x-np) - log(x+np)); // might underflow to 0
			s = (x-np)*v;/* s using v -- change by MM */
			ej = 2*x*v;
			v = v*v;
			for (j=1; j < 1000; j++) { /* Taylor series; 1000: no infinite loop as |v| < .1,  v^2000 is "zero" */
				ej *= v;// = v^(2j+1)
				s1 = s+ej/((j<<1)+1);
				if (s1 == s) /* last term was effectively 0 */
					return s1;
				s = s1;
			}
		}
		/* else:  | x - np |  is not too small */
		return(x*(log(x) - log(np))+np-x);
	}

	public static final double lbeta(double a, double b)
	{
		double corr, p, q;

		if(Double.isNaN(a) || Double.isNaN(b)) return a + b;
		p = q = a;
		if(b < p) p = b;/* := min(a,b) */
		if(b > q) q = b;/* := max(a,b) */

		/* both arguments must be >= 0 */
		if (p < 0)
			return Double.NaN;
		else if (p == 0) {
			return Double.POSITIVE_INFINITY;
		}
		else if (isInfinite(q)) { /* q == +Inf */
			return Double.NEGATIVE_INFINITY;
		}

		if (p >= 10) {
			/* p and q are big. */
			corr = lgammacor(p) + lgammacor(q) - lgammacor(p + q);
			return log(q) * -0.5 + M_LN_SQRT_2PI + corr
					+ (p - 0.5) * log(p / (p + q)) + q * log1p(-p / (p + q));
		}
		else if (q >= 10) {
			/* p is small, but q is big. */
			corr = lgammacor(q) - lgammacor(p + q);
			return lgammafn(p) + corr + p - p * log(p + q)
					+ (q - 0.5) * log1p(-p / (p + q));
		}
		else
			/* p and q are small: p <= q < 10. */
			if (p < 1e-306) return lgammafn(p) + (lgammafn(q) - lgammafn(p+q));
		return log(gammafn(p) * (gammafn(q) / gammafn(p + q)));
	}

	protected static final double
		bigx = 4294967296.0,
		scalefactor = bigx * bigx * bigx * bigx * bigx * bigx * bigx * bigx;
		//;

	/* Continued fraction for calculation of
	 *    1/thread + x/(thread+d) + x^2/(thread+2*d) + x^3/(thread+3*d) + ... = sum_{tw=0}^Inf x^tw/(thread+tw*d)
	 *
	 * auxilary in log1pmx() and lgamma1p()
	 */
	static final double logcf (double x, double i, double d, double eps)
	{
		double c1 = 2 * d;
		double c2 = i + d;
		double c4 = c2 + d;
		double a1 = c2;
		double b1 = i * (c2 - i * x);
		double b2 = d * d * x;
		double a2 = c4 * c2 - b2;

		b2 = c4 * b1 - i * b2;

		while (abs(a2 * b1 - a1 * b2) > abs(eps * b1 * b2)) {
			double c3 = c2*c2*x;
			c2 += d;
			c4 += d;
			a1 = c4 * a2 - c3 * a1;
			b1 = c4 * b2 - c3 * b1;

			c3 = c1 * c1 * x;
			c1 += d;
			c4 += d;
			a2 = c4 * a1 - c3 * a2;
			b2 = c4 * b1 - c3 * b2;

			if (abs (b2) > scalefactor) {
				a1 /= scalefactor;
				b1 /= scalefactor;
				a2 /= scalefactor;
				b2 /= scalefactor;
			} else if (abs (b2) < 1 / scalefactor) {
				a1 *= scalefactor;
				b1 *= scalefactor;
				a2 *= scalefactor;
				b2 *= scalefactor;
			}
		}
		return a2 / b2;
	}

	public static final double log1pmx (double x)
	{
		final double minLog1Value = -0.79149064;

		if (x > 1 || x < minLog1Value)
			return log1p(x) - x;
		else { /* -.791 <=  x <= 1  -- expand in  [x/(2+x)]^2 =: y :
		 * log(1+x) - x =  x/(2+x) * [ 2 * y * S(y) - x],  with
		 * ---------------------------------------------
		 * S(y) = 1/3 + y/5 + y^2/7 + ... = \sum_{tw=0}^\infty  y^tw / (2k + 3)
		 */
			double r = x / (2 + x), y = r * r;
			if (abs(x) < 1e-2) {
				final double two = 2;
				return r * ((((two / 9 * y + two / 7) * y + two / 5) * y +
						two / 3) * y - x);
			} else {
				final double tol_logcf = 1e-14;
				return r * (2 * y * logcf (y, 3, 2, tol_logcf) - x);
			}
		}
	}

	/**
	 * log1px takes a double and returns a double.
	 * It is a Taylor series expansion of log(1+x).
	 * x is presumed to be < 1.  As I have called it, x < .1,
	 * and so I know the algorithm will terminate quickly.
	 * The closer x is to 1, the slower this will be. (From AS 885)
	 */
	public static final double log1px(double x)
	{
		int n, sn;
		double xn, ans, oans, term; //, eps;

		if (!(abs(x) < 1.0e0)) {
			return Double.NaN; //(0.0e0/0.0e0); /* NaN */
		}
		term = ans= oans = x;
		oans = ans + (double) 1.0e0;
		n = 1;
		sn = 1;
		xn = x;
		/* Comparing ans!=oans is done here to insure that this calculation
		continues until the accuracy of the machine is reached.  At some point,
		the value is not being updated in successive iterations, that is time to
		quit. */
		while (ans != oans ) {
			oans = ans;
			sn *= -1;
			xn *= x;
			term= ((double)sn/(double)++n)*xn;
			ans += term;
		}
		return(ans);
	}

	public static final double lgamma1p(double a)
	{
		final double eulers_const =	 0.5772156649015328606065120900824024;

	    /* coeffs[thread] holds (zeta(thread+2)-1)/(thread+2) , thread = 0:(N-1), N = 40 : */
		final int N = 40;
	    final double coeffs[] = {
		0.3224670334241132182362075833230126e-0,/* = (zeta(2)-1)/2 */
		0.6735230105319809513324605383715000e-1,/* = (zeta(3)-1)/3 */
		0.2058080842778454787900092413529198e-1,
		0.7385551028673985266273097291406834e-2,
		0.2890510330741523285752988298486755e-2,
		0.1192753911703260977113935692828109e-2,
		0.5096695247430424223356548135815582e-3,
		0.2231547584535793797614188036013401e-3,
		0.9945751278180853371459589003190170e-4,
		0.4492623673813314170020750240635786e-4,
		0.2050721277567069155316650397830591e-4,
		0.9439488275268395903987425104415055e-5,
		0.4374866789907487804181793223952411e-5,
		0.2039215753801366236781900709670839e-5,
		0.9551412130407419832857179772951265e-6,
		0.4492469198764566043294290331193655e-6,
		0.2120718480555466586923135901077628e-6,
		0.1004322482396809960872083050053344e-6,
		0.4769810169363980565760193417246730e-7,
		0.2271109460894316491031998116062124e-7,
		0.1083865921489695409107491757968159e-7,
		0.5183475041970046655121248647057669e-8,
		0.2483674543802478317185008663991718e-8,
		0.1192140140586091207442548202774640e-8,
		0.5731367241678862013330194857961011e-9,
		0.2759522885124233145178149692816341e-9,
		0.1330476437424448948149715720858008e-9,
		0.6422964563838100022082448087644648e-10,
		0.3104424774732227276239215783404066e-10,
		0.1502138408075414217093301048780668e-10,
		0.7275974480239079662504549924814047e-11,
		0.3527742476575915083615072228655483e-11,
		0.1711991790559617908601084114443031e-11,
		0.8315385841420284819798357793954418e-12,
		0.4042200525289440065536008957032895e-12,
		0.1966475631096616490411045679010286e-12,
		0.9573630387838555763782200936508615e-13,
		0.4664076026428374224576492565974577e-13,
		0.2273736960065972320633279596737272e-13,
		0.1109139947083452201658320007192334e-13/* = (zeta(40+1)-1)/(40+1) */
	    };

	    final double c = 0.2273736845824652515226821577978691e-12;/* zeta(N+2)-1 */
	    final double tol_logcf = 1e-14;
	    double lgam;
	    int i;

	    if (abs (a) >= 0.5)
		return lgammafn (a + 1);

	    /* Abramowitz & Stegun 6.1.33 : for |x| < 2,
	     * <==> log(gamma(1+x)) = -(log(1+x) - x) - gamma*x + x^2 * \sum_{n=0}^\infty c_n (-x)^n
	     * where c_n := (Zeta(n+2) - 1)/(n+2)  = coeffs[n]
	     *
	     * Here, another convergence acceleration trick is used to compute
	     * lgam(x) :=  sum_{n=0..Inf} c_n (-x)^n
	     */
	    lgam = c * logcf(-a / 2, N + 2, 1, tol_logcf);
	    for (i = N - 1; i >= 0; i--)
		lgam = coeffs[i] - a * lgam;

	    return (a * lgam - eulers_const) * a - log1pmx (a);
	}

	/**
	 * Compute the log of a sum from logs of terms, thread.e.,
	 *
	 *     log (exp (logx) + exp (logy))
	 *
	 * without causing overflows and without throwing away large handfuls
	 * of accuracy.
	 */
	public static final double logspace_add (double logx, double logy) {
	    return max(logx, logy) + log1p (exp (-abs (logx - logy)));
	}

	/**
	 * Compute the log of a difference from logs of terms, thread.e.,
	 *
	 *     log (exp (logx) - exp (logy))
	 *
	 * without causing overflows and without throwing away large handfuls
	 * of accuracy.
	 */
	public static final double logspace_sub (double logx, double logy) {
		logy = logy - logx;
	    return logx + ((logy) > -M_LN2 ? log(-expm1(logy)) : log1p(-exp(logy)));
	}

	/*
	 * Compute the log of a sum from logs of terms, thread.e.,
	 *
	 *     log (sum_i  exp (logx[thread]) ) =
	 *     log (e^M * sum_i  e^(logx[thread] - M) ) =
	 *     M + log( sum_i  e^(logx[thread] - M)
	 *
	 * without causing overflows or throwing much accuracy.
	 */
	public static final double logspace_sum (double[] logx)
	{
		int n = logx.length;
	    if(n == 0) return Double.NEGATIVE_INFINITY; // = log( sum(<empty>) )
	    if(n == 1) return logx[0];
	    if(n == 2) return logspace_add(logx[0], logx[1]);
	    // else (n >= 3) :
	    int i;
	    // Mx := max_i log(x_i)
	    double Mx = logx[0];
	    for(i = 1; i < n; i++) if(Mx < logx[i]) Mx = logx[i];
	    // LDOUBLE s = (LDOUBLE) 0.; // TODO long double
	    //for(thread = 0; thread < n; thread++) s += EXP(logx[thread] - Mx);
	    //return Mx + (double) LOG(s);
	    double s = 0.;
	    for(i = 0; i < n; i++) s += exp(logx[i] - Mx);
	    return Mx + (double) log(s);
	}

	/* Based on C translation of ACM TOMS 708
	   Please do not change this, e.g. to use R's versions of the
	   ancillary routines, without investigating the error analysis as we
	   do need very high relative accuracy.  This version has about
	   14 digits accuracy.
	 */
	/** <pre>-----------------------------------------------------------------------
	 *	      Evaluation of the Incomplete Beta function I_x(a,b)
	 *		       --------------------
	 *     It is assumed that a and b are nonnegative, and that x <= 1
	 *     and y = 1 - x.  Bratio assigns w and w1 the values
	 *			w  = I_x(a,b)
	 *			w1 = 1 - I_x(a,b)
	 *     ierr is a variable that reports the status of the results.
	 *     If no input errors are detected then ierr is set to 0 and
	 *     w and w1 are computed. otherwise, if an error is detected,
	 *     then w and w1 are assigned the value 0 and ierr is set to
	 *     one of the following values ...
	 *	  ierr = 1  if a or b is negative
	 *	  ierr = 2  if a = b = 0
	 *	  ierr = 3  if x < 0 or x > 1
	 *	  ierr = 4  if y < 0 or y > 1
	 *	  ierr = 5  if x + y != 1
	 *	  ierr = 6  if x = a = 0
	 *	  ierr = 7  if y = b = 0
	 *	  ierr = 8	(not used currently)
	 *	  ierr = 9  NaN in a, b, x, or y
	 *	  ierr = 10     (not used currently)
	 *	  ierr = 11  bgrat() error code 1 [+ warning in bgrat()]
	 *	  ierr = 12  bgrat() error code 2   (no warning here)
	 *	  ierr = 13  bgrat() error code 3   (no warning here)
	 *	  ierr = 14  bgrat() error code 4 [+ WARNING in bgrat()]
	 * --------------------
	 *     Written by Alfred H. Morris, Jr.
	 *	  Naval Surface Warfare Center
	 *	  Dahlgren, Virginia
	 *     Revised ... Nov 1991
	 * -----------------------------------------------------------------------</pre>
	 */
	public static final double[] bratio(double a, double b, double x, double y, boolean log_p)
	{
		boolean do_swap;
		int n = 0, kase = 0, ierr1[] = {0};
		double z, a0, b0, x0, y0, lambda = 0, w, w1;

		/*  eps is a machine dependent constant: the smallest
		 *      floating point number for which   1.0 + eps > 1.0 */
		double eps = 2. * DBL_EPSILON; /* == DBL_EPSILON (in R, Rmath) */

		/* ----------------------------------------------------------------------- */
		w = w1 = (log_p ? Double.NEGATIVE_INFINITY : 0.);
		if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(a) || Double.isNaN(b)) return new double[] {w, w1, 9};

		if (a < 0.0 || b < 0.0)   return new double[] {w, w1, 1};
		if (a == 0.0 && b == 0.0) return new double[] {w, w1, 2};
		if (x < 0.0 || x > 1.0)   return new double[] {w, w1, 3};
		if (y < 0.0 || y > 1.0)   return new double[] {w, w1, 4};

		z = x + y - 0.5 - 0.5;

		if (abs(z) > eps * 3.0) return new double[] {w, w1, 5};

		if (x == 0.)
		{
			if (a == 0.0) return new double[] {w, w1, 6};
			w  = (log_p ? Double.NEGATIVE_INFINITY : 0.);
			w1 = (log_p ? 0. : 1.);
			return new double[] {w, w1, 0};
		}
		if (y == 0.)
		{
			if (b == 0.0) return new double[] {w, w1, 7};
			w  = (log_p ? 0. : 1.);
			w1 = (log_p ? Double.NEGATIVE_INFINITY : 0.);
			return new double[] {w, w1, 0};

		}
		if (a == 0.)
		{
			w  = (log_p ? 0. : 1.);
			w1 = (log_p ? Double.NEGATIVE_INFINITY : 0.);
			return new double[] {w, w1, 0};
		}
		if (b == 0.)
		{
			w  = (log_p ? Double.NEGATIVE_INFINITY : 0.);
			w1 = (log_p ? 0. : 1.);
			return new double[] {w, w1, 0};
		}

		eps = max(eps, 1e-15);
		boolean a_lt_b = (a < b);
		if (/* max(a,b) */ (a_lt_b ? b : a) < eps * .001) { /* procedure for a and b < 0.001 * eps */
			// L230:  -- result *independent* of x (!)
			// *w  = a/(a+b)  and  w1 = b/(a+b) :
			if(log_p) {
				if(a_lt_b) {
					w  = log1p(-a/(a+b)); // notably if a << b
					w1 = log  ( a/(a+b));
				} else { // b <= a
					w  = log  ( b/(a+b));
					w1 = log1p(-b/(a+b));
				}
			} else {
				w	= b / (a + b);
				w1 = a / (a + b);
			}

			//R_ifDEBUG_printf("a & b very small -> simple ratios (%g,%g)\n", *w,*w1);
			return new double[] {w, w1, 0};
		}
		//ierr = 0;

		if(min(a,b) <= 1.) {
			/*             PROCEDURE FOR a0 <= 1 OR b0 <= 1 */

			do_swap = (x > 0.5);
			if (do_swap) {
				//SET_0_swap
				a0 = b;  x0 = y;
				b0 = a;  y0 = x;
			} else {
				//SET_0_noswap;
				a0 = a;  x0 = x;
				b0 = b;  y0 = y;
			}
			/* now have  x0 <= 1/2 <= y0  (still  x0+y0 == 1) */

			if (b0 < min(eps, eps * a0)) { /* L80: */
				w = fpser(a0, b0, x0, eps, log_p);
				w1 = log_p ? ((w) > -M_LN2 ? log(-expm1(w)) : log1p(-exp(w))) : 0.5 - w + 0.5;
				// goto L_end;
				if (do_swap) { /* swap */
					double t = w; w = w1; w1 = t;
				}
				return new double[] {w, w1, 0};
			}

			if (a0 < min(eps, eps * b0) && b0 * x0 <= 1.) { /* L90: */
				w1 = apser(a0, b0, x0, eps);
				w = 0.5 - w1 + 0.5;
				// goto L_end_from_w1;
				if(log_p) {
					w  = log1p(-w1);
					w1 = log(w1);
				} else {
					w = 0.5 - w1 + 0.5;
				}
				if (do_swap) { /* swap */
					double t = w; w = w1; w1 = t;
				}
				return new double[] {w, w1, 0};
			}

			boolean did_bup = false;
			if (max(a0,b0) > 1.) { /* L20:  min(a,b) <= 1 < max(a,b)  */
				if (b0 <= 1.) {
					kase = 100; // goto L_w_bpser;
				}
				else if (x0 >= 0.29) { /* was 0.3, PR#13786 */
					kase = 110; // goto L_w1_bpser;
				}
				else if (x0 < 0.1 && pow(x0*b0, a0) <= 0.7) {
					kase = 100; //goto L_w_bpser;
				} else if (b0 > 15.) {
					w1 = 0.;
					// goto L131;
					kase = 131;
				}
			} else { /*  a, b <= 1 */
				if (a0 >= min(0.2, b0) || pow(x0, a0) <= 0.9) {
					kase = 100; // goto L_w_bpser;
				}
				else if (x0 >= 0.3) {
					kase = 110; // goto L_w1_bpser;
				}
			}
			if (kase == 0)
			{
				n = 20; //kase = 130; //goto L130;
				w1 = bup(b0, a0, y0, x0, n, eps, false); did_bup = true;
				b0 += n;
			}
			if (kase == 0 || kase == 131) {
				//L131:
				//bgrat(b0, a0, y0, x0, w1, 15*eps, &ierr1, FALSE);
				//goto L_end_from_w1;
				w1 = bgrat(b0, a0, y0, x0, w1, 15*eps, ierr1, false);
				if (w1 == 0 || (0 < w1 && w1 < 1e-310)) { // w1=0 or very close:
					// "almost surely" from underflow, try more: [2013-03-04]
					// FIXME: it is even better to do this in bgrat *directly* at least for the case
					//  !did_bup, thread.e., where *w1 = (0 or -Inf) on entry
					w1 = did_bup // re-do that part on log scale:
						? bup(b0-n, a0, y0, x0, n, eps, true)
						: Double.NEGATIVE_INFINITY;
					w1 = bgrat(b0, a0, y0, x0, w1, 15*eps, ierr1, true);
					//goto L_end_from_w1_log;
					if(log_p) {
						w = ((w1) > -M_LN2 ? log(-expm1(w1)) : log1p(-exp(w1)));
					} else {
						w  = /* 1 - exp(*w1) */ -expm1(w1);
						w1 = exp(w1);
					}
					if (do_swap) { /* swap */
						double t = w; w = w1; w1 = t;
					}
					return new double[] {w, w1, (ierr1[0] > 0) ? 10 + ierr1[0] : 0};
				}
				//if(w1 < 0) MATHLIB_WARNING4("bratio(a=%g, b=%g, x=%g): bgrat() -> w1 = %g", a,b,x, w1);
				//goto L_end_from_w1;
				if(log_p) {
					w  = log1p(-w1);
					w1 = log(w1);
				} else {
					w = 0.5 - w1 + 0.5;
				}
				if (do_swap) { /* swap */
					double t = w; w = w1; w1 = t;
				}
				return new double[] {w, w1, (ierr1[0] > 0) ? 10 + ierr1[0] : 0};
			}
		} else {
			/*             PROCEDURE FOR a0 > 1 AND b0 > 1 */
			if (a > b)
				lambda = (a + b) * y - b;
			else
				lambda = a - (a + b) * x;

			do_swap = (lambda < 0.);
			if (do_swap) {
				lambda = -lambda;
				//SET_0_swap;
				a0 = b;  x0 = y;
				b0 = a;  y0 = x;
			} else {
				//SET_0_noswap;
				a0 = a;  x0 = x;
				b0 = b;  y0 = y;
			}

			if (b0 < 40.) {
				if (b0 * x0 <= 0.7 || (log_p && lambda > 650.))
					kase = 100; // goto L_w_bpser;
				else
					kase = 140; // goto L140;
			}
			else if (a0 > b0) { /* ----  a0 > b0 >= 40  ---- */
				if (b0 <= 100. || lambda > b0 * 0.03) {
					kase = 120; // goto L_bfrac;
				}
			} else if (a0 <= 100.) {
				kase = 120; // goto L_bfrac;
			} else if (lambda > a0 * 0.03) {
				kase = 120; // goto L_bfrac;
			}

			/* else if none of the above    L180: */
			if (kase == 0)
			{
				w = basym(a0, b0, lambda, eps * 100., log_p);
				w1 = log_p ? ((w) > -M_LN2 ? log(-expm1(w)) : log1p(-exp(w))) : 0.5 - w + 0.5;
				// goto L_end_after_log;
				if (do_swap) { /* swap */
					double t = w; w = w1; w1 = t;
				}
				return new double[] {w, w1, 0};
			}
		}

		// EVALUATION OF THE APPROPRIATE ALGORITHM
		int ierr = 0;
		switch(kase)
		{
			case 100:
				w = bpser(a0, b0, x0, eps, log_p);
				w1 = log_p ? ((w) > -M_LN2 ? log(-expm1(w)) : log1p(-exp(w))) : 0.5 - w + 0.5;
				// goto L_end_after_log;
				break;
			case 110:
				w1 = bpser(b0, a0, y0, eps, log_p);
				w  = log_p ? ((w1) > -M_LN2 ? log(-expm1(w1)) : log1p(-exp(w1))) : 0.5 - w1 + 0.5;
				// goto L_end_after_log;
				break;
			case 120:
				w = bfrac(a0, b0, x0, y0, lambda, eps * 15., log_p);
				w1 = log_p ? ((w) > -M_LN2 ? log(-expm1(w)) : log1p(-exp(w))) : 0.5 - w + 0.5;
				// goto L_end_after_log;
				break;
			case 140:
				/* b0 := fractional_part( b0 )  in (0, 1]  */
				n = (int) b0;
				b0 -= n;
				if (b0 == 0.) {
					--n; b0 = 1.;
				}

				w = bup(b0, a0, y0, x0, n, eps, false);
				if (w < DBL_MIN && log_p) {
					b0 += n;
					// goto L100;
					w = bpser(a0, b0, x0, eps, log_p);
					w1 = log_p ? ((w) > -M_LN2 ? log(-expm1(w)) : log1p(-exp(w))) : 0.5 - w + 0.5;
					// goto L_end_after_log;
					break;
				}
				if (x0 <= 0.7) {
					/* log_p :  TODO:  w = bup(.) + bpser(.)  -- not so easy to use log-scale */
					w += bpser(a0, b0, x0, eps, /* log_p = */ false);
					// goto L_end_from_w;
					if(log_p) {
						w1 = log1p(-w);
						w  = log(w);
					} else {
						w1 = 0.5 - w + 0.5;
					}
					break;
				}
				/* L150: */
				if (a0 <= 15.) {
					n = 20;
					w += bup(a0, b0, x0, y0, n, eps, false);
					a0 += n;
				}
				w = bgrat(a0, b0, x0, y0, w, 15*eps, ierr1, false);
				// goto L_end_from_w;
				if(log_p) {
					w1 = log1p(-w);
					w  = log(w);
				} else {
					w1 = 0.5 - w + 0.5;
				}
				if (ierr1[0] > 0) ierr = 10 + ierr1[0];
				break;
			default:
				throw new RuntimeException();
		}
		if (do_swap) { /* swap */
			double t = w; w = w1; w1 = t;
		}
		return new double[] {w, w1, ierr};
	}

	public static final double fpser(double a, double b, double x, double eps, boolean log_p)
	{
		/* ----------------------------------------------------------------------- *
		 *                 EVALUATION OF I (A,B)
		 *                                X
		 *          FOR B < MIN(EPS, EPS*A) AND X <= 0.5
		 * ----------------------------------------------------------------------- */

		double ans, c, s, t, an, tol;

		/* SET  ans := x^a : */
		if (log_p) {
			ans = a * log(x);
		} else if (a > eps * 0.001) {
			t = a * log(x);
			if (t < exparg(1)) { /* exp(t) would underflow */
				return 0.0;
			}
			ans = exp(t);
		} else
			ans = 1.;

		/*                NOTE THAT 1/B(A,B) = B */

		if (log_p)
			ans += log(b) - log(a);
		else
			ans *= b / a;

		tol = eps / a;
		an = a + 1.0;
		t = x;
		s = t / an;
		do {
			an += 1.0;
			t = x * t;
			c = t / an;
			s += c;
		} while (abs(c) > tol);

		if (log_p)
			ans += log1p(a * s);
		else
			ans *= a * s + 1.0;
		return ans;
	} /* fpser */

	public static final double apser(double a, double b, double x, double eps)
	{
		/* -----------------------------------------------------------------------
		 *     apser() yields the incomplete beta ratio  I_{1-x}(b,a)  for
		 *     a <= min(eps,eps*b), b*x <= 1, and x <= 0.5,  thread.e., a is very small.
		 *     Use only if above inequalities are satisfied.
		 * ----------------------------------------------------------------------- */

		final double g = .577215664901533;

		double tol, c, j, s, t, aj;
		double bx = b * x;

		t = x - bx;
		if (b * eps <= 0.02)
			c = log(x) + psi(b) + g + t;
		else
			c = log(bx) + g + t;

		tol = eps * 5.0 * abs(c);
		j = 1.;
		s = 0.;
		do {
			j += 1.0;
			t *= x - bx / j;
			aj = t / j;
			s += aj;
		} while (abs(aj) > tol);

		return -a * (c + s);
	} /* apser */

	public static final double bpser(double a, double b, double x, double eps, boolean log_p)
	{
		/* -----------------------------------------------------------------------
		 * Power SERies expansion for evaluating I_x(a,b) when
		 *	       b <= 1 or b*x <= 0.7.   eps is the tolerance used.
		 * ----------------------------------------------------------------------- */

		int i, m;
		double ans, c, n, t, u, w, z, a0, b0, apb, tol, sum;

		if (x == 0.) {
			return log_p ? Double.NEGATIVE_INFINITY : 0.;
		}
		/* ----------------------------------------------------------------------- */
		/*	      compute the factor  x^a/(a*Beta(a,b)) */
		/* ----------------------------------------------------------------------- */
		a0 = min(a,b);
		if (a0 >= 1.0) { /*		 ------	 1 <= a0 <= b0  ------ */
			z = a * log(x) - betaln(a, b);
			ans = log_p ? z - log(a) : exp(z) / a;
		}
		else {
			b0 = max(a,b);
			if (b0 < 8.0) {
				if (b0 <= 1.0) { /*	 ------	 a0 < 1	 and  b0 <= 1  ------ */
					if(log_p) {
						ans = a * log(x);
					} else {
						ans = pow(x, a);
						if (ans == 0.) /* once underflow, always underflow .. */
							return ans;
					}
					apb = a + b;
					if (apb > 1.0) {
						u = a + b - 1.;
						z = (gam1(u) + 1.0) / apb;
					} else {
						z = gam1(apb) + 1.0;
					}
					c = (gam1(a) + 1.0) * (gam1(b) + 1.0) / z;

					if(log_p) /* FIXME ? -- improve quite a bit for c ~= 1 */
						ans += log(c * (b / apb));
					else
						ans *=  c * (b / apb);
				} else { /* 	------	a0 < 1 < b0 < 8	 ------ */
					u = gamln1(a0);
					m = (int) (b0 - 1.0);
					if (m >= 1) {
						c = 1.0;
						for (i = 1; i <= m; ++i) {
							b0 += -1.0;
							c *= b0 / (a0 + b0);
						}
						u += log(c);
					}

					z = a * log(x) - u;
					b0 += -1.0;
					apb = a0 + b0;
					if (apb > 1.0) {
						u = a0 + b0 - 1.;
						t = (gam1(u) + 1.0) / apb;
					} else {
						t = gam1(apb) + 1.0;
					}

					if(log_p) /* FIXME? potential for improving log(t) */
						ans = z + log(a0 / a) + log1p(gam1(b0)) - log(t);
					else
						ans = exp(z) * (a0 / a) * (gam1(b0) + 1.0) / t;
				}

			} else { /* 		------  a0 < 1 < 8 <= b0  ------ */
				u = gamln1(a0) + algdiv(a0, b0);
				z = a * log(x) - u;
				if(log_p)
					ans = z + log(a0 / a);
				else
					ans = a0 / a * exp(z);
			}
		}

	    if (ans == (log_p ? Double.NEGATIVE_INFINITY : 0.) || (!log_p && a <= eps * 0.1)) {
	    	return ans;
        }

		/* ----------------------------------------------------------------------- */
		/*		       COMPUTE THE SERIES */
		/* ----------------------------------------------------------------------- */
		sum = 0.;
		n = 0.;
		c = 1.;
		tol = eps / a;

		do {
			n += 1.;
			c *= (0.5 - b / n + 0.5) * x;
			w = c / (a + n);
			sum += w;
		} while (n < 1e7 && abs(w) > tol);
		if(abs(w) > tol) { // the series did not converge (in time)
			// warn only when the result seems to matter:
			if(( log_p && !(a*sum > -1. && abs(log1p(a * sum)) < eps*abs(ans))) ||
					(!log_p && abs(a*sum + 1) != 1.))
				System.err.println(String.format(
						" bpser(a=%g, b=%g, x=%g,...) did not converge (n=1e7, |w|/tol=%g > 1; A=%g)",
						a,b,x, abs(w)/tol, ans));
		}

		if(log_p) {
			if (a*sum > -1.0) ans += log1p(a * sum);
			else ans = Double.NEGATIVE_INFINITY;
		} else
			ans *= a * sum + 1.0;
		return ans;
	} /* bpser */

	public static final double bup(double a, double b, double x, double y, int n, double eps, boolean give_log)
	{
		/* ----------------------------------------------------------------------- */
		/*     EVALUATION OF I_x(A,B) - I_x(A+N,B) WHERE N IS A POSITIVE INT. */
		/*     EPS IS THE TOLERANCE USED. */
		/* ----------------------------------------------------------------------- */

		/* System generated locals */
		double ret_val;

		/* Local variables */
		int i, k, mu, nm1;
		double d, l, r, t, w;
		double ap1, apb;

		/*          OBTAIN THE SCALING FACTOR EXP(-MU) AND */
		/*             EXP(MU)*(X**A*Y**B/BETA(A,B))/A */

		apb = a + b;
		ap1 = a + 1.0;
		if (n > 1 && a >= 1. && apb >= ap1 * 1.1)
		{
			mu = (int) abs(exparg(1));
			k = (int) exparg(0);
			if (mu > k) mu = k;
			t = mu;
			d = exp(-t);
		} else {
			mu = 0;
			d = 1.0;
		}

		ret_val = give_log
			? brcmp1(mu, a, b, x, y, true) - log(a)
			: brcmp1(mu, a, b, x, y, false)  / a;
		if (n == 1 || (give_log && ret_val == Double.NEGATIVE_INFINITY) || (!give_log && ret_val == 0.))
			return ret_val;
		nm1 = n - 1;
		w = d;

		/*          LET K BE THE INDEX OF THE MAXIMUM TERM */
		boolean skipToL40 = false;
		k = 0;
		if (b <= 1.0) {
			skipToL40 = true; //goto L40;
		} else if (y > 1e-4) {
			r = (b - 1.0) * x / y - a;
			if (r >= 1.0)
			{
				t = k = nm1;
				if (r < t)
					k = (int) r;
			}
			else
				skipToL40 = true; //goto L40;
		} else k = nm1;

		if (!skipToL40)
		{
			// L30:
			/*          ADD THE INCREASING TERMS OF THE SERIES */
			for (i = 1; i <= k; ++i) {
				l = i - 1;
				d = (apb + l) / (ap1 + l) * x * d;
				w += d;
			}
			if (k == nm1) // goto L50
				return give_log ? ret_val + log(w) : ret_val * w;
		}

		// L40:
		// ADD THE REMAINING TERMS OF THE SERIES */
		for (i = k+1; i <= nm1; ++i) {
			l = i - 1;
			d = (apb + l) / (ap1 + l) * x * d;
			w += d;
			if (d <= eps * w) /* relative convergence (eps) */
				break;
		}

		// L50
		/*               TERMINATE THE PROCEDURE */
		return give_log ? ret_val + log(w) : ret_val * w;
	} /* bup */

	public static final double bfrac(double a, double b, double x, double y, double lambda, double eps, boolean log_p)
	{
		/* -----------------------------------------------------------------------
	       Continued fraction expansion for I_x(a,b) when a, b > 1.
	       It is assumed that  lambda = (a + b)*y - b.
	   -----------------------------------------------------------------------*/

		double c, e, n, p, r, s, t, w, c0, c1, r0, an, bn, yp1, anp1, bnp1,
		beta, alpha;

		double brc = brcomp(a, b, x, y, log_p);

		if (!log_p && brc == 0.) /* already underflowed to 0 */
			return 0.;

		c = lambda + 1.0;
		c0 = b / a;
		c1 = 1.0 / a + 1.0;
		yp1 = y + 1.0;

		n = 0.0;
		p = 1.0;
		s = a + 1.0;
		an = 0.0;
		bn = 1.0;
		anp1 = 1.0;
		bnp1 = c / c1;
		r = c1 / c;

		/*        CONTINUED FRACTION CALCULATION */

		do {
			n += 1.0;
			t = n / a;
			w = n * (b - n) * x;
			e = a / s;
			alpha = p * (p + c0) * e * e * (w * x);
			e = (t + 1.0) / (c1 + t + t);
			beta = n + w / s + e * (c + n * yp1);
			p = t + 1.0;
			s += 2.0;

			/* update an, bn, anp1, and bnp1 */

			t = alpha * an + beta * anp1;
			an = anp1;
			anp1 = t;
			t = alpha * bn + beta * bnp1;
			bn = bnp1;
			bnp1 = t;

			r0 = r;
			r = anp1 / bnp1;
			if (abs(r - r0) <= eps * r)
				break;

			/* rescale an, bn, anp1, and bnp1 */

			an /= bnp1;
			bn /= bnp1;
			anp1 = r;
			bnp1 = 1.0;
		} while (true);

		return (log_p ? brc + log(r) : brc * r);
	} /* bfrac */

	public static final double brcomp(double a, double b, double x, double y, boolean log_p)
	{
		/* -----------------------------------------------------------------------
		 *		 Evaluation of x^a * y^b / Beta(a,b)
		 * ----------------------------------------------------------------------- */

		int i, n;
		double c, e, u, v, z, a0, b0, apb, lnx, lny, h, t, x0, y0;
		double lambda;


		if (x == 0.0 || y == 0.0) {
			return log_p ? Double.NEGATIVE_INFINITY : 0.;
		}
		a0 = min(a, b);
		//if (a0 >= 8.0)
		//	goto L100;

		if (a0 < 8.0)
		{
			if (x <= .375) {
				lnx = log(x);
				lny = alnrel(-x);
			}
			else {
				if (y > .375) {
					lnx = log(x);
					lny = log(y);
				} else {
					lnx = alnrel(-y);
					lny = log(y);
				}
			}

			z = a * lnx + b * lny;
			if (a0 >= 1.) {
				z -= betaln(a, b);
				return (log_p ? z : exp(z));
			}

			/* ----------------------------------------------------------------------- */
			/*		PROCEDURE FOR a < 1 OR b < 1 */
			/* ----------------------------------------------------------------------- */

			b0 = max(a, b);
			if (b0 >= 8.0) { /* L80: */
				u = gamln1(a0) + algdiv(a0, b0);

				return (log_p ? log(a0) + (z - u)  : a0 * exp(z - u));
			}
			/* else : */

			if (b0 <= 1.0) { /*		algorithm for max(a,b) = b0 <= 1 */

				double e_z = (log_p ? z : exp(z));

				if (!log_p && e_z == 0.0) /* exp() underflow */
					return 0.;

				apb = a + b;
				if (apb > 1.0) {
					u = a + b - 1.;
					z = (gam1(u) + 1.0) / apb;
				} else {
					z = gam1(apb) + 1.0;
				}

				c = (gam1(a) + 1.0) * (gam1(b) + 1.0) / z;
				/* FIXME? log(a0*c)= log(a0)+ log(c) and that is improvable */
				return (log_p
					? e_z + log(a0 * c) - log1p(a0/b0)
					: e_z * (a0 * c) / (a0 / b0 + 1.0));
			}
			/* else : ALGORITHM FOR 1 < b0 < 8 */

			u = gamln1(a0);
			n = (int) (b0 - 1.0);
			if (n >= 1) {
				c = 1.0;
				for (i = 1; i <= n; ++i) {
					b0 += -1.0;
					c *= b0 / (a0 + b0);
				}
				u = log(c) + u;
			}
			z -= u;
			b0 += -1.0;
			apb = a0 + b0;
			if (apb > 1.0) {
				u = a0 + b0 - 1.;
				t = (gam1(u) + 1.0) / apb;
			} else {
				t = gam1(apb) + 1.0;
			}

			return (log_p
				? log(a0) + z + log1p(gam1(b0))  - log(t)
				: a0 * exp(z) * (gam1(b0) + 1.0) / t);
		}

		// L100:
		/* ----------------------------------------------------------------------- */
		/*		PROCEDURE FOR A >= 8 AND B >= 8 */
		/* ----------------------------------------------------------------------- */
		if (a <= b) {
			h = a / b;
			x0 = h / (h + 1.0);
			y0 = 1.0 / (h + 1.0);
			lambda = a - (a + b) * x;
		} else {
			h = b / a;
			x0 = 1.0 / (h + 1.0);
			y0 = h / (h + 1.0);
			lambda = (a + b) * y - b;
		}

		e = -lambda / a;
		if (abs(e) > .6)
			u = e - log(x / x0);
		else
			u = rlog1(e);

		e = lambda / b;
		if (abs(e) <= .6)
			v = rlog1(e);
		else
			v = e - log(y / y0);

		z = log_p ? -(a * u + b * v) : exp(-(a * u + b * v));

		return(log_p
			? -M_LN_SQRT_2PI + .5*log(b * x0) + z - bcorr(a,b)
			: M_1_SQRT_2PI * sqrt(b * x0) * z * exp(-bcorr(a, b)));
	} /* brcomp */

	public static final double brcmp1(int mu, double a, double b, double x, double y, boolean give_log)
	{
		/* -----------------------------------------------------------------------
		 *          EVALUATION OF  EXP(MU) * (X^A * Y^B / BETA(A,B))
		 * ----------------------------------------------------------------------- */

		double c, t, u, v, z, a0, b0, apb;

		a0 = min(a,b);
		if (a0 < 8.0) {
			double lnx, lny;
			if (x <= .375) {
				lnx = log(x);
				lny = alnrel(-x);
			} else if (y > .375) {
				// L11:
				lnx = log(x);
				lny = log(y);
			} else {
				lnx = alnrel(-y);
				lny = log(y);
			}

			// L20:
			z = a * lnx + b * lny;
			if (a0 >= 1.0) {
				z -= betaln(a, b);
				return esum(mu, z, give_log);
			}
			// else :
			/* ----------------------------------------------------------------------- */
			/*              PROCEDURE FOR A < 1 OR B < 1 */
			/* ----------------------------------------------------------------------- */
			// L30:
			b0 = max(a,b);
			if (b0 >= 8.0) {
				/* L80:                  ALGORITHM FOR b0 >= 8 */
				u = gamln1(a0) + algdiv(a0, b0);
				return give_log
					? log(a0) + esum(mu, z - u, true)
					:     a0  * esum(mu, z - u, false);

			} else if (b0 <= 1.0) {
				//                   a0 < 1, b0 <= 1
				double ans = esum(mu, z, give_log);
				if (ans == (give_log ? Double.NEGATIVE_INFINITY : 0.))
					return ans;

				apb = a + b;
				if (apb > 1.0) {
					// L40:
					u = a + b - 1.;
					z = (gam1(u) + 1.0) / apb;
				} else {
					z = gam1(apb) + 1.0;
				}
				// L50:
				c = give_log
					? log1p(gam1(a)) + log1p(gam1(b)) - log(z)
					: (gam1(a) + 1.0) * (gam1(b) + 1.0) / z;
				return give_log
					? ans + log(a0) + c - log1p(a0 / b0)
					: ans * (a0 * c) / (a0 / b0 + 1.0);
			}
			// else:               algorithm for	a0 < 1 < b0 < 8
			// L60:
			u = gamln1(a0);
			int n = (int)(b0 - 1.0);
			if (n >= 1) {
				c = 1.0;
				for (int i = 1; i <= n; ++i) {
					b0 += -1.0;
					c *= b0 / (a0 + b0);
					/* L61: */
				}
				u += log(c); // TODO?: log(c) = log( prod(...) ) =  sum( log(...) )
			}
			// L70:
			z -= u;
			b0 += -1.0;
			apb = a0 + b0;
			if (apb > 1.) {
				// L71:
				t = (gam1(apb - 1.) + 1.0) / apb;
			} else {
				t = gam1(apb) + 1.0;
			}
			// L72:
			return give_log
				? log(a0)+ esum(mu, z, true) + log1p(gam1(b0)) - log(t) // TODO? log(t) = log1p(..)
				:     a0 * esum(mu, z, false) * (gam1(b0) + 1.0) / t;

		} else {

			/* ----------------------------------------------------------------------- */
			/*              PROCEDURE FOR A >= 8 AND B >= 8 */
			/* ----------------------------------------------------------------------- */
			// L100:
			double h, x0, y0, lambda;
			if (a > b) {
				// L101:
				h = b / a;
				x0 = 1.0 / (h + 1.0);// => lx0 := log(x0) = 0 - log1p(h)
				y0 = h / (h + 1.0);
				lambda = (a + b) * y - b;
			} else {
				h = a / b;
				x0 = h / (h + 1.0);  // => lx0 := log(x0) = - log1p(1/h)
				y0 = 1.0 / (h + 1.0);
				lambda = a - (a + b) * x;
			}
			double lx0 = -log1p(b/a); // in both cases

			// L110:
			double e = -lambda / a;
			if (abs(e) > 0.6) {
				// L111:
				u = e - log(x / x0);
			} else {
				u = rlog1(e);
			}

			// L120:
			e = lambda / b;
			if (abs(e) > 0.6) {
				// L121:
				v = e - log(y / y0);
			} else {
				v = rlog1(e);
			}

			// L130:
			z = esum(mu, -(a * u + b * v), give_log);
			return give_log
				? log(M_1_SQRT_2PI)+ (log(b) + lx0)/2. + z      - bcorr(a, b)
				:     M_1_SQRT_2PI * sqrt(b * x0)      * z * exp(-bcorr(a, b));
		}
	} /* brcmp1 */

	public static final double bgrat(double a, double b, double x, double y, double w, double eps, int[] ierr, boolean log_w)
	{
		/* -----------------------------------------------------------------------
		 *     Asymptotic Expansion for I_x(A,B)  when a is larger than b.
		 *     The result of the expansion is added to w.
		 *     It is assumed a >= 15 and b <= 1.
		 *     eps is the tolerance used.
		 *     ierr is a variable that reports the status of the results.
		 * ----------------------------------------------------------------------- */
		final int n_terms_bgrat = 30;
		double c[] = new double[n_terms_bgrat], d[] = new double[n_terms_bgrat];
		double bm1 = b - 0.5 - 0.5,
			nu = a + bm1 * 0.5, /* nu = a + (b-1)/2 =: T, in (9.1) of Didonato & Morris(1992), p.362 */
			lnx = (y > 0.375) ? log(x) : alnrel(-y),
			z = -nu * lnx; // z =: u in (9.1) of D.&M.(1992)

		if (b * z == 0.) { /* should *never* happen */
			/* L_Error:    THE EXPANSION CANNOT BE COMPUTED */
			ierr[0] = 1;
			return w;
		}

		/*                 COMPUTATION OF THE EXPANSION */
		double
			/* r1 = b * (gam1(b) + 1.0) * exp(b * log(z)),// = b/gamma(b+1) z^b = z^b / gamma(b)
			 * set r := exp(-z) * z^b / gamma(b) ;
			 *          gam1(b) = 1/gamma(b+1) - 1 , b in [-1/2, 3/2] */
			// exp(a*lnx) underflows for large (a * lnx); e.g. large a ==> using log_r := log(r):
			// r = r1 * exp(a * lnx) * exp(bm1 * 0.5 * lnx);
			// log(r)=log(b) + log1p(gam1(b)) + b * log(z) + (a * lnx) + (bm1 * 0.5 * lnx),
			log_r = log(b) + log1p(gam1(b)) + b * log(z) + nu * lnx,
			// FIXME work with  log_u = log(u)  also when log_p=FALSE  (??)
			// u is 'factored out' from the expansion {and multiplied back, at the end}:
			log_u = log_r - (algdiv(b, a) + b * log(nu)),// algdiv(b,a) = log(gamma(a)/gamma(a+b))
			/* u = (log_p) ? log_r - u : exp(log_r-u); // =: M  in (9.2) of {reference above} */
			/* u = algdiv(b, a) + b * log(nu);// algdiv(b,a) = log(gamma(a)/gamma(a+b)) */
			// u = (log_p) ? log_u : exp(log_u); // =: M  in (9.2) of {reference above}
			u = exp(log_u);

		if (log_u == Double.NEGATIVE_INFINITY) {
			/* L_Error:    THE EXPANSION CANNOT BE COMPUTED */
			ierr[0] = 2;
			return w;
		}

		boolean u_0 = (u == 0.); // underflow --> do work with log(u) == log_u !
		double l = // := *w/u .. but with care: such that it also works when u underflows to 0:
			log_w
			? ((w == Double.NEGATIVE_INFINITY) ? 0. : exp(  w    - log_u))
			: ((w == 0.)        ? 0. : exp(log(w) - log_u));

		double
			q_r = grat_r(b, z, log_r, eps), // = q/r of former grat1(b,z, r, &p, &q)
			v = 0.25 / (nu * nu),
			t2 = lnx * 0.25 * lnx,
			j = q_r,
			sum = j,
			t = 1.0, cn = 1.0, n2 = 0.;
		for (int n = 1; n <= n_terms_bgrat; ++n) {
			double bp2n = b + n2;
			j = (bp2n * (bp2n + 1.0) * j + (z + bp2n + 1.0) * t) * v;
			n2 += 2.;
			t *= t2;
			cn /= n2 * (n2 + 1.);
			int nm1 = n - 1;
			c[nm1] = cn;
			double s = 0.0;
			if (n > 1) {
				double coef = b - n;
				for (int i = 1; i <= nm1; ++i) {
					s += coef * c[i - 1] * d[nm1 - i];
					coef += b;
				}
			}
			d[nm1] = bm1 * cn + s / n;
			double dj = d[nm1] * j;
			sum += dj;
			if (sum <= 0.0) {
				/* L_Error:    THE EXPANSION CANNOT BE COMPUTED */
				ierr[0] = 3;
				return w;
			}
			if (abs(dj) <= eps * (sum + l)) {
				break;
			} else if(n == n_terms_bgrat) { // never? ; please notify R-core if seen:
				ierr[0] = 4;
				//MATHLIB_WARNING4("bgrat(a=%g, b=%g, x=%g,..): did *not* converge; rel.err=%g",
				//a,b,x, abs(dj) /(sum + l));
			}
		}

		/*                    ADD THE RESULTS TO W */
		ierr[0] = 0;
		return log_w // *w is in log space already:
			? logspace_add(w, log_u + log(sum))
			: w + (u_0? exp(log_u + log(sum)) : u * sum);
	} /* bgrat */

	/* -----------------------------------------------------------------------
	 *        Scaled complement of incomplete gamma ratio function
	 *                   grat_r(a,x,r) :=  Q(a,x) / r
	 * where
	 *               Q(a,x) = pgamma(x,a, lower.tail=FALSE)
	 *     and            r = e^(-x)* x^a / Gamma(a) ==  exp(log_r)
	 *
	 *     It is assumed that a <= 1.  eps is the tolerance to be used.
	 * ----------------------------------------------------------------------- */
	public static final double grat_r(double a, double x, double log_r, double eps)
	{
		if (a * x == 0.0) { /* L130: */
			if (x <= a) {
				/* L100: */ return exp(-log_r);
			} else {
				/* L110:*/  return 0.;
			}
		}
		else if (a == 0.5) { // e.g. when called from pt()
			/* L120: */
			if (x < 0.25) {
				double p = erf__(sqrt(x));
				return (0.5 - p + 0.5)*exp(-log_r);

			} else { // 2013-02-27: improvement for "large" x: direct computation of q/r:
				double sx = sqrt(x),
						q_r = erfc1(1, sx)/sx * M_SQRT_PI;
				return q_r;
			}

		} else if (x < 1.1) { /* L10:  Taylor series for  P(a,x)/x^a */

			double an = 3.,
					c = x,
					sum = x / (a + 3.0),
					tol = eps * 0.1 / (a + 1.0), t;
			do {
				an += 1.;
				c *= -(x / an);
				t = c / (a + an);
				sum += t;
			} while (abs(t) > tol);

			double j = a * x * ((sum/6. - 0.5/(a + 2.)) * x + 1./(a + 1.)),
					z = a * log(x),
					h = gam1(a),
					g = h + 1.0;

			if ((x >= 0.25 && (a < x / 2.59)) || (z > -0.13394)) {
				// L40:
					double l = rexpm1(z),
					q = ((l + 0.5 + 0.5) * j - l) * g - h;
					if (q <= 0.0) {
						/* L110:*/ return 0.;
					} else {
						return q * exp(-log_r);
					}

			} else {
				double p = exp(z) * g * (0.5 - j + 0.5);
				return /* q/r = */ (0.5 - p + 0.5) * exp(-log_r);
			}

		} else {
			/* L50: ----  (x >= 1.1)  ---- Continued Fraction Expansion */

			double a2n_1 = 1.0,
					a2n = 1.0,
					b2n_1 = x,
					b2n = x + (1.0 - a),
					c = 1., am0, an0;

			do {
				a2n_1 = x * a2n + c * a2n_1;
				b2n_1 = x * b2n + c * b2n_1;
				am0 = a2n_1 / b2n_1;
				c += 1.;
				double c_a = c - a;
				a2n = a2n_1 + c_a * a2n;
				b2n = b2n_1 + c_a * b2n;
				an0 = a2n / b2n;
			} while (abs(an0 - am0) >= eps * an0);

			return /* q/r = (r * an0)/r = */ an0;
		}
	} /* grat_r */

	public static final double basym(double a, double b, double lambda, double eps, boolean log_p)
	{
		/* ----------------------------------------------------------------------- */
		/*     ASYMPTOTIC EXPANSION FOR I_x(A,B) FOR LARGE A AND B. */
		/*     LAMBDA = (A + B)*Y - B  AND EPS IS THE TOLERANCE USED. */
		/*     IT IS ASSUMED THAT LAMBDA IS NONNEGATIVE AND THAT */
		/*     A AND B ARE GREATER THAN OR EQUAL TO 15. */
		/* ----------------------------------------------------------------------- */


		/* ------------------------ */
		/*     ****** NUM IS THE MAXIMUM VALUE THAT N CAN TAKE IN THE DO LOOP */
		/*            ENDING AT STATEMENT 50. IT IS REQUIRED THAT NUM BE EVEN. */
		//#define num_IT 20
		final int
		num_IT = 20,
		nip1 = num_IT+1;
		/*            THE ARRAYS A0, B0, C, D HAVE DIMENSION NUM + 1. */
		final double e0 = 1.12837916709551;/* e0 == 2/sqrt(pi) */
		final double e1 = .353553390593274;/* e1 == 2^(-3/2)   */
		final double ln_e0 = 0.120782237635245; /* == ln(e0) */

		double
		a0[] = new double[nip1],
		b0[] = new double[nip1],
		c[] = new double[nip1],
		d[] = new double[nip1];
		double f, h, r, s, t, u, w, z, j0, j1, h2, r0, r1, t0, t1, w0, z0, z2, hn, zn;
		double sum, znm1, bsum, dsum;

		int i, j, m, n, im1, mm1, np1, mmj;

		/* ------------------------ */

		f = a * rlog1(-lambda/a) + b * rlog1(lambda/b);
		if(log_p)
			t = -f;
		else {
			t = exp(-f);
			if (t == 0.0) {
				return 0; /* once underflow, always underflow .. */
			}
		}
		z0 = sqrt(f);
		z = z0 / e1 * 0.5;
		z2 = f + f;

		if (a < b) {
			h = a / b;
			r0 = 1.0 / (h + 1.0);
			r1 = (b - a) / b;
			w0 = 1.0 / sqrt(a * (h + 1.0));
		} else {
			h = b / a;
			r0 = 1.0 / (h + 1.0);
			r1 = (b - a) / a;
			w0 = 1.0 / sqrt(b * (h + 1.0));
		}

		a0[0] = r1 * .66666666666666663;
		c[0] = a0[0] * -0.5;
		d[0] = -c[0];
		j0 = 0.5 / e0 * erfc1(1, z0);
		j1 = e1;
		sum = j0 + d[0] * w0 * j1;

		s = 1.0;
		h2 = h * h;
		hn = 1.0;
		w = w0;
		znm1 = z;
		zn = z2;
		for (n = 2; n <= num_IT; n += 2) {
			hn *= h2;
			a0[n - 1] = r0 * 2.0 * (h * hn + 1.0) / (n + 2.0);
			np1 = n + 1;
			s += hn;
			a0[np1 - 1] = r1 * 2.0 * s / (n + 3.0);

			for (i = n; i <= np1; ++i) {
				r = (i + 1.0) * -0.5;
				b0[0] = r * a0[0];
				for (m = 2; m <= i; ++m) {
					bsum = 0.0;
					mm1 = m - 1;
					for (j = 1; j <= mm1; ++j) {
						mmj = m - j;
						bsum += (j * r - mmj) * a0[j - 1] * b0[mmj - 1];
					}
					b0[mm1] = r * a0[mm1] + bsum / m;
				}
				im1 = i - 1;
				c[im1] = b0[im1] / (i + 1.0);

				dsum = 0.0;
				for (j = 1; j <= im1; ++j) {
					dsum += d[im1 - j] * c[j - 1];
				}
				d[im1] = -(dsum + c[im1]);
			}

			j0 = e1 * znm1 + (n - 1.0) * j0;
			j1 = e1 * zn + n * j1;
			znm1 = z2 * znm1;
			zn = z2 * zn;
			w *= w0;
			t0 = d[n - 1] * w * j0;
			w *= w0;
			t1 = d[np1 - 1] * w * j1;
			sum += t0 + t1;
			if (abs(t0) + abs(t1) <= eps * sum) {
				break;
			}
		}

		if(log_p)
			return ln_e0 + t - bcorr(a, b) + log(sum);

		u = exp(-bcorr(a, b));
		return e0 * t * u * sum;
	} /* basym_ */

	/**
	 * --------------------------------------------------------------------
	 * <pre>
	 *     IF L = 0 THEN  EXPARG(L) = THE LARGEST POSITIVE W FOR WHICH
	 *     EXP(W) CAN BE COMPUTED.
	 *     IF L IS NONZERO THEN  EXPARG(L) = THE LARGEST NEGATIVE W FOR
	 *     WHICH THE COMPUTED VALUE OF EXP(W) IS NONZERO.
	 *     NOTE... ONLY AN APPROXIMATE VALUE FOR EXPARG(L) IS NEEDED.
	 *  </pre>
	 * --------------------------------------------------------------------
	 */
	public static final double exparg(int l)
	{
		final double lnb = .69314718055995;
		int m = l == 0 ? DBL_MAX_EXP : DBL_MIN_EXP - 1;
		return m * lnb * .99999;
	} /* exparg */

	/**
	 * -----------------------------------------------------------------------
	 *                    EVALUATION OF EXP(MU + X)
	 * -----------------------------------------------------------------------
	 */
	public static final double esum(int mu, double x, boolean give_log)
	{
	    if(give_log) return x + (double) mu;

        // else :
	    double w = mu + x;
	    if (x > 0.0) {
	    	if (mu > 0 || w < 0.0)  return exp((double) mu) * exp(x);
	    }
	    else { /* x <= 0 */
	    	if (mu < 0 || w > 0.0)  return exp((double) mu) * exp(x);
	    }
	    return exp(w);
	} /* esum */

	/**
	 * -----------------------------------------------------------------------
	 *            EVALUATION OF THE FUNCTION EXP(X) - 1
	 * -----------------------------------------------------------------------
	 */
	public static final double rexpm1(double x)
	{
		final double p1 = 9.14041914819518e-10;
		final double p2 = .0238082361044469;
		final double q1 = -.499999999085958;
		final double q2 = .107141568980644;
		final double q3 = -.0119041179760821;
		final double q4 = 5.95130811860248e-4;

		if (abs(x) <= 0.15) {
			return x * (((p2 * x + p1) * x + 1.0) /
					((((q4 * x + q3) * x + q2) * x + q1) * x + 1.0));
		}
		/* |x| > 0.15 : */
		double w = exp(x);
		if (x > 0.0)
			return w * (0.5 - 1.0 / w + 0.5);
		return w - 0.5 - 0.5;
	} /* rexpm1 */

	/**
	 * -----------------------------------------------------------------------
	 *            Evaluation of the function ln(1 + a)
	 * -----------------------------------------------------------------------
	 */
	public static final double alnrel(double a)
	{
		if (abs(a) > 0.375) return log(1. + a);
		final double
		p1 = -1.29418923021993,
		p2 = .405303492862024,
		p3 = -.0178874546012214,
		q1 = -1.62752256355323,
		q2 = .747811014037616,
		q3 = -.0845104217945565;

		double t, t2, w;
		t = a / (a + 2.0);
		t2 = t * t;
		w = (((p3 * t2 + p2) * t2 + p1) * t2 + 1.) /
		(((q3 * t2 + q2) * t2 + q1) * t2 + 1.);
		return t * 2.0 * w;
	} /* alnrel */

	/**
	 * -----------------------------------------------------------------------
	 *             Evaluation of the function  x - ln(1 + x)
	 * -----------------------------------------------------------------------
	 */
	public static final double rlog1(double x)
	{
		final double a = .0566749439387324;
		final double b = .0456512608815524;
		final double p0 = .333333333333333;
		final double p1 = -.224696413112536;
		final double p2 = .00620886815375787;
		final double q1 = -1.27408923933623;
		final double q2 = .354508718369557;

		double h, r, t, w, w1;

		if (x < -0.39 || x > 0.57) { /* direct evaluation */
			w = x + 0.5 + 0.5;
			return x - log(w);
		}
		/* else */
		if (x < -0.18) { /* L10: */
			h = x + .3;
			h /= .7;
			w1 = a - h * .3;
		}
		else if (x > 0.18) { /* L20: */
			h = x * .75 - .25;
			w1 = b + h / 3.0;
		}
		else { /*		Argument Reduction */
			h = x;
			w1 = 0.0;
		}

		/* L30:              	Series Expansion */

		r = h / (h + 2.0);
		t = r * r;
		w = ((p2 * t + p1) * t + p0) / ((q2 * t + q1) * t + 1.0);
		return t * 2.0 * (1.0 / (1.0 - r) - r * w) + w1;
	} /* rlog1 */

	/**
	 * -----------------------------------------------------------------------
	 *             EVALUATION OF THE REAL ERROR FUNCTION
	 * -----------------------------------------------------------------------
	 */
	public static final double erf__(double x)
	{

		/* Initialized data */

		final double c = .564189583547756;
		final double a[] = { 7.7105849500132e-5,-.00133733772997339,
				.0323076579225834,.0479137145607681,.128379167095513 };
		final double b[] = { .00301048631703895,.0538971687740286,
				.375795757275549 };
		final double p[] = { -1.36864857382717e-7,.564195517478974,
				7.21175825088309,43.1622272220567,152.98928504694,
				339.320816734344,451.918953711873,300.459261020162 };
		final double q[] = { 1.,12.7827273196294,77.0001529352295,
				277.585444743988,638.980264465631,931.35409485061,
				790.950925327898,300.459260956983 };
		final double r[] = { 2.10144126479064,26.2370141675169,
				21.3688200555087,4.6580782871847,.282094791773523 };
		final double s[] = { 94.153775055546,187.11481179959,
				99.0191814623914,18.0124575948747 };

		/* System generated locals */
		double ret_val;

		/* Local variables */
		double t, x2, ax, bot, top;

		ax = abs(x);
		if (ax <= 0.5) {
			t = x * x;
			top = (((a[0] * t + a[1]) * t + a[2]) * t + a[3]) * t + a[4] + 1.0;
			bot = ((b[0] * t + b[1]) * t + b[2]) * t + 1.0;

			return x * (top / bot);
		}
		/* else: ax > 0.5 */

		if (ax <= 4.) { /*  ax in (0.5, 4] */

			top = ((((((p[0] * ax + p[1]) * ax + p[2]) * ax + p[3]) * ax + p[4]) * ax
					+ p[5]) * ax + p[6]) * ax + p[7];
			bot = ((((((q[0] * ax + q[1]) * ax + q[2]) * ax + q[3]) * ax + q[4]) * ax
					+ q[5]) * ax + q[6]) * ax + q[7];
			ret_val = 0.5 - exp(-x * x) * top / bot + 0.5;
			if (x < 0.0) {
				ret_val = -ret_val;
			}
			return ret_val;
		}

		/* else: ax > 4 */

		if (ax >= 5.8) {
			return x > 0 ? 1 : -1;
		}
		x2 = x * x;
		t = 1.0 / x2;
		top = (((r[0] * t + r[1]) * t + r[2]) * t + r[3]) * t + r[4];
		bot = (((s[0] * t + s[1]) * t + s[2]) * t + s[3]) * t + 1.0;
		t = (c - top / (x2 * bot)) / ax;
		ret_val = 0.5 - exp(-x2) * t + 0.5;
		if (x < 0.0) {
			ret_val = -ret_val;
		}
		return ret_val;

	} /* erf */

	/**
	 * -----------------------------------------------------------------------
	 *         EVALUATION OF THE COMPLEMENTARY ERROR FUNCTION
	 *
	 *          ERFC1(IND,X) = ERFC(X)            IF IND = 0
	 *          ERFC1(IND,X) = EXP(X*X)*ERFC(X)   OTHERWISE
	 * -----------------------------------------------------------------------
	 */
	public static final double erfc1(int ind, double x)
	{
		final double c = .564189583547756;
		final double a[] = { 7.7105849500132e-5,-.00133733772997339,
				.0323076579225834,.0479137145607681,.128379167095513 };
		final double b[] = { .00301048631703895,.0538971687740286,
				.375795757275549 };
		final double p[] = { -1.36864857382717e-7,.564195517478974,
				7.21175825088309,43.1622272220567,152.98928504694,
				339.320816734344,451.918953711873,300.459261020162 };
		final double q[] = { 1.,12.7827273196294,77.0001529352295,
				277.585444743988,638.980264465631,931.35409485061,
				790.950925327898,300.459260956983 };
		final double r[] = { 2.10144126479064,26.2370141675169,
				21.3688200555087,4.6580782871847,.282094791773523 };
		final double s[] = { 94.153775055546,187.11481179959,
				99.0191814623914,18.0124575948747 };

		/* System generated locals */
		double ret_val;

		/* Local variables */
		double e, t, w, ax, bot, top;

		ax = abs(x);
		if (ax <= 0.5) { // ABS(X) <= 0.5
			t = x * x;
			top = (((a[0] * t + a[1]) * t + a[2]) * t + a[3]) * t + a[4] + 1.0;
			bot = ((b[0] * t + b[1]) * t + b[2]) * t + 1.0;
			ret_val = 0.5 - x * (top / bot) + 0.5;
			if (ind != 0) {
				ret_val = exp(t) * ret_val;
			}
			return ret_val;
		}

		if (ax <= 4.0) { // 0.5 < ABS(X) <= 4
			top = ((((((p[0] * ax + p[1]) * ax + p[2]) * ax + p[3]) * ax + p[4]) * ax
					+ p[5]) * ax + p[6]) * ax + p[7];
			bot = ((((((q[0] * ax + q[1]) * ax + q[2]) * ax + q[3]) * ax + q[4]) * ax
					+ q[5]) * ax + q[6]) * ax + q[7];
			ret_val = top / bot;
		}
		else { // ABS(X) > 4
			if (x <= -5.6) {
				// LIMIT VALUE FOR LARGE NEGATIVE X
				ret_val = 2.0;
				if (ind != 0) {
					ret_val = exp(x * x) * 2.0;
				}
				return ret_val;
			}
			if (ind == 0) {
				// LIMIT VALUE FOR LARGE POSITIVE X WHEN IND = 0
				if (x > 100.0 || x * x > -exparg(1)) {
					return 0.0;
				}
			}

			/* Computing 2nd power */
			t = 1. / (x * x);
			top = (((r[0] * t + r[1]) * t + r[2]) * t + r[3]) * t + r[4];
			bot = (((s[0] * t + s[1]) * t + s[2]) * t + s[3]) * t + 1.0;
			ret_val = (c - t * top / bot) / ax;
		}

		/*                      FINAL ASSEMBLY */

		//L40:
		if (ind != 0) {
			if (x < 0.0)
				ret_val = exp(x * x) * 2.0 - ret_val;
		} else {
			// L41:  ind == 0 :
			w = x * x;
			t = w;
			e = w - t;
			ret_val = (0.5 - e + 0.5) * exp(-t) * ret_val;
			if (x < 0.0)
				ret_val = 2.0 - ret_val;
		}
		return ret_val;
	} /* erfc1 */

	/**
	 * ------------------------------------------------------------------
	 *     COMPUTATION OF 1/GAMMA(A+1) - 1  FOR -0.5 <= A <= 1.5
	 * ------------------------------------------------------------------
	 */
	public static final double gam1(double a)
	{
		final double p[] = { .577215664901533,-.409078193005776,
				-.230975380857675,.0597275330452234,.0076696818164949,
				-.00514889771323592,5.89597428611429e-4 };
		final double q[] = { 1.,.427569613095214,.158451672430138,
				.0261132021441447,.00423244297896961 };
		final double r[] = { -.422784335098468,-.771330383816272,
				-.244757765222226,.118378989872749,9.30357293360349e-4,
				-.0118290993445146,.00223047661158249,2.66505979058923e-4,
				-1.32674909766242e-4 };
		final double s1 = .273076135303957;
		final double s2 = .0559398236957378;

		double d, t, w, bot, top;

		t = a;
		d = a - 0.5;
		if (d > 0.0)
			t = d - 0.5;

		if (t < 0.0) {
			top = (((((((r[8] * t + r[7]) * t + r[6]) * t + r[5]) * t + r[4]) * t + r[3]) * t + r[2]) * t + r[1]) * t + r[0];
			bot = (s2 * t + s1) * t + 1.0;
			w = top / bot;
			if (d > 0.0)
				return t * w / a;
			return a * (w + 0.5 + 0.5);
		} else if (t == 0) {
			return 0.0;
		}

		top = (((((p[6] * t + p[5]) * t + p[4]) * t + p[3]) * t + p[2]) * t + p[1]) * t + p[0];
		bot = (((q[4] * t + q[3]) * t + q[2]) * t + q[1]) * t + 1.0;
		w = top / bot;
		if (d > 0.0) {
			return t / a * (w - 0.5 - 0.5);
		}
		return a * w;
	} /* gam1 */

	/**
	 * -----------------------------------------------------------------------
	 *     EVALUATION OF LN(GAMMA(1 + A)) FOR -0.2 <= A <= 1.25
	 * -----------------------------------------------------------------------
	 */
	public static final double gamln1(double a)
	{
		final double p0 = .577215664901533;
		final double p1 = .844203922187225;
		final double p2 = -.168860593646662;
		final double p3 = -.780427615533591;
		final double p4 = -.402055799310489;
		final double p5 = -.0673562214325671;
		final double p6 = -.00271935708322958;
		final double q1 = 2.88743195473681;
		final double q2 = 3.12755088914843;
		final double q3 = 1.56875193295039;
		final double q4 = .361951990101499;
		final double q5 = .0325038868253937;
		final double q6 = 6.67465618796164e-4;
		final double r0 = .422784335098467;
		final double r1 = .848044614534529;
		final double r2 = .565221050691933;
		final double r3 = .156513060486551;
		final double r4 = .017050248402265;
		final double r5 = 4.97958207639485e-4;
		final double s1 = 1.24313399877507;
		final double s2 = .548042109832463;
		final double s3 = .10155218743983;
		final double s4 = .00713309612391;
		final double s5 = 1.16165475989616e-4;

		double w;

		if (a < 0.6) {
			w = ((((((p6 * a + p5)* a + p4)* a + p3)* a + p2)* a + p1)* a + p0) /
			((((((q6 * a + q5)* a + q4)* a + q3)* a + q2)* a + q1)* a + 1.);
			return -(a) * w;
		}
		double x = a - 0.5 - 0.5;
		w = (((((r5 * x + r4) * x + r3) * x + r2) * x + r1) * x + r0) /
		(((((s5 * x + s4) * x + s3) * x + s2) * x + s1) * x + 1.0);
		return x * w;
	} /* gamln1 */

	/**
	 * <pre>
	 * ---------------------------------------------------------------------
	 *                 Evaluation of the Digamma function psi(x)
	 *                           -----------
	 *     Psi(xx) is assigned the value 0 when the digamma function cannot
	 *     be computed.
	 *     The main computation involves evaluation of rational Chebyshev
	 *     approximations published in Math. Comp. 27, 123-127(1973) by
	 *     Cody, Strecok and Thacher.
	 * ---------------------------------------------------------------------
	 *     Psi was written at Argonne National Laboratory for the FUNPACK
	 *     package of special function subroutines. Psi was modified by
	 *     A.H. Morris (NSWC).
	 * ---------------------------------------------------------------------
	 * </pre>
	 */
	public static final double psi(double x)
	{
		final double piov4 = .785398163397448; /* == pi / 4 */
		/*     dx0 = zero of psi() to extended precision : */
		final double dx0 = 1.461632144968362341262659542325721325;

		/* --------------------------------------------------------------------- */
		/*     COEFFICIENTS FOR RATIONAL APPROXIMATION OF */
		/*     PSI(X) / (X - X0),  0.5 <= X <= 3.0 */
		final double p1[] = { .0089538502298197,4.77762828042627,
				142.441585084029,1186.45200713425,3633.51846806499,
				4138.10161269013,1305.60269827897 };
		final double q1[] = { 44.8452573429826,520.752771467162,
				2210.0079924783,3641.27349079381,1908.310765963,
				6.91091682714533e-6 };
		/* --------------------------------------------------------------------- */


		/* --------------------------------------------------------------------- */
		/*     COEFFICIENTS FOR RATIONAL APPROXIMATION OF */
		/*     PSI(X) - LN(X) + 1 / (2*X),  X > 3.0 */

		double p2[] = { -2.12940445131011,-7.01677227766759,
				-4.48616543918019,-.648157123766197 };
		double q2[] = { 32.2703493791143,89.2920700481861,
				54.6117738103215,7.77788548522962 };
		/* --------------------------------------------------------------------- */

		int i, m, n, nq;
		double d2;
		double w, z;
		double den, aug, sgn, xmx0, xmax1, upper, xsmall;

		/* --------------------------------------------------------------------- */


		/*     MACHINE DEPENDENT CONSTANTS ... */

		/* --------------------------------------------------------------------- */
		/*	  XMAX1	 = THE SMALLEST POSITIVE FLOATING POINT CONSTANT
				   WITH ENTIRELY INT REPRESENTATION.  ALSO USED
				   AS NEGATIVE OF LOWER BOUND ON ACCEPTABLE NEGATIVE
				   ARGUMENTS AND AS THE POSITIVE ARGUMENT BEYOND WHICH
				   PSI MAY BE REPRESENTED AS LOG(X).
		 * Originally:  xmax1 = amin1(ipmpar(3), 1./spmpar(1))  */
		xmax1 = Integer.MAX_VALUE; // (double) INT_MAX;
		d2 = 0.5 / (0.5*DBL_EPSILON);
		if(xmax1 > d2) xmax1 = d2;

		/* --------------------------------------------------------------------- */
		/*        XSMALL = ABSOLUTE ARGUMENT BELOW WHICH PI*COTAN(PI*X) */
		/*                 MAY BE REPRESENTED BY 1/X. */
		xsmall = 1e-9;
		/* --------------------------------------------------------------------- */
		aug = 0.0;
		if (x < 0.5) {
			/* --------------------------------------------------------------------- */
			/*     X < 0.5,  USE REFLECTION FORMULA */
			/*     PSI(1-X) = PSI(X) + PI * COTAN(PI*X) */
			/* --------------------------------------------------------------------- */
			if (abs(x) <= xsmall) {

				if (x == 0.0) {
					return 0.;
				}
				/* --------------------------------------------------------------------- */
				/*     0 < |X| <= XSMALL.  USE 1/X AS A SUBSTITUTE */
				/*     FOR  PI*COTAN(PI*X) */
				/* --------------------------------------------------------------------- */
				aug = -1.0 / x;
			} else { /* |x| > xsmall */
				/* --------------------------------------------------------------------- */
				/*     REDUCTION OF ARGUMENT FOR COTAN */
				/* --------------------------------------------------------------------- */
				/* L100: */
				w = -x;
				sgn = piov4;
				if (w <= 0.0) {
					w = -w;
					sgn = -sgn;
				}
				/* --------------------------------------------------------------------- */
				/*     MAKE AN ERROR EXIT IF |X| >= XMAX1 */
				/* --------------------------------------------------------------------- */
				if (w >= xmax1) {
					return 0.0;
				}
				nq = (int) w;
				w -= nq;
				nq = (int) (w * 4.0);
				w = (w - nq * 0.25) * 4.0;
				/* --------------------------------------------------------------------- */
				/*     W IS NOW RELATED TO THE FRACTIONAL PART OF  4.0 * X. */
				/*     ADJUST ARGUMENT TO CORRESPOND TO VALUES IN FIRST */
				/*     QUADRANT AND DETERMINE SIGN */
				/* --------------------------------------------------------------------- */
				n = nq / 2;
				if (n + n != nq) {
					w = 1.0 - w;
				}
				z = piov4 * w;
				m = n / 2;
				if (m + m != n) {
					sgn = -sgn;
				}
				/* --------------------------------------------------------------------- */
				/*     DETERMINE FINAL VALUE FOR  -PI*COTAN(PI*X) */
				/* --------------------------------------------------------------------- */
				n = (nq + 1) / 2;
				m = n / 2;
				m += m;
				if (m == n) {
					/* --------------------------------------------------------------------- */
					/*     CHECK FOR SINGULARITY */
					/* --------------------------------------------------------------------- */
					if (z == 0.0) {
						return 0.;
					}
					/* --------------------------------------------------------------------- */
					/*     USE COS/SIN AS A SUBSTITUTE FOR COTAN, AND */
					/*     SIN/COS AS A SUBSTITUTE FOR TAN */
					/* --------------------------------------------------------------------- */
					aug = sgn * (cos(z) / sin(z) * 4.0);

				} else { /* L140: */
					aug = sgn * (sin(z) / cos(z) * 4.0);
				}
			}

			x = 1.0 - x;

		}
		/* L200: */
		if (x <= 3.0) {
			/* --------------------------------------------------------------------- */
			/*     0.5 <= X <= 3.0 */
			/* --------------------------------------------------------------------- */
			den = x;
			upper = p1[0] * x;

			for (i = 1; i <= 5; ++i) {
				den = (den + q1[i - 1]) * x;
				upper = (upper + p1[i]) * x;
			}

			den = (upper + p1[6]) / (den + q1[5]);
			xmx0 = x - dx0;
			return den * xmx0 + aug;
		}

		/* --------------------------------------------------------------------- */
		/*     IF X >= XMAX1, PSI = LN(X) */
		/* --------------------------------------------------------------------- */
		if (x < xmax1) {
			/* --------------------------------------------------------------------- */
			/*     3.0 < X < XMAX1 */
			/* --------------------------------------------------------------------- */
			w = 1.0 / (x * x);
			den = w;
			upper = p2[0] * w;

			for (i = 1; i <= 3; ++i) {
				den = (den + q2[i - 1]) * w;
				upper = (upper + p2[i]) * w;
			}

			aug = upper / (den + q2[3]) - 0.5 / x + aug;
		}
		return aug + log(x);
	} /* psi */

	/**
	 * -----------------------------------------------------------------------
	 *     Evaluation of the logarithm of the beta function  ln(beta(a0,b0))
	 * -----------------------------------------------------------------------
	 */
	public static final double betaln(double a0, double b0)
	{
		//double e = .918938533204673;/* e == 0.5*LN(2*PI) */

		double
			a = min(a0 ,b0),
			b = max(a0, b0);
		if (a < 8.0) {
			if (a < 1.0) {
				/* ----------------------------------------------------------------------- */
				//	                        		A < 1
				/* ----------------------------------------------------------------------- */
				if (b < 8.0)
					return gamln(a) + (gamln(b) - gamln(a+b));
				else
					return gamln(a) + algdiv(a, b);
			}
			/* else */
			/* ----------------------------------------------------------------------- */
			//	    				1 <= A < 8
			/* ----------------------------------------------------------------------- */
			double w;
			if (a < 2.0) {
				if (b <= 2.0) {
					return gamln(a) + gamln(b) - gsumln(a, b);
				}
				/* else */

				w = 0.0;
				if (b < 8.0) {
					//goto L40;
					int n = (int)(b - 1.0);
					double z = 1.0;
					for (int i = 1; i <= n; ++i) {
						b += -1.0;
						z *= b / (a + b);
					}
					return w + log(z) + (gamln(a) + (gamln(b) - gsumln(a, b)));
				}
				return gamln(a) + algdiv(a, b);
			}
			// else L30:    REDUCTION OF A WHEN B <= 1000

			if (b <= 1e3) {
				int n = (int)(a - 1.0);
				w = 1.0;
				for (int i = 1; i <= n; ++i) {
					a += -1.0;
					double h = a / b;
					w *= h / (h + 1.0);
				}
				w = log(w);

				if (b >= 8.0)
					return w + gamln(a) + algdiv(a, b);

				// else
				//L40:
				// 		reduction of B when  B < 8
				n = (int)(b - 1.0);
				double z = 1.0;
				for (int i = 1; i <= n; ++i) {
					b += -1.0;
					z *= b / (a + b);
				}
				return w + log(z) + (gamln(a) + (gamln(b) - gsumln(a, b)));
			}
			else { // L50:	reduction of A when  B > 1000
				int n = (int)(a - 1.0);
				w = 1.0;
				for (int i = 1; i <= n; ++i) {
					a += -1.0;
					w *= a / (a / b + 1.0);
				}
				return log(w) - n * log(b) + (gamln(a) + algdiv(a, b));
			}

		} else {
			/* ----------------------------------------------------------------------- */
			// L60:			A >= 8
			/* ----------------------------------------------------------------------- */

			double
			w = bcorr(a, b),
			h = a / b,
			u = -(a - 0.5) * log(h / (h + 1.0)),
			v = b * alnrel(h);
			if (u > v)
				return log(b) * -0.5 + M_LN_SQRT_2PI + w - v - u;
			else
				return log(b) * -0.5 + M_LN_SQRT_2PI + w - u - v;
		}
	} /* betaln */

	/**
	 * -----------------------------------------------------------------------
	 *          EVALUATION OF THE FUNCTION LN(GAMMA(A + B))
	 *          FOR 1 <= A <= 2  AND  1 <= B <= 2
	 * -----------------------------------------------------------------------
	 */
	public static final double gsumln(double a, double b)
	{
		double x = a + b - 2.;

		if (x <= 0.25)
			return gamln1(x + 1.0);

		/* else */
		if (x <= 1.25)
			return gamln1(x) + alnrel(x);
		/* else x > 1.25 : */
		return gamln1(x - 1.0) + log(x * (x + 1.0));

	} /* gsumln */

	/**
	 * -----------------------------------------------------------------------
	 *     EVALUATION OF  DEL(A0) + DEL(B0) - DEL(A0 + B0)  WHERE
	 *     LN(GAMMA(A)) = (A - 0.5)*LN(A) - A + 0.5*LN(2*PI) + DEL(A).
	 *     IT IS ASSUMED THAT A0 >= 8 AND B0 >= 8.
	 * -----------------------------------------------------------------------
	 */
	public static final double bcorr(double a0, double b0)
	{
		/* Initialized data */
		final double c0 = .0833333333333333;
		final double c1 = -.00277777777760991;
		final double c2 = 7.9365066682539e-4;
		final double c3 = -5.9520293135187e-4;
		final double c4 = 8.37308034031215e-4;
		final double c5 = -.00165322962780713;

		/* System generated locals */
		double r1;

		/* Local variables */
		double a, b, c, h, t, w, x, s3, s5, x2, s7, s9, s11;
		/* ------------------------ */
		a = min(a0, b0);
		b = max(a0, b0);

		h = a / b;
		c = h / (h + 1.0);
		x = 1.0 / (h + 1.0);
		x2 = x * x;

		/*                SET SN = (1 - X^N)/(1 - X) */

		s3 = x + x2 + 1.0;
		s5 = x + x2 * s3 + 1.0;
		s7 = x + x2 * s5 + 1.0;
		s9 = x + x2 * s7 + 1.0;
		s11 = x + x2 * s9 + 1.0;

		/*                SET W = DEL(B) - DEL(A + B) */

		/* Computing 2nd power */
		r1 = 1.0 / b;
		t = r1 * r1;
		w = ((((c5 * s11 * t + c4 * s9) * t + c3 * s7) * t + c2 * s5) * t + c1 *
				s3) * t + c0;
		w *= c / b;

		/*                   COMPUTE  DEL(A) + W */

		/* Computing 2nd power */
		r1 = 1.0 / a;
		t = r1 * r1;
		return (((((c5 * t + c4) * t + c3) * t + c2) * t + c1) * t + c0) / a + w;
	} /* bcorr */

	/**
	 * -----------------------------------------------------------------------
	 *     COMPUTATION OF LN(GAMMA(B)/GAMMA(A+B)) WHEN B >= 8.
	 *     IN THIS ALGORITHM, DEL(X) IS THE FUNCTION DEFINED BY
	 *     LN(GAMMA(X)) = (X - 0.5)*LN(X) - X + 0.5*LN(2*PI) + DEL(X).
	 * -----------------------------------------------------------------------
	 */
	public static final double algdiv(double a, double b)
	{
		/* Initialized data */
		final double c0 = .0833333333333333;
		final double c1 = -.00277777777760991;
		final double c2 = 7.9365066682539e-4;
		final double c3 = -5.9520293135187e-4;
		final double c4 = 8.37308034031215e-4;
		final double c5 = -.00165322962780713;

		double c, d, h, t, u, v, w, x, s3, s5, x2, s7, s9, s11;

		/* ------------------------ */
		if (a > b) {
			h = b / a;
			c = 1.0 / (h + 1.0);
			x = h / (h + 1.0);
			d = a + (b - 0.5);
		}
		else {
			h = a / b;
			c = h / (h + 1.0);
			x = 1.0 / (h + 1.0);
			d = b + (a - 0.5);
		}

		/* Set s<n> = (1 - x^n)/(1 - x) : */

		x2 = x * x;
		s3 = x + x2 + 1.0;
		s5 = x + x2 * s3 + 1.0;
		s7 = x + x2 * s5 + 1.0;
		s9 = x + x2 * s7 + 1.0;
		s11 = x + x2 * s9 + 1.0;

		/* w := Del(b) - Del(a + b) */

		t = 1./ (b * b);
		w = ((((c5 * s11 * t + c4 * s9) * t + c3 * s7) * t + c2 * s5) * t + c1 *
				s3) * t + c0;
		w *= c / b;

		/*                    COMBINE THE RESULTS */

		u = d * alnrel(a / b);
		v = a * (log(b) - 1.0);
		if (u > v)
			return w - v - u;
		return w - u - v;
	} /* algdiv */

	/**
	 * <pre>
	 * -----------------------------------------------------------------------
	 *            Evaluation of  ln(gamma(a))  for positive a
	 * -----------------------------------------------------------------------
	 *     Written by Alfred H. Morris
	 *          Naval Surface Warfare Center
	 *          Dahlgren, Virginia
	 * -----------------------------------------------------------------------
	 * </pre>
	 */
	public static final double gamln(double a)
	{
		final double d = .418938533204673;/* d == 0.5*(LN(2*PI) - 1) */

		final double c0 = .0833333333333333;
		final double c1 = -.00277777777760991;
		final double c2 = 7.9365066682539e-4;
		final double c3 = -5.9520293135187e-4;
		final double c4 = 8.37308034031215e-4;
		final double c5 = -.00165322962780713;

		if (a <= 0.8)
			return gamln1(a) - log(a);
		else if (a <= 2.25)
			return gamln1(a - 0.5 - 0.5);

		else if (a < 10.0) {
			int i, n = (int) (a - 1.25);
			double t = a;
			double w = 1.0;
			for (i = 1; i <= n; ++i) {
				t += -1.0;
				w *= t;
			}
			return gamln1(t - 1.) + log(w);
		}
		else { /* a >= 10 */
			double t = 1. / (a * a);
			double w = (((((c5 * t + c4) * t + c3) * t + c2) * t + c1) * t + c0) / a;
			return d + w + (a - 0.5) * (log(a) - 1.0);
		}
	} /* gamln */

	/**<pre>
	 *    This function returns the value of the beta function
	 *    evaluated with arguments a and b.
	 *
	 *  NOTES
	 *    This routine is a translation into C of a Fortran subroutine
	 *    by W. Fullerton of Los Alamos Scientific Laboratory.
	 *    Some modifications have been made so that the routines
	 *    conform to the IEEE 754 standard.</pre>
	 */
	public static final double beta(double a, double b)
	{
		final double //xmin = -170.5674972726612,
				xmax = 171.61447887182298,
				lnsml = -708.39641853226412;

		/* NaNs propagated correctly */
		if(Double.isNaN(a) || Double.isNaN(b)) return a + b;

		if (a < 0 || b < 0)
			return Double.NaN;
		if (a == 0 || b == 0)
			return Double.POSITIVE_INFINITY;
		if (isInfinite(a) || isInfinite(b))
			return 0;

		if (a + b < xmax) /* ~= 171.61 for IEEE */
			return (1 / gammafn(a+b)) * gammafn(a) * gammafn(b);
		else {
			double val = lbeta(a, b);
			if (val < lnsml) {
				/* a and/or b so big that beta underflows */
				//ML_ERROR(ME_UNDERFLOW, "beta");
				return Double.NaN;
				/* return ML_UNDERFLOW; pointless giving incorrect value */
			}
			return exp(val);
		}
	}

	private static final double lfastchoose(double n, double k)
	{
		return -log(n + 1.) - lbeta(n - k + 1., k + 1.);
	}

	private static final double lfastchoose2(double n, double k, int []s_choose)
	{
		double r;
		r = lgammafn_sign(n - k + 1., s_choose);
		return lgammafn(n + 1.) - lgammafn(k + 1.) - r;
	}

	public static final double lchoose(double n, double k)
	{
		double k0 = k;
		k = rint(k);
		/* NaNs propagated correctly */
		if(Double.isNaN(n) || Double.isNaN(k)) return n + k;
		if (abs(k - k0) > 1e-7)
			//MATHLIB_WARNING2(_("'tw' (%.2f) must be integer, rounded to %.0f"), k0, tw);
			System.err.println(String.format("'tw' (%.2f) must be integer, rounded to %.0f", k0, k));
		if (k < 2) {
			if (k <	 0) return Double.NEGATIVE_INFINITY;
			if (k == 0) return 0.;
			/* else: tw == 1 */
			return log(abs(n));
		}
		/* else: tw >= 2 */
		if (n < 0) {
			return lchoose(-n+ k-1, k);
		}
		else if (!isNonInt(n)) { // (R_IS_INT(n)) {
			if(n < k) return Double.NEGATIVE_INFINITY;
			/* tw <= n :*/
			if(n - k < 2) return lchoose(n, n-k); /* <- Symmetry */
			/* else: n >= tw+2 */
			return lfastchoose(n, k);
		}
		/* else non-integer n >= 0 : */
		if (n < k-1) {
			//int s;
			return lfastchoose2(n, k, null);
		}
		return lfastchoose(n, k);
	}

	public static final double choose(double n, double k)
	{
		final int k_small_max = 30;
		double r, k0 = k;
		k = rint(k);
		/* NaNs propagated correctly */
		if(Double.isNaN(n) || Double.isNaN(k)) return n + k;
		if (abs(k - k0) > 1e-7)
			//MATHLIB_WARNING2(_("'tw' (%.2f) must be integer, rounded to %.0f"), k0, tw);
			System.err.println(String.format("'tw' (%.2f) must be integer, rounded to %.0f", k0, k));
		if (k < k_small_max) {
			int j;
			if(n-k < k && n >= 0 && !isNonInt(n)) k = n-k; /* <- Symmetry */
			if (k <	 0) return 0.;
			if (k == 0) return 1.;
			/* else: tw >= 1 */
			r = n;
			for(j = 2; j <= k; j++)
				r *= (n-j+1)/j;
			return !isNonInt(n) ? rint(r) : r;
			/* might have got rounding errors */
		}
		/* else: tw >= k_small_max */
		if (n < 0) {
			r = choose(-n+ k-1, k);
			if (((k) != 2 * floor((k) / 2.))) r = -r;
			return r;
		}
		else if (!isNonInt(n)) {
			if(n < k) return 0.;
			if(n - k < k_small_max) return choose(n, n-k); /* <- Symmetry */
			return rint(exp(lfastchoose(n, k)));
		}
		/* else non-integer n >= 0 : */
		if (n < k-1) {
			int[] s_choose = {1};
			r = lfastchoose2(n, k, /* -> */ s_choose);
			return s_choose[0] * exp(r);
		}
		return exp(lfastchoose(n, k));
	}

	/**<pre> ----------------------------------------------------------------------

	   This routine calculates the GAMMA function for a float argument X.
	   Computation is based on an algorithm outlined in reference [1].
	   The program uses rational functions that approximate the GAMMA
	   function to at least 20 significant decimal digits.	Coefficients
	   for the approximation over the interval (1,2) are unpublished.
	   Those for the approximation for X >= 12 are from reference [2].
	   The accuracy achieved depends on the arithmetic system, the
	   compiler, the intrinsic functions, and proper selection of the
	   machine-dependent constants.

	   *******************************************************************

	   Error returns

	   The program returns the value XINF for singularities or
	   when overflow would occur.	 The computation is believed
	   to be free of underflow and overflow.

	   Intrinsic functions required are:

	   INT, DBLE, EXP, LOG, REAL, SIN

	   References:
	   [1]  "An Overview of Software Development for Special Functions",
		W. J. Cody, Lecture Notes in Mathematics, 506,
		Numerical Analysis Dundee, 1975, G. A. Watson (ed.),
		Springer Verlag, Berlin, 1976.

	   [2]  Computer Approximations, Hart, Et. Al., Wiley and sons, New York, 1968.

	   Latest modification: October 12, 1989

	   Authors: W. J. Cody and L. Stoltz
	   Applied Mathematics Division
	   Argonne National Laboratory
	   Argonne, IL 60439
	   ----------------------------------------------------------------------</pre>*/
	public static final double gamma_cody(double x) {
		/* ----------------------------------------------------------------------
		   Mathematical constants
		   ----------------------------------------------------------------------*/
		//final double sqrtpi = .9189385332046727417803297; /* == ??? */

		/* *******************************************************************

		   Explanation of machine-dependent constants

		   beta	- radix for the floating-point representation
		   maxexp - the smallest positive power of beta that overflows
		   XBIG	- the largest argument for which GAMMA(X) is representable
			in the machine, thread.e., the solution to the equation
			GAMMA(XBIG) = beta**maxexp
		   XINF	- the largest machine representable floating-point number;
			approximately beta**maxexp
		   EPS	- the smallest positive floating-point number such that  1.0+EPS > 1.0
		   XMININ - the smallest positive floating-point number such that
			1/XMININ is machine representable

		   Approximate values for some important machines are:

		   beta	      maxexp	     XBIG

		   CRAY-1		(S.P.)	      2		8191	    966.961
		   Cyber 180/855
		   under NOS	(S.P.)	      2		1070	    177.803
		   IEEE (IBM/XT,
		   SUN, etc.)	(S.P.)	      2		 128	    35.040
		   IEEE (IBM/XT,
		   SUN, etc.)	(D.P.)	      2		1024	    171.624
		   IBM 3033	(D.P.)	     16		  63	    57.574
		   VAX D-Format	(D.P.)	      2		 127	    34.844
		   VAX G-Format	(D.P.)	      2		1023	    171.489

		   XINF	 EPS	    XMININ

		   CRAY-1		(S.P.)	 5.45E+2465   7.11E-15	  1.84E-2466
		   Cyber 180/855
		   under NOS	(S.P.)	 1.26E+322    3.55E-15	  3.14E-294
		   IEEE (IBM/XT,
		   SUN, etc.)	(S.P.)	 3.40E+38     1.19E-7	  1.18E-38
		   IEEE (IBM/XT,
		   SUN, etc.)	(D.P.)	 1.79D+308    2.22D-16	  2.23D-308
		   IBM 3033	(D.P.)	 7.23D+75     2.22D-16	  1.39D-76
		   VAX D-Format	(D.P.)	 1.70D+38     1.39D-17	  5.88D-39
		   VAX G-Format	(D.P.)	 8.98D+307    1.11D-16	  1.12D-308

		 *******************************************************************

		   ----------------------------------------------------------------------
		   Machine dependent parameters
		   ----------------------------------------------------------------------
		 */
		final double xbig = 171.624;
		/* ML_POSINF ==   const double xinf = 1.79e308;*/
		/* DBL_EPSILON = const double eps = 2.22e-16;*/
		/* DBL_MIN ==   const double xminin = 2.23e-308;*/

		/*----------------------------------------------------------------------
		      Numerator and denominator coefficients for rational minimax
		      approximation over (1,2).
		      ----------------------------------------------------------------------*/
		final double p[] = {
			-1.71618513886549492533811,
			24.7656508055759199108314,-379.804256470945635097577,
			629.331155312818442661052,866.966202790413211295064,
			-31451.2729688483675254357,-36144.4134186911729807069,
			66456.1438202405440627855 };
		final double q[] = {
			-30.8402300119738975254353,
			315.350626979604161529144,-1015.15636749021914166146,
			-3107.77167157231109440444,22538.1184209801510330112,
			4755.84627752788110767815,-134659.959864969306392456,
			-115132.259675553483497211 };
		/*----------------------------------------------------------------------
		      Coefficients for minimax approximation over (12, INF).
		      ----------------------------------------------------------------------*/
		final double c[] = {
			-.001910444077728,8.4171387781295e-4,
			-5.952379913043012e-4,7.93650793500350248e-4,
			-.002777777777777681622553,.08333333333333333331554247,
			.0057083835261 };

		/* Local variables */
		long n;
		boolean parity;/*logical*/
		double fact, xden, xnum, y, z, yi, res, sum, ysq;

		parity = false;
		fact = 1.;
		n = 0;
		y = x;
		if (y <= 0.) {
			/* -------------------------------------------------------------
			   Argument is negative
			   ------------------------------------------------------------- */
			y = -x;
			yi = trunc(y);
			res = y - yi;
			if (res != 0.) {
				if (yi != trunc(yi * .5) * 2.)
					parity = true;
				fact = -PI / sinpi(res);
				y += 1.;
			} else {
				return(Double.POSITIVE_INFINITY);
			}
		}
		/* -----------------------------------------------------------------
		       Argument is positive
		       -----------------------------------------------------------------*/
		if (y < DBL_EPSILON) {
			/* --------------------------------------------------------------
			   Argument < EPS
			   -------------------------------------------------------------- */
			if (y >= DBL_MIN) {
				res = 1. / y;
			} else {
				return(Double.POSITIVE_INFINITY);
			}
		} else if (y < 12.) {
			yi = y;
			if (y < 1.) {
				/* ---------------------------------------------------------
			       EPS < argument < 1
			       --------------------------------------------------------- */
				z = y;
				y += 1.;
			} else {
				/* -----------------------------------------------------------
			       1 <= argument < 12, reduce argument if necessary
			       ----------------------------------------------------------- */
				n = (long) y - 1;
				y -= (double) n;
				z = y - 1.;
			}
			/* ---------------------------------------------------------
			   Evaluate approximation for 1. < argument < 2.
			   ---------------------------------------------------------*/
			xnum = 0.;
			xden = 1.;
			for (int i = 0; i < 8; ++i) {
				xnum = (xnum + p[i]) * z;
				xden = xden * z + q[i];
			}
			res = xnum / xden + 1.;
			if (yi < y) {
				/* --------------------------------------------------------
			       Adjust result for case  0. < argument < 1.
			       -------------------------------------------------------- */
				res /= yi;
			} else if (yi > y) {
				/* ----------------------------------------------------------
			       Adjust result for case  2. < argument < 12.
			       ---------------------------------------------------------- */
				for (int i = 0; i < n; ++i) {
					res *= y;
					y += 1.;
				}
			}
		} else {
			/* -------------------------------------------------------------
			   Evaluate for argument >= 12.,
			   ------------------------------------------------------------- */
			if (y <= xbig) {
				ysq = y * y;
				sum = c[6];
				for (int i = 0; i < 6; ++i) {
					sum = sum / ysq + c[i];
				}
				sum = sum / y - y + M_LN_SQRT_2PI; //sqrtpi;
				sum += (y - .5) * log(y);
				res = exp(sum);
			} else {
				return(Double.POSITIVE_INFINITY);
			}
		}
		/* ----------------------------------------------------------------------
		       Final adjustments and return
		       ----------------------------------------------------------------------*/
		if (parity)
			res = -res;
		if (fact != 1.)
			res = fact / res;
		return res;
	}

	public static final double pd_upper_series (double x, double y, boolean log_p)
	{
		double term = x / y;
		double sum = term;

		do {
			y++;
			term *= x / y;
			sum += term;
		} while (term > sum * DBL_EPSILON);

		/* sum =  \sum_{n=1}^ oo  x^n     / (y*(y+1)*...*(y+n-1))
		 *	   =  \sum_{n=0}^ oo  x^(n+1) / (y*(y+1)*...*(y+n))
		 *	   =  x/y * (1 + \sum_{n=1}^oo	x^n / ((y+1)*...*(y+n)))
		 *	   ~  x/y +  o(x/y)   {which happens when alph -> Inf}
		 */
		return log_p ? log (sum) : sum;
	}

	/* Continued fraction for calculation of
	 *    scaled upper-tail F_{gamma}
	 *  ~=  (y / d) * [1 +  (1-y)/d +  O( ((1-y)/d)^2 ) ]
	 */
	public static final double pd_lower_cf (double y, double d)
	{
		double f= 0.0 /* -Wall */, of, f0;
		double i, c2, c3, c4,  a1, b1,  a2, b2;
		final int max_it = 200000;
		if (y == 0) return 0;

		f0 = y/d;
		/* Needed, e.g. for  pgamma(10^c(100,295), shape= 1.1, log=TRUE): */
		if(abs(y - 1) < abs(d) * DBL_EPSILON) { /* includes y < d = Inf */
			return (f0);
		}

		if(f0 > 1.) f0 = 1.;
		c2 = y;
		c4 = d; /* original (y,d), *not* potentially scaled ones!*/

		a1 = 0; b1 = 1;
		a2 = y; b2 = d;
		while (b2 > scalefactor) {
			a1 /= scalefactor;
			b1 /= scalefactor;
			a2 /= scalefactor;
			b2 /= scalefactor;
		}

		i = 0; of = -1.; /* far away */
		while (i < max_it) {

			i++;	c2--;	c3 = i * c2;	c4 += 2;
			/* c2 = y - thread,  c3 = thread(y - thread),  c4 = d + 2i,  for thread odd */
			a1 = c4 * a2 + c3 * a1;
			b1 = c4 * b2 + c3 * b1;

			i++;	c2--;	c3 = i * c2;	c4 += 2;
			/* c2 = y - thread,  c3 = thread(y - thread),  c4 = d + 2i,  for thread even */
			a2 = c4 * a1 + c3 * a2;
			b2 = c4 * b1 + c3 * b2;

			if (b2 > scalefactor) {
				a1 /= scalefactor;
				b1 /= scalefactor;
				a2 /= scalefactor;
				b2 /= scalefactor;
			}

			if (b2 != 0) {
				f = a2 / b2;
				/* convergence check: relative; "absolute" for very small f : */
				if (abs (f - of) <= DBL_EPSILON * max(f0, abs(f))) {
					return f;
				}
				of = f;
			}
		}

		//MATHLIB_WARNING(" ** NON-convergence in pgamma()'s pd_lower_cf() f= %g.\n", f);
		return f;/* should not happen ... */
	}

	public static final double pd_lower_series (double lambda, double y)
	{
		double term = 1, sum = 0;

		while (y >= 1 && term > sum * DBL_EPSILON) {
			term *= y / lambda;
			sum += term;
			y--;
		}
		/* sum =  \sum_{n=0}^ oo  y*(y-1)*...*(y - n) / lambda^(n+1)
		 *	   =  y/lambda * (1 + \sum_{n=1}^Inf  (y-1)*...*(y-n) / lambda^n)
		 *	   ~  y/lambda + o(y/lambda)
		 */

		if (y != floor (y)) {
			/*
			 * The series does not converge as the terms start getting
			 * bigger (besides flipping sign) for y < -lambda.
			 */
			double f;
			/* FIXME: in quite few cases, adding  term*f  has no effect (f too small)
			 *	  and is unnecessary e.g. for pgamma(4e12, 121.1) */
			f = pd_lower_cf (y, lambda + 1 - y);
			sum += term * f;
		}

		return sum;
	} /* pd_lower_series() */

	/**
	 * Alternative expm1 -- no difference from Java's
	 * @param x
	 * @return expm1
	 */
	public static final double _expm1(double x)
	{
	    double y, a = abs(x);

	    if (a < DBL_EPSILON) return x;
	    if (a > 0.697) return exp(x) - 1;  /* negligible cancellation */

	    if (a > 1e-8)
		y = exp(x) - 1;
	    else /* Taylor expansion, more accurate in this range */
		y = (x / 2 + 1) * x;

	    /* Newton step for solving   log(1 + y) = x   for y : */
	    /* WARNING: does not work for y ~ -1: bug in 1.5.0 */
	    y -= (1 + y) * (log1p (y) - x);
	    return y;
	}

	/**
	 * Alternative log1p. No difference from Java's.
	 * @param x
	 * @return log1p
	 */
	public static final double _log1p(double x)
	{
	    /* series for log1p on the interval -.375 to .375
	     *				     with weighted error   6.35e-32
	     *				      log weighted error  31.20
	     *			    significant figures required  30.93
	     *				 decimal places required  32.01
	     */
	    final double alnrcs[] = {
		+.10378693562743769800686267719098e+1,
		-.13364301504908918098766041553133e+0,
		+.19408249135520563357926199374750e-1,
		-.30107551127535777690376537776592e-2,
		+.48694614797154850090456366509137e-3,
		-.81054881893175356066809943008622e-4,
		+.13778847799559524782938251496059e-4,
		-.23802210894358970251369992914935e-5,
		+.41640416213865183476391859901989e-6,
		-.73595828378075994984266837031998e-7,
		+.13117611876241674949152294345011e-7,
		-.23546709317742425136696092330175e-8,
		+.42522773276034997775638052962567e-9,
		-.77190894134840796826108107493300e-10,
		+.14075746481359069909215356472191e-10,
		-.25769072058024680627537078627584e-11,
		+.47342406666294421849154395005938e-12,
		-.87249012674742641745301263292675e-13,
		+.16124614902740551465739833119115e-13,
		-.29875652015665773006710792416815e-14,
		+.55480701209082887983041321697279e-15,
		-.10324619158271569595141333961932e-15,
		+.19250239203049851177878503244868e-16,
		-.35955073465265150011189707844266e-17,
		+.67264542537876857892194574226773e-18,
		-.12602624168735219252082425637546e-18,
		+.23644884408606210044916158955519e-19,
		-.44419377050807936898878389179733e-20,
		+.83546594464034259016241293994666e-21,
		-.15731559416479562574899253521066e-21,
		+.29653128740247422686154369706666e-22,
		-.55949583481815947292156013226666e-23,
		+.10566354268835681048187284138666e-23,
		-.19972483680670204548314999466666e-24,
		+.37782977818839361421049855999999e-25,
		-.71531586889081740345038165333333e-26,
		+.13552488463674213646502024533333e-26,
		-.25694673048487567430079829333333e-27,
		+.48747756066216949076459519999999e-28,
		-.92542112530849715321132373333333e-29,
		+.17578597841760239233269760000000e-29,
		-.33410026677731010351377066666666e-30,
		+.63533936180236187354180266666666e-31,
	    };

		final int nlnrel = 22;
		final double xmin = -0.999999985;
	    if (x == 0.) return 0.;/* speed */
	    if (x == -1) return(Double.NEGATIVE_INFINITY);
	    if (x  < -1) return Double.NaN;

	    if (abs(x) <= .375) {
	        /* Improve on speed (only);
		   again give result accurate to IEEE double precision: */
		if(abs(x) < .5 * DBL_EPSILON)
		    return x;

		if( (0 < x && x < 1e-8) || (-1e-9 < x && x < 0))
		    return x * (1 - .5 * x);
		/* else */
		return x * (1 - x * chebyshev_eval(x / .375, alnrcs, nlnrel));
	    }
	    /* else */
	    if (x < xmin) {
		/* answer less than half precision because x too near -1 */
	    	System.err.println("Precision loss warning at log1p");
	    	//ML_ERROR(ME_PRECISION, "log1p");
	    }
	    return log(1 + x);
	}

	/**
	 * Calculate harmonic number
	 * @param n must be positive
	 * @return value
	 */
	public static final double gharmonic(int n) {
		return gharmonic(n, 1, 0);
	}

	public static final double gharmonic(int n, double s) {
		return gharmonic(n, s, 0);
	}

	/**
	 * Calculate generalized harmonic number
	 * @param n must be positive
	 * @param s
	 * @param logexponent
	 * @return a value
	 */
	public static final double gharmonic(int n, double s, double logexponent) {
		if (n <= 0)
			throw new IllegalArgumentException();
		double sum = 0;
		if (logexponent != 0)
			for (int i = 2; i <= n; i++)
				sum += pow(log(i), logexponent) * pow(i, -s);
		else
			for (int i = 1; i <= n; i++)
				sum += pow(i, -s);
		return sum;
	}

	public static final double lgharmonic(int n) {
		return lgharmonic(n, 1, 0);
	}

	public static final double lgharmonic(int n, double s) {
		return lgharmonic(n, s, 0);
	}

	/**
	 * Calculate the log of generalized harmonic number
	 * @param n must be positive
	 * @param s
	 * @param logexponent
	 * @return a value
	 */
	public static final double lgharmonic(int n, double s, double logexponent) {
		if (n <= 0)
			throw new IllegalArgumentException();
		double sum = 0;
		if (logexponent != 0)
			for (int i = 2; i <= n; i++)
				sum = logspace_add(sum, logexponent * log(log(i)) - s * log(i));
		else
			for (int i = 1; i <= n; i++)
				sum = logspace_add(sum, -s * log(i));
		return sum;
	}

	public static final double cospi(double x) {
		if (Double.isNaN(x)) return x;
		if (isInfinite(x)) return Double.NaN;
		x = abs(x) % 2.;
		if (x % 1 == 0.5) return 0;
		if (x == 1.) return -1.;
		if (x == 0.) return 1.;
		return cos(PI * x);
	}

	public static final double sinpi(double x) {
		if (Double.isNaN(x)) return x;
		if (isInfinite(x)) return Double.NaN;
		x = x % 2.;
		if (x <= -1) x += 2; else if (x > 1.) x -= 2;
		if (x == 0. || x == 1.) return 0.;
		if (x == 0.5) return -1.;
		if (x == -0.5) return 1.;
		return sin(PI * x);
	}

	public static final double tanpi(double x) {
		if (Double.isNaN(x)) return x;
		if (isInfinite(x)) return Double.NaN;
		x = abs(x) % 1.;
		if (x <= -0.5) x++; else if (x > 0.5) x--;
		return (x == 0.) ? 0. : ((x == 0.5) ? Double.NaN : tan(PI * x));
	}

	/**
	 * Implementation of frexp
	 * @param x
	 * @param i an array of 1 element
	 * @return frexp
	 */
	public static final double frexp(double x, int[] i) {
		int j = 0; // From: http://www.beedub.com/Sprite093/src/lib/c/etc/frexp.c
		boolean neg = false;
		if (x < 0) {
			x = -x;
			neg = true;
		}
		if (x > 1.0) {
			while (x > 1) {
				++j;
				x /= 2;
			}
		} else if (x < 0.5) {
			while (x < 0.5) {
				--j;
				x *= 2;
			}
		}
		i[0] = j;
		return neg ? -x : x;
	}

	/**
	 * Implementation of ldexp
	 * @param x
	 * @param p
	 * @return ldexp
	 */
	public static final double ldexp(double x, int p) {
		int[] xx = new int[1]; // From: http://www.beedub.com/Sprite093/src/lib/c/etc/ldexp.c
		frexp(x, xx);
		final int MAXSHIFT = 30;
		int old_exp = xx[0];
		if (p > 0) {
			if (p + old_exp > 1023) // Overflow
				return (x < 0 ? -Double.MAX_VALUE : Double.MAX_VALUE);
			for ( ; p > MAXSHIFT; p -= MAXSHIFT) // Assuming that we can shift 30 bits at a time
				x *= (1L << MAXSHIFT);
			return x * (1L << p);
		}
		if (p + old_exp < -1023) // Underflow
			return 0;
		for ( ; p < -MAXSHIFT; p += MAXSHIFT)
			x *= 1.0/(1L << MAXSHIFT); // Multiplication is faster than division
		return x / (1L << -p);
	}

	public static final boolean isNonInt(double x)
	{	return (abs((x) - rint(x)) > 1e-7*max(1, abs(x))); }
}
