package io.github.yabench.commons.tests;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class TestUtils {

    private final Class<?> clazz;
    private final String path;
    
    public TestUtils(final Class<?> clazz) {
        this.clazz = clazz;
        this.path = new StringBuilder("/")
                .append(clazz.getCanonicalName().replaceAll("\\.", "\\/"))
                .append("/").toString();
        System.out.println(path);
    }
    
    public File readToFile(final String name) {
        return FileUtils.toFile(clazz.getResource(path + name));
    }
    
    public String readToString(final String name) 
            throws IOException {
        return IOUtils.toString(clazz.getResourceAsStream(path + name));
    }
    
    public Reader readToReader(final String name) throws IOException {
        return new StringReader(readToString(name));
    }
    
}
