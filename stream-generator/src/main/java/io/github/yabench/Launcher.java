package io.github.yabench;

import java.io.File;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class Launcher {

    private static final String PROGRAM_NAME = "stream-generator";
    private static final String ARG_NAME = "name";
    private static final String ARG_DEST = "dest";
    private static final String ARG_HELP = "help";

    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();
        Options options = createCLIOptions();

        try {
            CommandLine cli = parser.parse(options, args);
            if (cli.hasOption(ARG_NAME) && cli.hasOption(ARG_DEST)) {
                StreamGeneratorFactory testFactory = new StreamGeneratorFactory(
                        new File(cli.getOptionValue(ARG_DEST)));
                if (cli.hasOption(ARG_HELP)) {
                    OptionGroup group = testFactory.getTestOptions(
                            cli.getOptionValue(ARG_NAME));
                    options.addOptionGroup(group);
                    printHelp(options);
                } else {
                    StreamGenerator test = testFactory
                            .createTest(cli.getOptionValue(ARG_NAME), cli);
                    if (test != null) {
                        if (cli.hasOption(ARG_HELP)) {
                            testFactory.getTestOptions(
                                    cli.getOptionValue(ARG_NAME));
                            printHelp(options);
                        } else {
                            try {
                                test.generate();
                            } finally {
                                test.close();
                            }
                        }
                    } else {
                        printHelp(options);
                    }
                }
            } else {
                printHelp(options);
            }
        } catch (ParseException exp) {
            System.out.println(exp.getMessage() + "\n");

            printHelp(options);
        } catch (Exception iex) {
            iex.printStackTrace();
        }
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(PROGRAM_NAME, options);
    }

    private static Options createCLIOptions() {
        Options opts = new Options();

        Option dest = OptionBuilder
                .withArgName("path")
                .withDescription("destination folder")
                .hasArg()
                .isRequired()
                .create(ARG_DEST);

        Option name = OptionBuilder
                .withArgName("name")
                .withDescription("the name of the test scenarion")
                .hasArg()
                .isRequired()
                .create(ARG_NAME);

        Option help = OptionBuilder
                .withDescription("print this message")
                .create(ARG_HELP);

        opts.addOption(dest);
        opts.addOption(name);
        opts.addOption(help);

        return opts;
    }

}
