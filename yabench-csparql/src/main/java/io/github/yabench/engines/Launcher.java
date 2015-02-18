package io.github.yabench.engines;

import com.google.common.io.Files;
import io.github.yabench.commons.AbstractLauncher;
import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import io.github.yabench.commons.RDFStreamReader;

import io.github.yabench.commons.TemporalTriple;
import java.nio.charset.Charset;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher extends AbstractLauncher {

    // C:\Users\peter\Documents\yabench\c-sparql-engine>java -jar
    // .\target\c-sparql-engine-0.0.1-SNAPSHOT-jar-with-dependencies.jar -query
    // asdf -dest asdfdest -source ..\streams\TestQ1
    private static final String PROGRAM_NAME = "yabench-csparql";
    private static final String ARG_QUERY = "query";
    private static final String ARG_SOURCE = "source";
    private static final String ARG_DEST = "dest";
    private static final String ARG_HELP = "help";
    private final static Logger log = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        Launcher launcher = new Launcher();
        launcher.launch(args);
    }

    @Override
    public String getName() {
        return PROGRAM_NAME;
    }

    @Override
    public void launch(String[] args) {
        CommandLineParser parser = new PosixParser();
        Options options = createCLIOptions();

        try {
            CommandLine cli = parser.parse(options, args);
            // 1. read query file and replace paremeters, i.e., window-size and window-slide
            final String query = Files.toString(
                    new File(cli.getOptionValue(ARG_QUERY)),
                    Charset.defaultCharset());

            // 2. read quads from source file
            Path source = new File(cli.getOptionValue(ARG_SOURCE)).toPath();
            RDFStreamReader reader = new RDFStreamReader(source);

            log.info("initialize engine");
            EngineFactory engineFactory = new EngineFactory();
            try (Engine engine = engineFactory.create()) {
                engine.initialize();

                log.info("register query");
                engine.registerQuery(query);
                engine.registerResultListener();

                long time = 0;
                TemporalTriple triple;
                while ((triple = reader.readNext()) != null) {
                    Thread.sleep(triple.getTime() - time);

                    engine.stream(triple.getStatement());

                    time = triple.getTime();
                }
            }
        } catch (ParseException exp) {
            printHelp(options, exp.getMessage());
        } catch (Exception iex) {
            log.error(iex.getMessage(), iex);
        }
    }

    private Options createCLIOptions() {
        Options opts = new Options();

        Option query = OptionBuilder
                .withArgName("query")
                .withDescription("destination file containing the query to be registered at the engine")
                .hasArg()
                .isRequired()
                .create(ARG_QUERY);

        Option dest = OptionBuilder
                .withArgName("dest")
                .withDescription("destination folder of query results")
                .hasArg()
                .isRequired()
                .create(ARG_DEST);

        Option source = OptionBuilder
                .withArgName("source")
                .withDescription("source file containing the triples to be streamed")
                .hasArg()
                .isRequired()
                .create(ARG_SOURCE);

        Option help = OptionBuilder
                .withDescription("print this message")
                .create(ARG_HELP);

        opts.addOption(query);
        opts.addOption(source);
        opts.addOption(dest);
        opts.addOption(help);

        return opts;
    }
}
