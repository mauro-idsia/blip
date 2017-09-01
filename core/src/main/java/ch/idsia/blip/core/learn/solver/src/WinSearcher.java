package ch.idsia.blip.core.learn.solver.src;

import ch.idsia.blip.core.learn.solver.BaseSolver;
import ch.idsia.blip.core.learn.solver.src.obs.ObsSearcher;
import ch.idsia.blip.core.utils.data.ArrayUtils;
import ch.idsia.blip.core.utils.other.ParentSet;
import ch.idsia.blip.core.utils.other.RandomStuff;

import java.util.Random;


/**
 * Hybrid greedy hill exploration
 */
public class WinSearcher extends ObsSearcher {

    protected int max_windows = 4;

    private int[] todo;

    protected int window = 1;

    protected int asobs = 1;

    private boolean[] b_forbidden;

    private boolean[] f_forbidden;

    private ParentSet[] emptyS;

    protected int[] work_vars;

    protected ParentSet[] work_str;

    protected double work_sk;

    protected boolean gain;

    protected ParentSet[] best_str;

    protected int[] best_vars;

    protected double best_sk;

    protected Random r = new Random(System.currentTimeMillis());

    public WinSearcher(BaseSolver solver) {
        super(solver);
    }

    public void init(ParentSet[][] scores, int thread) {
        super.init(scores, thread);

        this.todo = new int[this.n_var];
        for (int i = 0; i < this.n_var; i++) {
            this.todo[i] = i;
        }
        this.f_forbidden = new boolean[this.n_var];
        this.b_forbidden = new boolean[this.n_var];

        this.emptyS = new ParentSet[this.n_var];
        for (int i = 0; i < this.n_var; i++) {
            this.emptyS[i] = this.m_scores[i][(this.m_scores[i].length - 1)];
        }
        this.work_vars = new int[this.n_var];

        this.work_str = new ParentSet[this.n_var];

        this.best_str = new ParentSet[this.n_var];

        this.best_vars = new int[this.n_var];
    }

    public boolean greedy(int[] vars, int pivot) {
        this.gain = false;
        this.best_sk = this.last_sk;

        prepareForbidden(vars, pivot);

        prepareSearch(vars);
        this.forbidden = this.f_forbidden;
        if (this.window == 1) {
            greedy1Forward(pivot);
        } else if (this.window == 2) {
            greedy2Forward(pivot);
        } else {
            greedyNForward(pivot);
        }
        prepareSearch(vars);
        this.forbidden = this.b_forbidden;
        if (this.window == 1) {
            greedy1Backward(pivot);
        } else if (this.window == 2) {
            greedy2Backward(pivot);
        } else {
            greedyNBackward(pivot);
        }
        if (!this.gain) {
            return false;
        }
        RandomStuff.cloneStr(this.best_str, this.last_str);
        this.last_sk = this.best_sk;
        ArrayUtils.cloneArray(this.best_vars, vars);
        return true;
    }

    private void greedyNBackward(int pivot) {
        for (int ix = pivot; ix >= this.window; ix--) {
            varSwitchNBackward(ix);
            checkBest();
        }
    }

    private void greedy2Backward(int pivot) {
        for (int ix = pivot; ix >= this.window; ix--) {
            varSwitch2Backward(ix);
            checkBest();
        }
    }

    private void greedy1Backward(int pivot) {
        for (int ix = pivot; ix >= this.window; ix--) {
            varSwitch1(ix, false);
            checkBest();
        }
    }

    private void greedyNForward(int pivot) {
        for (int ix = pivot; ix < this.n_var - this.window; ix++) {
            varSwitchNForward(ix);
            checkBest();
        }
    }

    private void greedy2Forward(int pivot) {
        for (int ix = pivot; ix < this.n_var - this.window; ix++) {
            varSwitch2Forward(ix);
            checkBest();
        }
    }

    private void greedy1Forward(int pivot) {
        for (int ix = pivot; ix < this.n_var - this.window; ix++) {
            varSwitch1(ix, true);
            checkBest();
        }
    }

    private void prepareForbidden(int[] vars, int pivot) {
        if (pivot > this.n_var / 2) {
            ArrayUtils.cloneArray(this.voidB, this.f_forbidden);
            for (int i = this.n_var - 1; i > pivot; i--) {
                this.f_forbidden[vars[i]] = true;
            }
        } else {
            ArrayUtils.cloneArray(this.fullB, this.f_forbidden);
            for (int i = 0; i <= pivot; i++) {
                this.f_forbidden[vars[i]] = false;
            }
        }
        ArrayUtils.cloneArray(this.f_forbidden, this.b_forbidden);
    }

    private void varSwitchNBackward(int ix) {
        int x = this.work_vars[(ix - this.window)];
        boolean improve_x = false;
        for (int i = 0; i < this.window; i++) {
            int a = this.work_vars[(ix - i)];
            this.forbidden[a] = false;
            if (this.cand[x][a]) {
                improve_x = true;
            }
        }
        if (improve_x) {
            bests(x);
        }
        this.forbidden[x] = true;
        for (int i = 0; i < this.window; i++) {
            int a = this.work_vars[(ix - i)];
            if (find(x, this.work_str[a].parents)) {
                bests(a);
            }
            this.forbidden[a] = true;
        }
        int t = this.work_vars[(ix - this.window)];
        for (int i = this.window; i > 0; i--) {
            this.work_vars[(ix - i)] = this.work_vars[(ix - (i - 1))];
        }
        this.work_vars[ix] = t;
    }

