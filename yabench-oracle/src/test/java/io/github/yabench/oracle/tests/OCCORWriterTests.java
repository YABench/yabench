package io.github.yabench.oracle.tests;

import io.github.yabench.oracle.BindingWindow;
import io.github.yabench.oracle.TripleWindow;
import io.github.yabench.oracle.tests.comparators.OnContentChangeComparator.OCCORWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class OCCORWriterTests {

    @Test
    public void test() throws IOException {
        Checker checker = new Checker();
        OCCORWriter writer = new OCCORWriter(checker, 10000, 10000);

        writer.writeFound(emptyTW(), newBW(10000, 12000), newBW(10002, 11003));
        assertEquals(1, checker.last.size());
        assertEquals("1.0,1.0,0,0,0,0,10000,-1", checker.last.get(0));
        writer.writeFound(emptyTW(), newBW(10000, 14000), newBW(10002, 12003));
        assertEquals(1, checker.last.size());
        writer.writeFound(emptyTW(), newBW(10000, 15000), newBW(10002, 15001));
        assertEquals(1, checker.last.size());
        writer.writeFound(emptyTW(), newBW(10000, 17000), newBW(10002, 17001));
        assertEquals(1, checker.last.size());
        writer.writeFound(emptyTW(), newBW(10000, 18000), newBW(10002, 18001));
        assertEquals(1, checker.last.size());
        writer.writeFound(emptyTW(), newBW(10000, 20000), newBW(10002, 20001));
        assertEquals(1, checker.last.size());
        writer.writeFound(emptyTW(), newBW(20000, 21000), newBW(20005, 21010));
        assertEquals(2, checker.last.size());
        assertEquals("1.0,1.0,0,0,0,10000,20000,-1", checker.last.get(1));
        writer.writeFound(emptyTW(), newBW(20000, 23000), newBW(20005, 23010));
        assertEquals(2, checker.last.size());
        writer.writeFound(emptyTW(), newBW(20000, 24000), newBW(20005, 24010));
        assertEquals(2, checker.last.size());
        writer.writeFound(emptyTW(), newBW(20000, 26000), newBW(20005, 26010));
        assertEquals(2, checker.last.size());
        writer.writeFound(emptyTW(), newBW(20000, 27000), newBW(20005, 27010));
        assertEquals(2, checker.last.size());
        writer.writeFound(emptyTW(), newBW(20000, 29000), newBW(20005, 29010));
        assertEquals(2, checker.last.size());

        writer.flush();
        assertEquals(3, checker.last.size());
        assertEquals("1.0,1.0,0,0,0,20000,29000,-1", checker.last.get(2));
    }

    private BindingWindow newBW(long start, long end) {
        return new BindingWindow(Collections.EMPTY_LIST, start, end);
    }
    
    private TripleWindow emptyTW() {
        return new TripleWindow(Collections.EMPTY_LIST, 0, 0);
    }

    private class Checker extends Writer {

        public List<String> last = new ArrayList<>();

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            last.add(new String(cbuf, off, len - 1));
        }

        @Override
        public void flush() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void close() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }
}
