package io.github.yabench.commons;

import com.hp.hpl.jena.rdf.model.Statement;
import java.util.Objects;

public class TemporalTriple {

    private final Statement stmt;
    private final long time;

    public TemporalTriple(Statement stmt, long time) {
        this.stmt = stmt;
        this.time = time;
    }

    public Statement getStatement() {
        return stmt;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return new StringBuilder(String.valueOf(time))
                .append(" : ").append(stmt.asTriple().toString()).toString();
    }

    @Override
    public int hashCode() {
        return stmt.hashCode() + ((Long)time).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TemporalTriple other = (TemporalTriple) obj;
        if (!Objects.equals(this.stmt, other.stmt)) {
            return false;
        }
        return this.time == other.time;
    }
    
}
