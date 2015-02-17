package io.github.yabench.oracle.tests;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.IOUtils;

public abstract class AbstractOracleTest implements OracleTest {

    private static final String QUERY_TEMPLATE_NAME = "query.template";
    private final CommandLine cli;
    private final Reader reader;

    public AbstractOracleTest(File inputStream, CommandLine cli) throws IOException {
        this.reader = new FileReader(inputStream);
        this.cli = cli;
    }

    protected CommandLine getOptions() {
        return cli;
    }

    protected Reader getReader() {
        return reader;
    }

    @Override
    public void close() throws IOException {
        reader.close();
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
