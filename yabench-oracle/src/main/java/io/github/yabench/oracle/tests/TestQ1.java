package io.github.yabench.oracle.tests;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * C-SPARQL query:
 *
 * PREFIX om-owl: <http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#>
 * PREFIX weather: <http://knoesis.wright.edu/ssw/ont/weather.owl#>
 *
 * REGISTER QUERY q AS SELECT ?sensor ?tempvalue ?obs 
 * FROM NAMED STREAM <http://cwi.nl/SRBench/observations> [RANGE %WSIZE% MS STEP %WSLIDE% MS]
 * WHERE { 
 *  ?obs om-owl:observedProperty weather:_AirTemperature ;
 *      om-owl:procedure ?sensor ; 
 *      om-owl:result [om-owl:floatValue ?tempvalue] .
 *  FILTER(?tempvalue > %TEMP%)
 * }
 */
public class TestQ1 extends AbstractOracleTest {

    private static final String ARG_TEMPERATURE = "temp";
    private static final String VAR_KEY_TEMP = "%TEMP%";

    public TestQ1(File inputStream, File queryResults, File output,
            CommandLine cli)
            throws IOException {
        super(inputStream, queryResults, output, cli);
    }

    public static Option[] expectedOptions() {
        Option[] parent = getExpectedOptions();

        Option temperature = OptionBuilder
                .isRequired()
                .withType(Float.class)
                .hasArg()
                .withArgName("temperature")
                .withDescription("FILTER value")
                .create(ARG_TEMPERATURE);

        return Stream.concat(
                Arrays.stream(parent),
                Arrays.stream(new Option[]{temperature}))
                .toArray(Option[]::new);
    }

    @Override
    public void init() throws Exception {
        getVars().put(VAR_KEY_TEMP, 
                getCommandLine().getOptionValue(ARG_TEMPERATURE));
        super.init();
    }
}
