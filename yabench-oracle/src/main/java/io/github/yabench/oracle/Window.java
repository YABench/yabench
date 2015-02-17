package io.github.yabench.oracle;

import com.hp.hpl.jena.rdf.model.Model;

public class Window {

    private final Model content;
    private final long start;
    private final long end;

    public Window(final Model model, final long start, final long end) {
        this.content = model;
        this.start = start;
        this.end = end;
    }

    public Model getContent() {
        return content;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
    
}
