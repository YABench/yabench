package io.github.yabench.commons;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RDFStreamReader implements AutoCloseable {

    // s,p,o + timestamp/interval = 4
    private static final int TUPLE_SIZE = 4;
    private final BufferedReader reader;
    private TemporalTriple lastTriple = null;

    public RDFStreamReader(File stream) throws IOException {
        this(stream.toPath());
    }

    public RDFStreamReader(Path stream) throws IOException {
        this(Files.newBufferedReader(stream));
    }

    public RDFStreamReader(Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    /**
     * @return null if the end of the stream has been reached
     * @throws IOException
     */
    public TemporalTriple readNextTriple() throws IOException {
        final String line = reader.readLine();
        if (line != null) {
            String[] tuple = line.split(" ", TUPLE_SIZE + 1);
            Resource subject = ResourceFactory.createResource(
                    tuple[0].substring(1, tuple[0].length() - 1));
            Property predicate = ResourceFactory.createProperty(
                    tuple[1].substring(1, tuple[1].length() - 1));
            RDFNode object = createObject(tuple[2]);

            Statement stmt = ResourceFactory.createStatement(
                    subject, predicate, object);
            long time = Long.parseLong(tuple[TUPLE_SIZE - 1]
                    .substring(1, tuple[TUPLE_SIZE - 1].length() - 1));

            return new TemporalTriple(stmt, time);
        } else {
            return null;
        }
    }

    public TemporalGraph readNextGraph() throws IOException {
        final List<TemporalTriple> triples = new ArrayList<>();
        if (lastTriple != null) {
            triples.add(lastTriple);
        }
        for (;;) {
            final TemporalTriple triple = readNextTriple();
            if (triple != null) {
                if (lastTriple == null) {
                    triples.add(triple);
                    lastTriple = triple;
                } else {
                    if (lastTriple.getTime() == triple.getTime()) {
                        triples.add(triple);
                        lastTriple = triple;
                    } else {
                        lastTriple = triple;
                        break;
                    }
                }
            } else {
                lastTriple = null;
                break;
            }
        }
        return triples.isEmpty() ? null : new TemporalGraph(triples);
    }

    /**
     * @param string
     * @return
     */
    private RDFNode createObject(String tuple) {
        //if it is datatyped...
        if (tuple.contains("^^")) {
            String[] objectSplit = tuple.split("\\^\\^");
            String objectString = objectSplit[0];
            String dtype = objectSplit[1];

            //if it is a float datatype...
            if (dtype.toLowerCase().contains("float")) {
                return ResourceFactory.createTypedLiteral(
                        objectString, XSDDatatype.XSDfloat);
            } else {
                return ResourceFactory.createTypedLiteral(objectString);
            }
        } else {
            return ResourceFactory.createResource(
                    tuple.substring(1, tuple.length() - 1));
        }

    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

}
