package io.github.yabench.oracle;

public class OracleResult {

    private double precision;
    private double recall;
    private long startshift;
    private long endshift;
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

	/**
	 * @return the startshift
	 */
	public long getStartshift() {
		return startshift;
	}

	/**
	 * @param startshift the startshift to set
	 */
	public void setStartshift(long startshift) {
		this.startshift = startshift;
	}

	/**
	 * @return the endshift
	 */
	public long getEndshift() {
		return endshift;
	}

	/**
	 * @param endshift the endshift to set
	 */
	public void setEndshift(long endshift) {
		this.endshift = endshift;
	}
	

	/**
	 * @return the endshift
	 */
	public long getDelay() {
		return delay;
	}

	/**
	 * @param endshift the endshift to set
	 */
	public void setDelay(long delay) {
		this.delay = delay;
	}

}
