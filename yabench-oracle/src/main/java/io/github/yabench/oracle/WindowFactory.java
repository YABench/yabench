package io.github.yabench.oracle;

import java.time.Duration;

public class WindowFactory {

    private final Duration size;
    private final Duration slide;
    private long numberOfSlides = 0;

    public WindowFactory(final Duration windowSize, final Duration windowSlide) {
        this.size = windowSize;
        this.slide = windowSlide;
    }

    public Duration getWindowSize() {
        return size;
    }

    public Window nextWindow() {
        numberOfSlides++;

        final long windowEnd = numberOfSlides * slide.toMillis();
        final long windowStart = windowEnd - size.toMillis() > 0
                ? windowEnd - size.toMillis() : 0;

        return new Window(windowStart, windowEnd);
    }

    public Window nextWindow(final long nextContentTimestamp) {
        final long windowEnd = nextContentTimestamp;

        while (numberOfSlides * slide.toMillis() + size.toMillis() < windowEnd) {
            numberOfSlides++;
        }

        if (numberOfSlides - 1 > 0) {
            numberOfSlides--;
        } else {
            numberOfSlides = 0;
        }

        return new Window(numberOfSlides * slide.toMillis(), windowEnd);
    }

    public Window nextWindow(Window previous, long nextContentTimestamp) {
        Window newWindow = nextWindow(nextContentTimestamp);
        
        if(newWindow.getStart() < previous.getStart()) {
            return new Window(previous.getStart(), newWindow.getEnd());
        }
        return newWindow;
    }

    /**
     * @return the size
     */
    public Duration getSize() {
        return size;
    }

    /**
     * @return the slide
     */
    public Duration getSlide() {
        return slide;
    }
}