    private void varSwitchNForward(int ix) {
        int x = this.work_vars[(ix + this.window)];
        boolean find_x = false;
        for (int i = 0; i < this.window; i++) {
            int a = this.work_vars[(ix + i)];
            this.forbidden[a] = true;
            if (find(a, this.work_str[x].parents)) {
                find_x = true;
            }
        }
        if (find_x) {
            bests(x);
        }
        this.forbidden[x] = false;
        for (int i = 0; i < this.window; i++) {
            int a = this.work_vars[(ix + i)];
            if (this.cand[a][x]) {
                bests(a);
            }
            this.forbidden[a] = false;
        }
        int t = this.work_vars[(ix + this.window)];
        for (int i = this.window; i > 0; i--) {
            this.work_vars[(ix + i)] = this.work_vars[(ix + (i - 1))];
        }
        this.work_vars[ix] = t;
    }

    private void varSwitch2Backward(int ix) {
        int a = this.work_vars[ix];
        int b = this.work_vars[(ix - 1)];
        int x = this.work_vars[(ix - 2)];

        this.forbidden[a] = false;
        this.forbidden[b] = false;
        if ((this.cand[x][a]) || (this.cand[x][b])) {
            bests(x);
        }
        this.forbidden[x] = true;
        if (find(x, this.work_str[a].parents)) {
            bests(a);
        }
        this.forbidden[a] = true;
        if (find(x, this.work_str[b].parents)) {
            bests(b);
        }
        int t = this.work_vars[(ix - 2)];
        this.work_vars[(ix - 2)] = this.work_vars[(ix - 1)];
        this.work_vars[(ix - 1)] = this.work_vars[ix];
        this.work_vars[ix] = t;
    }

    private void varSwitch2Forward(int ix) {
        int a = this.work_vars[ix];
        int b = this.work_vars[(ix + 1)];
        int x = this.work_vars[(ix + 2)];

        this.forbidden[a] = true;
        this.forbidden[b] = true;
        if ((find(a, this.work_str[x].parents)) || (find(b, this.work_str[x].parents))) {
            bests(x);
        }
        this.forbidden[x] = false;
        if (this.cand[a][x]) {
            bests(a);
        }
        this.forbidden[a] = false;
        if (this.cand[b][x]) {
            bests(b);
        }
        int t = this.work_vars[(ix + 2)];
        this.work_vars[(ix + 2)] = this.work_vars[(ix + 1)];
        this.work_vars[(ix + 1)] = this.work_vars[ix];
        this.work_vars[ix] = t;
    }

    private void varSwitch1(int ix, boolean forward) {
        int x;
        int a;
        if (forward) {
            a = this.work_vars[ix];
            x = this.work_vars[(ix + 1)];
        } else {
            a = this.work_vars[(ix - 1)];
            x = this.work_vars[ix];
        }
        this.forbidden[a] = true;
        if (find(a, this.work_str[x].parents)) {
            bests(x);
        }
        this.forbidden[x] = false;
        if (this.cand[a][x]) {
            bests(a);
        }
        if (forward) {
            ArrayUtils.swapArray(this.work_vars, ix, ix + 1);
        } else {
            ArrayUtils.swapArray(this.work_vars, ix, ix - 1);
        }
    }

    public ParentSet[] search() {
        this.vars = this.smp.sample();

        initStr();

        winasobs();

        return this.last_str;
    }

    protected void checkBest() {
        if (this.work_sk + this.eps > this.best_sk) {
            this.best_sk = this.work_sk;
            RandomStuff.cloneStr(this.work_str, this.best_str);
            ArrayUtils.cloneArray(this.work_vars, this.best_vars);
            this.gain = true;
        }
    }

    protected void prepareSearch(int[] vars) {
        RandomStuff.cloneStr(this.last_str, this.work_str);
        ArrayUtils.cloneArray(vars, this.work_vars);
        this.work_sk = this.last_sk;
    }

    public void initStr() {
        ArrayUtils.cloneArray(this.voidB, this.forbidden);
        RandomStuff.cloneStr(this.emptyS, this.work_str);
        for (int i = this.n_var - 1; i >= 0; i--) {
            this.forbidden[this.vars[i]] = true;
            bests(this.vars[i]);
        }
        RandomStuff.cloneStr(this.work_str, this.last_str);
        this.last_sk = checkSk();
    }

    public void winasobs() {
        initStr();

        int cnt = 0;
        this.window = 1;
        while (this.window <= this.max_windows) {
            this.solver.checkTime();
            if (!this.solver.still_time) {
                return;
            }
            if (cnt < this.n_var - 1) {
                int index = randInt(cnt + 1, this.n_var - 1);
                ArrayUtils.swap(this.todo, cnt, index);
            }
            if (greedy(this.vars, this.todo[cnt])) {
                cnt = 0;

                this.window = 1;
                if (this.last_sk > this.solver.best_sk) {
                    this.solver.newStructure(this.last_str);
                }
            } else {
                cnt++;
            }
            if (cnt >= this.n_var) {
                this.window += 1;
                cnt = 0;
            }
        }
    }

    protected void bests(int a) {
        this.old_sk = this.work_str[a].sk;
        check:
        for (ParentSet pSet : this.m_scores[a]) {
            for (int p : pSet.parents) {
                if (this.forbidden[p]) {
                    break check;
                }
            }
            this.work_str[a] = pSet;
            this.work_sk = (this.work_sk + pSet.sk - this.old_sk);

            return;
        }
    }
}
