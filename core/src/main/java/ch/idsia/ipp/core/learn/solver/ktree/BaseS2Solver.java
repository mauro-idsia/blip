package ch.idsia.ipp.core.learn.solver.ktree;


import ch.idsia.ipp.core.common.io.GobnilpReader;
import ch.idsia.ipp.core.common.io.ScoreReader;
import ch.idsia.ipp.core.common.io.ScoreWriter;
import ch.idsia.ipp.core.common.tw.Dandelion;
import ch.idsia.ipp.core.common.tw.KTree;
import ch.idsia.ipp.core.learn.solver.ScoreSolver;
import ch.idsia.ipp.core.learn.solver.src.Searcher;
import ch.idsia.ipp.core.utils.ParentSet;
import ch.idsia.ipp.core.utils.StreamGobbler;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static ch.idsia.ipp.core.utils.RandomStuff.*;


/**
 * IS ALIVE! IS AAAAALIVE!
 */
public class BaseS2Solver extends ScoreSolver {

    private final Logger log = Logger.getLogger(BaseS2Solver.class.getName());

    public int tw;

    public String ph_work;

    public String ph_gobnilp;

    public void init(ScoreReader sc, int max_exec, int treewidth) {
        super.init(sc, max_exec);
        this.tw = treewidth;
    }

    @Override
    protected String name() {
        return "Gobnilp Dandelion Solver!";
    }

    @Override
    protected void prepare()  {
        super.prepare();
        if (verbose > 0) {
            log("tw: " + tw + "\n");
        }

        File w = new File(ph_work);

        deleteFolder(new File(ph_work));
        w.mkdir();
    }

    @Override
    protected Searcher getSearcher() {
        return null;
    }

    @Override
    public BaseSearcher getNewSearcher(int i) {
        return new BaseS2Searcher(this, i);
    }

    private class BaseS2Searcher extends BaseSearcher {

        public BaseS2Searcher(BaseS2Solver solver, int i) {
            super(solver, i);
        }

        @Override
        public void run() {

            int iter = 0;

            while (still_time) {

                KTree K = sampleKtree(thread, iter);

                if (K == null) {
                    checkTime();
                    continue;
                }

                ParentSet[][] p_scores = K.selectScores(sc.m_scores);

                // K.T.graph(f("%s/%d", ph_work, thread));
                String jkl = f("%s/%d-%d.jkl", ph_work, thread, iter);
                String out = f("%s/%d-%d.gob", ph_work, thread, iter);
                String err = f("%s/%d-%d.err", ph_work, thread, iter);

                ScoreWriter.go(p_scores, jkl);

                try {
                    ProcessBuilder builder = new ProcessBuilder(ph_gobnilp,
                            "-f=jkl", jkl);

                    Process p = builder.start();

                    StreamGobbler e = new StreamGobbler(p.getErrorStream(), err);
                    e.start();
                    StreamGobbler o = new StreamGobbler(p.getInputStream(), out);
                    o.start();


                    p.waitFor();

                } catch (IOException e) {
                    logExp(log, e);
                } catch (InterruptedException e) {
                    logExp(log, e);
                }


                GobnilpReader g = new GobnilpReader();

                g.go(out);

                newStructure(g.new_str);

                iter++;
                checkTime();
            }

        }
    }

    KTree sampleKtree(int i, int j) {
        return KTree.decode(Dandelion.sample(n_var, tw));
    }

}

