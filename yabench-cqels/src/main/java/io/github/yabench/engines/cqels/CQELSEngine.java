package io.github.yabench.engines.cqels;

import com.hp.hpl.jena.rdf.model.Statement;
import io.github.yabench.engines.commons.AbstractEngine;
import io.github.yabench.engines.commons.Query;
import io.github.yabench.engines.commons.ResultListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import org.deri.cqels.engine.ContinuousListener;
import org.deri.cqels.engine.ContinuousSelect;
import org.deri.cqels.engine.ExecContext;
import org.deri.cqels.engine.RDFStream;

public class CQELSEngine extends AbstractEngine {

    private static final String STREAM_URI = "http://ex.org/streams/test";
    private static final String CQELS_HOME = "cqels_home";
    private ExecContext execContext;
    private ContinuousListener resultListener;
    private RDFStream rdfStream;

    public CQELSEngine() {
        File home = new File(CQELS_HOME);
        if (!home.exists()) {
            home.mkdir();
        }
        this.execContext = new ExecContext(CQELS_HOME, true);
    }

    @Override
    public void initialize() {
        rdfStream = new RDFStream(execContext, STREAM_URI) {

            @Override
            public void stop() {
                //Nothing
            }
        };
    }

    @Override
    public void registerResultListener(ResultListener listener) {
        resultListener = new CQELSResultListenerProxy(execContext, listener);
    }

    @Override
    public void registerQuery(final Query query) throws ParseException {
        ContinuousSelect select = execContext
                .registerSelect(query.getQueryString());

        if (resultListener != null) {
            select.register(resultListener);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void stream(Statement stmt) {
        rdfStream.stream(stmt.asTriple());
    }

    @Override
    public void close() throws IOException {
        resultListener = null;
        rdfStream.stop();
        execContext = null;
    }

}
