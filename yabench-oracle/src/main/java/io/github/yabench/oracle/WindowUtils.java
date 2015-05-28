package io.github.yabench.oracle;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import java.util.ArrayList;
import java.util.List;

public final class WindowUtils {

    private static final int FIRST = 0;

    public static BindingWindow mergeBindings(final List<BindingWindow> windows) {
        if (windows == null || windows.isEmpty()) {
            throw new IllegalArgumentException("Can't join a empty list of BindingWindows!");
        }
        final long start = windows.get(FIRST).getStart();
        final long end = windows.get(FIRST).getEnd();
        final List<Binding> bindings = new ArrayList<>();

        for (BindingWindow window : windows) {
            bindings.addAll(window.getBindings());
        }

        return new BindingWindow(bindings, start, end);
    }
    
    public static BindingWindow merge(final List<BindingWindow> windows) {
        if (windows == null || windows.isEmpty()) {
            throw new IllegalArgumentException("Can't join a empty list of BindingWindows!");
        }
        
        final List<Binding> bindings = new ArrayList<>();
        long start = windows.get(FIRST).getStart();
        long end = windows.get(FIRST).getEnd();
        
        for (BindingWindow window : windows) {
            if(window.getStart() < start) {
                start = window.getStart();
            }
            if(window.getEnd() > end) {
                end = window.getEnd();
            }
            bindings.addAll(bindings);
        }
        
        return new BindingWindow(bindings, start, end);
    }
    
    public static BindingWindow diff(BindingWindow one, BindingWindow two) {
        
        if (two == null) {
            return one;
        }
        final List<Binding> bindings = new ArrayList<>();
        if (one.equals(two)) {
            return new BindingWindow(bindings, one.getStart(), one.getEnd());
        } else {
            bindings.addAll(one.getBindings());
            bindings.removeAll(two.getBindings());
        }
        return new BindingWindow(bindings, one.getStart(), one.getEnd());
    }

    public static boolean match(final BindingWindow one,
            final List<BindingWindow> matches) {
        return matches.stream().anyMatch((bw) -> (one.equalsByContent(bw)));
    }

    public static BindingWindow findMatch(final BindingWindow one,
            final List<BindingWindow> matches) {
        for (BindingWindow window : matches) {
            if (one.equalsByContent(window)) {
                return window;
            }
        }
        return null;
    }

}
