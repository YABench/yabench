package io.github.yabench.engines.csparql;

import com.hp.hpl.jena.rdf.model.Statement;

import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;
import eu.larkc.csparql.core.engine.CsparqlEngine;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;
import io.github.yabench.engines.commons.AbstractEngine;
import io.github.yabench.engines.commons.AbstractEngineLauncher;
import io.github.yabench.engines.commons.Query;
import io.github.yabench.engines.commons.ResultListener;

import java.text.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSPARQLEngine extends AbstractEngine {

    private static final String STREAM_URI = "http://ex.org/streams/test";
    private CsparqlEngine engine;
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
        engine = null;
    }

    @Override
    public void registerQuery(final Query query, final ResultListener listener)
            throws ParseException {
        csparqlProxy = engine.registerQuery(query.getQueryString(), false);
        
        if (csparqlProxy != null) {
            csparqlProxy.addObserver(new CSPARQLResultListenerProxy(listener));
        } else {
            throw new IllegalArgumentException();
        }
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
