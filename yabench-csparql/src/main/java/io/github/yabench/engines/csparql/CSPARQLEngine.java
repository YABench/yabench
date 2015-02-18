package io.github.yabench.engines.csparql;

import com.hp.hpl.jena.rdf.model.Statement;
import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;
import eu.larkc.csparql.core.engine.ConsoleFormatter;
import eu.larkc.csparql.core.engine.CsparqlEngine;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import io.github.yabench.engines.AbstractEngine;
import java.text.ParseException;

public class CSPARQLEngine extends AbstractEngine {

    private static final String STREAM_URI = "http://ex.org/streams/test";
    private final CsparqlEngine engine;
    private final RdfStream stream;
    
    private CsparqlQueryResultProxy csparqlProxy;

    public CSPARQLEngine() {
        this.engine = new CsparqlEngineImpl();
        this.stream = new RdfStream(STREAM_URI);
    }

    @Override
    public void initialize() {
        engine.initialize();
        engine.registerStream(stream);
    }

    @Override
    public void close() {
        engine.unregisterQuery(csparqlProxy.getId());
        engine.unregisterStream(stream.getIRI());
        engine.destroy();
    }

    @Override
    public void registerQuery(final String query) throws ParseException {
        csparqlProxy = engine.registerQuery(query, false);
    }

    @Override
    public void registerResultListener() {
        csparqlProxy.addObserver(new ConsoleFormatter());
    }

    @Override
    public void stream(Statement stmt) {
        stream.put(new RdfQuadruple(
                stmt.getSubject().toString(),
                stmt.getPredicate().toString(),
                stmt.getObject().toString(),
                System.currentTimeMillis()));
    }

}
