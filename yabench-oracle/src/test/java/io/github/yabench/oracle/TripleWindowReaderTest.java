package io.github.yabench.oracle;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import io.github.yabench.commons.TemporalRDFReader;
import io.github.yabench.commons.TemporalTriple;
import io.github.yabench.oracle.readers.TripleWindowReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import static org.junit.Assert.*;

public class TripleWindowReaderTest {

    private static final String PREFIX = "/io/github/yabench/oracle/tests/WindowFactoryTest/";

    @Test
    public void testEquals() {
        TripleWindow one = new TripleWindow(Collections.EMPTY_LIST, 0, 30000);
        TripleWindow two = new TripleWindow(Collections.EMPTY_LIST, 0, 30000);
        assertEquals(one, two);

        List<TemporalTriple> triples = new ArrayList<TemporalTriple>() {
            {
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
            }
        };

        one = new TripleWindow(triples, 0, 30000);
        two = new TripleWindow(triples, 0, 30000);
        assertEquals(one, two);
    }

    @Test
    public void testWindowFactoryOnWindowClose() throws IOException {
        final String testPrefix = "testOnWindowClose/";
        final Reader reader = new StringReader(
                IOUtils.toString(this.getClass().getResourceAsStream(
                                PREFIX + testPrefix + "input.stream")));
        final Duration windowSize = Duration.of(60000, ChronoUnit.MILLIS);
        final Duration windowSlide = Duration.of(30000, ChronoUnit.MILLIS);
        final long delay = 0;

        final WindowFactory windowFactory = new WindowFactory(windowSize, windowSlide);
        final TripleWindowReader tripleWindowFactory = new TripleWindowReader(reader);

        //#1
        Window window = windowFactory.nextWindow();
        TripleWindow actual = tripleWindowFactory.readNextWindow(
                window.withShiftToRight(delay));
        assertNotNull(actual);
        TripleWindow expected = load(testPrefix + "1.window", 0, 30000);
        assertEquals(expected, actual);

        //#2
        window = windowFactory.nextWindow();
        actual = tripleWindowFactory.readNextWindow(window.withShiftToRight(delay));
        assertNotNull(actual);
        expected = load(testPrefix + "2.window", 0, 60000);
        assertEquals(expected, actual);

        //#3
        window = windowFactory.nextWindow();
        actual = tripleWindowFactory.readNextWindow(window.withShiftToRight(delay));
        assertNotNull(actual);
        expected = load(testPrefix + "3.window", 30000, 90000);
        assertEquals(expected, actual);

        //#4
        window = windowFactory.nextWindow();
        actual = tripleWindowFactory.readNextWindow(window.withShiftToRight(delay));
        assertNotNull(actual);
        expected = load(testPrefix + "4.window", 60000, 120000);
        assertEquals(expected, actual);

        //#5
        window = windowFactory.nextWindow();
        actual = tripleWindowFactory.readNextWindow(window.withShiftToRight(delay));
        assertNotNull(actual);
        expected = load(testPrefix + "5.window", 90000, 150000);
        assertEquals(expected, actual);

        //#6 This window already doesn't contains new triple from 
        //the input stream, but a subset of the previous one.
        window = windowFactory.nextWindow();
        actual = tripleWindowFactory.readNextWindow(window.withShiftToRight(delay));
        assertNotNull(actual);
        expected = load(testPrefix + "6.window", 120000, 180000);
        assertEquals(expected, actual);

        window = windowFactory.nextWindow();
        actual = tripleWindowFactory.readNextWindow(window.withShiftToRight(delay));
        assertNull(actual);
    }

