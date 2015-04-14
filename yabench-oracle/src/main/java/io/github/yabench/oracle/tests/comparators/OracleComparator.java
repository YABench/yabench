package io.github.yabench.oracle.tests.comparators;

import java.io.IOException;

public interface OracleComparator {
    
    public static final long NO_DELAY = 0;
    
    public void compare() throws IOException;
    
}
