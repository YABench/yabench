package io.github.yabench.oracle;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import org.apache.commons.io.IOUtils;

public class OracleResultsWriter implements Closeable {
    
    private static final String SEPARATOR = ",";
    private static final String NEWLINE = "\n";
    private final Writer writer;

    public OracleResultsWriter(final Writer writer) {
        this.writer = writer;
    }
    
    public void write(final double precision, final double recall, 
            final long delay, final int actualSize, final int expectedSize, 
            final int windowSize) throws IOException {
        writer.write(new StringBuilder()
                .append(precision)
                .append(SEPARATOR)
                .append(recall)
                .append(SEPARATOR)
                .append(delay)
                .append(SEPARATOR)
                .append(actualSize)
                .append(SEPARATOR)
                .append(expectedSize)
                .append(SEPARATOR)
                .append(windowSize)
                .append(NEWLINE)
                .toString());
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(writer);
    }
    
}
