package io.github.yabench.engines.commons;

import java.time.Duration;

public interface Query {

    public String getQueryString();

    public Duration getWindowSize();

    public Duration getWindowSlide();
}
