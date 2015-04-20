package io.github.yabench.oracle;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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

    public BindingWindow(final Binding binding,
            final long start, final long end) {
        super(start, end);
        this.bindings = Arrays.asList(binding);
    }

    public boolean contains(Binding binding) {
        return bindings.contains(binding);
    }

    public List<Binding> getBindings() {
        return bindings;
    }

    public List<BindingWindow> splitByOneBinding() {
        final List<BindingWindow> newBindings = new ArrayList<>(bindings.size());
        bindings.stream().forEach((b) -> {
            newBindings.add(new BindingWindow(b, getStart(), getEnd()));
        });
        return newBindings;
    }

    public BindingWindow remove(BindingWindow o) {
        if (o == null) {
            return this;
        } else if (this.equals(o)) {
            return null;
        } else {
            o.getBindings().stream().forEach((b) -> {
                this.bindings.remove(b);
            });
        }
        return this;
    }

    public boolean isEmpty() {
        if (this.bindings.isEmpty()) {
            return true;
        } else if (this.bindings.size() == 1) {
            final Binding binding = this.bindings.get(0);
            if (binding.isEmpty()) {
                return true;
            } else {
                final Iterator<Var> vars = binding.vars();
                while (vars.hasNext()) {
                    final Var next = vars.next();
                    if (binding.get(next) != null) {
                        return false;
                    }
                }
                return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(super.toString())
                .append(". Bindings:\n");
        builder.append("[\n");
        bindings.stream().forEachOrdered((b) -> {
            builder.append(b).append("\n");
        });
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return super.hashCode() + bindings.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
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

    public boolean equalsByContent(BindingWindow other) {
        return this.bindings.equals(other.getBindings());
    }

    public BindingWindow equals(final List<BindingWindow> bindingWindows) {
        for (BindingWindow bw : bindingWindows) {
            if (this.equalsByContent(bw)) {
                return bw;
            }
        }
        return null;
    }

}
