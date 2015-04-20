package io.github.yabench.oracle.tests.comparators;

import io.github.yabench.oracle.readers.BufferedTWReader;
import io.github.yabench.oracle.readers.EngineResultsReader;
import io.github.yabench.oracle.readers.TripleWindowReader;
import io.github.yabench.oracle.OracleResultsWriter;
import io.github.yabench.oracle.QueryExecutor;
import io.github.yabench.oracle.WindowFactory;
import io.github.yabench.oracle.WindowPolicy;

public class OracleComparatorBuilder {
    
    private final BufferedTWReader inputStreamReader;
    private final EngineResultsReader queryResultsReader;
    private final WindowFactory windowFactory;
    private final QueryExecutor queryExecutor;
    private final OracleResultsWriter oracleResultsWriter;
    private final boolean graceful;

    public OracleComparatorBuilder(BufferedTWReader inputStreamReader,
            EngineResultsReader queryResultsReader,
            WindowFactory windowFactory, QueryExecutor queryExecutor,
            OracleResultsWriter oracleResultsWriter, boolean graceful) {
        this.inputStreamReader = inputStreamReader;
        this.queryResultsReader = queryResultsReader;
        this.windowFactory = windowFactory;
        this.queryExecutor = queryExecutor;
        this.oracleResultsWriter = oracleResultsWriter;
        this.graceful = graceful;
    }
    
    public OracleComparator newComparator(final WindowPolicy policy) {
        switch (policy) {
            case ONWINDOWCLOSE:
                return new OnWindowCloseComparator(
                        inputStreamReader, queryResultsReader, windowFactory,
                        queryExecutor, oracleResultsWriter, graceful);
            case ONCONTENTCHANGE:
                return new OnContentChangeComparator(
                        inputStreamReader, queryResultsReader, windowFactory,
                        queryExecutor, oracleResultsWriter, graceful);
            default:
                throw new UnsupportedOperationException(
                        "Can't build oracle comparator for the given window policy!");
        }
    }
    
}
