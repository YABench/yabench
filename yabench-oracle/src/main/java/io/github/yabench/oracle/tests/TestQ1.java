package io.github.yabench.oracle.tests;

import io.github.yabench.oracle.BindingWindow;
import io.github.yabench.oracle.FMeasure;
import io.github.yabench.oracle.QueryExecutor;
import io.github.yabench.oracle.TripleWindow;
import io.github.yabench.oracle.WindowFactory;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * PREFIX om-owl: <http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#>
 * PREFIX weather: <http://knoesis.wright.edu/ssw/ont/weather.owl#>
 *
 * REGISTER QUERY q AS SELECT ?sensor ?tempvalue ?obs FROM NAMED STREAM
 * <http://cwi.nl/SRBench/observations> [RANGE %WSIZE% MS STEP %WSLIDE% MS]
 * WHERE { ?obs om-owl:observedProperty weather:_AirTemperature ;
 * om-owl:procedure ?sensor ; om-owl:result [om-owl:floatValue ?tempvalue] .
 * FILTER(?tempvalue > %TEMP%) }
 */
public class TestQ1 extends AbstractOracleTest {

    private static final String ARG_WINDOWSIZE = "windowsize";
    private static final String ARG_WINDOWSLIDE = "windowslide";
    private static final String ARG_TEMPERATURE = "temp";
    private static final String VAR_KEY_TEMP = "%TEMP%";

    private final Map<String, String> vars = new HashMap<>();

    private long windowSize;
    private long windowSlide;
    private WindowFactory windowFactory;

    public TestQ1(File inputStream, File queryResults, File output, 
            CommandLine cli)
            throws IOException {
        super(inputStream, queryResults, output, cli);
    }

    public static Option[] expectedOptions() {
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

        Option temperature = OptionBuilder
                .isRequired()
                .withType(Float.class)
                .hasArg()
                .withArgName("temperature")
                .withDescription("FILTER value")
                .create(ARG_TEMPERATURE);

        return new Option[]{windowSize, windowSlide, temperature};
    }

    @Override
    public void init() throws Exception {
        windowSize = Long.parseLong(getOptions().getOptionValue(ARG_WINDOWSIZE));
        windowSlide = Long.parseLong(getOptions().getOptionValue(ARG_WINDOWSLIDE));

        vars.put(VAR_KEY_TEMP, getOptions().getOptionValue(ARG_TEMPERATURE));

        windowFactory = new WindowFactory(getInputStreamReader(), windowSize, windowSlide);
        
        getQueryResultsReader().initialize();
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
