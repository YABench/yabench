package io.github.yabench.oracle.tests;

import io.github.yabench.oracle.tests.comparators.OracleComparator;
import io.github.yabench.oracle.tests.comparators.OracleComparatorBuilder;
import io.github.yabench.oracle.QueryExecutor;
import io.github.yabench.oracle.readers.EngineResultsReader;
import io.github.yabench.oracle.readers.TripleWindowReader;
import io.github.yabench.oracle.OracleResultsWriter;
import io.github.yabench.oracle.readers.BufferedTWReader;
import io.github.yabench.oracle.WindowFactory;
import io.github.yabench.oracle.WindowPolicy;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OracleTestImpl implements OracleTest {

    private static final Logger logger = LoggerFactory.getLogger(OracleTestImpl.class);
    private final TripleWindowReader inputStreamReader;
    private final OracleResultsWriter oracleResultsWriter;
    private final EngineResultsReader queryResultsReader;
    private final Duration windowSize;
    private final Duration windowSlide;
    private final WindowPolicy windowPolicy;
    private final boolean graceful;
    private final Properties properties;
    private final String queryTemplate;

    OracleTestImpl(File inputStream, File queryResults, File output, 
            Duration windowSize, Duration windowSlide, 
            WindowPolicy windowPolicy, boolean graceful, Properties properties, 
            String queryTemplate) 
            throws IOException {
        this.inputStreamReader = new TripleWindowReader(new FileReader(inputStream));
        this.oracleResultsWriter = new OracleResultsWriter(new FileWriter(output));
        this.queryResultsReader = new EngineResultsReader(new FileReader(queryResults));
        this.windowSize = windowSize;
        this.windowSlide = windowSlide;
        this.windowPolicy = windowPolicy;
        this.graceful = graceful;
        this.properties = properties;
        this.queryTemplate = queryTemplate;
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(inputStreamReader);
        IOUtils.closeQuietly(oracleResultsWriter);
        IOUtils.closeQuietly(queryResultsReader);
    }

    @Override
    public void compare() throws IOException {
        queryResultsReader.initialize(windowSize);

        final QueryExecutor queryExecutor = 
                new QueryExecutor(queryTemplate, properties);
        final WindowFactory windowFactory = 
                new WindowFactory(windowSize, windowSlide);
        final OracleComparator comparator = new OracleComparatorBuilder(
                inputStreamReader, queryResultsReader, windowFactory,
                queryExecutor, oracleResultsWriter, graceful)
                .newComparator(windowPolicy);
        
        comparator.compare();
    }
}
