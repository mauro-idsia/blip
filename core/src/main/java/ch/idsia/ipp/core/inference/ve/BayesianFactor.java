package ch.idsia.ipp.core.inference.ve;


import ch.idsia.ipp.core.common.BayesianNetwork;
import ch.idsia.ipp.core.utils.data.array.TDoubleArrayList;
import ch.idsia.ipp.core.utils.data.array.TIntArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static ch.idsia.ipp.core.utils.data.ArrayUtils.cloneArray;


/**
 * Inference factor based on Bayesian probabilities
 */
public class BayesianFactor {

    /**
     * Flag if we want to use log computations
     */
    public static boolean logComp = true;

    /**
     * Domain
     */
    public final TIntArrayList dom;

    /**
     * Cardinality of each variable
     */
    public final TIntArrayList card;

    /**
     * Stride of each variable
     */
    public TIntArrayList stride;

    /**
     * Total size of potentials
     */
    private int size;

    /**
     * Potentials (ordered by variables in numerical order)
     */
    final double[] potent;

    /**
     * Void constructor.
     */
    public BayesianFactor() {
        dom = new TIntArrayList();
        card = new TIntArrayList();
        updateStride();
        potent = new double[size];
    }

    /**
     * Normal construction: provide domain and arities of variable of interest
     *
     * @param n_dom  new domain
     * @param n_card new cardinality
     */
    public BayesianFactor(TreeSet<Integer> n_dom, Map<Integer, Integer> n_card) {
        // Set domain
        dom = new TIntArrayList();
        card = new TIntArrayList();
        for (int v : n_dom) {
            dom.add(v);
            card.add(n_card.get(v));
        }

        // Set stride
        updateStride();

        // Prepare potentials
        potent = new double[size];
    }

    /**
     * Construction as reduction of the given factor by the given variable
     *
     * @param psi original factor
     * @param v   variable to remove
     */
    public BayesianFactor(BayesianFactor psi, int v) {

        // Set domain
        dom = new TIntArrayList();
        dom.addAll(psi.dom);

        // Set cardinality
        card = new TIntArrayList();
        card.addAll(psi.card);

        int i_v = dom.indexOf(v);

        card.removeAt(i_v);
        dom.removeAt(i_v);

        // Set stride
        updateStride();

        // Prepare potentials
        potent = new double[size];
    }

    /**
     * Construction as union of the given factors
     *
     * @param psi1 first factor
     * @param psi2 second factor
     */
    public BayesianFactor(BayesianFactor psi1, BayesianFactor psi2) {

        // Set domain
        dom = new TIntArrayList();
        card = new TIntArrayList();

        int i = 0;
        int j = 0;

        while ((i < psi1.dom.size()) && (j < psi2.dom.size())) {
            if (psi1.dom.get(i) == psi2.dom.get(j)) {
                dom.add(psi1.dom.get(i));
                card.add(psi1.card.get(i));
                i++;
                j++;
            } else if (psi1.dom.get(i) < psi2.dom.get(j)) {
                dom.add(psi1.dom.get(i));
                card.add(psi1.card.get(i));
                i++;
            } else {
                dom.add(psi2.dom.get(j));
                card.add(psi2.card.get(j));
                j++;
            }
        }

        while (i < psi1.dom.size()) {
            dom.add(psi1.dom.get(i));
            card.add(psi1.card.get(i));
            i++;
        }

        while (j < psi2.dom.size()) {
            dom.add(psi2.dom.get(j));
            card.add(psi2.card.get(j));
            j++;
        }

        // Set stride
        updateStride();

        // Prepare potentials
        potent = new double[size];

    }

    /**
     * Construction from the CPT of a Bayesian Network
     *
     * @param v  variable of interest
     * @param bn bayesian network
     */
    public BayesianFactor(int v, BayesianNetwork bn) {

        // Set domain
        dom = new TIntArrayList();
        for (int p : bn.parents(v)) {
            dom.add(p);
        }
        dom.add(v);
        dom.sort();

        // Set cardinality
        card = new TIntArrayList();
        for (int c : dom.toArray()) {
            card.add(bn.arity(c));
        }

        // Set stride
        updateStride();

        /*
         System.out.println(bn.name( v));
         System.out.println(dom + " - " + card + " - " + stride + " - " + size);
         System.out.println(bn.potentials(v).length);
         */

        // Prepare potentials
        potent = new double[size];
        updatePotent(bn.potentials(v));

    }

