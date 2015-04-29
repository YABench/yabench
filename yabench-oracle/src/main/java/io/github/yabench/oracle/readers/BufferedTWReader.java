package io.github.yabench.oracle.readers;

import io.github.yabench.commons.TemporalRDFReader;
import io.github.yabench.commons.TemporalTriple;
import io.github.yabench.oracle.TripleWindow;
import io.github.yabench.oracle.Window;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

public class BufferedTWReader extends TripleWindowReader {

    private TreeSet<TemporalTriple> buffer = new TreeSet<>();

    public BufferedTWReader(Reader reader) {
        super(new TemporalRDFReader(reader));
    }

    public BufferedTWReader(TemporalRDFReader reader) {
        super(reader);
    }

    public BufferedTWReader(TripleWindowReader reader) {
        super(reader.getReader());
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

    public TripleWindow readWindowWithoutFirstGraph(Window window) {
        long afterFirst = buffer.higher(
                new TemporalTriple(null, window.getStart() + 1)).getTime();
        
        List<TemporalTriple> triples = Arrays.asList(buffer.stream()
                .filter((t) -> {
                    return isBetween(t.getTime(), afterFirst, window.getEnd());
                })
                .toArray(TemporalTriple[]::new));
        return new TripleWindow(triples, afterFirst, window.getEnd());
    }

    public TripleWindow prevWindow(final Window winwow) {
        return prevWindow(winwow, false);
    }

    public TripleWindow prevWindow(final Window window, boolean inclusive) {
        final TemporalTriple tripleStart = buffer.lower(
                new TemporalTriple(null, window.getStart()));
        final long end = inclusive ? window.getEnd() : buffer.lower(
                new TemporalTriple(null, window.getEnd())).getTime();

        if (tripleStart != null) {
            List<TemporalTriple> triples = Arrays.asList(
                    buffer.stream()
                    .filter((TemporalTriple t) -> {
                        return isBetween(t.getTime(),
                                tripleStart.getTime(), end);
                    })
                    .toArray(TemporalTriple[]::new));

            return new TripleWindow(triples, tripleStart.getTime(), window.getEnd());
        } else {
            return null;
        }
    }

    private boolean isBetween(long it, long start, long end) {
        return it >= start && it <= end;
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
