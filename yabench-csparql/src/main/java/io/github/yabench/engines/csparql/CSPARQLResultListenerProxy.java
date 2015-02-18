package io.github.yabench.engines.csparql;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.RDFTuple;
import eu.larkc.csparql.core.ResultFormatter;
import io.github.yabench.engines.ResultListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class CSPARQLResultListenerProxy extends ResultFormatter {

    private final ResultListener listener;
    
    public CSPARQLResultListenerProxy(ResultListener listener) {
        this.listener = listener;
    }

    @Override
    public void update(Observable o, Object table) {
        final RDFTable rdfTable = (RDFTable) table;
        final List<Binding> bindings = new ArrayList<>();
        final String[] vars = rdfTable.getNames().toArray(new String[]{});
        for(RDFTuple t : rdfTable) {
            final String[] values = t.toString().split("\t");
            final BindingMap binding = BindingFactory.create();
            for(int i = 0; i < vars.length; i++) {
                binding.add(Var.alloc(vars[i]), toNode(values[i]));
            }
            bindings.add(binding);
        }
        listener.update(bindings);
    }
    
    private Node toNode(String value) {
        if(value.startsWith("http://")) {
            return NodeFactory.createURI(value);
        } else {
            return NodeFactory.createLiteral(value);
        }
    }
    
}
