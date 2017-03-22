package ch.idsia.blip.core.inference.ve;


import ch.idsia.blip.core.common.BayesianNetwork;
import ch.idsia.blip.core.common.arcs.Undirected;
import ch.idsia.blip.core.utils.RandomStuff;
import ch.idsia.blip.core.utils.data.array.TIntArrayList;
import ch.idsia.blip.core.utils.data.hash.TIntIntHashMap;
import ch.idsia.blip.core.utils.data.set.TIntHashSet;

import java.util.*;
import java.util.logging.Logger;

import static ch.idsia.blip.core.utils.RandomStuff.pf;
import static ch.idsia.blip.core.utils.RandomStuff.randInt;
import static ch.idsia.blip.core.utils.data.ArrayUtils.find;


/**
 * Variable Elimination: query methods
 */
public class Inference {

    /**
     * Logger.
     */
    private static final Logger log = Logger.getLogger(Inference.class.getName());
    private final int verbose;
    private final BayesianNetwork bn;

    public EliminMethod elim = EliminMethod.Heu;

    /**
     * @param bayesNet network of interest
     * @param verb     verbose flag
     */
    public Inference(BayesianNetwork bayesNet, int verb) {
        this.bn = bayesNet;
        this.verbose = verb;

        HashMap<Integer, int[]> cacheElimOrder = new HashMap<Integer, int[]>();
    }

    public Inference(BayesianNetwork bayesNet, boolean verb) {
        this(bayesNet, verb ? 1 : 0);

    }

    /**
     * Shuffle "around" the greedy list
     *
     * @param greedy elimination order
     */
    private static void shuffleGreedy(int[] greedy) {
        // Number of inversions
        int n_inver = randInt(1, Math.max(1, greedy.length / 4));

        for (int i = 0; i < n_inver; i++) {
            int a = randInt(0, greedy.length - 1);
            int b = randInt(0, greedy.length - 1);

            int t = greedy[b];

            greedy[b] = greedy[a];
            greedy[a] = t;
        }
    }

    /**
     * Apply an heuristic criteria
     *
     * @param v      variable to analyze
     * @param bn     network of interest
     * @param arcs   arcs in the current network
     * @param vars   list of variable yet to sum out
     * @param method heuristic to apply
     * @return effect of the next choice under this elimination criteria
     */
    private static int heuristicEliminationCriteria(int v, BayesianNetwork bn, Undirected arcs, TIntArrayList vars, EliminMethod method) {

        if (method == EliminMethod.MinFill) {
            return arcs.findFillArcs(v, vars).length;
        }

        if (method == EliminMethod.MinWidth) {

            TIntHashSet aux = new TIntHashSet();

            aux.add(v);
            for (int i = 0; i < vars.size(); i++) {
                int o = vars.getQuick(i);

                if ((o != v) && arcs.check(v, o)) {
                    aux.add(o);
                }
            }
            for (int i : arcs.findFillArcs(v, vars)) {
                int[] aux2 = arcs.r_index(i);

                aux.add(aux2[0]);
                aux.add(aux2[1]);
            }

            int size = 1;

            for (int a : aux.toArray()) {
                size *= bn.arity(a);
            }
            return size;
        }
        return 0;
    }

    /**
     * Find nuisance (external to the current query) variables
     *
     * @param bn      network of interest
     * @param query   variables of query
     * @param ev_keys evidence applied
     * @param verbose verbose flag
     * @return list of barren variables w.r.t. query and evidence
     */
    public static TIntArrayList findNuisance(BayesianNetwork bn, TIntArrayList query, TIntIntHashMap ev_keys, boolean verbose) {

        TIntArrayList nuisance = new TIntArrayList();

        TIntArrayList relevant = new TIntArrayList();

        relevant.addAll(query);
        relevant.addAll(ev_keys.keys());

        for (int v = 0; v < bn.n_var; v++) {
            if (relevant.contains(v)) {
                continue;
            }

            if (containsOne(bn.getAncestors(v), relevant)
                    && containsOne(bn.getDescendents(v), relevant)) {
                continue;
            }

            nuisance.add(v);
        }

        nuisance.sort();

        if (verbose) {
            System.out.printf("Nuisance: %s%n", nuisance);
        }

        return nuisance;
    }

