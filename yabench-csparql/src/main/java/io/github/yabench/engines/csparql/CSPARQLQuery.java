package io.github.yabench.engines.csparql;

import java.io.IOException;
import java.time.Duration;
import io.github.yabench.commons.utils.TimeUtils;
import io.github.yabench.engines.commons.AbstractQuery;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSPARQLQuery extends AbstractQuery {

    private static final Pattern WINDOW_REGEXP = Pattern.compile("\\[(.*?)\\]");
    private final Duration windowSize;
    private final Duration windowSlide;

    /**
     * @param query
     */
    public CSPARQLQuery(String query) {
        super(query);

        Matcher matcher = WINDOW_REGEXP.matcher(query);

        String windowDefinition = null;
        while (matcher.find()) {
            windowDefinition = matcher.group(1);
            break;
        }

        if (windowDefinition != null) {
            int range = windowDefinition.indexOf("RANGE");
            int firstS = windowDefinition.indexOf("s");
            int step = windowDefinition.indexOf("STEP");
            int secondS = windowDefinition.lastIndexOf("s");

            this.windowSize = TimeUtils
                    .parseDuration(windowDefinition.substring(range + 6, firstS + 1));
            this.windowSlide = TimeUtils
                    .parseDuration(windowDefinition.substring(step + 5, secondS + 1));
        } else {
            throw new IllegalArgumentException();
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
