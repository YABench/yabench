package io.github.yabench.oracle.tests.comparators;

import io.github.yabench.commons.TemporalTriple;
import io.github.yabench.oracle.BindingWindow;
import io.github.yabench.oracle.readers.EngineResultsReader;
import io.github.yabench.oracle.readers.BufferedTWReader;
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
    private final BufferedTWReader isReader;
    private final EngineResultsReader qrReader;
    private final WindowFactory windowFactory;
    private final QueryExecutor qexec;
    private final OracleResultsWriter orWriter;
    private final boolean graceful;
    private TripleWindow previousInputWindow;

    OnContentChangeComparator(TripleWindowReader inputStreamReader,
            EngineResultsReader queryResultsReader,
            WindowFactory windowFactory, QueryExecutor queryExecutor,
            OracleResultsWriter oracleResultsWriter, boolean graceful) {
        this.isReader = inputStreamReader;
        this.qrReader = queryResultsReader;
        this.windowFactory = windowFactory;
        this.qexec = queryExecutor;
        this.orWriter = oracleResultsWriter;
        this.graceful = graceful;
    }

    @Override
    public void compare() throws IOException {
        final OracleResultBuilder orBuilder = new OracleResultBuilder();
        boolean tryOnceMore = false;
        BindingWindow expected = null;
        BindingWindow actual = null;
        for (int i = 1;; i++) {
            if (!tryOnceMore) {
                expected = nextExpectedResult();
            }
//            logger.debug("Ideal expected: {}", expected);
            if (expected != null) {
                if (!tryOnceMore) {
                    actual = qrReader.next();
                }
                if (actual != null) {
                    if (!actual.equalsByContent(expected)) {
                        if (graceful && !tryOnceMore) {
                            logger.debug("========Trying in graceful mode========");
                            final List<BindingWindow> results
                                    = tryToFindExpectedResult(expected, actual);
                            tryOnceMore = true;
                            final int numberOfResults = results.size();
                            for (int j = 0; j < numberOfResults; j++) {
                                final BindingWindow found = actual.equals(results);
                                if (found != null) {
//                                    logger.debug("Found in graceful: {}", found);
                                    results.remove(found);
                                    orWriter.write(orBuilder
                                            .precision(1.0)
                                            .recall(1.0)
                                            .build());
                                    if ((actual = qrReader.next()) == null) {
                                        throw new IllegalStateException();
                                    }
                                } else {
                                    logger.debug("[Graceful Mode] Missing results: {}", results);
                                    orWriter.write(orBuilder
                                            .precision(0)
                                            .recall(0)
                                            .build());
                                }
                            }
                        } else {
                            tryOnceMore = false;
                            logger.debug("Missing result: {}", expected);
                            orWriter.write(orBuilder
                                    .precision(0)
                                    .recall(0)
                                    .build());
                        }
                    } else {
//                        logger.debug("Found in ideal: {}", expected);
                        tryOnceMore = false;
                        orWriter.write(orBuilder.precision(1.0).recall(1.0)
                                .build());
                    }
                } else {
                    throw new IllegalStateException();
                }
            } else {
                if (qrReader.hasNext()) {
                    throw new IllegalStateException();
                } else {
                    break;
                }
            }
        }
    }

    private BindingWindow nextExpectedResult() throws IOException {
        BindingWindow expected = null;
        do {
            final long nextTime = isReader.readTimestampOfNextTriple();
            if (nextTime > 0) {
                final Window window = windowFactory.nextWindow(nextTime);
                final TripleWindow inputWindow = isReader.readNextWindow(window);

                final BindingWindow previous;
                if (previousInputWindow != null) {
                    previousInputWindow = filter(previousInputWindow, window);
                    previous = qexec.executeSelect(previousInputWindow);
                } else {
                    previous = null;
                }

                final BindingWindow current = qexec.executeSelect(inputWindow);

                expected = current.isEmpty() ? null : current.remove(previous);

                previousInputWindow = inputWindow;
            } else {
                break;
            }
        } while (expected == null);
        return expected;
    }

    private List<BindingWindow> tryToFindExpectedResult(BindingWindow previous,
            BindingWindow actual)
            throws IOException {
        List<BindingWindow> results = null;
        Window prevWindow = previous;
        BindingWindow nePrev = previous;
        BindingWindow ne = null;
        do {
            do {
                final TripleWindow niw = isReader.prevWindow(prevWindow);
                if (niw != null) {
//                    logger.debug("Previous: {}", nePrev.toString());
                    BindingWindow cbw = qexec.executeSelect(niw);
//                    logger.debug("Current: {}", cbw.toString());
                    if (!cbw.isEmpty()) {
                        ne = cbw.remove(nePrev);
                    } else {
                        ne = null;
                    }
                    prevWindow = niw;
                } else {
                    break;
                }
            } while (ne == null);

            if (ne != null) {
                results = ne.splitByOneBinding();
                if (actual.equals(results) != null) { break; }
            } else {
                break;
            }
        } while (true);
        return results;
    }

    /**
     * Removes triples from the given triple window which are out of the given
     * window scope.
     *
     * @param tripleWindow a triple window
     * @param window new window scope
     * @return
     */
    private TripleWindow filter(final TripleWindow tripleWindow,
            final Window window) {
        final List<TemporalTriple> triples = new ArrayList<>(Arrays.asList(
                tripleWindow.getTriples().stream()
                .filter((triple) -> triple.getTime() >= window.getStart())
                .toArray(TemporalTriple[]::new)));
        return new TripleWindow(window, triples);
    }

}
