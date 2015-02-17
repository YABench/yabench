package io.github.yabench.oracle;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;

public class QueryExecutor {
    
    public ResultSetRewindable executeSelect(final Model model, final String strQuery) {      
        Query query = QueryFactory.create(strQuery);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            return ResultSetFactory.copyResults(results);
        }
    }
    
}
