package io.github.yabench.engines.csparql;

import io.github.yabench.engines.commons.AbstractEngineLauncher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher extends AbstractEngineLauncher {

    private final static Logger logger = LoggerFactory.getLogger(Launcher.class);
    private static final String PROGRAM_NAME = "yabench-csparql";

    public static void main(String[] args) {
        Launcher launcher = new Launcher();
        launcher.launch(args);
    }

    @Override
    public String getName() {
        return PROGRAM_NAME;
    }

    @Override
    public void onInputStreamEnd() throws InterruptedException {
        logger.info("waiting for last window to close + 2000ms buffer ...");
        // TODO: however, we only need to wait here if we run
        // C-SPARQL tests. in CQELS we do not have to wait until the
        // final window close, because results are returned
        // immediately. so maybe this should be changed
        Thread.sleep(getQuery().getWindowSize().toMillis() + 2000);
    }

}
