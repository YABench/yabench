package io.github.yabench.oracle;

import io.github.yabench.commons.AbstractLauncher;
import io.github.yabench.oracle.tests.TestFactory;
import io.github.yabench.oracle.tests.OracleTest;
import java.io.File;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class Launcher extends AbstractLauncher {

    private static final String PROGRAM_NAME = "yabench-oracle";
    private static final String ARG_HELP = "help";
    private static final String ARG_INPUTSTREAM = "inputstream";
    private static final String ARG_QUERYRESULTS = "queryresults";
    private static final String ARG_OUTPUT = "output";
    private static final String ARG_TESTNAME = "test";
    
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
            
            final String testName = cli.getOptionValue(ARG_TESTNAME);
            final File inputStream = new File(cli.getOptionValue(ARG_INPUTSTREAM));
            final File queryResults = new File(cli.getOptionValue(ARG_QUERYRESULTS));
            final File output = new File(cli.getOptionValue(ARG_OUTPUT));
            
            TestFactory testFactory = new TestFactory(
                    inputStream, queryResults, output);
            Option[] expectedOptions = testFactory
                    .getExpectedOptions(testName);
            if (expectedOptions != null) {
                mergeOptions(options, expectedOptions);
                cli = parser.parse(options, args);
                
                if(cli.hasOption(ARG_HELP)) {
                    printHelp(options);
                } else {
                    try (OracleTest test = testFactory
                            .createTest(cli.getOptionValue(ARG_TESTNAME), cli)) {
                        test.init();
                        test.compare();
                    }
                }
            } else {
                printHelp(options,
                        String.format("Test with name '%1s' not found!", testName));
            }
        } catch (ParseException ex) {
            printHelp(options, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Options createCLIOptions() {
        Options opt = new Options();

        Option inputStream = OptionBuilder
                .isRequired()
                .hasArg()
                .withArgName("file")
                .create(ARG_INPUTSTREAM);

        Option queryResults = OptionBuilder
                .isRequired()
                .hasArg()
                .withArgName("file")
                .create(ARG_QUERYRESULTS);
        
        Option output = OptionBuilder
                .isRequired()
                .hasArg()
                .withArgName("file")
                .create(ARG_OUTPUT);

        Option testName = OptionBuilder
                .isRequired()
                .hasArg()
                .withArgName("name")
                .create(ARG_TESTNAME);

        Option help = OptionBuilder
                .create(ARG_HELP);

        opt.addOption(inputStream);
        opt.addOption(queryResults);
        opt.addOption(output);
        opt.addOption(testName);
        opt.addOption(help);
        return opt;
    }

    @Override
    public String getName() {
        return PROGRAM_NAME;
    }

}
