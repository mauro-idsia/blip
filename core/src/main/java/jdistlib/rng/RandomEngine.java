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
 * @author Roby Joehanes
 */
public abstract class RandomEngine {
    long mSeed;

    public void setSeed(long seed) {
        mSeed = seed;
    }

    public long getSeed() {
        return mSeed;
    }

    public abstract double nextGaussian();

    public abstract double nextDouble();

    public abstract float nextFloat();

    public abstract int nextInt();

    public abstract int nextInt(int n);

    public abstract long nextLong();

    public abstract long nextLong(long l);

    public double random() {
        return nextDouble();
    }

    public abstract RandomEngine clone();
}
