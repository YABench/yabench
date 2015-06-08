package io.github.yabench.oracle;

public class OracleResultBuilder {

    private static final double DEFAULT_DOUBLE = -1;
    private static final long DEFAULT_LONG = -1;
    private static final int DEFAULT_INT = -1;
    private double precision = DEFAULT_DOUBLE;
    private double recall = DEFAULT_DOUBLE;
    private long startshift = DEFAULT_LONG;
    private long endshift = DEFAULT_LONG;
    private long delay = DEFAULT_LONG;
    private long expectedInputSize = DEFAULT_LONG;
    private int actualResultSize = DEFAULT_INT;
    private int expectedResultSize = DEFAULT_INT;
    
    public OracleResultBuilder() {
    }
    
    public OracleResultBuilder precision(final double precision) {
        this.precision = precision;
        return this;
    }
    
    public OracleResultBuilder recall(final double recall) {
        this.recall = recall;
        return this;
    }
    
    public OracleResultBuilder fMeasure(final FMeasure fMeasure) {
        this.precision = fMeasure.getPrecisionScore();
        this.recall = fMeasure.getRecallScore();
        return this;
    }

    public OracleResultBuilder startshift(final long startshift) {
        this.startshift = startshift;
        return this;
    }
    
    public OracleResultBuilder endshift(final long endshift) {
        this.endshift = endshift;
        return this;
    }

    public OracleResultBuilder delay(final long delay) {
        this.delay = delay;
        return this;
    }
    
    public OracleResultBuilder actualResultSize(int actualResultSize) {
        this.actualResultSize = actualResultSize;
        return this;
    }

    public OracleResultBuilder expectedResultSize(int expectedResultSize) {
        this.expectedResultSize = expectedResultSize;
        return this;
    }
    
    public OracleResultBuilder resultSize(final BindingWindow expected, 
            final BindingWindow actual) {
        this.expectedResultSize = expected.getBindings().size();
        this.actualResultSize = actual.getBindings().size();
        return this;
    }
    
    public OracleResultBuilder expectedInputSize(final long expected) {
        this.expectedInputSize = expected;
        return this;
    }
    
    public OracleResult build() {
        final OracleResult result = new OracleResult();
        result.setPrecision(precision);
        result.setRecall(recall);
        result.setActualResultSize(actualResultSize);
        result.setExpectedResultSize(expectedResultSize);
        result.setExpectedInputSize(expectedInputSize);
        result.setStartshift(startshift);
        result.setEndshift(endshift);
        result.setDelay(delay);
        return result;
    }
    
}
