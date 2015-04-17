package io.github.yabench.commons.utils;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import java.util.Iterator;

public class NodeUtils {
    
    private static final String QUOTE = "\"";
    private static final String GT = ">";
    private static final String LT = "<";
    
    public static String unquoting(final String string) {
        final StringBuilder builder = new StringBuilder(string);
        if(builder.indexOf(QUOTE) == 0) {
            builder.deleteCharAt(0);
        }
        if(builder.lastIndexOf(QUOTE) == builder.length() - 1) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }
    
    public static String toUri(final String string) {
        final StringBuilder builder = new StringBuilder(string);
        if(builder.indexOf(LT) == 0) {
            builder.deleteCharAt(0);
        }
        if(builder.lastIndexOf(GT) == builder.length() - 1) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    public static Node toNode(String value) {
        if (value.startsWith("http://")) {
            return NodeFactory.createURI(value);
        } else {
            if(value.contains("^^")) {
                String[] parts = value.split("\\^\\^");
                RDFDatatype dtype = NodeFactory.getType(toUri(parts[1]));
                return NodeFactory.createLiteral(unquoting(parts[0]), dtype);
            } else {
                return NodeFactory.createLiteral(value);
            }
        }
    }
    
    public static Binding toBinding(QuerySolution soln) {
        final Iterator<String> vars = soln.varNames();
        final BindingMap binding = BindingFactory.create();
        while(vars.hasNext()) {
            final String var = vars.next();
            binding.add(Var.alloc(var), soln.get(var).asNode());
        }
        return binding;
    }

    public static Binding toBinding(String[] vars, String string, String separator) {
        String[] values = string.split(separator);
        final BindingMap binding = BindingFactory.create();
        for (int i = 0; i < vars.length; i++) {
            binding.add(Var.alloc(vars[i]), NodeUtils.toNode(values[i]));
        }
        return binding;
    }

}
