package io.github.yabench.oracle;

import java.time.Duration;
import org.junit.Test;
import static org.junit.Assert.*;

public class WindowFactoryTest {

    @Test
    public void testNextWindowWithTimestampe() {
        WindowFactory wf = new WindowFactory(
                Duration.ofMillis(5000), Duration.ofMillis(5000));
        
        Window w = wf.nextWindow(1000);
        assertEquals(0, w.getStart());
        assertEquals(1000, w.getEnd());
        
        w = wf.nextWindow(1500);
        assertEquals(0, w.getStart());
        assertEquals(1500, w.getEnd());
        
        w = wf.nextWindow(7000);
        assertEquals(5000, w.getStart());
        assertEquals(7000, w.getEnd());
        
        w = wf.nextWindow(10000);
        assertEquals(5000, w.getStart());
        assertEquals(10000, w.getEnd());
        
        w = wf.nextWindow(13000);
        assertEquals(10000, w.getStart());
        assertEquals(13000, w.getEnd());
    }

}
