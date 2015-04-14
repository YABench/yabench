package io.github.yabench.oracle;

public class OracleResult {

    private double precision;
    private double recall;
    private long delay;
    private int actualResultSize;
    private int expectedResultSize;
    private int expectedInputSize;

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getRecall() {
        return recall;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public int getActualResultSize() {
        return actualResultSize;
    }

    public void setActualResultSize(int actualResultSize) {
        this.actualResultSize = actualResultSize;
    }

    public int getExpectedResultSize() {
        return expectedResultSize;
    }

    public void setExpectedResultSize(int expectedResultSize) {
        this.expectedResultSize = expectedResultSize;
    }

    public int getExpectedInputSize() {
        return expectedInputSize;
    }

    public void setExpectedInputSize(int expectedInputSize) {
        this.expectedInputSize = expectedInputSize;
    }

}
