package io.github.yabench.commons;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class RDFStreamReader {

    // s,p,o + timestamp/interval = 4
    private static final int TUPLE_SIZE = 4;
    private final BufferedReader reader;

    public RDFStreamReader(Path stream) throws IOException {
        this.reader = Files.newBufferedReader(stream);
    }

    public RDFStreamReader(Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    /**
     * @return null if the end of the stream has been reached
     * @throws IOException
     */
    public TemporalTriple readNext() throws IOException {
        String line = reader.readLine();
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

}
