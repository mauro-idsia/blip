package ch.idsia.blip.api;

import ch.idsia.blip.core.utils.IncorrectCallException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.util.logging.Logger;

import static ch.idsia.blip.core.utils.RandomStuff.logExp;
import static ch.idsia.blip.core.utils.RandomStuff.p;

public abstract class Api {

    @Option(name="-v", usage="Verbose level")
    protected static int  verbose = 0;

    public abstract void exec() throws Exception;

    public static void defaultMain(String[] o_args, Api api, Logger log) {

        String[] args = new String[o_args.length - 1];
        System.arraycopy(o_args, 1, args, 0, o_args.length - 1);

        CmdLineParser parser = new CmdLineParser(api);

        if (args.length == 0 || (args.length == 1 && "help".equals(args[0].trim().toLowerCase()))) {
            p("Task command line options: ");
            parser.printUsage(System.out);
            return;
        }

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            // handling of wrong arguments
            p("Parsing of command line failed, execution halted. Reason: ");
            p(e.getMessage());
            return;
        }

        try {
            api.exec();
        }  catch (IncorrectCallException exp) {
            p("Execution failed. Reason: ");
            p(exp.getMessage());
        } catch (Throwable exp) {
            log.severe("Error in: " + api.getClass().getName());
            logExp(log, exp);
        }
    }

}
