package io.github.yabench.commons;

import com.hp.hpl.jena.rdf.model.Statement;

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
    
}