    public BayesianFactor(int v, BayesianNetwork bn, double[] new_p) {
        // Set domain
        dom = new TIntArrayList();
        dom.add(v);

        // Set cardinality
        card = new TIntArrayList();
        card.add(bn.arity(v));

        // Set stride
        updateStride();

        potent = new double[size];
        cloneArray(new_p, potent);
    }

    /**
     * @param nums List of values
     * @return sum of values
     */
    private static double logSum(TDoubleArrayList nums) {

        double max_exp = nums.max();
        double sum = 0.0;

        for (int i = 0; i < nums.size(); i++) {
            sum += Math.exp(nums.getQuick(i) - max_exp);
            // printf("nums[thread]: %.5f, max_exp: %.5f, diff: %.5f", nums[thread], max_exp, nums[thread] - max_exp);
        }

        return Math.log(sum) + max_exp;
    }

    /**
     * @param nums List of values
     * @return sum of values
     */
    private static double listSum(TDoubleArrayList nums) {
        double s = 0;

        for (int i = 0; i < nums.size(); i++) {
            s += nums.getQuick(i);
        }
        return s;
    }

    /**
     * Update value of stride
     */
    private void updateStride() {
        int s = 1;

        stride = new TIntArrayList(dom.size());
        for (int i = 0; i < dom.size(); i++) {
            stride.add(s);
            s *= card.get(i);
        }
        size = s;
    }

    /**
     * Update potentials from the given
     *
     * @param n_potent potentials to copy
     */
    public void updatePotent(double[] n_potent) {

        // Copy potentials
        int l = 0;

        for (double p : n_potent) {
            potent[l++] = logComp ? Math.log(p) : p;
        }
    }

    public String toString() {

        return String.format(
                "size: %d && dom: %s && card: %s && stride; %s && potent:%s\n\n",
                size, dom, card, stride, printPotent());
    }

    /**
     * Check if potentials sum to 1
     *
     * @return if potentials are ok
     */
    private double diffPotent() {
        double sum = 0.0;

        for (double p : potent) {
            sum += logComp ? Math.exp(p) : p;
        }

        return Math.abs(sum - 1.0);
    }

    /**
     * Print potentials
     *
     * @return description of potentials
     */
    public String printPotent() {
        StringBuilder str = new StringBuilder();

        str.append("[ ");
        for (int j = 0; j < potent.length; j++) {
            str.append(String.format("%.8f ", getPotent(j)));
        }
        str.append("]");

        return str.toString();
    }

    /**
     * Normalize to 1 the potentials distribution
     */
    public void normalize() {

        double sum = 0.0;

        for (int i = 0; i < size; i++) {
            sum += logComp ? Math.exp(potent[i]) : potent[i];
        }
        for (int i = 0; i < size; i++) {
            potent[i] = logComp
                    ? potent[i] - Math.log(sum)
                    : potent[i] / sum;
        }

    }

    /**
     * Get potentials from index
     *
     * @param j index
     * @return potential
     */
    private double getPotent(int j) {
        return logComp ? Math.exp(potent[j]) : potent[j];
    }

    /**
     * Get all potentials (eventually in normal form)
     *
     * @return potentials of the factor
     */
    public double[] getPotent() {
        double[] d = new double[size];

        for (int i = 0; i < size; i++) {
            d[i] = logComp ? Math.exp(potent[i]) : potent[i];
        }
        return d;
    }

    /**
     * Reduce a factor by the given evidence
     *
     * @param e   index of the evidence variable
     * @param val value of the evidence variable
     * @return resulting factor
     */
    public BayesianFactor reduction(int e, int val) {

        BayesianFactor n_psi = new BayesianFactor(this, e);

        int p = n_psi.size;

        int i_v = dom.indexOf(e);

        int v_card = card.get(i_v);
        int v_stride = stride.get(i_v);

        // First position of evidence variable with given variable
        int k = val * v_stride;
        int l = 0;

        for (int i = 0; i < p; i++) {

            // System.out.println(thread+ " " + tw);
            n_psi.potent[i] = logComp ? Math.exp(potent[k]) : potent[k];

            // Advance to next position in the potentials
            k++;
            l++;
            if (l == v_stride) {
                l = 0;
                k += v_stride * (v_card - 1);
            }
        }

        if (logComp) {
            // Go back to log
            for (int i = 0; i < p; i++) {
                n_psi.potent[i] = Math.log(n_psi.potent[i]);
            }
        }

        /*
         // Normalize?
         for (int thread = 0; thread < p; thread++) {
         n_potent[thread] = logComp ? Math.log(n_potent[thread] / sum)  : n_potent[thread] / sum;
         }
         */
        return n_psi;
    }

