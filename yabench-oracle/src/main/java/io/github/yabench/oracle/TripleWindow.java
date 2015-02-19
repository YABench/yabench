package io.github.yabench.oracle;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import io.github.yabench.commons.TemporalTriple;
import java.util.List;

public class TripleWindow {

    private final List<TemporalTriple> triples;
    private final long start;
    private final long end;

    public TripleWindow(final List<TemporalTriple> triples,
            final long start, final long end) {
        this.triples = triples;
        this.start = start;
        this.end = end;
    }

    public Model getModel() {
        Model model = ModelFactory.createDefaultModel();
        triples.stream().forEach((triple) -> {
            model.add(triple.getStatement());
        });
        return model;
    }

    public List<TemporalTriple> getTriples() {
        return triples;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    @Override
    public int hashCode() {
        return triples.hashCode()
                + ((Long) start).hashCode()
                + ((Long) end).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if (obj instanceof TripleWindow) {
            TripleWindow that = (TripleWindow) obj;
            if(that.getTriples() == null) {
                return this.triples == null;
            }
            if (!this.triples.equals(that.getTriples())) {
                return false;
            }
            return !(this.start != that.start || this.end != that.end);
        }
        return false;
    }

}
