package io.github.yabench.oracle;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import java.util.List;

public class BindingWindow {

    private final List<Binding> bindings;
    private final long end;
    private final long start;

    public BindingWindow(final List<Binding> bindings, final long start, 
            final long end) {
        this.bindings = bindings;
        this.end = end;
        this.start = start;
    }
    
    public BindingWindow(final List<Binding> bindings, final long end) {
        this(bindings, -1, end);
    }
    
    public boolean contains(Binding binding) {
        return bindings.contains(binding);
    }
    
    public List<Binding> getBindings() {
        return bindings;
    }
    
    public long getStart() {
        return start;
    }
    
    public long getEnd() {
        return end;
    }
    
}
