package ch.idsia.ipp.core.learn.solver.samp;

import ch.idsia.ipp.core.common.arcs.Directed;
import ch.idsia.ipp.core.utils.data.array.TIntArrayList;

import java.util.Random;

import static ch.idsia.ipp.core.utils.data.ArrayUtils.expandArray;
import static ch.idsia.ipp.core.utils.data.ArrayUtils.find;

/**
 * Samples topological orders - with constraints (given by skeleton)
 */
public class SkelSampler implements Sampler{

    private int[][] parents;
    private int[][] childrens;

    private int n;

    // for each variable its position
    private int[] state;

    Random r;

    private int burn_iterat = 100;

    public static int[][] getParents(Directed skel) {
        int n = skel.n;
        int[][] parents = new int[n][];
        for (int i = 0; i < n; i++)
            parents[i] = new int[0];

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (i != j && !skel.check(i, j) && skel.check(j, i))
                    parents[i] = expandArray(parents[i], j);

        return parents;
    }

    public SkelSampler(Directed skel) {
        this(getParents(skel));
    }

        public SkelSampler(int[][] parents) {
            this.parents = parents;
            this.n = parents.length;


        r = new Random(System.currentTimeMillis());

        this.childrens = new int[n][];
        for (int i = 0; i < n; i++)
            childrens[i] = new int[0];

        for (int i = 0; i < n; i++)
            for (int p: parents[i]) {
                childrens[p] = expandArray(childrens[p], i);
            }
    }

    @Override
    // Sample a start state, the cycle some time (burn-in)
    public void init() {

        TIntArrayList todo = new TIntArrayList();
        TIntArrayList done = new TIntArrayList();

        for (int i = 0; i < n; i++)
            todo.add(i);

        state = new int[n];
        // choose a variable for position thread
        for (int i = 0; i < n; i++) {
            TIntArrayList avail = new TIntArrayList();
            for (int j = 0; j < todo.size(); j++) {
                int a = todo.get(j);
                // if every parent has already been inserted
                if (okForNow(a, done)) avail.add(a);
            }
            int c = avail.get(r.nextInt(avail.size()));
            state[c] = i;
            todo.remove(c);
            done.add(c);
        }

        sample();
    }

    private boolean okForNow(int j, TIntArrayList done) {
        for (int p: parents[j])
            if (!done.contains(p))
                return  false;
        return true;
    }

    @Override
    public int[] sample() {
        for (int i = 0; i < burn_iterat; i++) {
            nextState();
        }

        int[] ord = new int[n];
        for (int i = 0; i< n; i++)
            ord[state[i]] = i;

        return ord;
    }

    private void nextState() {

        // Choose random pair
        int i = r.nextInt(n);
        int j = i;
        while(i == j)
            j = r.nextInt(n);

        if (find(j, parents[i]) || find(i, parents[j]))
            return;

        if (!swapOk(i, j))
            return;

        if (!swapOk(j, i))
            return;

        int t = state[i];
        state[i] = state[j];
        state[j] = t;

    }

    /**
     * It's ok for variable thread to take the place of variable j?
     */
    private boolean swapOk(int i, int j) {

        for (int p: parents[i]) {
            if (state[j] < state[p])
                return  false;
        }

        for (int c: childrens[i]) {
            if (state[j] > state[c])
                return  false;
        }

        return true;
    }
}
