package io.github.yabench.oracle;

import io.github.yabench.commons.RDFStreamReader;
import io.github.yabench.commons.TemporalTriple;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TripleWindowFactory {

    private final RDFStreamReader reader;
    private List<TemporalTriple> content = new ArrayList<>();

    public TripleWindowFactory(Reader reader) throws IOException {
        this.reader = new RDFStreamReader(reader);
    }

    /**
     * @param window
     * @param delay
     * 
     * @return null if the end of the stream has been reached
     * @throws IOException 
     */
    public TripleWindow nextTripleWindow(final Window window, final long delay) 
            throws IOException {
        content = new ArrayList<>(Arrays.asList(content.stream()
                .filter((triple)-> triple.getTime() >= window.getStart())
                .toArray(TemporalTriple[]::new)));
        
        TemporalTriple triple;
        while ((triple = reader.readNext()) != null) {
            if (triple.getTime() <= window.getEnd()) {
                content.add(triple);
            } else {
                break;
            }
        }
        
        final TripleWindow w = new TripleWindow(
                window, new ArrayList<>(content));

        if(triple != null) {
            content.add(triple);
        } else if (triple == null && content.isEmpty()){
            return null;
        }

        return w;
    }
}
