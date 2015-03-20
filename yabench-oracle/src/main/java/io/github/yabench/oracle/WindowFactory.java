package io.github.yabench.oracle;

import java.time.Duration;

public class WindowFactory {

    private final Duration windowSize;
    private final Duration windowSlide;
    private long numberOfSlides = 0;
    
    public WindowFactory(final Duration windowSize, final Duration windowSlide) {
        this.windowSize = windowSize;
        this.windowSlide = windowSlide;
    }
    
    public Window nextWindow() {
        numberOfSlides++;
        
        final long windowEnd = numberOfSlides * windowSlide.toMillis();
        final long windowStart = windowEnd - windowSize.toMillis() > 0 ? 
                windowEnd - windowSize.toMillis() : 0;
        
        return new Window(windowStart, windowEnd);
    }
    
}
