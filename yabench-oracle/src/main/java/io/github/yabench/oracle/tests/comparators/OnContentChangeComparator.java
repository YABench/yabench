package io.github.yabench.oracle.tests.comparators;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import io.github.yabench.commons.TemporalGraph;
import io.github.yabench.commons.TemporalTriple;
import io.github.yabench.oracle.BindingWindow;
import io.github.yabench.oracle.readers.EngineResultsReader;
import io.github.yabench.oracle.FMeasure;
import io.github.yabench.oracle.readers.BufferedTWReader;
import io.github.yabench.oracle.OracleResult;
import io.github.yabench.oracle.OracleResultBuilder;
import io.github.yabench.oracle.OracleResultsWriter;
import io.github.yabench.oracle.QueryExecutor;
import io.github.yabench.oracle.TripleWindow;
import io.github.yabench.oracle.Window;
import io.github.yabench.oracle.WindowFactory;
import io.github.yabench.oracle.readers.TripleWindowReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnContentChangeComparator implements OracleComparator {

    private static final Logger logger = LoggerFactory.getLogger(
            OnContentChangeComparator.class);
    private final TripleWindowReader inputStreamReader;
    private final EngineResultsReader queryResultsReader;
    private final WindowFactory windowFactory;
    private final QueryExecutor queryExecutor;
    private final OracleResultsWriter oracleResultsWriter;
    private TripleWindow previousInputWindow;

    OnContentChangeComparator(BufferedTWReader inputStreamReader,
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
                    final FMeasure fMeasure = new FMeasure().calculateScores(
                            expected.getBindings(), actual.getBindings());

                    if (!fMeasure.getNotFoundReferences().isEmpty()) {
                        logger.info("#{} Window [{}:{}]. Missing triples:\n{}",
                                i, expected.getStart(), expected.getEnd(),
                                fMeasure.getNotFoundReferences());
                    }

                    final OracleResult result = oracleResultBuilder
                            .fMeasure(fMeasure)
                            .resultSize(expected, actual)
                            .build();
                    oracleResultsWriter.write(result);
                } else {
                    throw new IllegalStateException(
                            "Actual results have more windows then expected!");
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
                    .readNextGraph();
            if (inputGraph != null) {
                final Window window = windowFactory.nextWindow(
                        inputGraph.getTime());
                final TripleWindow inputWindow = inputStreamReader
                        .readNextWindow(window);

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
        if(isEmpty(two)) {
            return null;
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
    
    private boolean isEmpty(final BindingWindow window) {
        if(window.getBindings().isEmpty()) {
            return true;
        } else if(window.getBindings().size() == 1) {
            final Binding binding = window.getBindings().get(0);
            if(binding.isEmpty()) {
                return true;
            } else {
                final Iterator<Var> vars = binding.vars();
                while (vars.hasNext()) {
                    final Var next = vars.next();
                    if(binding.get(next) != null) {
                        return false;
                    }
                }
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Removes triples from the given triple window which are out of 
     * the given window scope.
     * 
     * @param tripleWindow a triple window
     * @param window new window scope
     * @return 
     */
    private TripleWindow filter(final TripleWindow tripleWindow, 
            final Window window) {
        final List<TemporalTriple> triples = new ArrayList<>(Arrays.asList(
                tripleWindow.getTriples().stream()
                .filter((triple) -> triple.getTime() >= (window.getStart()))
                .toArray(TemporalTriple[]::new)));
        return new TripleWindow(window, triples);
    }

}
