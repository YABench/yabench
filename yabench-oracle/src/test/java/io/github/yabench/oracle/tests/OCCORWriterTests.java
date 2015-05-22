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
    public void testAllFound() throws IOException {
        Checker checker = new Checker();
        OCCORWriter writer = new OCCORWriter(checker, 10000, 10000);

        writer.writeFound(emptyTW(), newBW(10000, 12000), newBW(10002, 12003));
        assertEquals(1, checker.last.size());
        assertEquals("1.0,1.0,0,0,0,0,10000,0", checker.last.get(0));
        writer.writeFound(emptyTW(), newBW(10000, 14000), newBW(10002, 14003));
        assertEquals(1, checker.last.size());
        writer.writeFound(emptyTW(), newBW(10000, 15000), newBW(10002, 15001));
        assertEquals(1, checker.last.size());
        writer.writeFound(emptyTW(), newBW(10000, 17000), newBW(10002, 17002));
        assertEquals(1, checker.last.size());
        writer.writeFound(emptyTW(), newBW(10000, 18000), newBW(10002, 18003));
        assertEquals(1, checker.last.size());
        writer.writeFound(emptyTW(), newBW(10000, 20000), newBW(10002, 20006));
        assertEquals(1, checker.last.size());
        writer.writeFound(emptyTW(), newBW(20000, 21000), newBW(20005, 21010));
        assertEquals(2, checker.last.size());
        assertEquals("1.0,1.0,0,0,0,10000,20000,3", checker.last.get(1));
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
        assertEquals("1.0,1.0,0,0,0,20000,29000,10", checker.last.get(2));
    }

    @Test
    public void testMixedSingleResultTrue() throws IOException {
        Checker checker = new Checker();
        OCCORWriter writer = new OCCORWriter(checker, 5000, 5000);

        writer.writeFound(emptyTW(), newBW(0, 27), newBW(0, 91));
        writer.writeFound(emptyTW(), newBW(5000, 6058), newBW(5001, 6091));
        assertEquals(1, checker.last.size());
        assertEquals("1.0,1.0,0,0,0,0,27,64", checker.last.get(0));
        writer.writeFound(emptyTW(), newBW(5000, 8060), newBW(5001, 8091));
        writer.writeFound(emptyTW(), newBW(10000, 10058), newBW(10001, 10091));
        assertEquals(2, checker.last.size());
        assertEquals("1.0,1.0,0,0,0,5000,8060,32", checker.last.get(1));
        writer.writeFound(emptyTW(), newBW(10000, 14057), newBW(10001, 14091));

        writer.flush();
        assertEquals(3, checker.last.size());
        assertEquals("1.0,1.0,0,0,0,10000,14057,33", checker.last.get(2));
    }

    @Test
    public void testMixedSingleResultFalse() throws IOException {
        Checker checker = new Checker();
        OCCORWriter writer = new OCCORWriter(checker, 5000, 5000);

        writer.writeFound(emptyTW(), newBW(0, 3091), newBW(0, 3091 + 46));
        writer.writeMissing(emptyTW(), newBWList(5000, 9091, 1), newBW(5001, 9091 + 46));
        assertEquals(1, checker.last.size());
        assertEquals("1.0,1.0,0,0,0,0,3091,46", checker.last.get(0));
        writer.writeFound(emptyTW(), newBW(5000, 9091), newBW(5001, 9091 + 38));
        writer.writeMissing(emptyTW(), newBWList(15000, 18091, 1), newBW(15001, 18091 + 38));
        assertEquals(3, checker.last.size());
        assertEquals("0.5,0.5,0,0,0,5000,9091,38", checker.last.get(1));
        assertEquals("1.0,1.0,0,0,0,10000,15000,0", checker.last.get(2));
        writer.writeMissing(emptyTW(), newBWList(15000, 18091, 1), newBW(15001, 18091 + 38));
        writer.writeFound(emptyTW(), newBW(15000, 18091), newBW(15001, 18091 + 42));
        writer.writeMissing(emptyTW(), newBWList(20000, 24091, 1), newBW(20001, 24091 + 42));
        assertEquals(4, checker.last.size());
        assertEquals("0.3333333333333333,0.3333333333333333,0,0,0,15000,18091,42", checker.last.get(3));
        writer.writeFound(emptyTW(), newBW(20000, 24091), newBW(20001, 24091 + 43));
        writer.writeMissingExpected(newBW(22129, 27129));
        assertEquals(5, checker.last.size());
        assertEquals("0.5,0.5,0,0,0,20000,24091,43", checker.last.get(4));
        
        writer.flush();
        assertEquals(6, checker.last.size());
        assertEquals("0.0,1.0,0,-1,-1,25000,27129,0", checker.last.get(5));
    }

    private BindingWindow newBW(long start, long end) {
        return new BindingWindow(Collections.EMPTY_LIST, start, end);
    }

    private List<BindingWindow> newBWList(long start, long end, int bindings) {
        final List<BindingWindow> list = new ArrayList<>();

        for (int i = 0; i < bindings; i++) {
            list.add(newBW(start, end));
        }

        return list;
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
