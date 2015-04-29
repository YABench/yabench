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
    
    public Writer getWriter() {
        return writer;
    }
    
    public void write(final OracleResult result) throws IOException {
        writer.write(new StringBuilder()
                .append(result.getPrecision())
                .append(SEPARATOR)
                .append(result.getRecall())
                .append(SEPARATOR)
                .append(result.getActualResultSize())
                .append(SEPARATOR)
                .append(result.getExpectedResultSize())
                .append(SEPARATOR)
                .append(result.getExpectedInputSize())
                .append(SEPARATOR)
                .append(result.getStartshift())
                .append(SEPARATOR)
                .append(result.getEndshift())
                .append(SEPARATOR)
                .append(result.getDelay())
                .append(NEWLINE)
                .toString());
    }
    
    public void write(final double precision, final double recall, 
            final int actualSize, final int expectedSize, 
            final int windowSize, final long startshift, final long endshift, final long delay) throws IOException {
        writer.write(new StringBuilder()
                .append(precision)
                .append(SEPARATOR)
                .append(recall)
                .append(SEPARATOR)
                .append(actualSize)
                .append(SEPARATOR)
                .append(expectedSize)
                .append(SEPARATOR)
                .append(windowSize)
                .append(SEPARATOR)
                .append(startshift)
                .append(SEPARATOR)
                .append(endshift)
                .append(SEPARATOR)
                .append(delay)
                .append(NEWLINE)
                .toString());
    }

    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(writer);
    }
    
}
