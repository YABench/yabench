package io.github.yabench.oracle.tests.comparators;

import io.github.yabench.commons.TemporalTriple;
import io.github.yabench.oracle.BindingWindow;
import io.github.yabench.oracle.OracleResult;
import io.github.yabench.oracle.readers.EngineResultsReader;
import io.github.yabench.oracle.readers.BufferedTWReader;
import io.github.yabench.oracle.OracleResultBuilder;
import io.github.yabench.oracle.OracleResultsWriter;
import io.github.yabench.oracle.QueryExecutor;
import io.github.yabench.oracle.TripleWindow;
import io.github.yabench.oracle.Window;
import io.github.yabench.oracle.WindowFactory;
import io.github.yabench.oracle.WindowUtils;
import io.github.yabench.oracle.readers.TripleWindowReader;
import java.io.IOException;
import java.io.Writer;
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
    private final OCCORWriter orWriter;
    private final boolean graceful;
    private TripleWindow previousInputWindow;

    OnContentChangeComparator(TripleWindowReader inputStreamReader,
            EngineResultsReader queryResultsReader,
            WindowFactory windowFactory, QueryExecutor queryExecutor,
            OracleResultsWriter oracleResultsWriter, boolean graceful) {
        this.isReader = new BufferedTWReader(inputStreamReader);
        this.qrReader = queryResultsReader;
        this.windowFactory = windowFactory;
        this.qexec = queryExecutor;
        this.orWriter = new OCCORWriter(oracleResultsWriter,
                windowFactory.getSlide().toMillis(),
                windowFactory.getSize().toMillis());
        this.graceful = graceful;
    }

    @Override
    public void compare() throws IOException {
        BindingWindow actual = null;
        for (;;) {
            final List<BindingWindow> expected = nextExpectedResult();
            if (expected != null && !expected.isEmpty()) {
                /**
                 * Remove from the buffer input triples which won't be used
                 * anymore.
                 */
                isReader.purge(expected.get(0).getStart()
                        - windowFactory.getSize().toMillis() * 2);

//                logger.debug("Expected: {}", expected);
                if (actual == null) {
                    actual = qrReader.next();
                }
//                logger.debug("Actual: {}", actual);
                if (actual != null) {
                    if (actual.equals(expected) != null) {
                        actual = find(expected, actual);
                    } else {
                        if (graceful) {
                            logger.debug("Expected haven't found, so trying graceful mode!");
                            final List<BindingWindow> expectedInGraceful
                                    = tryToFindExpectedResult(
                                            WindowUtils.join(expected),
                                            actual);
                            actual = find(expectedInGraceful, actual);

                            if (actual == null) {
                                actual = qrReader.next();
                            }

                            if (actual != null) {
                                BindingWindow found;
                                if ((found = actual.equals(expected)) != null) {
//                                    logger.debug("Found: {}", found);

                                    orWriter.writeFound(found, actual);

                                    actual = null;
                                } else {
//                                    logger.debug("It was again: {}", actual);
                                }
                            }
                        } else {
                            logger.debug("Expected, but haven't found: {}", expected);
                            orWriter.writeMissing(expected, actual);

                            actual = null;
                        }
                    }
                } else {
                    logger.debug("It was expected more actual results!");

                    orWriter.writeMissingActual(expected);
                }
            } else {
                /**
                 * If still there are actual query results, then write results
                 * with lowest precision/recall.
                 */
                if ((actual = qrReader.next()) != null) {
                    logger.warn("There are still actual query results!");
                    do {
                        orWriter.writeMissingExpected(actual);

                        logger.debug("{}", actual);
                    } while ((actual = qrReader.next()) != null);
                }

                orWriter.flush();

                break;
            }
        }
    }

    private BindingWindow find(List<BindingWindow> results, BindingWindow actual)
            throws IOException {
        final int numberOfResults = results.size();
        for (int i = 0; i < numberOfResults; i++) {
            final BindingWindow found = actual.equals(results);
            if (found != null) {
//                logger.debug("Found: {}", found);

                results.remove(found);

                orWriter.writeFound(found, actual);
                if ((results.size() > 0)
                        && (actual = qrReader.next()) == null) {
                    logger.warn("Expected more actual results! {}", results);

                    orWriter.writeMissingActual(results);

                    return null;
                }
            } else {
                logger.debug("Expected, but haven't found: {}", results);
//                logger.debug("It was: {}", actual);
                orWriter.writeMissing(results, actual);

                return actual;
            }
        }

        return null;
    }

    private List<BindingWindow> nextExpectedResult() throws IOException {
        BindingWindow expected = null;
        do {
            final long nextTime = isReader.readTimestampOfNextTriple();
            if (nextTime >= 0) {
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
//                logger.debug("Ideal previous: {}", previous);
//                logger.debug("Ideal current: {}", current);

                if (!current.isEmpty()) {
                    expected = current.remove(previous);
                    if (expected == null || expected.isEmpty()) {
                        expected = null;
                    }
                } else {
                    expected = null;
                }
                previousInputWindow = inputWindow;
            } else {
                break;
            }
        } while (expected == null);
        return expected == null ? null : expected.splitByOneBinding();
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
//                logger.debug("{}", niw.getModel().listStatements(null, null, 
//                        ResourceFactory.createTypedLiteral("97", XSDDatatype.XSDfloat)).toList());
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
                    ne = null;
                    break;
                }
            } while (ne == null);
            logger.debug("Trying: {}", ne);
            logger.debug("Actual: {}", actual);
            if (ne != null) {
                results = ne.splitByOneBinding();
                if (actual.equals(results) != null) {
                    break;
                }
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

    private class OCCORWriter extends OracleResultsWriter {

        private static final double ONE = 1.0;
        private static final double ZERO = 0.0;
        private static final int FIRST = 0;
        private final OracleResultBuilder builder = new OracleResultBuilder();
        private final List<OracleResult> results = new ArrayList<>();
        private final long windowSize;
        private final long windowSlide;
        private int currentWindowNumber = FIRST;

        public OCCORWriter(OracleResultsWriter writer, long windowSlide, long windowSize) {
            this(writer.getWriter(), windowSlide, windowSize);
        }

        public OCCORWriter(Writer writer, long windowSlide, long windowSize) {
            super(writer);
            this.windowSlide = windowSlide;
            this.windowSize = windowSize;
        }

        public void writeMissingActual(List<BindingWindow> expected) throws IOException {
            for (BindingWindow e : expected) {
                writeMissingActual(e);
            }
        }

        public void writeMissingActual(BindingWindow expected) throws IOException {
            write(builder
                    .precision(ZERO)
                    .recall(ZERO)
                    .expectedResultSize(expected.getBindings().size())
                    .build());
        }

        public void writeMissingExpected(BindingWindow actual) throws IOException {
            write(builder
                    .precision(ZERO)
                    .recall(ZERO)
                    .actualResultSize(actual.getBindings().size())
                    .build());
        }

        public void writeMissing(List<BindingWindow> expected, BindingWindow actual)
                throws IOException {
            for (BindingWindow e : expected) {
                writeMissing(e, actual);
            }
        }

        public void writeMissing(BindingWindow expected, BindingWindow actual)
                throws IOException {
            write(builder
                    .precision(0.0)
                    .recall(0.0)
                    .startshift(expected.getStart())
                    .endshift(expected.getEnd())
                    .actualResultSize(actual.getBindings().size())
                    .expectedResultSize(expected.getBindings().size())
                    .build());
        }

        public void writeFound(BindingWindow expected, BindingWindow actual)
                throws IOException {
            write(builder
                    .precision(1.0)
                    .recall(1.0)
                    .startshift(expected.getStart())
                    .endshift(expected.getEnd())
                    .actualResultSize(actual.getBindings().size())
                    .expectedResultSize(expected.getBindings().size())
                    .build());
        }

        @Override
        public void write(OracleResult result) throws IOException {
            int numberOfSlides = 0;
            while ((numberOfSlides + 1) * windowSlide <= result.getEndshift()) {
                numberOfSlides++;
            }

            if (numberOfSlides == currentWindowNumber
                    || currentWindowNumber == FIRST) {
                results.add(result);

                writeEmptyWindows(currentWindowNumber, numberOfSlides);

                currentWindowNumber = numberOfSlides;
            } else {
                if (numberOfSlides - currentWindowNumber > 1) {
                    writeEmptyWindows(currentWindowNumber + 1, numberOfSlides);
                }

                if (!results.isEmpty()) {
                    super.write(merge(results));
                }

                //Clean the buffer to collect results for the next window
                results.clear();
                results.add(result);
                currentWindowNumber = numberOfSlides;
            }
        }

        private void writeEmptyWindows(int current, int slides)
                throws IOException {
            for (int i = current; i < slides; i++) {
                super.write(builder
                        .precision(1.0)
                        .recall(1.0)
                        .startshift(currentWindowNumber * windowSlide)
                        .endshift(currentWindowNumber * windowSlide + windowSize)
                        .expectedInputSize(0)
                        .expectedResultSize(0)
                        .actualResultSize(0)
                        .build());
            }
        }

        private OracleResult merge(List<OracleResult> rs) {
            double precision = 0;
            double recall = 0;
            int actualRS = 0;
            int expectedRS = 0;
            long windowStart = Long.MAX_VALUE;
            long windowEnd = Long.MIN_VALUE;

            for (OracleResult r : results) {
                precision += r.getPrecision();
                recall += r.getRecall();
                actualRS += r.getActualResultSize();
                expectedRS += r.getExpectedResultSize();
                if (r.getStartshift() < windowStart) {
                    windowStart = r.getStartshift();
                }
                if (r.getEndshift() > windowEnd) {
                    windowEnd = r.getEndshift();
                }
            }

            final OracleResult windowResult = builder
                    .precision(precision / results.size())
                    .recall(recall / results.size())
                    .startshift(windowStart)
                    .endshift(windowEnd)
                    .expectedResultSize(expectedRS)
                    .actualResultSize(actualRS)
                    .build();
            return windowResult;
        }

        public void flush() throws IOException {
            final OracleResult windowResult = merge(results);

            super.write(windowResult);
        }

    }
}
