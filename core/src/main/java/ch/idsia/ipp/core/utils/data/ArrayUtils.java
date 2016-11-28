package ch.idsia.ipp.core.utils.data;


import ch.idsia.ipp.core.utils.data.array.TIntArrayList;
import ch.idsia.ipp.core.utils.data.set.TIntHashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class ArrayUtils {

    public static void swap(int[] a, int i, int j) {
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    public static void shuffleArray(int[] ar) {
        Random rnd = new Random(System.currentTimeMillis());

        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swapArray
            int a = ar[index];

            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public static double mean(double[] nums) {
        double s = 0;

        for (double n : nums) {
            s += n;
        }
        return s / nums.length;
    }

    public static int[] union(int[] dom1, int[] dom2) {

        int[] aux = new int[dom1.length + dom2.length];
        int i = 0;
        int j = 0;
        int k = 0;

        while ((i < dom1.length) && (j < dom2.length)) {
            if (dom1[i] < dom2[j]) {
                aux[k++] = dom1[i++];
            } else {
                aux[k++] = dom2[j++];
            }
        }

        while (i < dom1.length) {
            aux[k++] = dom1[i++];
        }

        while (j < dom2.length) {
            aux[k++] = dom2[j++];
        }

        int[] uni = new int[k];

        System.arraycopy(aux, 0, uni, 0, k);
        return uni;
    }

    /**
     * Compute number of intersections of array of int
     *
     * @param arr1 first array
     * @param arr2 second array
     * @return dom_size of intersection
     */
    public static int intersectN(int[] arr1, int[] arr2) {
        int n = 0;
        int i = 0;
        int j = 0;
        int n1 = arr1.length;
        int n2 = arr2.length;

        while ((i < n1) && (j < n2)) {
            if (arr1[i] < arr2[j]) {
                i++;
            } else if (arr2[j] < arr1[i]) {
                j++;
            } else {

                /* equal! */
                n++;
                i++;
                j++;
            }
        }

        return n;
    }

    /**
     * Compute intersection of array of int
     *
     * @param arr1 first array
     * @param arr2 second array
     * @return intersection
     */
    public static int[] intersect(int[] arr1, int[] arr2) {

        int i = 0;
        int j = 0;
        int n1 = arr1.length;
        int n2 = arr2.length;

        TIntArrayList aux = new TIntArrayList((n1 < n2) ? n1 : n2);

        while ((i < n1) && (j < n2)) {
            if (arr1[i] < arr2[j]) {
                i++;
            } else if (arr2[j] < arr1[i]) {
                j++;
            } else {

                /* equal! */
                aux.add(arr1[i]);
                i++;
                j++;
            }
        }

        return aux.toArray();
    }

    /**
     * Reduce the array
     *
     * @param set_p original parent set
     * @param p     variable to remove
     * @return new parent set
     */
    public static int[] reduceArray(int[] set_p, int p) {
        int[] set_p2 = new int[set_p.length - 1];
        int i = 0;

        for (int p2 : set_p) {
            if (p2 != p) {
                set_p2[i++] = p2;
            }
        }
        return set_p2;
    }

    /**
     * Expand the array (keeps ordering)
     *
     * @param set_p original parent set
     * @param p2    new variable to add
     * @return new parent set
     */
    public static int[] expandArray(int[] set_p, int p2) {
        int[] set_p2 = new int[set_p.length + 1];
        boolean b = true;
        int j = 0;

        for (int i = 0; i < set_p2.length; i++) {
            if (b && (((j == set_p.length) || (p2 < set_p[j])))) {
                set_p2[i] = p2;
                b = false;
            } else {
                set_p2[i] = set_p[j++];
            }
        }
        return set_p2;
    }

    // Adds the new variable, and removes the dand one
    static public int[] reduceAndIncreaseArray(int[] set_p, int add, int elim) {
        int[] set_p2 = new int[set_p.length];

        set_p2[0] = add;
        int i = 1;

        for (int p2 : set_p) {
            if (p2 != elim) {
                set_p2[i++] = p2;
            }
        }

        return set_p2;
    }

    /**
     * Swap elements in list
     *
     * @param v list
     * @param i index first element
     * @param j index second element
     */
    public static void swapArray(int[] v, int i, int j) {
        int t = v[i];

        v[i] = v[j];
        v[j] = t;
    }

    public static long parentSetToHash(int[] pS, int n) {
        if (pS.length == 0) {
            return -1;
        }

        long hash_p = 0;
        long mx = 1;

        for (int p : pS) {
            hash_p += mx * p;
            mx *= n;
        }
        return hash_p;
    }

    /**
     * @param hash hash of the parent set
     * @return parents in the set (indexes)
     */
    public static int[] hashToParentSet(long hash, int n) {

        if (hash == -1) {
            return new int[0];
        }
        if (hash == 0) {
            return new int[] { 0};
        }

        int s = (int) (Math.log(hash) / Math.log(n)) + 1;
        int[] set_p = new int[s];

        int i = 0;

        while (hash > 0) {
            set_p[i++] = (int) (hash % n);

            if (hash == 1) {
                hash = 0;
            } else {
                hash /= n;
            }
        }

        Arrays.sort(set_p);
        return set_p;
    }

    public static void cloneArray(int[] a, int[] b) {
        System.arraycopy(a, 0, b, 0, a.length);
    }

    public static int[] cloneArray(int[] a) {
        int[] b = new int[a.length];
        System.arraycopy(a, 0, b, 0, a.length);
        return b;
    }

    public static String toString(int[] v) {

        StringBuilder sb = new StringBuilder();
        for (int n : v) {
            if (sb.length() > 0) sb.append(',');
            sb.append(n);
        }
        return sb.toString();

    }

    public static void cloneArray(short[] a, short[] b) {
        System.arraycopy(a, 0, b, 0, a.length);
    }

    public static void cloneArray(double[] a, double[] b) {
        System.arraycopy(a, 0, b, 0, a.length);
    }

    public static void cloneArray(boolean[] a, boolean[] b) {
        System.arraycopy(a, 0, b, 0, a.length);
    }

    public static void cloneArray(TIntArrayList[] a, TIntArrayList[] b) {
        System.arraycopy(a, 0, b, 0, a.length);
    }

    public static void cloneArray(TIntHashSet[] a, TIntHashSet[] b) {
        System.arraycopy(a, 0, b, 0, a.length);
    }

    public static int max(int[] v) {
        int a = v[0];

        for (int b : v) {
            if (b > a) {
                a = b;
            }
        }
        return a;
    }

    public static int max_index(int[] v) {
        int a = v[0];
        int i = 0;

        for (int j = 1; j < v.length; j++) {
            if (v[j] > a) {
                a = v[j];
                i = j;
            }
        }
        return i;
    }

    public static int max_index(double[] v) {
        double a = v[0];
        int i = 0;

        for (int j = 1; j < v.length; j++) {
            if (v[j] > a) {
                a = v[j];
                i = j;
            }
        }
        return i;
    }

    public static boolean find(int a, int[] l) {
        return Arrays.binarySearch(l, a) >= 0;
    }

    public static boolean sameArray(int[] a, int[] b) {
        if (a.length != b.length) {
            return false;
        }

        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }

        return true;
    }

    public static int[] unique(int[] s) {
        TIntHashSet aux = new TIntHashSet();

        aux.addAll(s);
        return aux.toArray();
    }

    public static int[] arr(int n, int d) {
        int[] a = new int[n];

        for (int i = 0; i < n; i++) {
            a[i] = d;
        }
        return a;
    }

    public static int[] ones(int n) {
        return arr(n, 1);
    }

    public static int[] zeros(int n) {
        return arr(n, 0);
    }

    public static List<int[]> powerSet(int[] orig) {
        // System.out.println(Arrays.toString(orig));
        List<int[]> sets = new ArrayList<int[]>();

        if (orig.length == 0) {
            sets.add(new int[0]);
            return sets;
        }

        Integer head = orig[0];
        int[] rest = new int[orig.length - 1];

        System.arraycopy(orig, 1, rest, 0, orig.length - 1);

        for (int[] set : powerSet(rest)) {
            int[] n_set = new int[set.length + 1];

            n_set[0] = head;
            System.arraycopy(set, 0, n_set, 1, set.length);
            Arrays.sort(n_set);
            sets.add(set);
            sets.add(n_set);
        }

        return sets;

    }

    private static void getSubsets2(int[] superSet, int k, int idx, TIntArrayList current, List<int[]> solution) {
        // successful stop clause
        if (current.size() == k) {
            solution.add(current.toArray());
            return;
        }
        // unseccessful stop clause
        if (idx == superSet.length) {
            return;
        }
        Integer x = superSet[idx];

        current.add(x);
        // "guess" x is in the subset
        getSubsets2(superSet, k, idx + 1, current, solution);
        current.remove(x);
        // "guess" x is not in the subset
        getSubsets2(superSet, k, idx + 1, current, solution);
    }

    public static List<int[]> getSubsets(int[] superSet, int k) {
        List<int[]> res = new ArrayList<int[]>();

        getSubsets2(superSet, k, 0, new TIntArrayList(), res);
        return res;
    }

    public static void reverse(int[] v) {
        for(int i = 0; i < v.length / 2; i++)
        {
            int temp = v[i];
            v[i] = v[v.length - i - 1];
            v[v.length - i - 1] = temp;
        }
    }
}
