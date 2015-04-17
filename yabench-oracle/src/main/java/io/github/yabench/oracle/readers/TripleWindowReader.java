package io.github.yabench.oracle.readers;

import io.github.yabench.commons.TemporalRDFReader;
import io.github.yabench.commons.TemporalTriple;
import io.github.yabench.oracle.TripleWindow;
import io.github.yabench.oracle.Window;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TripleWindowReader extends TemporalRDFReader {
    
    private List<TemporalTriple> content = new ArrayList<>();
    
    public TripleWindowReader(final File stream) 
            throws IOException {
        super(stream);
    }

    public TripleWindowReader(Path stream) throws IOException {
        super(stream);
    }

    public TripleWindowReader(Reader reader) {
        super(reader);
    }
    
    public TripleWindow readNextWindow(final Window window) throws IOException {
        content = new ArrayList<>(Arrays.asList(content.stream()
                .filter((triple) -> triple.getTime() >= (window.getStart()))
                .toArray(TemporalTriple[]::new)));

        TemporalTriple triple;
        while ((triple = readNextTriple()) != null) {
            if (triple.getTime() <= (window.getEnd())) {
                content.add(triple);
            } else {
                break;
            }
        }

        final TripleWindow w = new TripleWindow(new ArrayList<>(content),
                window.getStart(), window.getEnd());

        if (triple != null) {
            content.add(triple);
        } else if (triple == null && content.isEmpty()) {
            return null;
        }

        return w;
    }
    
}
