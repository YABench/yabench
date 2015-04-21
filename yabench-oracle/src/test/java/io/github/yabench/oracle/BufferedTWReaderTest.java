package io.github.yabench.oracle;

import io.github.yabench.oracle.readers.BufferedTWReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import static org.junit.Assert.*;

public class BufferedTWReaderTest {
    
    private static final String PREFIX = "/io/github/yabench/oracle/tests/BufferedTWReaderTest/";
    
    @Test
    public void testPrevWindow() throws IOException {
        final String testPrefix = "testPrevWindow/";
        final Reader reader = new StringReader(
                IOUtils.toString(this.getClass().getResourceAsStream(
                                PREFIX + testPrefix + "input.stream")));
        BufferedTWReader btwreader = new BufferedTWReader(reader);
        
        Window initial = new Window(114091, 204091);
        btwreader.readNextWindow(initial);
        
        TripleWindow next = btwreader.prevWindow(initial);
        assertEquals(84091, next.getStart());
        assertEquals(204091, next.getEnd());
        
        next = btwreader.prevWindow(next);
        
        assertEquals(54091, next.getStart());
        assertEquals(204091, next.getEnd());
        
        next = btwreader.prevWindow(next);
        
        assertEquals(24091, next.getStart());
        assertEquals(204091, next.getEnd());
        
        next = btwreader.prevWindow(next);
        
        assertNull(next);
    }
    
}
