package io.github.yabench.engines;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;

/**
 * @author peter
 *
 */
public abstract class AbstractQuery implements Query {

	private String query;
	private String windowDefinition;
	protected Duration windowSize;
	protected Duration windowSlide;

	public AbstractQuery(String query) throws IOException {
		this.query = query;
		
		Pattern windowRegEx = Pattern.compile("\\[(.*?)\\]");
		Matcher matchPattern = windowRegEx.matcher(this.query);
		
		while(matchPattern.find()) {
            this.windowDefinition = matchPattern.group(1);
            break;
        }
		
		

		
	}

	@Override
	public String getQueryString() {
		return this.query;
	}
	
	@Override
	public String getwindowDefinition() {
		return this.windowDefinition;
	}
	
	@Override
	public Duration getWindowSize() {
		return this.windowSize;
	}
	
	@Override
	public Duration getWindowSlide() {
		return this.windowSlide;
	}
}
