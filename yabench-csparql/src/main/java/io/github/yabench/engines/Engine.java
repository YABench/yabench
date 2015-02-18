package io.github.yabench.engines;

import com.hp.hpl.jena.rdf.model.Statement;
import java.io.Closeable;
import java.text.ParseException;

public interface Engine extends Closeable {
    
    public void initialize();
    
    public void registerResultListener(ResultListener listener);
    
    public void registerQuery(String query) throws ParseException;
    
    public void stream(Statement stmt);
    
}
