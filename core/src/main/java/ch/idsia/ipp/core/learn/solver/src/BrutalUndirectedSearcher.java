package ch.idsia.ipp.core.learn.solver.src;

import ch.idsia.ipp.core.common.arcs.Und;
import ch.idsia.ipp.core.learn.solver.brtl.BrutalUndirectedSolver;
import ch.idsia.ipp.core.utils.ParentSet;
import ch.idsia.ipp.core.utils.data.ArrayUtils;
import ch.idsia.ipp.core.utils.data.SIntSet;
import ch.idsia.ipp.core.utils.data.common.TIntIterator;
import ch.idsia.ipp.core.utils.data.set.TIntHashSet;

import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import static ch.idsia.ipp.core.utils.RandomStuff.pf;
import static ch.idsia.ipp.core.utils.data.ArrayUtils.*;

/**
 * Brutal on Undirected
 */
public class BrutalUndirectedSearcher implements Searcher {

    private final BrutalUndirectedSolver solver;

    private final int tw;

    private final Und und;

    private final int n_var;
    private final int thread;

    private int[] vars;

    private Und new_und;

    private int new_sk;

    private TreeSet<SIntSet> handles;

    private int[] initCl;

    private long start;

    private int cnt;

    public BrutalUndirectedSearcher(BrutalUndirectedSolver solver, int tw, Und und) {
        this.solver = solver;
        this.tw = tw;
        this.und = und;

        this.n_var = und.n;

            this.thread = solver.thread;
            solver.thread += 1;

    }

    @Override
    public void init(ParentSet[][] scores) {
        // does nothing with scores
    }

    // Greedily optimize a network!
    @Override
    public ParentSet[] search(int[] vars) {

        this.vars = vars;

        start = System.currentTimeMillis();
        cnt = 1;

        // clear all
        clear();

        // Init the first maximal clique
        initClique();

        Result res;

        TIntHashSet todo = new TIntHashSet();


        // Greedy behaviour (best variable at each point)
        if (solver.behaviour == 0) {
            // For every new variable, add a new maximal clique
            for (int i = tw + 1; i < n_var; i++) {
                int v = vars[i];
                todo.add(v);
            }

            while(!todo.isEmpty()) {
                // Search the best subset of neighborhood - existing handle
                res = bestVariable(todo);

//                if (res.neigh.length == 0) {
//                    pf ("Thread %d, null neighborhood!!! \n", thread);
//                    return null;
//                }

                // pf("Chosen %d \n", res.v);
                if (go(res, todo)) return null;

                todo.remove(res.v);

            }
        } else {
            // Stochastic behaviour (follow sampling)
            for (int i = tw + 1; i < n_var; i++) {
                int v = vars[i];

                res = bestSubset(v);

                // pf("Chosen %d \n", res.v);
                if (go(res, todo)) return null;

            }
        }


        solver.newUndirected(new_sk, new_und);

        return null;
    }

    private boolean go(Result res, TIntHashSet todo) {
        // update the chosen parent set
        update(res.v, res.neigh);

        // add the new handlers
        // add new handler = new clique with size tw
        // created  just now
        SIntSet h = res.handle;
        for (int elim : h.set) {
            addHandler(new SIntSet(reduceAndIncreaseArray(h.set, res.v, elim)));
        }


        solver.checkTime();
        if (!solver.still_time) {
            return true;
        }

        double elapsed = (System.currentTimeMillis() - start) / 1000.0;
        if (elapsed > cnt * 10) {
            pf ("Thread %d, remaining to look: %d, score: %d - %.2f \n", thread, todo.size(), new_sk, elapsed);
            cnt += 1;
        }
        return false;
    }

    private void update(int v, int[] fin) {
        for (int f: fin) {
            new_und.mark(v, f);
            new_sk += 1;
            // pf("Marking %d %d \n", v, f);

        }
    }

    private void initClique() {
        initCl = new int[tw+1];
        System.arraycopy(vars, 0, initCl, 0, tw +1);

        // Update parent set
        for (int i1 = 0; i1 < initCl.length; i1++) {
            int v1 = initCl[i1];
            for (int i2 = i1 + 1; i2 < initCl.length; i2++) {
                int v2 = initCl[i2];

                if (find(v1, und.neigh[v2])) {
                    new_und.mark(v1, v2);
                    new_sk += 1;
                    // pf("Marking %d %d \n", v1, v2);
                }
            }
        }

        // Add new handlers
        for (int v : initCl) {
            addHandler(new SIntSet(reduceArray(initCl,v)));
        }
    }

    protected void clear() {

        new_und = new Und(und.n);
        new_sk = 0;

        // List of handlers for parent sets
        // (list of quasi-maximal cliques, where a variable
        // can choose its parent sets)
        handles = new TreeSet<SIntSet>();
    }

    protected void addHandler(SIntSet sIntSet) {
        handles.add(sIntSet);
    }

    protected Result bestVariable(TIntHashSet todo) {

        int max_sk = 0;
        SIntSet max_h = null;
        int max_v = -1;

        TIntIterator it = todo.iterator();
        while (it.hasNext()) {
            int v = it.next();
            for (SIntSet h : handles) {

                int sk = getSk(und.neigh[v], h.set, v);

                if (sk > max_sk) {
                    max_h = h;
                    max_sk = sk;
                    max_v = v;
                }
            }
        }

        if (max_sk == 0)
            return new Result(todo.iterator().next(), new int[0], rand(handles));

        int[] fin = ArrayUtils.intersect(und.neigh[max_v], max_h.set);

        return new Result(max_v, fin, max_h);
    }

    protected Result bestSubset(int v) {

        int max_sk = 0;
        SIntSet max_h = null;

            for (SIntSet h : handles) {

                int sk = getSk(und.neigh[v], h.set, v);

                if (sk > max_sk) {
                    max_h = h;
                    max_sk = sk;
                }
            }


        if (max_sk == 0)
            return new Result(v, new int[0], rand(handles));

        int[] fin = ArrayUtils.intersect(und.neigh[v], max_h.set);

        return new Result(v, fin, max_h);
    }

    private int getSk(int[] neigh, int[] set, int v) {
        int sk = 0;
        for (int n: neigh) {
            if (!find(n, new_und.neigh[v]) && find (n, set))
                sk += 1;
        }
        return sk;
    }

    protected SIntSet rand(TreeSet<SIntSet> h) {
        int v = new Random().nextInt(h.size());
        Iterator<SIntSet> i = h.iterator();
        while (v > 1) {
            i.next();
            v--;
        }
        return i.next();
    }

    private class Result {

        public SIntSet handle;
        public int v;
        public int[] neigh;

        public Result(int v, int[] neigh, SIntSet handle) {
            this.neigh = neigh;
            this.v = v;
            this.handle = handle;
        }
    }
}
