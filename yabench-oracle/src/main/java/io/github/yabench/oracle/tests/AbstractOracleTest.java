package io.github.yabench.oracle.tests;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import org.apache.commons.cli.CommandLine;

public abstract class AbstractOracleTest implements OracleTest {

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
    
}
