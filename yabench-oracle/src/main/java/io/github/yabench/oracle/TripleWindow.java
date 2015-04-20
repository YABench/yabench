package io.github.yabench.oracle;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import io.github.yabench.commons.TemporalTriple;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class TripleWindow extends Window {

    private final List<TemporalTriple> triples;
    private TreeSet<Long> timestamps = new TreeSet<Long>();
    //private final long[] timestamps;

    public TripleWindow(final Window window,
            final List<TemporalTriple> triples) {
        super(window.getStart(), window.getEnd());
        this.triples = triples;
        
        for (TemporalTriple temporalTriple : this.triples) {
        	this.timestamps.add(temporalTriple.getTime());
        }
 

    }

    public TripleWindow(final List<TemporalTriple> triples,
            final long start, final long end) {
        super(start, end);
        this.triples = triples;
        
        for (TemporalTriple temporalTriple : this.triples) {
        	this.timestamps.add(temporalTriple.getTime());
        }
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

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(super.toString())
                .append(" Triples:\n");
        
        triples.stream().forEachOrdered((t) -> {
            builder.append(t).append("\n");
        });
        
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return triples.hashCode() + super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof TripleWindow) {
            TripleWindow that = (TripleWindow) obj;
            if (that.getTriples() == null) {
                return this.triples == null;
            }
            if (!this.triples.equals(that.getTriples())) {
                return false;
            }
            return super.equals(that);
        }
        return false;
    }

	/**
	 * @return the timestamps
	 */
	public TreeSet<Long> getTimestamps() {
		return timestamps;
	}
	
	public TreeSet<Long> getTimestampsExceptFirst() {
		TreeSet<Long> tempTimestamps = this.timestamps;
		tempTimestamps.pollFirst();
		return tempTimestamps;

	}

}
