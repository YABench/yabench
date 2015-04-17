package io.github.yabench.oracle.readers;

import io.github.yabench.commons.TemporalTriple;
import io.github.yabench.oracle.TripleWindow;
import io.github.yabench.oracle.Window;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class BufferedTWReader extends TripleWindowReader {

    private Set<TemporalTriple> buffer = new TreeSet<>();

    public BufferedTWReader(Reader reader) {
        super(reader);
    }

    @Override
    public TripleWindow readNextWindow(final Window window)
            throws IOException {
        final TripleWindow tw = super.readNextWindow(window);

        if (tw != null) {
            buffer.addAll(tw.getTriples());
        }

        return tw;
    }

    public TripleWindow readFromBuffer(final Window window) {
        final List<TemporalTriple> triples = Arrays.asList(buffer.stream()
                .filter((triple) -> triple.getTime() >= (window.getStart()))
                .toArray(TemporalTriple[]::new));

        return new TripleWindow(window, triples);
    }

    /**
     * Purge the buffer till the given time.
     *
     * @param floor duration in milliseconds (ms)
     */
    public void purge(long floor) {
        buffer = new TreeSet<>(
                Arrays.asList(buffer.stream()
                        .filter((triple) -> triple.getTime() >= floor)
                        .toArray(TemporalTriple[]::new)));
    }

}
