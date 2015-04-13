package io.github.yabench.oracle.tests;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class AbstractOracleTestTest {
    
    private static final String PREFIX = 
            "/io/github/yabench/oracle/tests/AbstractOracleTestTest/";

    @Test
    public void testOnContentChange() {
        final String TEST_PREFIX = "testOnContentChange/";
        final File input = read(PREFIX + TEST_PREFIX + "input.stream");
        final File actual = read(PREFIX + TEST_PREFIX + "results.stream");
//        TestFactory testFactory = new TestFactory(input, actual, null)
    }
    
    private File read(final String name) {
        return FileUtils.toFile(this.getClass().getResource(name));
    }
    
}
