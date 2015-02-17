package io.github.yabench;

import java.io.IOException;

public interface StreamGenerator {
      
    public void generate() throws IOException;
    
    public void close() throws IOException;
    
}