    /**
     * @param v1 first list
     * @param v2 second list
     * @return if an element from the first list is contained in the second list
     */
    private static boolean containsOne(TIntHashSet v1, TIntArrayList v2) {
        for (int i = 0; i < v2.size(); i++) {
            if (v1.contains(v2.getQuick(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compute exact query, via variable elimination.
     *
     * @param query    List of query (variables indexes)
     * @param evidence Map of evidence (variable index -> variable value)
     * @return Factor on only query variable
     */
    public BayesianFactor query(TIntArrayList query, TIntIntHashMap evidence) {

        if (verbose > 1) {
            System.out.printf("New query. Query: %s, evidence: %s\n", query,
                    evidence);
            // System.out.println("Variables: " + l_nm_var);
        }

        if (query == null) {
            return new BayesianFactor();
        }

        query.sort();

        if (evidence == null) {
            evidence = new TIntIntHashMap();
        }

        int[] ev_keys = evidence.keys();

        // Find barren
        TIntArrayList barren = findBarren(query, ev_keys);

        // Find nuisance
        // TIntArrayList nuisance = findNuisance(this, query, ev_keys, verbose);

        // Get relevant network (remove barren / nuisance)
        TIntArrayList rel2 = new TIntArrayList();

        for (int v = 0; v < bn.n_var; v++) {
            if (!barren.contains(v)) { // && !nuisance.contains(v)) {
                rel2.add(v);
            }
        }

        int[] rel = rel2.toArray();

        // Find elimination order
        TIntArrayList init = new TIntArrayList();

        for (int r : rel) {
            if (!query.contains(r) && !evidence.containsKey(r)) {
                init.add(r);
            }

        }

        int[] order = new int[0];

        long start = System.currentTimeMillis();
        if (!init.isEmpty()) {
            order = findEliminationOrder(init.toArray());
        }

        if (verbose > 1) {
            System.out.printf("Elimination order: %s\n", Arrays.toString(order));
            pf("Required: %d \n", System.currentTimeMillis() - start);
        }

        List<BayesianFactor> factors = new ArrayList<BayesianFactor>();

        for (int v : rel) {
            // Create factors from nodes
            BayesianFactor psi = new BayesianFactor(v, bn);

            // Search for evidence to reduce
            for (int e : ev_keys) {
                // If it contains evidence
                if (psi.dom.contains(e)) {
                    // Subsitute factor with reduction by evidence
                    psi = psi.reduction(e, evidence.get(e));
                }
            }

            // Search for barren to marginalize
            for (int i = 0; i < barren.size(); i++) {
                int b = barren.get(i);

                // If it contains evidence
                if (psi.dom.contains(b)) {
                    // Subsitute factor with reduction by evidence
                    psi = psi.marginalization(b);
                }
            }

            // Add factor
            if (!psi.dom.isEmpty()) {
                factors.add(psi);
            }
        }

        // For each factor in the elimination order
        for (int o : order) {

            if (verbose > 1) {
                System.out.println(verbose);
                System.out.printf("\nEliminating: %d # ", o);
            }

            // Search factor to multiply (they contain the variable to eliminate)
            List<BayesianFactor> toMult = new ArrayList<BayesianFactor>();

            for (BayesianFactor psi : factors) {
                if (psi.dom.contains(o)) {
                    toMult.add(psi);

                    if (verbose > 2) {
                        System.out.printf("\n %s", psi.dom);
                    }
                }
            }

            // Multiply factors
            BayesianFactor n_psi = toMult.get(0);

            toMult.remove(0);
            factors.remove(n_psi);
            for (BayesianFactor psi : toMult) {

                try {
                    n_psi = n_psi.product(psi);
                } catch (Exception ex) {
                    // System.out.println(n_psi + " - " + psi);
                    System.out.println(ex.getMessage());
                }
                factors.remove(psi);
            }

            n_psi.normalize();

            // Marginalize variable
            n_psi = n_psi.marginalization(o);

            // Re-add to factors list
            if (!n_psi.dom.isEmpty()) {
                factors.add(n_psi);

                if (verbose > 2) {
                    System.out.printf(" # final: %s ", n_psi.dom);
                }
            }

        }

        if (verbose > 1) {
            System.out.printf("Final factors: %d%n", factors.size());
        }

        BayesianFactor res = factors.get(0);

        factors.remove(0);

        if (!factors.isEmpty()) {
            for (BayesianFactor f : factors) {
                res = res.product(f);
            }
        }

        res.normalize();

        if (RandomStuff.different(res.dom, query)) {
            log.severe(
                    String.format(
                            "Resulting factors has a domain (%s) different than query (%s)!",
                            res.dom, query));
        }

        if (verbose > 1) {
            System.out.println(
                    String.format("Result: %s - %d%n", res.dom,
                    res.potent.length));
        }

        // You should have only one factor in list
        return res;
    }

    public BayesianFactor query(int q, TIntIntHashMap e) {
        TIntArrayList q2 = new TIntArrayList();

        q2.add(q);
        return query(q2, e);
    }

    public BayesianFactor query(TIntArrayList q) {
        return query(q, new TIntIntHashMap());
    }

    public BayesianFactor query(int q, HashMap<Integer, double[]> e) {
        TIntArrayList q2 = new TIntArrayList();

        q2.add(q);
        return query(q2, e);
    }

    /**
     * Compute exact query, via variable elimination.
     *
     * @param query    List of query (variables indexes)
     * @param evidence Map of evidence (variable index -> probabilies)
     * @return Factor on only query variable
     */
    private BayesianFactor query(TIntArrayList query, HashMap<Integer, double[]> evidence) {

        if (verbose > 1) {
            System.out.printf("New query. Query: %s, evidence: %s\n", query,
                    evidence);
            // System.out.println("Variables: " + l_nm_var);
        }

        if (query == null) {
            return new BayesianFactor();
        }

        query.sort();

        if (evidence == null) {
            evidence = new HashMap<Integer, double[]>();
        }

        Set<Integer> s = evidence.keySet();
        int[] ev_keys = new int[s.size()];
        int j = 0;

        for (int c : s) {
            ev_keys[j++] = c;
        }

        Arrays.sort(ev_keys);

        // Find barren
        TIntArrayList barren = findBarren(query, ev_keys);

        // Find nuisance
        // TIntArrayList nuisance = findNuisance(this, query, ev_keys, verbose);

        // Get relevant network (remove barren / nuisance)
        TIntArrayList rel2 = new TIntArrayList();

        for (int v = 0; v < bn.n_var; v++) {
            if (!barren.contains(v)) { // && !nuisance.contains(v)) {
                rel2.add(v);
            }
        }

        int[] rel = rel2.toArray();

        // Find elimination order
        TIntArrayList init = new TIntArrayList();

        for (int r : rel) {
            if (!query.contains(r)) {
                init.add(r);
            }

        }

        int[] order = new int[0];

        if (!init.isEmpty()) {
            order = findEliminationOrder(init.toArray());
        }

        if (verbose > 1) {
            System.out.printf("Elimination order: %s\n", Arrays.toString(order));
        }

        List<BayesianFactor> factors = new ArrayList<BayesianFactor>();

        for (int e : ev_keys) {
            BayesianFactor psi = new BayesianFactor(e, bn, evidence.get(e));

            factors.add(psi);
        }

        for (int v : rel) {
            BayesianFactor psi = new BayesianFactor(v, bn);

            factors.add(psi);
        }

        for (BayesianFactor psi : factors) {
            // Search for barren to marginalize
            for (int i = 0; i < barren.size(); i++) {
                int b = barren.get(i);

                // If it contains evidence
                if (psi.dom.contains(b)) {
                    // Subsitute factor with reduction by evidence
                    psi = psi.marginalization(b);
                }
            }

        }

        // For each factor in the elimination order
        for (int o : order) {

            if (verbose > 1) {
                System.out.println(verbose);
                System.out.printf("\nEliminating: %d # ", o);
            }

            // Search factor to multiply (they contain the variable to eliminate)
            List<BayesianFactor> toMult = new ArrayList<BayesianFactor>();

            for (BayesianFactor psi : factors) {
                if (psi.dom.contains(o)) {
                    toMult.add(psi);

                    if (verbose > 2) {
                        System.out.printf("\n %s", psi.dom);
                    }
                }
            }

            // Multiply factors
            BayesianFactor n_psi = toMult.get(0);

            toMult.remove(0);
            factors.remove(n_psi);
            for (BayesianFactor psi : toMult) {
                n_psi = n_psi.product(psi);
                factors.remove(psi);
            }

            // Marginalize variable
            n_psi = n_psi.marginalization(o);

            // Re-add to factors list
            if (!n_psi.dom.isEmpty()) {
                factors.add(n_psi);

                if (verbose > 2) {
                    System.out.printf(" # final: %s ", n_psi.dom);
                }
            }

        }

        if (verbose > 1) {
            System.out.printf("Final factors: %d%n", factors.size());
        }

        BayesianFactor res = factors.get(0);

        factors.remove(0);

        if (!factors.isEmpty()) {
            for (BayesianFactor f : factors) {
                res = res.product(f);
            }
        }

        res.normalize();

        if (RandomStuff.different(res.dom, query)) {
            log.severe(
                    String.format(
                            "Resulting factors has a domain (%s) different than query (%s)!",
                            res.dom, query));
        }

        if (verbose > 1) {
            System.out.println(
                    String.format("Result: %s - %d%n", res.dom,
                    res.potent.length));
        }

        // You should have only one factor in list
        return res;
    }

    /**
     * Find an elimination order for the given variables
     *
     * @param vars list of variables to analyze
     * @return list of variable indexes, in the elimination order
     */
    public int[] findEliminationOrder(int[] vars) {
        return findNewEliminatonOrder(vars);

        /*
         Arrays.sort(vars);

         Integer hash = Arrays.hashCode(vars);

         if (!cacheElimOrder.containsKey(hash)) {
         cacheElimOrder.put(hash, findNewEliminatonOrder(vars));
         ;
         }
         return cacheElimOrder.get(hash);
         */

    }

    private int[] findNewEliminatonOrder(int[] vars) {

        if (elim == EliminMethod.MinFill || elim == EliminMethod.MinWidth)
            return tryEliminationOrder(vars, elim);
        else if (elim == EliminMethod.Greedy) {
            Simulation sim = new Simulation(bn, verbose);
            return greedyEliminationOrder(vars, sim);
        }

        /*
         orders.add(ord);

         for (TIntArrayList ord: orders) {
         double eval = simulateInference(bn, ord, verbose);

         if (eval < bestEval) {
         bestOrd = ord.c;
         bestEval = eval;
         }
         }*/

        Simulation sim = new Simulation(bn, verbose);

        int[] ord = vars.clone();
        int[] bestOrd = vars.clone();
        double bestEval = Double.MAX_VALUE;

        // Try some random
        for (int var : vars) {

            shuffleGreedy(ord);

            double eval = sim.simulateInference(ord);

            if (eval < bestEval) {
                bestOrd = ord.clone();
                bestEval = eval;
            }
        }

        return bestOrd;
    }

    /**
     * Determine the greedy elimination order - minimum size of the resulting factors for each variable
     *
     * @param n_vars list of variables to analyze
     * @param sim
     * @return greedy elimination order
     */
    private int[] greedyEliminationOrder(int[] n_vars, Simulation sim) {

        TIntArrayList vars = new TIntArrayList(n_vars);

        int[] order = new int[n_vars.length];

        List<Simulation.FakeFactor> factors = new ArrayList<Simulation.FakeFactor>();

        for (int i = 0; i < vars.size(); i++) {
            Simulation.FakeFactor psi = new Simulation.FakeFactor(vars.get(i),
                    bn);

            factors.add(psi);
        }

        for (int i = 0; i < n_vars.length; i++) {

            int best_size = Integer.MAX_VALUE;
            int best_v = -1;

            for (int v : vars.toArray()) {

                List<Simulation.FakeFactor> n_factors = new ArrayList<Simulation.FakeFactor>();

                n_factors.addAll(factors);
                sim.simulateElimin(v, n_factors);

                // Compute current size
                int size = 0;

                for (Simulation.FakeFactor psi : n_factors) {
                    size += psi.size;
                }

                if (size < best_size) {
                    best_size = size;
                    best_v = v;
                }

            }

            sim.simulateElimin(best_v, factors);
            order[i] = best_v;
            vars.remove(best_v);
        }

        return order;
    }

    /**
     * Try to apply an heuristic for finding a good elimination order
     *
     * @param n_vars list of variables to analyze
     * @param method heuristic to apply
     * @return the elimination order
     */
    public int[] tryEliminationOrder(int[] n_vars, EliminMethod method) {

        int[] order = new int[n_vars.length];
        TIntArrayList vars = new TIntArrayList();

        vars.addAll(n_vars);

        // Build matrix of arcs in the graph
        Undirected arcs = new Undirected(bn);

        int k = 0;

        // Continue to find best variable for elimination
        while (!vars.isEmpty()) {
            int bestV = -1;
            int bestHeur = Integer.MAX_VALUE;

            // Evaluate each remaining variable
            for (int i = 0; i < vars.size(); i++) {
                int v = vars.getQuick(i);
                int heur = heuristicEliminationCriteria(v, bn, arcs, vars,
                        method);

                // System.out.println("heur: " + heur);
                if (heur < bestHeur) {
                    bestHeur = heur;
                    bestV = v;
                }
            }

            // Update structures
            order[k++] = bestV;
            vars.remove(bestV);

            for (int i : arcs.findFillArcs(bestV, vars)) {
                arcs.mark(i);

             //    int[] arr = arcs.r_index(thread);
                // System.out.println(String.format("%s -> %s [color='red'];", arr[0], arr[1]));
            }
            // System.out.println(" --- ");
        }
        return order;
    }

    /**
     * Find barren (leaf, or parents of leaf, external to the current query)
     *
     * @param query   variables of query
     * @param ev_keys evidence applied
     * @return list of barren variables w.r.t. query and evidence
     */
    private TIntArrayList findBarren(TIntArrayList query, int[] ev_keys) {

        TIntArrayList barren = new TIntArrayList();

        Arrays.sort(ev_keys);

        TIntArrayList rev_topol = new TIntArrayList();
        rev_topol.addAll(bn.getTopologicalOrder());

        rev_topol.reverse();

        for (int i = 0; i < rev_topol.size(); i++) {
            int v = rev_topol.get(i);

            if (isBarren(query, ev_keys, barren, v)) {
                barren.add(v);
            }
        }

        barren.sort();

        if (verbose > 2) {
            System.out.printf("Barren: %s\n", barren);
        }

        return barren;
    }

    /**
     * Determine if the variable is barren in the current situation.
     *
     * @param query    variables of query
     * @param evidence evidence applied
     * @param barren   dand barren variables
     * @param v        variable to analyze
     * @return if the variable is barren w.r.t. query, evidence, and dand barren
     */
    private boolean isBarren(TIntArrayList query, int[] evidence, TIntArrayList barren, int v) {
        if (query.contains(v)) {
            return false;
        }
        if (find(v, evidence)) {
            return false;
        }
        for (int c = 0; c < bn.n_var; c++) {
            // If it's not a children of v continue
            if (Arrays.binarySearch(bn.parents(c), v) < 0) {
                continue;
            }
            // If it's a children, but it's not in barren, then v is not barren
            if (!barren.contains(c)) {
                return false;
            }
        }
        return true;
    }

    public BayesianFactor query(int i) {
        TIntArrayList q = new TIntArrayList();

        q.add(i);
        return query(q);
    }

    public int mpe(int i, TIntIntHashMap m) {
        BayesianFactor v = query(i, m);
        int max = -1;
        double max_p = -Double.MAX_VALUE;
        for (int j = 0; j < v.potent.length; j++) {
            if (v.potent[j] > max_p) {
                max_p = v.potent[j];
                max = j;
            }
        }
        return max;
    }

    /**
     * Possible heuristics to apply in the elimination order decision
     */
    public enum EliminMethod {

        /**
         * Minimum number of new arcs to add
         */
        MinFill,
        Heu,
        Greedy,
        /**
         * Minimum width of the joint tree
         */
        MinWidth
    }

}

