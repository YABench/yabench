package io.github.yabench.oracle;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import java.util.List;

public class BindingWindow {

    private final List<Binding> bindings;
    private final long end;

    public BindingWindow(final List<Binding> bindings, final long end) {
        this.bindings = bindings;
        this.end = end;
    }
    
    public boolean contains(Binding binding) {
        return bindings.contains(binding);
    }
    
    public List<Binding> getBindings() {
        return bindings;
    }
    
    public long getEnd() {
        return end;
    }
    
}
