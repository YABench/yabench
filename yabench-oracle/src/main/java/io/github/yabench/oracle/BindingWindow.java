package io.github.yabench.oracle;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import java.util.List;
import java.util.Objects;

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

    @Override
    public String toString() {
        return new StringBuilder(super.toString())
                .append(" Triples:\n").append(bindings).toString();
    }

    @Override
    public int hashCode() {
        return super.hashCode() + bindings.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BindingWindow other = (BindingWindow) obj;
        return super.equals(other)
                && Objects.equals(this.bindings, other.bindings);
    }

}
