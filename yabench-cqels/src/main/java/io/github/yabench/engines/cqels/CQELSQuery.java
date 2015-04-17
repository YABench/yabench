package io.github.yabench.engines.cqels;

import io.github.yabench.commons.utils.TimeUtils;
import io.github.yabench.engines.commons.AbstractQuery;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CQELSQuery extends AbstractQuery {
    
    private static final Pattern WINDOW_REGEXP = Pattern.compile(
            ".*\\[\\s*RANGE\\s*(\\d+[sm]+)\\s+SLIDE\\s+(\\d+[sm]+)\\s*\\].*", Pattern.DOTALL);
    private final Duration windowSize;
    private final Duration windowSlide;

    public CQELSQuery(String query) {
        super(query);
        
        Matcher matcher = WINDOW_REGEXP.matcher(query);
        if(matcher.matches()) {
            this.windowSize = TimeUtils.parseDuration(matcher.group(1));
            this.windowSlide = TimeUtils.parseDuration(matcher.group(2));
        } else {
            this.windowSize = Duration.ZERO;
            this.windowSlide = Duration.ZERO;
            //throw new IllegalArgumentException();
        }
    }

    @Override
    public Duration getWindowSize() {
        return windowSize;
    }

    @Override
    public Duration getWindowSlide() {
        return windowSlide;
    }
    
}
