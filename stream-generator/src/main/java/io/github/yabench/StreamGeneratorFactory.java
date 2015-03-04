package io.github.yabench;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.reflections.Reflections;

public class StreamGeneratorFactory {
    
    private final static String METHOD_EXPECTED_OPTIONS = "expectedOptions";
    private final File destination;
    
    public StreamGeneratorFactory(File destination) {
        this.destination = destination;
    }
    
    public StreamGenerator createTest(String testName, CommandLine cliOptions) throws Exception {
        Class<?> testClass = findTest(testName);
        if(testClass != null) {
            final Path testDest = 
                    Files.createFile(destination.toPath());
            return (StreamGenerator) testClass
                    .getConstructor(Path.class, CommandLine.class)
                    .newInstance(testDest, cliOptions);
        } else {
            return null;
        }
    }
    
    public List<Option> getTestOptions(String testName) throws Exception {
        Class<?> testClass = findTest(testName);
        if(testClass != null) {
            return (List<Option>) testClass
                    .getMethod(METHOD_EXPECTED_OPTIONS).invoke(null, new Object[]{});
        } else {
            return null;
        }
    }
    
    private Class<?> findTest(String testName) {
        Reflections reflections = new Reflections();
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(RSPTest.class);
        Optional<Class<?>> r = classes.stream()
                .filter(c -> c.getSimpleName().equals(testName))
                .findFirst();
        if(r.isPresent()) {
            return r.get();
        } else {
            return null;
        }
    }
}
