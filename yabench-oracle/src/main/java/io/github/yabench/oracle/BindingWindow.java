package io.github.yabench.oracle;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import java.util.List;

public class BindingWindow extends Window {

    private final List<Binding> bindings;

    public BindingWindow(final List<Binding> bindings, final long start, 
            final long end) {
        super(start, end);
        this.bindings = bindings;
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
    
}
