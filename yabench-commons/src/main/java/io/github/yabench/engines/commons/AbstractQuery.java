package io.github.yabench.engines.commons;

import java.io.IOException;

public abstract class AbstractQuery implements Query {

    private final String query;

    public AbstractQuery(final String query) {
        this.query = query;
    }

    @Override
    public String getQueryString() {
        return this.query;
    }
}
