package io.github.yabench.oracle.tests;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.reflections.Reflections;

public class TestFactory {

    private final static String METHOD_EXPECTED_OPTIONS = "expectedOptions";
    
    private final File inputStream;
    private final File queryResults;
    private final File output;

    public TestFactory(File inputStream, File actualResults, File output) {
        this.inputStream = inputStream;
        this.queryResults = actualResults;
        this.output = output;
    }

    /**
     * @param testName
     * @return null if test with the given name doesn't exist
     * @throws Exception 
     */
    public Option[] getExpectedOptions(String testName) throws Exception {
        Class<?> testClass = findOracleTestByClassName(testName);
        if (testClass != null) {
            return (Option[]) testClass
                    .getMethod(METHOD_EXPECTED_OPTIONS).invoke(null, new Object[]{});
        } else {
            return null;
        }
    }
    
    public OracleTest createTest(String testName, CommandLine cliOptions) throws Exception {
        Class<?> testClass = findOracleTestByClassName(testName);
        if(testClass != null) {
            return (OracleTest) testClass
                    .getConstructor(File.class, File.class, File.class, 
                            CommandLine.class)
                    .newInstance(inputStream, queryResults, output, cliOptions);
        } else {
            return null;
        }
    }

    private Class<?> findOracleTestByClassName(String className) {
        Reflections reflections = new Reflections("io.github.yabench.oracle.tests");
        Set<Class<? extends OracleTest>> classes = reflections.getSubTypesOf(OracleTest.class);
        Optional<Class<? extends OracleTest>> r = classes.stream()
                .filter(c -> c.getSimpleName().equals(className))
                .findFirst();
        if (r.isPresent()) {
            return r.get();
        } else {
            return null;
        }
    }

}