    @Test
    public void testWindowFactoryOnContentChange() throws IOException {
        final String testPrefix = "testOnContentChange/";
        final Reader reader = new StringReader(
                IOUtils.toString(this.getClass().getResourceAsStream(
                                PREFIX + testPrefix + "input.stream")));
        final Duration windowSize = Duration.of(60000, ChronoUnit.MILLIS);
        final Duration windowSlide = Duration.of(30000, ChronoUnit.MILLIS);
        final long delay = 0;

        final WindowFactory windowFactory = new WindowFactory(windowSize, windowSlide);
        final TripleWindowReader tripleWindowFactory = new TripleWindowReader(reader);

        //#1
        long contentTimestamp = 0;
        Window window = windowFactory.nextWindow(contentTimestamp);
        TripleWindow actual = tripleWindowFactory.readNextWindow(
                window.withShiftToRight(delay));
        assertNotNull(actual);
        TripleWindow expected = load(testPrefix + "1.window", 0, contentTimestamp);
        assertEquals(expected, actual);

        //#2
        contentTimestamp = 32434;
        window = windowFactory.nextWindow(contentTimestamp);
        actual = tripleWindowFactory.readNextWindow(
                window.withShiftToRight(delay));
        assertNotNull(actual);
        expected = load(testPrefix + "2.window", 0, contentTimestamp);
        assertEquals(expected, actual);

        //#3
        contentTimestamp = 34125;
        window = windowFactory.nextWindow(contentTimestamp);
        actual = tripleWindowFactory.readNextWindow(
                window.withShiftToRight(delay));
        assertNotNull(actual);
        expected = load(testPrefix + "3.window", 0, contentTimestamp);
        assertEquals(expected, actual);

        //#4
        contentTimestamp = 37047;
        window = windowFactory.nextWindow(contentTimestamp);
        actual = tripleWindowFactory.readNextWindow(
                window.withShiftToRight(delay));
        assertNotNull(actual);
        expected = load(testPrefix + "4.window", 0, contentTimestamp);
        assertEquals(expected, actual);

        //#5
        contentTimestamp = 42365;
        window = windowFactory.nextWindow(contentTimestamp);
        actual = tripleWindowFactory.readNextWindow(
                window.withShiftToRight(delay));
        assertNotNull(actual);
        expected = load(testPrefix + "5.window", 0, contentTimestamp);
        assertEquals(expected, actual);

        //#6
        contentTimestamp = 62434;
        window = windowFactory.nextWindow(contentTimestamp);
        actual = tripleWindowFactory.readNextWindow(
                window.withShiftToRight(delay));
        assertNotNull(actual);
        expected = load(testPrefix + "6.window", 30000, contentTimestamp);
        assertEquals(expected, actual);

        //#7
        contentTimestamp = 94125;
        window = windowFactory.nextWindow(contentTimestamp);
        actual = tripleWindowFactory.readNextWindow(
                window.withShiftToRight(delay));
        assertNotNull(actual);
        expected = load(testPrefix + "7.window", 60000, contentTimestamp);
        assertEquals(expected, actual);

        contentTimestamp = 160000; //Random number
        window = windowFactory.nextWindow(contentTimestamp);
        actual = tripleWindowFactory.readNextWindow(
                window.withShiftToRight(delay));
        assertNull(actual);
    }

    @Test
    public void testNextNewTimestamp() throws IOException {
        final String testPrefix = "testNextNewTimestamp/";
        final Reader reader = new StringReader(
                IOUtils.toString(this.getClass().getResourceAsStream(
                                PREFIX + testPrefix + "input.stream")));
        
        TripleWindowReader twReader = new TripleWindowReader(reader);
        
        assertEquals(24091, twReader.readTimestampOfNextTriple());
        assertEquals(24091, twReader.readTimestampOfNextTriple());
        
        twReader.readNextWindow(new Window(0, 24091));
        
        assertEquals(54091, twReader.readTimestampOfNextTriple());
        
        twReader.readNextWindow(new Window(0, 114091));
        
        assertEquals(144091, twReader.readTimestampOfNextTriple());
        
        twReader.readNextWindow(new Window(120000, 264091));
        
        assertEquals(294091, twReader.readTimestampOfNextTriple());
        
        twReader.readNextWindow(new Window(180000, 294091));
        
        assertEquals(-1, twReader.readTimestampOfNextTriple());
    }

    private TripleWindow load(String fileName, long start, long end)
            throws IOException {
        TemporalRDFReader reader = new TemporalRDFReader(
                new StringReader(IOUtils.toString(
                                this.getClass().getResourceAsStream(PREFIX + fileName))));

        List<TemporalTriple> triples = new ArrayList<>();
        TemporalTriple triple;
        while ((triple = reader.readNextTriple()) != null) {
            triples.add(triple);
        }

        return new TripleWindow(triples, start, end);
    }

}
