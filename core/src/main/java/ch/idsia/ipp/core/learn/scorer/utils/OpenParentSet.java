package ch.idsia.ipp.core.learn.scorer.utils;


import ch.idsia.ipp.core.utils.data.ArrayUtils;
import ch.idsia.ipp.core.utils.data.SIntSet;


/**
 * Entry of a parent set in the linked-list queue.
 */
public class OpenParentSet implements Comparable<OpenParentSet> {

    private final SIntSet p1;
    public final int p2;
    public final double sk;

    public OpenParentSet(SIntSet p1, int p2, double sk) {
        this.p1 = p1;
        this.p2 = p2;
        this.sk = sk;
    }

    @Override
    public int compareTo(OpenParentSet other) {
        if (sk < other.sk) {
            return 1;
        }
        return -1;
    }

    public String toString() {
        return String.format("(%s %d %.3f)", p1.toString(), p2, sk);
    }

    public int[] getNewParentSet() {
        return ArrayUtils.expandArray(p1.set, p2);
    }
}

