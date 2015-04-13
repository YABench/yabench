package io.github.yabench.oracle;

import io.github.yabench.commons.RDFStreamReader;
import io.github.yabench.commons.TemporalGraph;
import io.github.yabench.commons.TemporalTriple;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputStreamReader implements Closeable {

    private final RDFStreamReader reader;
    private List<TemporalTriple> content = new ArrayList<>();

    public InputStreamReader(Reader reader) throws IOException {
        this.reader = new RDFStreamReader(reader);
    }

    /**
     * Creates a triple window based on the given window scope and delay.
     *
     * @param window
     * @param delay
     *
     * @return null if the end of the stream has been reached
     * @throws IOException
     */
    public TripleWindow nextTripleWindow(final Window window, final long delay)
            throws IOException {
        content = new ArrayList<>(Arrays.asList(content.stream()
                .filter((triple) -> triple.getTime() >= (window.getStart() + delay))
                .toArray(TemporalTriple[]::new)));

        TemporalTriple triple;
        while ((triple = reader.readNextTriple()) != null) {
            if (triple.getTime() <= (window.getEnd() + delay)) {
                content.add(triple);
            } else {
                break;
            }
        }

        final TripleWindow w = new TripleWindow(new ArrayList<>(content),
                window.getStart() + delay, window.getEnd() + delay);

        if (triple != null) {
            content.add(triple);
        } else if (triple == null && content.isEmpty()) {
            return null;
        }

        return w;
    }

    /**
     * Reads next triple from the reader and adds it in the next window.
     *
     * @return null if the end of the stream has been reached
     * @throws IOException
     */
    public TemporalTriple nextTriple() throws IOException {
        final TemporalTriple triple = reader.readNextTriple();
        if (triple != null) {
            content.add(triple);
        }
        return triple;
    }
    
    public TemporalGraph nextGraph() throws IOException {
        final TemporalGraph graph = reader.readNextGraph();
        if(graph != null) {
            content.addAll(graph.getTriples());
        }
        return graph;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
