package io.github.yabench.utils;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import io.github.yabench.commons.StatementComparator;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
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

            final List<Statement> graphsList = Lists.newArrayList(
                    graphs.listStatements(null, ResourceFactory.createProperty(hasTimestamp), (RDFNode) null));
            graphsList.sort(new GraphComparator());
            
            try (FileWriter writer = new FileWriter(outputFileName)) {
                for(Statement graphStmt : graphsList) {
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
                            builder.append(ResourceFactory.createTypedLiteral(
                                    obs.getString(), XSDDatatype.XSDfloat))
                                    .append(" ");
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
    
    private static class GraphComparator implements Comparator<Statement> {

        @Override
        public int compare(Statement o1, Statement o2) {
            return Long.compare(o1.getLong(), o2.getLong());
        }
    
    }

}
