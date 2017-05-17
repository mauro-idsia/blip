package ch.idsia.blip.api;

import ch.idsia.blip.core.utils.IncorrectCallException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.OptionHandler;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.logging.Logger;

import static ch.idsia.blip.core.utils.RandomStuff.logExp;
import static ch.idsia.blip.core.utils.RandomStuff.p;

public abstract class Api {

    @Option(name="-v", usage="Verbose level")
    protected int verbose = 0;

    @Option(name="-seed", usage="Random seed")
    protected int seed = 0;

    @Option(name="-b", usage="Number of machine cores to use (if 0, all are used)")
    protected int thread_pool_size = 1;

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
            for (OptionHandler t : parser.getArguments()) {
                p(t);
                p(t.option);
            }
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

    protected HashMap<String, String> options() {

        HashMap<String, String> options = new HashMap<String, String>();

            for (Class c = getClass(); c != null; c = c.getSuperclass()) {

                // p(c.getName());

                for (Field f : c.getDeclaredFields()) {

                    // p(f.getName());
                    f.setAccessible(true);

                    try {

                    Option o = f.getAnnotation(Option.class);
                    if (o != null) {
                        options.put(f.getName(), String.valueOf(f.get(this)));
                    }

                } catch (IllegalAccessException ex) {
                    logExp(ex);
                }

            }
            }


        return options;
    }


}
