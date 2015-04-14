package io.github.yabench.oracle.tests.comparators;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import io.github.yabench.commons.TemporalGraph;
import io.github.yabench.commons.TemporalTriple;
import io.github.yabench.oracle.BindingWindow;
import io.github.yabench.oracle.EngineResultsReader;
import io.github.yabench.oracle.FMeasure;
import io.github.yabench.oracle.InputStreamReader;
import io.github.yabench.oracle.OracleResult;
import io.github.yabench.oracle.OracleResultBuilder;
import io.github.yabench.oracle.OracleResultsWriter;
import io.github.yabench.oracle.QueryExecutor;
import io.github.yabench.oracle.TripleWindow;
import io.github.yabench.oracle.Window;
import io.github.yabench.oracle.WindowFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnContentChangeComparator implements OracleComparator {

    private static final Logger logger = LoggerFactory.getLogger(
            OnContentChangeComparator.class);
    private final InputStreamReader inputStreamReader;
    private final EngineResultsReader queryResultsReader;
    private final WindowFactory windowFactory;
    private final QueryExecutor queryExecutor;
    private final OracleResultsWriter oracleResultsWriter;
    private TripleWindow previousInputWindow;

    OnContentChangeComparator(InputStreamReader inputStreamReader,
            EngineResultsReader queryResultsReader,
            WindowFactory windowFactory, QueryExecutor queryExecutor, 
            OracleResultsWriter oracleResultsWriter) {
        this.inputStreamReader = inputStreamReader;
        this.queryResultsReader = queryResultsReader;
        this.windowFactory = windowFactory;
        this.queryExecutor = queryExecutor;
        this.oracleResultsWriter = oracleResultsWriter;
    }

    @Override
    public void compare() throws IOException {
        final OracleResultBuilder oracleResultBuilder = new OracleResultBuilder();
        for (int i = 1;; i++) {
            final BindingWindow actual = queryResultsReader.nextBindingWindow();
            if (actual != null) {
                final BindingWindow expected = nextExpectedResult();
                if (expected != null) {
//                        logger.debug("Window #{}. {}", i, expected.toString());
                    final FMeasure fMeasure = new FMeasure();
                    fMeasure.updateScores(expected.getBindings().toArray(),
                            actual.getBindings().toArray());

                    if (!fMeasure.getNotFound().isEmpty()) {
                        logger.info("Window #{} [{}:{}]. Missing triples:\n{}",
                                i, expected.getStart(), expected.getEnd(),
                                fMeasure.getNotFound());
                    }
                    
                    final OracleResult result = oracleResultBuilder
                            .fMeasure(fMeasure)
                            .resultSize(expected, actual)
                            .build();
                    oracleResultsWriter.write(result);
                } else {
                    throw new IllegalStateException();
                }
            } else {
                break;
            }
        }
    }

    private BindingWindow nextExpectedResult()
            throws IOException {
        BindingWindow expected = null;
        do {
            final TemporalGraph inputGraph = inputStreamReader
                    .nextGraph();
            if (inputGraph != null) {
                final Window window = windowFactory.nextWindow(
                        inputGraph.getTime());
                final TripleWindow inputWindow = inputStreamReader
                        .nextTripleWindow(window, NO_DELAY);

                final BindingWindow previous = previousInputWindow != null
                        ? queryExecutor
                        .executeSelect(filter(previousInputWindow, window))
                        : null;
                final BindingWindow current = queryExecutor
                        .executeSelect(inputWindow);
                expected = difference(previous, current);

                previousInputWindow = inputWindow;
            } else {
                break;
            }
        } while (expected == null);
        return expected;
    }

    private BindingWindow difference(BindingWindow one, BindingWindow two) {
        if (one == null) {
            if (two.getBindings().isEmpty()) {
                return null;
            } else {
                return two;
            }
        } else if (one.equals(two)) {
            return null;
        } else {
            List<Binding> rest = new ArrayList<>(two.getBindings());
            rest.removeAll(one.getBindings());
            return rest.isEmpty()
                    ? null
                    : new BindingWindow(rest, two.getStart(), two.getEnd());
        }
    }

    private TripleWindow filter(final TripleWindow tripleWindow, final Window window) {
        List<TemporalTriple> triples = new ArrayList<>(Arrays.asList(
                tripleWindow.getTriples().stream()
                .filter((triple) -> triple.getTime() >= (window.getStart()))
                .toArray(TemporalTriple[]::new)));
        return new TripleWindow(window, triples);
    }

}
