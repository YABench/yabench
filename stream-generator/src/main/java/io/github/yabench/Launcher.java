package io.github.yabench;

import io.github.yabench.commons.AbstractLauncher;
import java.io.File;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class Launcher extends AbstractLauncher {

    private static final String PROGRAM_NAME = "stream-generator";
    private static final String ARG_NAME = "name";
    private static final String ARG_DEST = "dest";
    private static final String ARG_HELP = "help";

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
            CommandLine cli = parser.parse(options, args, true);

            StreamGeneratorFactory testFactory = new StreamGeneratorFactory(
                    new File(cli.getOptionValue(ARG_DEST)));
            final String sgName = cli.getOptionValue(ARG_NAME);
            final List<Option> sgOptions = testFactory.getTestOptions(sgName);

            if (sgOptions != null) {
                mergeOptions(options, sgOptions);
                cli = parser.parse(options, args);

                if (cli.hasOption(ARG_HELP)) {
                    printHelp(options);
                } else {
                    StreamGenerator sg = testFactory.createTest(sgName, cli);
                    try {
                        sg.generate();
                    } finally {
                        sg.close();
                    }
                }
            } else {
                printHelp(options, String.format(
                        "Stream generator with name %1s not found!", sgName));
            }
        } catch (ParseException exp) {
            printHelp(options, exp.getMessage());
        } catch (Exception iex) {
            iex.printStackTrace();
        }
    }

    private Options createCLIOptions() {
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
