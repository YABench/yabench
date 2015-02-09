package org.rspbench.tester;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RDFStreamReader {

    private static final int TUPLE_SIZE = 4;
    private final BufferedReader reader;
    private String line;
    private Statement stmt;
    private long time;

    public RDFStreamReader(Path stream) throws IOException {
        this.reader = Files.newBufferedReader(stream);
    }

    public boolean hasNext() throws IOException {
        line = reader.readLine();
        if (line != null) {
            String[] tuple = line.split(" ", TUPLE_SIZE);
            Resource subject = ResourceFactory.createResource(tuple[0]);
            Property predicate = ResourceFactory.createProperty(tuple[1]);
            RDFNode object = ResourceFactory.createTypedLiteral(tuple[2]);
            stmt = ResourceFactory.createStatement(subject, predicate, object);
            time = Long.parseLong(tuple[TUPLE_SIZE - 1]);
            return true;
        } else {
            stmt = null;
            time = -1;
            return false;
        }
    }

    public Statement nextStatement() {
        return stmt;
    }

    public long nextTime() {
        return time;
    }

}
