package io.github.yabench.oracle.tests.comparators;

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
import io.github.yabench.oracle.readers.BufferedERReader;
import io.github.yabench.oracle.readers.TripleWindowReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OnContentChangeComparator implements OracleComparator {

    private static final Logger logger = LoggerFactory.getLogger(
            OnContentChangeComparator.class);
    private final BufferedTWReader isReader;
    private final BufferedERReader qrReader;
    private final WindowFactory windowFactory;
    private final QueryExecutor qexec;
    private final OCCORWriter orWriter;
    private final boolean graceful;

    OnContentChangeComparator(TripleWindowReader inputStreamReader,
            EngineResultsReader queryResultsReader,
            WindowFactory windowFactory, QueryExecutor queryExecutor,
            OracleResultsWriter oracleResultsWriter, boolean graceful) {
        this.isReader = new BufferedTWReader(inputStreamReader);
        this.qrReader = new BufferedERReader(queryResultsReader);
        this.windowFactory = windowFactory;
        this.qexec = queryExecutor;
        this.orWriter = new OCCORWriter(oracleResultsWriter,
                windowFactory.getSlide().toMillis(),
                windowFactory.getSize().toMillis());
        this.graceful = graceful;
    }

    @Override
    public void compare() throws IOException {
        if (graceful) {
            compareInGraceful();
        } else {
            compareInNonGraceful();
        }
    }

    public void compareInGraceful() throws IOException {
        BindingWindow current = null;
        BindingWindow previous = null;
        int actualIndex = -1;
        do {
            final long nextTime = isReader.readTimestampOfNextTriple();
            if (nextTime >= 0) {
                Window window = windowFactory.nextWindow(nextTime, 1);
                TripleWindow inputWindow = isReader.readNextWindow(window);
                if (actualIndex < 0) {
                    actualIndex = qrReader.nextIndex();
                }
                if (current != null && !current.isEmpty()) {
                    previous = current;
                }
//                logger.debug("New previous: {}", previous);
                boolean previousMatched = false;
                boolean tryPreviousWindow = false;
                TripleWindow previousWindow = null;
                do {
//                    logger.debug("Input: {}", inputWindow);
                    current = qexec.executeSelect(inputWindow);
                    if (!current.isEmpty()) {
//                        logger.debug("Current: {}", current);
                        BindingWindow currentReduced;
                        if (previous != null) {
                            currentReduced = WindowUtils.diff(current, previous);
                        } else {
                            currentReduced = current;
                        }
                        if (currentReduced != null && !currentReduced.isEmpty()) {
                            final List<BindingWindow> expected = currentReduced.split();
//                            logger.debug("Previous: {}", previous);
//                            logger.debug("Expected: {}", expected);
                            int j = actualIndex;
                            int actualSize = 0;
                            BindingWindow actual = qrReader.getOrNext(j++);
//                            logger.debug("Actual: {}", actual);
                            if (actual != null) {
                                actualSize += actual.getBindings().size();
                                if (WindowUtils.match(actual, expected)) {
                                    final int numberOfResults = expected.size();
                                    for (int i = 0; i < numberOfResults; i++) {
                                        final BindingWindow found = WindowUtils.findMatch(actual, expected);
                                        if (found != null) {
                                            expected.remove(found);
//                                            logger.debug("Found: {}", found);
                                            actualSize += actual.getBindings().size();

                                            if (!expected.isEmpty()) {
                                                actual = qrReader.getOrNext(j++);
//                                                logger.debug("Next actual: {}", actual);
                                                if (actual == null) {
                                                    break;
                                                }
                                            }
                                        } else {
                                            if (tryPreviousWindow) {
                                                logger.debug("Missing triples: {}", expected);
                                                orWriter.writeMissing(
                                                        inputWindow, expected, actual);

                                                expected.clear();
                                                j--; //get back one actual result
                                            }
                                            break;
                                        }
                                    }
                                    previousMatched = true;
                                    previousWindow = inputWindow;
                                } else {
                                    if (previousMatched) {
//                                        logger.debug("Current window doesn't match, "
//                                                + "but previous did, so let's switch back!");
                                        tryPreviousWindow = true;
                                    }
                                    previousMatched = false;
                                }
                                if (!expected.isEmpty()) {
                                    if (tryPreviousWindow) {
                                        inputWindow = previousWindow;
                                    } else {
//                                        logger.debug("Not equal, reducing and trying once more!");
                                        inputWindow = isReader
                                                .readWindowWithoutFirstGraph(inputWindow);
                                    }
                                } else {
                                    orWriter.writeFound(current, inputWindow, actualSize);
                                    actualIndex = j;

                                    break;
                                }
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }
                } while (true);
            } else {
                BindingWindow actual = qrReader.getOrNext(actualIndex++);
                if (actual != null) {
                    logger.warn("There are still actual results!");
                    do {
                        orWriter.writeMissingExpected(actual);
                        logger.debug("{}", actual);
                    } while ((actual = qrReader.getOrNext(actualIndex++)) != null);
                }
                break;
            }
        } while (true);

        orWriter.flush();
    }

    public void compareInNonGraceful() throws IOException {
        BindingWindow previous = null;
        BindingWindow actual = null;
        do {
            final long nextTime = isReader.readTimestampOfNextTriple();
            if (nextTime >= 0) {
                final Window window = windowFactory.nextWindow(nextTime);
                final TripleWindow inputWindow = isReader.readNextWindow(window);

                final BindingWindow current = qexec.executeSelect(inputWindow);

                if (!current.isEmpty()) {
                    List<BindingWindow> expected = WindowUtils
                            .diff(current, previous).split();
                    if (!expected.isEmpty()) {
//                        logger.debug("Expected: {}", expected);

                        if (actual == null) {
                            actual = qrReader.next();
                        }
//                        logger.debug("Actual: {}", actual);

                        if (actual != null) {
                            actual = find(inputWindow, expected, actual);
                        } else {
                            logger.debug(
                                    "Expected, but there are no actual results anymore: {}",
                                    expected);
                            orWriter.writeMissingActual(expected);
                        }

                        previous = current;
                    }
                }
                isReader.purge(window.getStart());
            } else {
                if ((actual = qrReader.next()) != null) {
                    logger.debug("Didn't find expected for:");
                    do {
                        logger.debug("{}", actual);
                        orWriter.writeMissingExpected(actual);
                    } while ((actual = qrReader.next()) != null);
                }

                break;
            }
        } while (true);
        
        orWriter.flush();
    }

    private BindingWindow find(TripleWindow inputWindow, 
            List<BindingWindow> results, BindingWindow actual)
            throws IOException {
        final int numberOfResults = results.size();
        for (int i = 0; i < numberOfResults; i++) {
            final BindingWindow found = WindowUtils.findMatch(actual, results);
            if (found != null) {
//                logger.debug("Found: {}", found);

                results.remove(found);

                orWriter.writeFound(inputWindow, found, actual);

                if (!results.isEmpty() && (actual = qrReader.next()) == null) {
                    logger.warn(
                            "Expected, but there are no actual results anymore: {}",
                            results);

                    orWriter.writeMissingActual(results);
                    return null;
                }
            } else {
                logger.debug("Expected, but haven't found: {}", results);
//                logger.debug("It was: {}", actual);
                orWriter.writeMissing(inputWindow, results, actual);

                return actual;
                }
            }

        return null;
    }

    public static class OCCORWriter extends OracleResultsWriter {

        public static final double ONE = 1.0;
        public static final double ZERO = 0.0;
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

        public void writeMissing(TripleWindow inputWindow, 
                List<BindingWindow> expected, BindingWindow actual)
                throws IOException {
            for (BindingWindow e : expected) {
                writeMissing(inputWindow, e, actual);
            }
        }

        public void writeMissing(TripleWindow inputWindow, 
                BindingWindow expected, BindingWindow actual)
                throws IOException {
            write(builder
                    .precision(ZERO)
                    .recall(ZERO)
                    .startshift(expected.getStart())
                    .endshift(expected.getEnd())
                    .actualResultSize(actual.getBindings().size())
                    .expectedResultSize(expected.getBindings().size())
                    .expectedInputSize(inputWindow.getTriples().size())
                    .build());
        }

        public void writeFound(BindingWindow expected, TripleWindow input,
                int actualResults) throws IOException {
            write(builder
                    .precision(ONE)
                    .recall(ONE)
                    .startshift(expected.getStart())
                    .endshift(expected.getEnd())
                    .actualResultSize(actualResults)
                    .expectedResultSize(expected.getBindings().size())
                    .expectedInputSize(input.getTriples().size())
                    .build());
        }

        public void writeFound(TripleWindow inputWindow, 
                BindingWindow expected, BindingWindow actual)
                throws IOException {
            write(builder
                    .precision(ONE)
                    .recall(ONE)
                    .startshift(expected.getStart())
                    .endshift(expected.getEnd())
                    .actualResultSize(actual.getBindings().size())
                    .expectedResultSize(expected.getBindings().size())
                    .expectedInputSize(inputWindow.getTriples().size())
                    .delay(actual.getEnd() - expected.getEnd())
                    .build());
        }

        @Override
        public void write(OracleResult result) throws IOException {
            int numberOfSlides = 0;
            while ((numberOfSlides + 1) * windowSlide < result.getEndshift()) {
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
                        .precision(ONE)
                        .recall(ONE)
                        .startshift(i * windowSlide)
                        .endshift(i * windowSlide + windowSize)
                        .expectedInputSize(0)
                        .expectedResultSize(0)
                        .actualResultSize(0)
                        .delay(0)
                        .build());
            }
        }

        private OracleResult merge(List<OracleResult> rs) {
            double precision = 0;
            double recall = 0;
            int actualRS = 0;
            int expectedRS = 0;
            int expectedIS = 0;
            long delay = 0;
            long windowStart = Long.MAX_VALUE;
            long windowEnd = Long.MIN_VALUE;

            for (OracleResult r : rs) {
                precision += r.getPrecision();
                recall += r.getRecall();
                actualRS += r.getActualResultSize();
                expectedRS += r.getExpectedResultSize();
                expectedIS += r.getExpectedInputSize();
                delay += r.getDelay();
                if (r.getStartshift() < windowStart) {
                    windowStart = r.getStartshift();
                }
                if (r.getEndshift() > windowEnd) {
                    windowEnd = r.getEndshift();
                }
            }
            
            int divisor = rs.size() == 0? 1 : rs.size();

            final OracleResult windowResult = builder
                    .precision(precision / divisor)
                    .recall(recall / divisor)
                    .delay(delay / divisor)
                    .startshift(windowStart)
                    .endshift(windowEnd)
                    .expectedResultSize(expectedRS)
                    .expectedInputSize(expectedIS / divisor)
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
