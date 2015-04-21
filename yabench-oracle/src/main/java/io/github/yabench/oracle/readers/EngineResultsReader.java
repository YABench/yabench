package io.github.yabench.oracle.readers;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import io.github.yabench.commons.utils.NodeUtils;
import io.github.yabench.oracle.BindingWindow;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class EngineResultsReader implements AutoCloseable, Closeable {

    private static final String TAB = "\t";
    private static final int NOT_FOUND = -1;
    private final BufferedReader reader;
    private long initialTimestamp;
    private String[] variables;
    private long currentTimestamp = NOT_FOUND;
    private long windowSize;
    private boolean empty = false;

    public EngineResultsReader(final Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    public void initialize(final Duration windowSize) throws IOException {
        this.windowSize = windowSize.toMillis();
        initialTimestamp = Long.parseLong(reader.readLine());
        
        String tmp;
        if ((tmp = reader.readLine()) != null) {
            variables = tmp.split(TAB);
            currentTimestamp = Long.parseLong(reader.readLine());
        } else {
            empty = true;
        }
    }

    public BindingWindow next() throws IOException {
        if(empty) {
            return null;
        }
        
        final long windowEndTimestamp = currentTimestamp - initialTimestamp;
        final long windowStartTimestamp = windowEndTimestamp - windowSize > 0
                ? windowEndTimestamp - windowSize : 0;

        String line = reader.readLine();
        if (line != null) {
            final List<Binding> content = new ArrayList<>();
            do {
                if (line.contains(TAB)) {
                    content.add(NodeUtils.toBinding(variables, line, TAB));
                } else {
                    currentTimestamp = Long.parseLong(line);
                    break;
                }
            } while ((line = reader.readLine()) != null);
            return new BindingWindow(content, windowStartTimestamp, 
                    windowEndTimestamp);
        } else {
            currentTimestamp = NOT_FOUND;
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

}
