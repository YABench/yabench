package io.github.yabench.oracle.tests;

import java.io.Closeable;
import java.io.IOException;

public interface OracleTest extends Closeable {
    
    public String getName();
    
    public void init() throws Exception;
    
    public int compare();
    
    @Override
    public void close() throws IOException;
}
