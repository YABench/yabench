package io.github.yabench.oracle.tests.comparators;

import io.github.yabench.oracle.BindingWindow;
import io.github.yabench.oracle.readers.EngineResultsReader;
import io.github.yabench.oracle.FMeasure;
import io.github.yabench.oracle.readers.BufferedTWReader;
import io.github.yabench.oracle.OracleResultBuilder;
import io.github.yabench.oracle.OracleResultsWriter;
import io.github.yabench.oracle.QueryExecutor;
import io.github.yabench.oracle.TripleWindow;
import io.github.yabench.oracle.Window;
import io.github.yabench.oracle.WindowFactory;
import io.github.yabench.oracle.readers.TripleWindowReader;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnWindowCloseComparator implements OracleComparator {

    private static final Logger logger = LoggerFactory.getLogger(
            OnWindowCloseComparator.class);
    private final TripleWindowReader inputStreamReader;
    private final EngineResultsReader queryResultsReader;
    private final WindowFactory windowFactory;
    private final QueryExecutor queryExecutor;
    private final OracleResultsWriter oracleResultsWriter;
    private final boolean graceful;

    OnWindowCloseComparator(TripleWindowReader inputStreamReader,
            EngineResultsReader queryResultsReader,
            WindowFactory windowFactory, QueryExecutor queryExecutor,
            OracleResultsWriter oracleResultsWriter, boolean graceful) {
        this.inputStreamReader = inputStreamReader;
        this.queryResultsReader = queryResultsReader;
        this.windowFactory = windowFactory;
        this.queryExecutor = queryExecutor;
        this.oracleResultsWriter = oracleResultsWriter;
        this.graceful = graceful;
    }

    @Override
    public void compare() throws IOException {
        final OracleResultBuilder oracleResultBuilder = new OracleResultBuilder();
        for (int i = 1;; i++) {
            final Window window = windowFactory.nextWindow();
            final BindingWindow actual = queryResultsReader.nextBindingWindow();
            if (actual != null) {
                final long delay = 0;
                final TripleWindow inputWindow = inputStreamReader
                        .readNextWindow(window);

                if (inputWindow != null) {
                    final BindingWindow expected = queryExecutor
                            .executeSelect(inputWindow);

                    final FMeasure fMeasure = new FMeasure().calculateScores(
                            expected.getBindings(),actual.getBindings());

                    if (!fMeasure.getNotFoundReferences().isEmpty()) {
                        logger.info("Window #{} [{}:{}]. Missing triples:\n{}",
                                i, inputWindow.getStart(), inputWindow.getEnd(),
                                fMeasure.getNotFoundReferences());
                    }
                    
                    oracleResultsWriter.write(oracleResultBuilder
                            .fMeasure(fMeasure)
                            .delay(delay)
                            .resultSize(expected, actual)
                            .expectedInputSize(inputWindow.getTriples().size())
                            .build());
                } else {
                    throw new IllegalStateException(
                            "Actual results have more windows then expected!");
                }
            } else {
                break;
            }
        }
    }

    private long calculateDelay(final Window one, final Window two) {
        return graceful ? two.getEnd() - one.getEnd() : NO_DELAY;
    }

}
