package io.github.yabench.oracle.tests;

import io.github.yabench.oracle.tests.comparators.OracleComparator;
import io.github.yabench.oracle.tests.comparators.OracleComparatorBuilder;
import io.github.yabench.commons.TimeUtils;
import io.github.yabench.oracle.QueryExecutor;
import io.github.yabench.oracle.EngineResultsReader;
import io.github.yabench.oracle.OracleResultsWriter;
import io.github.yabench.oracle.InputStreamReader;
import io.github.yabench.oracle.WindowFactory;
import io.github.yabench.oracle.WindowPolicy;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
    private static final String QUERY_TEMPLATE_NAME = "query.template";
    private static final String ARG_WINDOWSIZE = "windowsize";
    private static final String ARG_WINDOWSLIDE = "windowslide";
    private static final String ARG_WINDOWPOLICY = "windowpolicy";
    private static final String ARG_GRACEFUL = "graceful";
    private static final String ARG_GRACEFUL_DEFAULT = "true";
    private final CommandLine cli;
    private final InputStreamReader inputStreamReader;
    private final OracleResultsWriter oracleResultsWriter;
    private final EngineResultsReader queryResultsReader;
    private final Map<String, String> vars = new HashMap<>();
    private OracleComparator comparator;

    AbstractOracleTest(File inputStream, File queryResults, File output,
            CommandLine cli)
            throws IOException {
        this.inputStreamReader = new InputStreamReader(new FileReader(inputStream));
        this.oracleResultsWriter = new OracleResultsWriter(new FileWriter(output));
        this.queryResultsReader = new EngineResultsReader(new FileReader(queryResults));
        this.cli = cli;
    }

    protected CommandLine getCommandLine() {
        return cli;
    }

    protected Map<String, String> getVars() {
        return vars;
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
        final Duration windowSize = TimeUtils.parseDuration(getCommandLine()
                .getOptionValue(ARG_WINDOWSIZE));
        final Duration windowSlide = TimeUtils.parseDuration(getCommandLine()
                .getOptionValue(ARG_WINDOWSLIDE));
        final boolean graceful = Boolean.parseBoolean(getCommandLine()
                .getOptionValue(ARG_GRACEFUL, ARG_GRACEFUL_DEFAULT));
        final WindowPolicy windowPolicy = WindowPolicy.valueOf(getCommandLine()
                .getOptionValue(ARG_WINDOWPOLICY).toUpperCase());

        queryResultsReader.initialize(windowSize);

        final QueryExecutor queryExecutor = new QueryExecutor(loadQueryTemplate(), vars);
        final WindowFactory windowFactory = new WindowFactory(windowSize, windowSlide);
        comparator = new OracleComparatorBuilder(
                inputStreamReader, queryResultsReader, windowFactory,
                queryExecutor, oracleResultsWriter, graceful)
                .newComparator(windowPolicy);
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

    @Override
    public void compare() throws IOException {
        comparator.compare();
    }
}
