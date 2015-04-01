package io.github.yabench.engines.commons;

import com.hp.hpl.jena.rdf.model.Statement;
import java.io.Closeable;
import java.text.ParseException;

public interface Engine extends Closeable {
    
    public void initialize();
    
    public void registerQuery(final Query query, final ResultListener listener) 
            throws ParseException;
    
    public void stream(final Statement stmt);
    
}
