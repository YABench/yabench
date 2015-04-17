package io.github.yabench.commons.utils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class TimeUtils {

    public static Duration parseDuration(final String duration) {
        if(duration.endsWith("ms")) {
            return Duration.of(
                    Long.parseLong(duration.substring(0, duration.length()-2)), 
                    ChronoUnit.MILLIS);
        }
        if(duration.endsWith("s")) {
            return Duration.of(
                    Long.parseLong(duration.substring(0, duration.length()-1)), 
                    ChronoUnit.SECONDS);
        }
        if(duration.endsWith("m")) {
            return Duration.of(
                    Long.parseLong(duration.substring(0, duration.length()-1)), 
                    ChronoUnit.MINUTES);
        }
        if(duration.endsWith("h")) {
            return Duration.of(
                    Long.parseLong(duration.substring(0, duration.length()-1)), 
                    ChronoUnit.HOURS);
        }
        return Duration.of(Long.parseLong(duration), ChronoUnit.MILLIS);
    }
    
}
