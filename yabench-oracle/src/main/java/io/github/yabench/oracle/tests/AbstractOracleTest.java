package io.github.yabench.oracle.tests;

import io.github.yabench.oracle.ResultsReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.IOUtils;

abstract class AbstractOracleTest implements OracleTest {

    private static final String QUERY_TEMPLATE_NAME = "query.template";
    private final CommandLine cli;
    private final Reader isReader;
    private final ResultsReader arReader;

    AbstractOracleTest(File inputStream, File actualResults, CommandLine cli) 
            throws IOException {
        this.isReader = new FileReader(inputStream);
        this.arReader = new ResultsReader(new FileReader(actualResults));
        this.cli = cli;
    }

    protected CommandLine getOptions() {
        return cli;
    }

    protected Reader getISReader() {
        return isReader;
    }
    
    protected ResultsReader getARReader() {
        return arReader;
    }

    @Override
    public void close() throws IOException {
        isReader.close();
        arReader.close();
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
