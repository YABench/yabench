package io.github.yabench.oracle;

import io.github.yabench.commons.AbstractLauncher;
import io.github.yabench.oracle.tests.OracleTestBuilder;
import io.github.yabench.oracle.tests.OracleTest;
import java.io.File;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher extends AbstractLauncher {

    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);
    private static final String PROGRAM_NAME = "yabench-oracle";
    private static final String ARG_HELP = "help";
    private static final String ARG_GRACEFUL = "graceful";
    private static final String ARG_GRACEFUL_DEFAULT = "true";
    private static final String ARG_INPUTSTREAM_LONG = "inputstream";
    private static final String ARG_INPUTSTREAM_SHORT = "is";
    private static final String ARG_QUERYRESULTS_LONG = "queryresults";
    private static final String ARG_QUERYRESULTS_SHORT = "qr";
    private static final String ARG_OUTPUT_LONG = "output";
    private static final String ARG_OUTPUT_SHORT = "o";
    private static final String ARG_QUERY_LONG = "query";
    private static final String ARG_QUERY_SHORT = "q";
    private static final String ARG_VARIABLE_PREFIX = "P";

    public static void main(String[] args) {
        Launcher launcher = new Launcher();
        launcher.launch(args);
    }

    @Override
    public void launch(String[] args) {
        CommandLineParser parser = new PosixParser();
        Options options = createCLIOptions();

        try {
            CommandLine cli = parser.parse(options, args, true);

            final File query = new File(cli.getOptionValue(ARG_QUERY_LONG));
            final File inputStream = new File(cli.getOptionValue(ARG_INPUTSTREAM_LONG));
            final File queryResults = new File(cli.getOptionValue(ARG_QUERYRESULTS_LONG));
            final File output = new File(cli.getOptionValue(ARG_OUTPUT_LONG));
            final boolean graceful = Boolean.parseBoolean(
                    cli.getOptionValue(ARG_GRACEFUL, ARG_GRACEFUL_DEFAULT));
            final Properties props = cli.getOptionProperties(ARG_VARIABLE_PREFIX);

            if (!props.containsKey("WSIZE") || !props.containsKey("WPOLICY")) {
                throw new ParseException(
                        "Missing required option: -PWSIZE, -PWPOLICY");
            }

            OracleTestBuilder testFactory = new OracleTestBuilder(
                    inputStream, queryResults, output, query)
                    .withGraceful(graceful)
                    .withVariables(props);
            if (cli.hasOption(ARG_HELP)) {
                printHelp(options);
            } else {
                try (OracleTest test = testFactory.build()) {
                    test.compare();
                }
            }
        } catch (ParseException ex) {
            printHelp(options, ex.getMessage());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private static Options createCLIOptions() {
        Options opt = new Options();

        Option inputStream = OptionBuilder
                .isRequired()
                .hasArg()
                .withArgName("file")
                .withDescription("input stream created by the stream generator")
                .withLongOpt(ARG_INPUTSTREAM_LONG)
                .create(ARG_INPUTSTREAM_SHORT);

        Option queryResults = OptionBuilder
                .isRequired()
                .hasArg()
                .withArgName("file")
                .withDescription("results of the continuous query")
                .withLongOpt(ARG_QUERYRESULTS_LONG)
                .create(ARG_QUERYRESULTS_SHORT);

        Option output = OptionBuilder
                .isRequired()
                .hasArg()
                .withArgName("file")
                .withLongOpt(ARG_OUTPUT_LONG)
                .create(ARG_OUTPUT_SHORT);

        Option query = OptionBuilder
                .isRequired()
                .hasArg()
                .withArgName("file")
                .withDescription("the SPARQL query to reproduce the query results")
                .withLongOpt(ARG_QUERY_LONG)
                .create(ARG_QUERY_SHORT);

        Option variables = OptionBuilder
                .isRequired()
                .withArgName("key=value")
                .withDescription("at least 'WSIZE' and 'WPOLICY' are required. "
                        + "WPOLICY = <onwindowclose|oncontentchange>")
                .hasArgs(2)
                .withValueSeparator()
                .create(ARG_VARIABLE_PREFIX);

        Option graceful = OptionBuilder
                .withType(Boolean.class)
                .hasArg()
                .create(ARG_GRACEFUL);

        Option help = OptionBuilder
                .create(ARG_HELP);

        opt.addOption(inputStream);
        opt.addOption(queryResults);
        opt.addOption(output);
        opt.addOption(query);
        opt.addOption(variables);
        opt.addOption(help);
        opt.addOption(graceful);
        return opt;
    }

    @Override
    public String getName() {
        return PROGRAM_NAME;
    }

}
