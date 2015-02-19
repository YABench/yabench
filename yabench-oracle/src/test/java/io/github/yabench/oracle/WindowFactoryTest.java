package io.github.yabench.oracle;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import io.github.yabench.commons.RDFStreamReader;
import io.github.yabench.commons.TemporalTriple;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import static org.junit.Assert.*;

public class WindowFactoryTest {
    
    private static final String PREFIX = "/io/github/yabench/oracle/tests/WindowFactoryTest/";
    
    @Test
    public void testEquals() {
        TripleWindow one = new TripleWindow(null, 0, 30000);
        TripleWindow two = new TripleWindow(null, 0, 30000);
        assertEquals(one, two);
        
        List<TemporalTriple> triples = new ArrayList<TemporalTriple>(){{
            add(new TemporalTriple(
                    ResourceFactory.createStatement(
                            ResourceFactory.createResource(), 
                            ResourceFactory.createProperty("http://ex.com#a"), 
                            ResourceFactory.createTypedLiteral("35.0", XSDDatatype.XSDfloat)), 
                    0));
            add(new TemporalTriple(
                    ResourceFactory.createStatement(
                            ResourceFactory.createResource(), 
                            ResourceFactory.createProperty("http://ex.com#a"), 
                            ResourceFactory.createTypedLiteral("36.0", XSDDatatype.XSDfloat)), 
                    20000));
        }};
        
        one = new TripleWindow(triples, 0, 30000);
        two = new TripleWindow(triples, 0, 30000);
        assertEquals(one, two);
    }
    
    @Test
    public void test() throws IOException {
        final Reader reader = new StringReader(
                IOUtils.toString(this.getClass().getResourceAsStream(
                PREFIX + "input.stream")));
        final long windowSize = 60000;
        final long windowSlide  = 30000;
        
        WindowFactory windowFactory = new WindowFactory(reader, windowSize, windowSlide);
        
        //#1
        TripleWindow actual = windowFactory.nextWindow();
        assertNotNull(actual);
        TripleWindow expected = load("1.window", 0, 30000);
        assertEquals(expected, actual);
        
        //#2
        actual = windowFactory.nextWindow();
        assertNotNull(actual);
        expected = load("2.window", 0, 60000);
        assertEquals(expected, actual);
        
        //#3
        actual = windowFactory.nextWindow();
        assertNotNull(actual);
        expected = load("3.window", 30000, 90000);
        assertEquals(expected, actual);
        
        //#4
        actual = windowFactory.nextWindow();
        assertNotNull(actual);
        expected = load("4.window", 60000, 120000);
        assertEquals(expected, actual);
        
        //#5
        actual = windowFactory.nextWindow();
        assertNotNull(actual);
        expected = load("5.window", 90000, 150000);
        assertEquals(expected, actual);
        
        actual = windowFactory.nextWindow();
        assertNull(actual);
    }
    
    private TripleWindow load(String fileName, long start, long end) 
            throws IOException {
        RDFStreamReader reader = new RDFStreamReader(
                new StringReader(IOUtils.toString(
                        this.getClass().getResourceAsStream(PREFIX + fileName))));
        
        List<TemporalTriple> triples = new ArrayList<>();
        TemporalTriple triple;
        while((triple = reader.readNext()) != null) {
            triples.add(triple);
        }
        
        return new TripleWindow(triples, start, end);
    }
    
}
