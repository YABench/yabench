package io.github.yabench.oracle.tests;

import io.github.yabench.oracle.Window;
import io.github.yabench.oracle.WindowFactory;
import java.io.File;
import java.io.IOException;
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

    private static final String TESTNAME = "TestQ1";
    private static final String ARG_WINDOWSIZE = "windowsize";
    private static final String ARG_WINDOWSLIDE = "windowslide";
    private static final String ARG_TEMPERATURE = "temp";

    private long windowSize;
    private long windowSlide;
    private float temp;
    private WindowFactory windowFactory;

    public TestQ1(File inputStream, CommandLine cli) throws IOException {
        super(inputStream, cli);
    }

    @Override
    public String getName() {
        return TESTNAME;
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
        temp = Float.parseFloat(getOptions().getOptionValue(ARG_TEMPERATURE));
        windowFactory = new WindowFactory(getReader(), windowSize, windowSlide);
    }

    @Override
    public int compare() throws IOException {
        Window window;
        while ((window = windowFactory.nextWindow()) != null)  {
            //write to database
            //query expected results
            //compare with the actual results
        }
        return 0;
    }

}
