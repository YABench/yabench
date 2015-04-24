package io.github.yabench.utils;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.jena.riot.RDFDataMgr;

public class CSRBenchToYABenchConverter {

    private static final String hasTimestamp = "http://www.streamreasoning.org/schema/benchmark#hasTimestamp";

    public static void main(String[] args) {
        if ((args.length <= 1 || args[0] == null) || args[1] == null) {
            System.out.println("Wrong arguments!");
        } else {
            final String inputFileName = args[0];
            final String outputFileName = args[1];

            final Dataset dataset = DatasetFactory.createMem();

            RDFDataMgr.read(dataset, inputFileName);

            final Model graphs = dataset.getNamedModel(
                    "http://www.streamreasoning.org/schema/benchmark#graphsList");

            final StmtIterator graphsIter = graphs.listStatements(
                    null, ResourceFactory.createProperty(hasTimestamp), (RDFNode) null);
            try (FileWriter writer = new FileWriter(outputFileName)) {
                while (graphsIter.hasNext()) {
                    final Statement graphStmt = graphsIter.nextStatement();
                    final long timestamp = graphStmt.getLong();
                    final String graphName = graphStmt.getSubject().getURI();

                    final StmtIterator obsIter = dataset.getNamedModel(graphName)
                            .listStatements();
                    while (obsIter.hasNext()) {
                        final Statement obs = obsIter.next();
                        final StringBuilder builder = new StringBuilder()
                                .append("<").append(obs.getSubject()).append("> ")
                                .append("<").append(obs.getPredicate()).append("> ");
                        if (obs.getObject().isURIResource()) {
                            builder.append("<").append(obs.getObject()).append("> ");
                        } else {
                            builder.append(obs.getObject()).append(" ");
                        }
                        builder.append("\"").append(timestamp).append("\"\n");

                        writer.write(builder.toString());
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}
