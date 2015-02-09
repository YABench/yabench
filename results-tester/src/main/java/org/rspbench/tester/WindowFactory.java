package org.rspbench.tester;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import java.io.IOException;
import java.nio.file.Path;

public class WindowFactory {

    private final RDFStreamReader reader;
    private final long windowSize;
    private final long windowSlide;
    private long numberOfSlides = 0;
    private Model window = ModelFactory.createDefaultModel();

    public WindowFactory(Path input, long windowSize, long windowSlide)
            throws IOException {
        this.reader = new RDFStreamReader(input);
        this.windowSize = windowSize;
        this.windowSlide = windowSlide;
    }
    
    public boolean hasNextWindow() {
        return numberOfSlides > 0 && window.size() > 0;
    }

    public Model nextWindow() throws IOException {
        final Model result = ModelFactory.createDefaultModel();
        long[] bndrs = windowBoundaries();
        while (reader.hasNext()) {
            final long time = reader.nextTime(); //ms
            final Statement stmt = reader.nextStatement();
            if(time <= bndrs[1]) {
                window.add(stmt);
            } else {
                result.add(window);
                
                //Save the last statement and time for the next call
                window = ModelFactory.createDefaultModel();
                window.add(stmt);
                
                return result;
            }
        }
        return result;
    }

    private long[] windowBoundaries() {
        long[] boundaries = new long[2];
        boundaries[0] = numberOfSlides++ * windowSlide;
        boundaries[1] = boundaries[0] + windowSize;
        return boundaries;
    }
}
