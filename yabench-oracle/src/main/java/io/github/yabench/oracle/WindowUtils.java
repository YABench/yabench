package io.github.yabench.oracle;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import java.util.ArrayList;
import java.util.List;

public final class WindowUtils {
    
    private static final int FIRST = 0;
    
    public static BindingWindow join(final List<BindingWindow> windows) {
        if(windows == null || windows.isEmpty()) {
            throw new IllegalArgumentException("Can't join a empty list of BindingWindows!");
        }
        final long start = windows.get(FIRST).getStart();
        final long end = windows.get(FIRST).getEnd();
        final List<Binding> bindings = new ArrayList<>();
        
        for(BindingWindow window : windows) {
            bindings.addAll(window.getBindings());
        }
        
        return new BindingWindow(bindings, start, end);
    }
    
}
