package io.github.yabench.oracle;

import io.github.yabench.oracle.tests.OracleTest;
import java.io.File;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class Launcher {

    private static final String PROGRAM_NAME = "yabench-oracle";
    private static final String ARG_HELP = "help";
    private static final String ARG_INPUTSTREAM = "inputstream";
    private static final String ARG_OUTPUTSTREAM = "outputstream";
    private static final String ARG_TESTNAME = "test";

    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();
        Options options = createCLIOptions();

        try {
            CommandLine cli = parser.parse(options, args, true);
            
            final String testName = cli.getOptionValue(ARG_TESTNAME);
            final File inputStream = new File(cli.getOptionValue(ARG_INPUTSTREAM));
            
            TestFactory testFactory = new TestFactory(inputStream);
            Option[] expectedOptions = testFactory
                    .getExpectedOptions(testName);
            if (expectedOptions != null) {
                addOptions(options, expectedOptions);
                cli = parser.parse(options, args);
                
                if(cli.hasOption(ARG_HELP)) {
                    printHelp(options);
                } else {
                    OracleTest test = testFactory
                            .createTest(cli.getOptionValue(ARG_TESTNAME), cli);
                    try {
                        test.init();
                    } finally {
                        test.close();
                    }
                }
            } else {
                printHelp(options,
                        String.format("Test with name '%1s' not found!", testName));
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
            printHelp(options, ex.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private static Options addOptions(Options dest, Option... options) {
        for(Option opt : options) {
            dest.addOption(opt);
        }
        return dest;
    }

    private static void printHelp(Options options, String... messages) {
        HelpFormatter formatter = new HelpFormatter();
        System.out.println();
        for (String msg : messages) {
            System.out.println(msg);
        }
        System.out.println();
        formatter.printHelp(PROGRAM_NAME, options);
    }

    private static Options createCLIOptions() {
        Options opt = new Options();

        Option inputStream = OptionBuilder
                .isRequired()
                .hasArg()
                .withArgName("file")
                .create(ARG_INPUTSTREAM);

        Option outputStream = OptionBuilder
                .isRequired()
                .hasArg()
                .withArgName("file")
                .create(ARG_OUTPUTSTREAM);

        Option testName = OptionBuilder
                .isRequired()
                .hasArg()
                .withArgName("name")
                .create(ARG_TESTNAME);

        Option help = OptionBuilder
                .create(ARG_HELP);

        opt.addOption(inputStream);
        opt.addOption(outputStream);
        opt.addOption(testName);
        opt.addOption(help);
        return opt;
    }

}
