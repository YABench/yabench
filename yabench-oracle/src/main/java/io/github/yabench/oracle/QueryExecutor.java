package io.github.yabench.oracle;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import io.github.yabench.commons.NodeUtils;
import java.util.ArrayList;
import java.util.List;

public class QueryExecutor {
    
    public BindingWindow executeSelect(final TripleWindow input, final String strQuery) {      
        Query query = QueryFactory.create(strQuery);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, input.getModel())) {
            ResultSet results = qexec.execSelect();
            final List<Binding> bindings = new ArrayList<>();
            while(results.hasNext()) {
                final QuerySolution soln = results.next();
                bindings.add(NodeUtils.toBinding(soln));
            }
            return new BindingWindow(bindings, input.getEnd());
        }
    }
    
}