    /**
     * Compute the product of two factors, each with its domain and probabilities distribution.
     *
     * @param psi2 Second factor
     * @return resulting factor
     */
    public BayesianFactor product(BayesianFactor psi2) {

        int j = 0;
        int k = 0;

        // Create new factor
        BayesianFactor n_psi = new BayesianFactor(this, psi2);
        int d = n_psi.dom.size();
        int p = n_psi.potent.length;

        // System.out.println("N_psi: " + n_psi.dom + " - " + RandomStuff.printOrdMap(n_psi.stride));

        // Auxilary structures
        int[] assignment = new int[d];
        int[] card = new int[d];

        int[] stride1 = new int[d];
        int[] shift1 = new int[d];

        int[] stride2 = new int[d];
        int[] shift2 = new int[d];

        for (int m = 0; m < n_psi.dom.size(); m++) {
            assignment[m] = 0;

            int v = n_psi.dom.getQuick(m);

            card[m] = n_psi.card.get(m);

            stride1[m] = (dom.contains(v) ? stride.get(dom.indexOf(v)) : 0);
            shift1[m] = (card[m] - 1) * stride1[m];

            stride2[m] = (psi2.dom.contains(v)
                    ? psi2.stride.get(psi2.dom.indexOf(v))
                    : 0);
            shift2[m] = (card[m] - 1) * stride2[m];
        }

        // System.out.println("p: " + p);
        for (int i = 0; i < p; i++) {
            // n_psi.potent[thread] = potent[j] + psi2.potent[tw];
            n_psi.potent[i] = logComp
                    ? potent[j] + psi2.potent[k]
                    : potent[j] * psi2.potent[k];
            for (int l = 0; l < d; l++) {
                assignment[l] += 1;
                if (assignment[l] == card[l]) {
                    assignment[l] = 0;
                    j -= shift1[l];
                    k -= shift2[l];
                } else {
                    j += stride1[l];
                    k += stride2[l];
                    break;
                }
            }
        }

        return n_psi;
    }

    /**
     * Sums out a variable from a factor
     *
     * @param v variable to sum out
     * @return new factor
     */
    public BayesianFactor marginalization(int v) {

        BayesianFactor n_psi = new BayesianFactor(this, v);

        int p = n_psi.size;

        int l = 0;
        int j = 0;

        int i_v = dom.indexOf(v);

        int v_card = card.get(i_v);
        int v_stride = stride.get(i_v);

        List<TDoubleArrayList> aux = new ArrayList<TDoubleArrayList>();

        for (int i = 0; i < p; i++) {
            aux.add(new TDoubleArrayList());
        }

        for (int i = 0; i < p; i++) {

            // Sum (of logarithms)
            int k = j;

            for (int h = 0; h < v_card; h++) {
                aux.get(i).add(potent[k]);
                k += v_stride;
            }

            // Advance to next starting position
            l++;
            if (l == v_stride) {
                l = 0;
                j = k - v_stride + 1;
            } else {
                j++;
            }

        }

        for (int i = 0; i < p; i++) {
            n_psi.potent[i] += logComp
                    ? logSum(aux.get(i))
                    : listSum(aux.get(i));
        }

        /*
         // Normalize?
         for (int thread = 0; thread < p; thread++) {
         // n_psi.potent[thread] = logComp ? Math.log(n_psi.potent[thread] / sum)  : n_psi.potent[thread] / sum;
         n_psi.potent[thread] = logComp ? Math.log(n_psi.potent[thread] )  : n_psi.potent[thread] ;
         }*/

        return n_psi;
    }

    /**
     * Combine a factor of the same dimension
     *
     * @param psi2 Second factor
     */
    public void combine(BayesianFactor psi2) {

        // System.out.printf("psi1: %s%n", psi1);
        // System.out.printf("psi2: %s%n", psi2);

        double sum = 0.0;

        for (int i = 0; i < potent.length; i++) {
            potent[i] = logComp
                    ? potent[i] + psi2.potent[i]
                    : potent[i] * psi2.potent[i];
            sum += logComp ? Math.exp(potent[i]) : potent[i];
        }
        for (int i = 0; i < potent.length; i++) {
            potent[i] = logComp
                    ? potent[i] - Math.log(sum)
                    : potent[i] / sum;
        }
    }

}
