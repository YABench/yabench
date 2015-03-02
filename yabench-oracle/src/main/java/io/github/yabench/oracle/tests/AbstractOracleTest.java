package io.github.yabench.oracle.tests;

import io.github.yabench.oracle.ResultsReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.IOUtils;

abstract class AbstractOracleTest implements OracleTest {

    private static final String QUERY_TEMPLATE_NAME = "query.template";
    private final CommandLine cli;
    private final Reader inputStreamReader;
    private final Writer outputWriter;
    private final ResultsReader queryResultsReader;

    AbstractOracleTest(File inputStream, File queryResults, File output, 
            CommandLine cli) 
            throws IOException {
        this.inputStreamReader = new FileReader(inputStream);
        this.outputWriter = new FileWriter(output);
        this.queryResultsReader = new ResultsReader(new FileReader(queryResults));
        this.cli = cli;
    }

    protected CommandLine getOptions() {
        return cli;
    }

    protected Reader getInputStreamReader() {
        return inputStreamReader;
    }
    
    protected Writer getOutputWriter() {
        return outputWriter;
    }
    
    protected ResultsReader getQueryResultsReader() {
        return queryResultsReader;
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(inputStreamReader);
        IOUtils.closeQuietly(outputWriter);
        IOUtils.closeQuietly(queryResultsReader);
    }

    protected String loadQueryTemplate() throws IOException {
        final String path = new StringBuilder("/")
                .append(this.getClass().getName().replace(".", "/"))
                .append("/")
                .append(QUERY_TEMPLATE_NAME).toString();
        return IOUtils.toString(this.getClass().getResourceAsStream(path));
    }
    
    protected String resolveVars(final String template, 
            final Map<String, String> vars) {
        String result = new String(template);
        for(String key : vars.keySet()) {
            result = result.replaceAll(key, vars.get(key));
        }
        return result;
    }

}
