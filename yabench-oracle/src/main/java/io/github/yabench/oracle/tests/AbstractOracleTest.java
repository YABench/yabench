package io.github.yabench.oracle.tests;

import io.github.yabench.commons.TimeUtils;
import io.github.yabench.oracle.BindingWindow;
import io.github.yabench.oracle.FMeasure;
import io.github.yabench.oracle.QueryExecutor;
import io.github.yabench.oracle.ResultsReader;
import io.github.yabench.oracle.TripleWindow;
import io.github.yabench.oracle.WindowFactory;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.io.IOUtils;

abstract class AbstractOracleTest implements OracleTest {

    private static final String QUERY_TEMPLATE_NAME = "query.template";
    private static final String ARG_WINDOWSIZE = "windowsize";
    private static final String ARG_WINDOWSLIDE = "windowslide";
    private final CommandLine cli;
    private final Reader inputStreamReader;
    private final Writer outputWriter;
    private final ResultsReader queryResultsReader;
    private final Map<String, String> vars = new HashMap<>();
    private Duration windowSize;
    private Duration windowSlide;
    private WindowFactory windowFactory;

    AbstractOracleTest(File inputStream, File queryResults, File output,
            CommandLine cli)
            throws IOException {
        this.inputStreamReader = new FileReader(inputStream);
        this.outputWriter = new FileWriter(output);
        this.queryResultsReader = new ResultsReader(new FileReader(queryResults));
        this.cli = cli;
    }

    protected CommandLine getCommandLine() {
        return cli;
    }

    protected Reader getInputStreamReader() {
        return inputStreamReader;
    }

    protected Writer getOutputWriter() {
        return outputWriter;
    }

    protected ResultsReader getQueryResultsReader() {
        return queryResultsReader;
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
        return new Option[]{windowSize, windowSlide};
    }

    @Override
    public void init() throws Exception {
        windowSize = TimeUtils.parseDuration(getCommandLine().getOptionValue(ARG_WINDOWSIZE));
        windowSlide = TimeUtils.parseDuration(getCommandLine().getOptionValue(ARG_WINDOWSLIDE));

        windowFactory = new WindowFactory(getInputStreamReader(), windowSize, windowSlide);

        getQueryResultsReader().initialize();
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(inputStreamReader);
        IOUtils.closeQuietly(outputWriter);
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

    @Override
    public void compare() throws IOException {
        final QueryExecutor qexec = new QueryExecutor();
        final String query = resolveVars(loadQueryTemplate(), vars);
        TripleWindow inputWindow;
        while ((inputWindow = windowFactory.nextWindow()) != null) {
            final BindingWindow expected = qexec.executeSelect(
                    inputWindow, query);
            final BindingWindow actual = getQueryResultsReader().nextWindow();

            FMeasure fMeasure = new FMeasure();
            fMeasure.updateScores(expected.getBindings().toArray(),
                    actual.getBindings().toArray());

            getOutputWriter().write(
                    new StringBuilder()
                    .append(fMeasure.getPrecisionScore())
                    .append('\t')
                    .append(fMeasure.getRecallScore())
                    .append('\t')
                    .append(actual.getEnd() - expected.getEnd())
                    .append('\n')
                    .toString());
        }
    }

}
