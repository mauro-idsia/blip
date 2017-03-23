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
import jdistlib.rng.RandomEngine;

import static jdistlib.math.MathFunctions.isInfinite;

public class ChiSquare extends GenericDistribution {
	public static double density(double x, double df, boolean give_log) {
	    return Gamma.density(x, df / 2., 2., give_log);
	}

	public static double cumulative(double x, double df, boolean lower_tail, boolean log_p) {
	    return Gamma.cumulative(x, df/2., 2., lower_tail, log_p);
	}

	public static double quantile(double p, double df, boolean lower_tail, boolean log_p) {
	    return Gamma.quantile(p, 0.5 * df, 2.0, lower_tail, log_p);
	}

	public static double random(double df, RandomEngine random) {
	    if (isInfinite(df) || df < 0.0) return Double.NaN;
	    return Gamma.random(df / 2.0, 2.0, random);
	}

	public static double[] random(int n, double df, RandomEngine random) {
		double[] rand = new double[n];
		for (int i = 0; i < n; i++)
			rand[i] = random(df, random);
		return rand;
	}

	private final double df;

	public ChiSquare(double df) {
		this.df = df;
	}

	@Override
	public double density(double x, boolean log) {
		return density(x, df, log);
	}

	@Override
	public double cumulative(double p, boolean lower_tail, boolean log_p) {
		return cumulative(p, df, lower_tail, log_p);
	}

	@Override
	public double quantile(double q, boolean lower_tail, boolean log_p) {
		return quantile(q, df, lower_tail, log_p);
	}

	@Override
	public double random() {
		return random(df, random);
	}
}
