package io.github.yabench.oracle;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.aggregate.AggregateRegistry;
import io.github.yabench.commons.utils.NodeUtils;
import io.github.yabench.oracle.sparql.AccAvg;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class QueryExecutor {

    private final Query query;

    public QueryExecutor(final String template, final Properties variables) {
        this.query = QueryFactory.create(resolveVars(template, variables));
    }

    static {
        registerCustomAggregates();
    }

    public BindingWindow executeSelect(final TripleWindow input) {
        try (QueryExecution qexec = QueryExecutionFactory.create(query, input.getModel())) {
            ResultSet results = qexec.execSelect();
            final List<Binding> bindings = new ArrayList<>();
            while (results.hasNext()) {
                final QuerySolution soln = results.next();
                bindings.add(NodeUtils.toBinding(soln));
            }
            return new BindingWindow(bindings, input.getStart(), input.getEnd());
        }
    }

    private String resolveVars(final String template, final Properties vars) {
        String result = new String(template);
        for (String key : vars.stringPropertyNames()) {
            result = result.replaceAll("\\$\\{" + key + "\\}", vars.getProperty(key));
        }
        return result;
    }

    private static void registerCustomAggregates() {
        AggregateRegistry.register("http://yabench/avg", new AccAvg.Factory());
    }

}
