package io.github.yabench.oracle;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import io.github.yabench.commons.RDFStreamReader;
import io.github.yabench.commons.TemporalTriple;
import java.io.IOException;
import java.io.Reader;
import javax.annotation.Nullable;

public class WindowFactory {

    private final RDFStreamReader reader;
    private final long windowSize;
    private final long windowSlide;
    private long numberOfSlides = 0;
    private Model window = ModelFactory.createDefaultModel();

    public WindowFactory(Reader reader, long windowSize, long windowSlide)
            throws IOException {
        this.reader = new RDFStreamReader(reader);
        this.windowSize = windowSize;
        this.windowSlide = windowSlide;
    }

    /**
     * @return null if the end of the stream has been reached
     * @throws IOException 
     */
    public Window nextWindow() throws IOException {
        final long windowStart = numberOfSlides * windowSlide;
        final long windowEnd = numberOfSlides * windowSlide + windowSize;

        TemporalTriple triple;
        while ((triple = reader.readNext()) != null) {
            if (triple.getTime() <= windowEnd) {
                window.add(triple.getStatement());
            } else {
                break;
            }
        }
        
        final Window w = new Window(
                ModelFactory.createDefaultModel().add(window), 
                windowStart, 
                windowEnd);

        //creating a new window for the next call
        window = ModelFactory.createDefaultModel();
        if (triple != null) {
            window.add(triple.getStatement());
        }

        numberOfSlides++;

        return w.getContent().size() > 0 ? w : null;
    }
}
