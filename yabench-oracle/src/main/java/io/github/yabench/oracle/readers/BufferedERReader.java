package io.github.yabench.oracle.readers;

import io.github.yabench.oracle.BindingWindow;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BufferedERReader {
    
    private final EngineResultsReader reader;
    private final List<BindingWindow> buffer = new ArrayList<>();

    public BufferedERReader(EngineResultsReader reader) {
        this.reader = reader;
    }

    public BindingWindow next() throws IOException {
        final BindingWindow next = reader.next();

        buffer.add(next);

        return next;
    }
    
    public int nextIndex() {
        return buffer.size();
    }
    
    public BindingWindow getOrNext(int index) throws IOException {
        if(buffer.size() <= index) {
            return next();
        } else {
            return buffer.get(index);
        }
    }
    
}
