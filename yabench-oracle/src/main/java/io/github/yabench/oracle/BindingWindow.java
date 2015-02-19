package io.github.yabench.oracle;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import java.util.List;

public class BindingWindow {

    private final List<Binding> bindings;

    public BindingWindow(List<Binding> bindings) {
        this.bindings = bindings;
    }
    
    public boolean contains(Binding binding) {
        return bindings.contains(binding);
    }
    
    public List<Binding> getBindings() {
        return bindings;
    }
    
}
