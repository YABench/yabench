import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import io.github.yabench.commons.StatementComparator;
import org.junit.Test;
import static org.junit.Assert.*;

public class StatementComparatorTest {

    @Test
    public void test() {
        Statement stmt1 = new StatementImpl(
                resource("http://knoesis.wright.edu/ssw/MeasureData_AirTemperature_C0837_2004_08_08_07_15_00"), 
                property("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"),
                object("97", XSDDatatype.XSDfloat));
        Statement stmt2 = new StatementImpl(
                resource("http://knoesis.wright.edu/ssw/MeasureData_AirTemperature_C1111_2004_08_08_07_15_00"), 
                property("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"),
                object("97", XSDDatatype.XSDfloat));
        
        assertEquals(-1, new StatementComparator().compare(stmt1, stmt2));
        
        stmt1 = new StatementImpl(
                resource("http://knoesis.wright.edu/ssw/MeasureData_AirTemperature_C0837_2004_08_08_07_15_00"), 
                property("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"),
                object("97", XSDDatatype.XSDfloat));
        stmt2 = new StatementImpl(
                resource("http://knoesis.wright.edu/ssw/MeasureData_AirTemperature_C0837_2004_08_08_07_15_00"), 
                property("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"),
                object("97", XSDDatatype.XSDfloat));
        
        assertEquals(0, new StatementComparator().compare(stmt1, stmt2));
        
        stmt1 = new StatementImpl(
                resource("http://knoesis.wright.edu/ssw/MeasureData_AirTemperature_C1111_2004_08_08_07_15_00"), 
                property("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"),
                object("97", XSDDatatype.XSDfloat));
        stmt2 = new StatementImpl(
                resource("http://knoesis.wright.edu/ssw/MeasureData_AirTemperature_C0837_2004_08_08_07_15_00"), 
                property("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"),
                object("97", XSDDatatype.XSDfloat));
        
        assertEquals(1, new StatementComparator().compare(stmt1, stmt2));
        
        stmt1 = new StatementImpl(
                resource("http://knoesis.wright.edu/ssw/MeasureData_AirTemperature_C0837_2004_08_08_07_15_00"), 
                property("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"),
                object("90", XSDDatatype.XSDfloat));
        stmt2 = new StatementImpl(
                resource("http://knoesis.wright.edu/ssw/MeasureData_AirTemperature_C0837_2004_08_08_07_15_00"), 
                property("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"),
                object("97", XSDDatatype.XSDfloat));
        
        assertTrue(new StatementComparator().compare(stmt1, stmt2) < 0);
        
        stmt1 = new StatementImpl(
                resource("http://knoesis.wright.edu/ssw/MeasureData_AirTemperature_C0837_2004_08_08_07_15_00"), 
                property("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"),
                object("97", XSDDatatype.XSDfloat));
        stmt2 = new StatementImpl(
                resource("http://knoesis.wright.edu/ssw/MeasureData_AirTemperature_C0837_2004_08_08_07_15_00"), 
                property("http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#floatValue"),
                object("90", XSDDatatype.XSDfloat));
        
        assertTrue(new StatementComparator().compare(stmt1, stmt2) > 0);
    }
    
    private Resource resource(String uri) {
        return ResourceFactory.createResource(uri);
    }
    
    private Property property(String uri) {
        return ResourceFactory.createProperty(uri);
    }
    
    private RDFNode object(String value, RDFDatatype type) {
        return ResourceFactory.createTypedLiteral(value, type);
    }
}
