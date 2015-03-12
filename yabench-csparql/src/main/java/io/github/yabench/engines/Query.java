package io.github.yabench.engines;

import java.time.Duration;

/**
 * @author peter
 *
 */
public interface Query {
	
	public String getQueryString();
	
	public Duration getWindowSize();
	
	public Duration getWindowSlide();

	public String getwindowDefinition();


}
