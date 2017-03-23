/*
 * Roby Joehanes
 * 
 * Copyright 2007 Roby Joehanes
 * This file is distributed under the GNU General Public License version 3.0.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package jdistlib.rng;

/**
 * <P>Implementation of CMWC4096 (Complementary-multiply-with-carry) random number generator
 * by George Marsaglia. The period is approximately 2^131086.
 *
 * <P><a href="http://groups.google.com/group/comp.lang.c/browse_thread/thread/a9915080a4424068/">Original post</a>
 * <P>Paper: Marsaglia, George. 2003. Random number generators. Journal of Modern Applied
 * Statistical Methods, 2(1): 2-13. (<a href="http://tbf.coe.wayne.edu/jmasm/vol2_no1.pdf">link</a>)
 *
 * <P>Weakness: The quality is dependent upon the choice of seed. Some seed actually may weaken
 * the algorithm significantly. A known good seed is 362436. Marsaglia suggested that any seed
 * less than 809430660 is fine, but many detractors criticize that it is not necessarily so.
 *
 * @author Roby Joehanes
 *
 */
public class RandomCMWC extends RandomEngine
{
	private final long[] mBuffer = new long[4096];
	private int mIndex = 4095;
	private boolean mHaveNextGaussian = false;
	private double mNextGaussian;

	{	// Dyn-initState
		mSeed = 362436; //  choose random initial < 809430660
	}

	@Override
	public final int nextInt()
	{
		long t, x;
		mIndex = (mIndex + 1) & 4095;
		t = 18782L * mBuffer[mIndex] + mSeed;
		mSeed = (t >> 32); x = t + mSeed;
		if (x < mSeed) { x++; mSeed++; }
		return (int) (mBuffer[mIndex] = 0xfffffffe - x);
	}

	@Override
	public final int nextInt(final int n)
	{
		long t, x;
		if ((n & -n) == n) // if n is even
		{
			mIndex = (mIndex + 1) & 4095;
			t = 18782L * mBuffer[mIndex] + mSeed;
			mSeed = (t >> 32); x = t + mSeed;
			if (x < mSeed) { x++; mSeed++; }
			return (int) ((n * ((mBuffer[mIndex] = 0xfffffffe - x) >>> 1) ) >> 31);
		}
		int bits, val;
		do
		{
			mIndex = (mIndex + 1) & 4095;
			t = 18782L * mBuffer[mIndex] + mSeed;
			mSeed = (t >> 32); x = t + mSeed;
			if (x < mSeed) { x++; mSeed++; }
			bits = (int) ((mBuffer[mIndex] = 0xfffffffe - x) >>> 1);
			val = bits % n;
		} while (bits - val + (n-1) < 0);
		return val;
	}

	@Override
	public final long nextLong()
	{
		long t, x;
		mIndex = (mIndex + 1) & 4095;
		t = 18782L * mBuffer[mIndex] + mSeed;
		mSeed = (t >> 32); x = t + mSeed;
		if (x < mSeed) { x++; mSeed++; }
		return (mBuffer[mIndex] = 0xfffffffe - x);
	}

	@Override
	public final long nextLong(final long n)
	{
		long t, x;
		if ((n & -n) == n) // if n is even
		{
			mIndex = (mIndex + 1) & 4095;
			t = 18782L * mBuffer[mIndex] + mSeed;
			mSeed = (t >> 32); x = t + mSeed;
			if (x < mSeed) { x++; mSeed++; }
			return (int) ((n * ((mBuffer[mIndex] = 0xfffffffe - x) >>> 1) ) >> 31);
		}
		long bits, val;
		do
		{
			mIndex = (mIndex + 1) & 4095;
			t = 18782L * mBuffer[mIndex] + mSeed;
			mSeed = (t >> 32); x = t + mSeed;
			if (x < mSeed) { x++; mSeed++; }
			bits = (mBuffer[mIndex] = 0xfffffffe - x) >>> 1;
			val = bits % n;
		} while (bits - val + (n-1) < 0);
		return val;
	}

	@Override
	public final double nextGaussian()
	{
		if (mHaveNextGaussian)
		{
			mHaveNextGaussian = false;
			return mNextGaussian;
		}
		double v1, v2, s;

		do
		{
			int y, z, a, b;
			long t, x;

			// Generate four random integers, unrolled
			mIndex = (mIndex + 1) & 4095;
			t = 18782L * mBuffer[mIndex] + mSeed;
			mSeed = (t >> 32); x = t + mSeed;
			if (x < mSeed) { x++; mSeed++; }
			y = (int) (mBuffer[mIndex] = 0xfffffffe - x);

			mIndex = (mIndex + 1) & 4095;
			t = 18782L * mBuffer[mIndex] + mSeed;
			mSeed = (t >> 32); x = t + mSeed;
			if (x < mSeed) { x++; mSeed++; }
			z = (int) (mBuffer[mIndex] = 0xfffffffe - x);

			mIndex = (mIndex + 1) & 4095;
			t = 18782L * mBuffer[mIndex] + mSeed;
			mSeed = (t >> 32); x = t + mSeed;
			if (x < mSeed) { x++; mSeed++; }
			a = (int) (mBuffer[mIndex] = 0xfffffffe - x);

			mIndex = (mIndex + 1) & 4095;
			t = 18782L * mBuffer[mIndex] + mSeed;
			mSeed = (t >> 32); x = t + mSeed;
			if (x < mSeed) { x++; mSeed++; }
			b = (int) (mBuffer[mIndex] = 0xfffffffe - x);

			v1 = 2 * (((((long)(y >>> 6)) << 27) + (z >>> 5)) / (double)(1L << 53)) - 1;
			v2 = 2 * (((((long)(a >>> 6)) << 27) + (b >>> 5)) / (double)(1L << 53)) - 1;
			s = v1 * v1 + v2 * v2;
		} while (s >= 1 || s==0);
		double multiplier = Math.sqrt(-2 * Math.log(s)/s);
		mHaveNextGaussian = true;
		mNextGaussian = v2 * multiplier;
		return v1 * multiplier;
	}

	// Shamelessly taken from Colt
	public double nextDouble() {
		double nextDouble;

		do {
			// -9.223372036854776E18 == (double) Long.MIN_VALUE
			// 5.421010862427522E-20 == 1 / Math.pow(2,64) == 1 / ((double) Long.MAX_VALUE - (double) Long.MIN_VALUE);
			nextDouble = ((double) nextLong() - -9.223372036854776E18)  *  5.421010862427522E-20;
		}
		// catch loss of precision of long --> double conversion
		while (! (nextDouble>0.0 && nextDouble<1.0));

		// --> in (0.0,1.0)
		return nextDouble;
	}

	public float nextFloat() {
		return (float) nextDouble();
	}

	public RandomCMWC clone() {
		return new RandomCMWC();
	}
}
