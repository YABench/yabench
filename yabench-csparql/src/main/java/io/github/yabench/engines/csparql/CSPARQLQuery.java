package io.github.yabench.engines.csparql;

import java.io.IOException;
import java.time.Duration;

import io.github.yabench.commons.TimeUtils;
import io.github.yabench.engines.AbstractQuery;
/**
 * @author peter
 *
 */
public class CSPARQLQuery extends AbstractQuery {

	/**
	 * @param query
	 * @throws IOException
	 */
	public CSPARQLQuery(String query) throws IOException {
		super(query);
		
		int range = this.getwindowDefinition().indexOf("RANGE");
		int firstS = this.getwindowDefinition().indexOf("s");
		int step = this.getwindowDefinition().indexOf("STEP");
		int secondS = this.getwindowDefinition().lastIndexOf("s");

		this.windowSize = TimeUtils.parseDuration(this.getwindowDefinition().substring(range+6, firstS+1));
		this.windowSlide = TimeUtils.parseDuration(this.getwindowDefinition().substring(step+5, secondS+1));		
		
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
