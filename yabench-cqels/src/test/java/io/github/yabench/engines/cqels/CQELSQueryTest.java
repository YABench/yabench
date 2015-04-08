package io.github.yabench.engines.cqels;

import java.time.Duration;
import org.junit.Test;
import static org.junit.Assert.*;

public class CQELSQueryTest {

    @Test
    public void test() {
        final String queryString = ""
                + "PREFIX om-owl: <http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#> \n"
                + "PREFIX weather: <http://knoesis.wright.edu/ssw/ont/weather.owl#> \n"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
                + "SELECT ?sensor ?obs \n"
                + "WHERE { \n"
                + "  STREAM <http://ex.org/streams/test> [RANGE 120s SLIDE 1m] { \n"
                + "   ?obs om-owl:observedProperty weather:_AirTemperature ; \n"
                + "	om-owl:procedure ?sensor ; \n"
                + "	om-owl:result ?res . \n"
                + "   ?res om-owl:floatValue ?value . \n"
                + "  } \n"
                + "  FILTER(?value > %TEMP%) \n"
                + "}";
        CQELSQuery query = new CQELSQuery(queryString);

        assertEquals(Duration.ofSeconds(120), query.getWindowSize());
        assertEquals(Duration.ofSeconds(60), query.getWindowSlide());
    }
}
