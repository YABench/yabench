package io.github.yabench.oracle;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import io.github.yabench.commons.NodeUtils;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ResultsReader implements AutoCloseable, Closeable {

    private static final String TAB = "\t";
    private final BufferedReader reader;
    private long initialTimestamp;
    private String[] variables;
    private long currentTimestamp;
    private long windowSize;

    public ResultsReader(final Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    public void initialize(final Duration windowSize) throws IOException {
        this.windowSize = windowSize.toMillis();
        initialTimestamp = Long.parseLong(reader.readLine());
        variables = reader.readLine().split(TAB);
        currentTimestamp = Long.parseLong(reader.readLine());
    }

    public BindingWindow nextWindow() throws IOException {
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
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

}
