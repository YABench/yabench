package io.github.yabench.oracle;

public class Window {

    private final long start;
    private final long end;

    public Window(final long start, final long end) {
        this.start = start;
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return new StringBuilder("Window [")
                .append(start).append(":").append(end).append("]").toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Window) {
            Window that = (Window) obj;
            return !(this.start != that.start || this.end != that.end);
        }
        return false;
    }

    /**
     * @return 
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (int) (this.start ^ (this.start >>> 32));
        hash = 17 * hash + (int) (this.end ^ (this.end >>> 32));
        return hash;
    }

}
