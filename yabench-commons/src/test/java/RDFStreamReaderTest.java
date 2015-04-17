
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import io.github.yabench.commons.TemporalRDFReader;
import java.io.IOException;
import java.io.StringReader;
import org.junit.Test;
import static org.junit.Assert.*;

public class RDFStreamReaderTest {

    @Test
    public void test() throws IOException {
        final String stream
                = "<http://knoesis.wright.edu/ssw/MeasureData_AirTemperature_A7_0> "
                + "<http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue> "
                + "15.0^^<http://www.w3.org/2001/XMLSchema#float> \"0\" .";

        StringReader input = new StringReader(stream);
        TemporalRDFReader reader = new TemporalRDFReader(input);

        assertEquals(ResourceFactory.createTypedLiteral("15.0", XSDDatatype.XSDfloat),
                reader.readNextTriple().getStatement().getObject().asLiteral());
    }
}
