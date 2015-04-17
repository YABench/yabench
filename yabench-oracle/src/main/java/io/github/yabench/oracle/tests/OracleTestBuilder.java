package io.github.yabench.oracle.tests;

import com.google.common.io.Files;
import io.github.yabench.commons.utils.TimeUtils;
import io.github.yabench.oracle.WindowPolicy;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Properties;

public class OracleTestBuilder {
    
    private static final String WSIZE = "WSIZE";
    private static final String WSLIDE = "WSLIDE";
    private static final String WPOLICY = "WPOLICY";
    
    private final File inputStream;
    private final File queryResults;
    private final File output;
    private final String queryTemplate;
    
    private boolean graceful;
    private Duration windowSize;
    private Duration windowSlide;
    private WindowPolicy windowPolicy;
    private Properties properties;

    public OracleTestBuilder(File inputStream, File actualResults, File output, 
            File query) throws IOException {
        this.inputStream = inputStream;
        this.queryResults = actualResults;
        this.output = output;
        this.queryTemplate = Files.toString(query, Charset.defaultCharset());
    }
    
    public OracleTestBuilder withVariables(Properties properties) {
        this.windowSize = TimeUtils.parseDuration(properties.getProperty(WSIZE));
        this.windowPolicy = WindowPolicy.valueOf(
                properties.getProperty(WPOLICY).toUpperCase());
        if(properties.containsKey(WSLIDE)) {
            this.windowSlide = TimeUtils.parseDuration(
                    properties.getProperty(WSLIDE));
        } else {
            this.windowSlide = this.windowSize;
        }
        this.properties = properties;
        return this;
    }
    
    public OracleTestBuilder withGraceful(boolean graceful) {
        this.graceful = graceful;
        return this;
    }
    
    public OracleTest build() throws IOException {
        OracleTestImpl test = new OracleTestImpl(
                inputStream, queryResults, output, windowSize, windowSlide, 
                windowPolicy, graceful, properties, queryTemplate);
        return test;
    }

}
