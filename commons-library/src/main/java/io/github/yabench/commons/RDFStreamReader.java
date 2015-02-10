package io.github.yabench.commons;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
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

	// s,p,o + timestamp/interval = 4
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
			// +1 to avoid bug, if " ." is included in the last tuple
			// prohibiting correct parsing of the time
			String[] tuple = line.split(" ", TUPLE_SIZE + 1);
			Resource subject = ResourceFactory.createResource(tuple[0].substring(1, tuple[0].length() - 1));
			Property predicate = ResourceFactory.createProperty(tuple[1].substring(1, tuple[1].length() - 1));
			RDFNode object = createObject(tuple[2]);

			stmt = ResourceFactory.createStatement(subject, predicate, object);
			time = Long.parseLong(tuple[TUPLE_SIZE - 1].substring(1, tuple[TUPLE_SIZE - 1].length() - 1));
			return true;
		} else {
			stmt = null;
			time = -1;
			return false;
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
				return ResourceFactory.createTypedLiteral(objectString, XSDDatatype.XSDfloat);
			} else {
				return ResourceFactory.createTypedLiteral(objectString);
			}
		} else {
			return ResourceFactory.createResource(tuple.substring(1, tuple.length() - 1));
		}

	}

	public Statement nextStatement() {
		return stmt;
	}

	public long nextTime() {
		return time;
	}

}
