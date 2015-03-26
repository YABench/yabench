package io.github.yabench.engines.cqels;

import io.github.yabench.engines.commons.AbstractEngineLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher extends AbstractEngineLauncher {
    
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);
    private static final String PROGRAM_NAME = "yabench-cqels";

    public static void main(String[] args) {
        Launcher launcher = new Launcher();
        launcher.launch(args);

        /**
         * It's a workaround. Otherwise the JVM doesn't exit.
         */
        System.exit(0);
    }
    
    @Override
    public String getName() {
        return PROGRAM_NAME;
    }
    
}
