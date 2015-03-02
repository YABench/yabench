package io.github.yabench.oracle;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import io.github.yabench.commons.NodeUtils;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class ResultsReader implements AutoCloseable, Closeable {

    private static final String TAB = "\t";
    private final BufferedReader reader;
    private long initialTimestamp;
    private String[] variables;
    private long currentTimestamp;

    public ResultsReader(final Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    public void initialize() throws IOException {
        initialTimestamp = Long.parseLong(reader.readLine());
        variables = reader.readLine().split(TAB);
        currentTimestamp = Long.parseLong(reader.readLine());
    }

    public BindingWindow nextWindow() throws IOException {
        final long timestamp = currentTimestamp - initialTimestamp;
        String line = reader.readLine();
        if (line != null) {
            final List<Binding> content = new ArrayList<>();
            do {
                if(line.contains(TAB)) {
                    content.add(NodeUtils.toBinding(variables, line, TAB));
                } else {
                    currentTimestamp = Long.parseLong(line);
                    break;
                }
            } while ((line = reader.readLine()) != null);
            return new BindingWindow(content, timestamp);
        } else {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

}
