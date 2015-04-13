package io.github.yabench.oracle.tests;

import io.github.yabench.commons.TimeUtils;
import io.github.yabench.oracle.BindingWindow;
import io.github.yabench.oracle.FMeasure;
import io.github.yabench.oracle.QueryExecutor;
import io.github.yabench.oracle.EngineResultsReader;
import io.github.yabench.oracle.OracleResultsWriter;
import io.github.yabench.oracle.TripleWindow;
import io.github.yabench.oracle.TripleWindowFactory;
import io.github.yabench.oracle.Window;
import io.github.yabench.oracle.WindowFactory;
import io.github.yabench.oracle.WindowPolicy;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractOracleTest implements OracleTest {

    private static final Logger logger = LoggerFactory.getLogger(
            AbstractOracleTest.class);
    private static final long NO_DELAY = 0;
    private static final String QUERY_TEMPLATE_NAME = "query.template";
    private static final String ARG_WINDOWSIZE = "windowsize";
    private static final String ARG_WINDOWSLIDE = "windowslide";
    private static final String ARG_WINDOWPOLICY = "windowpolicy";
    private static final String ARG_GRACEFUL = "graceful";
    private static final String ARG_GRACEFUL_DEFAULT = "true";
    private final CommandLine cli;
    private final Reader inputStreamReader;
    private final OracleResultsWriter oracleResultsWriter;
    private final EngineResultsReader queryResultsReader;
    private final Map<String, String> vars = new HashMap<>();
    private Duration windowSize;
    private Duration windowSlide;
    private WindowPolicy windowPolicy;
    private boolean graceful;

    AbstractOracleTest(File inputStream, File queryResults, File output,
            CommandLine cli)
            throws IOException {
        this.inputStreamReader = new FileReader(inputStream);
        this.oracleResultsWriter = new OracleResultsWriter(new FileWriter(output));
        this.queryResultsReader = new EngineResultsReader(new FileReader(queryResults));
        this.cli = cli;
    }

    protected CommandLine getCommandLine() {
        return cli;
    }

    protected Reader getInputStreamReader() {
        return inputStreamReader;
    }

    protected OracleResultsWriter getOracleResultsWriter() {
        return oracleResultsWriter;
    }

    protected EngineResultsReader getQueryResultsReader() {
        return queryResultsReader;
    }

    protected Map<String, String> getVars() {
        return vars;
    }

    protected WindowPolicy getWindowPolicy() {
        return windowPolicy;
    }

    protected static Option[] getExpectedOptions() {
        Option windowSize = OptionBuilder
                .isRequired()
                .withType(Long.class)
                .hasArg()
                .withArgName("ms")
                .withDescription("the window size")
                .create(ARG_WINDOWSIZE);

        Option windowSlide = OptionBuilder
                .isRequired()
                .withType(Long.class)
                .hasArg()
                .withArgName("ms")
                .withDescription("the window slide")
                .create(ARG_WINDOWSLIDE);

        Option windowPolicy = OptionBuilder
                .isRequired()
                .hasArg()
                .withArgName("onwindowclose|oncontentchange")
                .create(ARG_WINDOWPOLICY);

        Option graceful = OptionBuilder
                .withType(Boolean.class)
                .hasArg()
                .create(ARG_GRACEFUL);

        return new Option[]{windowSize, windowSlide, graceful, windowPolicy};
    }

    @Override
    public void init() throws Exception {
        windowSize = TimeUtils.parseDuration(getCommandLine()
                .getOptionValue(ARG_WINDOWSIZE));
        windowSlide = TimeUtils.parseDuration(getCommandLine()
                .getOptionValue(ARG_WINDOWSLIDE));
        graceful = Boolean.parseBoolean(getCommandLine()
                .getOptionValue(ARG_GRACEFUL, ARG_GRACEFUL_DEFAULT));
        windowPolicy = WindowPolicy.valueOf(getCommandLine()
                .getOptionValue(ARG_WINDOWPOLICY).toUpperCase());

        getQueryResultsReader().initialize(windowSize);
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(inputStreamReader);
        IOUtils.closeQuietly(oracleResultsWriter);
        IOUtils.closeQuietly(queryResultsReader);
    }

    protected String loadQueryTemplate() throws IOException {
        final String path = new StringBuilder("/")
                .append(this.getClass().getName().replace(".", "/"))
                .append("/")
                .append(QUERY_TEMPLATE_NAME).toString();
        return IOUtils.toString(this.getClass().getResourceAsStream(path));
    }

    protected String resolveVars(final String template,
            final Map<String, String> vars) {
        String result = new String(template);
        for (String key : vars.keySet()) {
            result = result.replaceAll(key, vars.get(key));
        }
        return result;
    }

    private long calculateDelay(final Window one, final Window two) {
        return two.getEnd() - one.getEnd();
    }

    @Override
    public void compare() throws IOException {
        final WindowFactory windowFactory = new WindowFactory(
                windowSize, windowSlide);
        final TripleWindowFactory tripleWindowFactory
                = new TripleWindowFactory(inputStreamReader);

        switch (windowPolicy) {
            case ONWINDOWCLOSE:
                compareOnWindowClose(windowFactory, tripleWindowFactory);
                break;
            case ONCONTENTCHANGE:
                compareOnContentChange(windowFactory, tripleWindowFactory);
                break;
            default:
                throw new UnsupportedOperationException(
                        "Given window policy is not supported!");
        }
    }

    private void compareOnWindowClose(WindowFactory windowFactory,
            TripleWindowFactory tripleWindowFactory)
            throws IOException {
        for (int i = 1;; i++) {
            final Window window = windowFactory.nextWindow();
            final BindingWindow actual = getQueryResultsReader()
                    .nextBindingWindow();
            if (actual != null) {
                final long delay = calculateDelay(window, actual);

                final TripleWindow inputWindow;
                if (graceful) {
                    inputWindow = tripleWindowFactory.nextTripleWindow(
                            window, delay);
                } else {
                    inputWindow = tripleWindowFactory.nextTripleWindow(
                            window, NO_DELAY);
                }

                if (inputWindow != null) {
                    final QueryExecutor qexec = new QueryExecutor();
                    final String query = resolveVars(loadQueryTemplate(), vars);
                    final BindingWindow expected = qexec.executeSelect(
                            inputWindow, query);

                    FMeasure fMeasure = new FMeasure();
                    fMeasure.updateScores(expected.getBindings().toArray(),
                            actual.getBindings().toArray());

                    if (!fMeasure.getNotFound().isEmpty()) {
                        logger.info("Window #{} [{}:{}]. Missing triples:\n{}",
                                i, inputWindow.getStart(), inputWindow.getEnd(),
                                fMeasure.getNotFound());
                    }

                    oracleResultsWriter.write(fMeasure.getPrecisionScore(),
                            fMeasure.getRecallScore(),
                            delay,
                            actual.getBindings().size(),
                            expected.getBindings().size(),
                            inputWindow.getTriples().size());
                } else {
                    //TODO: That's it?
                    break;
                }
            } else {
                break;
            }
        }
    }

    private void compareOnContentChange(WindowFactory windowFactory,
            TripleWindowFactory tripleWindowFactory) throws IOException {
        for (int i = 1;; i++) {
            final BindingWindow actual = getQueryResultsReader()
                    .nextBindingWindow();
            if (actual != null) {
                final Window window = windowFactory.nextWindow(actual.getEnd());
                final long delay = graceful
                        ? calculateDelay(window, actual) : NO_DELAY;

                final TripleWindow inputWindow;
                inputWindow = tripleWindowFactory.nextTripleWindow(
                        window, delay);

                if (inputWindow != null) {
                    final QueryExecutor qexec = new QueryExecutor();
                    final String query = resolveVars(loadQueryTemplate(), vars);
                    final BindingWindow expected = qexec.executeSelect(
                            inputWindow, query);

                    FMeasure fMeasure = new FMeasure();
                    fMeasure.updateScores(expected.getBindings().toArray(),
                            actual.getBindings().toArray());

                    if (!fMeasure.getNotFound().isEmpty()) {
                        logger.info("Window #{} [{}:{}]. Missing triples:\n{}",
                                i, inputWindow.getStart(), inputWindow.getEnd(),
                                fMeasure.getNotFound());
                    }

                    oracleResultsWriter.write(fMeasure.getPrecisionScore(),
                            fMeasure.getRecallScore(),
                            delay,
                            actual.getBindings().size(),
                            expected.getBindings().size(),
                            inputWindow.getTriples().size());
                } else {
                    //TODO: That's it?
                    break;
                }
            } else {
                break;
            }
        }
    }

}
