package io.github.yabench.oracle.tests.comparators;

import io.github.yabench.oracle.BindingWindow;
import io.github.yabench.oracle.EngineResultsReader;
import io.github.yabench.oracle.FMeasure;
import io.github.yabench.oracle.InputStreamReader;
import io.github.yabench.oracle.OracleResultBuilder;
import io.github.yabench.oracle.OracleResultsWriter;
import io.github.yabench.oracle.QueryExecutor;
import io.github.yabench.oracle.TripleWindow;
import io.github.yabench.oracle.Window;
import io.github.yabench.oracle.WindowFactory;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnWindowCloseComparator implements OracleComparator {

    private static final Logger logger = LoggerFactory.getLogger(
            OnWindowCloseComparator.class);
    private final InputStreamReader inputStreamReader;
    private final EngineResultsReader queryResultsReader;
    private final WindowFactory windowFactory;
    private final QueryExecutor queryExecutor;
    private final OracleResultsWriter oracleResultsWriter;
    private final boolean graceful;

    OnWindowCloseComparator(InputStreamReader inputStreamReader,
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
                final long delay = calculateDelay(window, actual);
                final TripleWindow inputWindow = inputStreamReader
                        .nextTripleWindow(window, delay);

                if (inputWindow != null) {
                    final BindingWindow expected = queryExecutor
                            .executeSelect(inputWindow);

                    FMeasure fMeasure = new FMeasure();
                    fMeasure.updateScores(expected.getBindings().toArray(),
                            actual.getBindings().toArray());

                    if (!fMeasure.getNotFound().isEmpty()) {
                        logger.info("Window #{} [{}:{}]. Missing triples:\n{}",
                                i, inputWindow.getStart(), inputWindow.getEnd(),
                                fMeasure.getNotFound());
                    }
                    
                    oracleResultsWriter.write(oracleResultBuilder
                            .fMeasure(fMeasure)
                            .delay(delay)
                            .resultSize(expected, actual)
                            .expectedInputSize(inputWindow.getTriples().size())
                            .build());
                } else {
                    throw new IllegalStateException();
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
