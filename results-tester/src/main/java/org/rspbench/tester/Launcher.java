package org.rspbench.tester;

import com.hp.hpl.jena.rdf.model.Model;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class Launcher {
    
    private static final String PROGRAM_NAME = "results-tester";
    private static final String ARG_INPUTSTREAM = "inputstream";
    private static final String ARG_WINDOWSIZE = "windowsize";
    private static final String ARG_WINDOWSLIDE = "windowslide";
    
    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();
        Options options = createCLIOptions();
        
        try {
            CommandLine cli = parser.parse(options, args);
            if(cli.hasOption(ARG_INPUTSTREAM)) {
                WindowFactory windowFactory = new WindowFactory(
                        new File(cli.getOptionValue(ARG_INPUTSTREAM)).toPath(), 
                        (Long) cli.getParsedOptionValue(ARG_WINDOWSIZE), 
                        (Long) cli.getParsedOptionValue(ARG_WINDOWSLIDE));
                
                while(windowFactory.hasNextWindow()) {
                    Model window = windowFactory.nextWindow();
                    
                    //write to database
                    //query expected results
                    //compare with the actual results
                }
            } else {
                printHelp(options);
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        } catch (ParseException ex) {
            System.out.println(ex.getMessage() + "\n");
            printHelp(options);
        }
    }
    
    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(PROGRAM_NAME, options);
    }

    
    private static Options createCLIOptions() {
        Options opt = new Options();
        
        Option inputStream = OptionBuilder
                .hasArg()
                .withArgName("file")
                .create(ARG_INPUTSTREAM);
        
        Option windowSize = OptionBuilder
                .withType(Long.class)
                .hasArg()
                .withArgName("ms")
                .create(ARG_WINDOWSIZE);
        
        Option windowSlide = OptionBuilder
                .withType(Long.class)
                .hasArg()
                .withArgName("ms")
                .create(ARG_WINDOWSLIDE);
        
        opt.addOption(inputStream);
        opt.addOption(windowSize);
        opt.addOption(windowSlide);
        return opt;
    }
    
}
