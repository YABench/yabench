package io.github.yabench.engines.commons;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import java.util.List;

public interface ResultListener {
    
    public void update(final String[] vars, final List<Binding> binding);
    
}
