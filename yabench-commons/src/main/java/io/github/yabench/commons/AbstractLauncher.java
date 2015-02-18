package io.github.yabench.commons;

import java.util.List;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public abstract class AbstractLauncher {
    
    public abstract String getName();
    
    public abstract void launch(String[] args);
    
    protected Options mergeOptions(Options dest, Option... options) {
        for(Option opt : options) {
            dest.addOption(opt);
        }
        return dest;
    }
    
    protected Options mergeOptions(Options dest, List<Option> options) {
        for(Option opt : options) {
            dest.addOption(opt);
        }
        return dest;
    }
    
    protected void printHelp(Options options, String... messages) {
        HelpFormatter formatter = new HelpFormatter();
        System.out.println();
        for (String msg : messages) {
            System.out.println(msg);
        }
        System.out.println();
        formatter.printHelp(getName(), options);
    }
    
}
