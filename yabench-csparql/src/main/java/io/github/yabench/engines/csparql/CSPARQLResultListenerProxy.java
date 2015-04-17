package io.github.yabench.engines.csparql;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.core.ResultFormatter;
import io.github.yabench.commons.utils.NodeUtils;
import io.github.yabench.engines.commons.ResultListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class CSPARQLResultListenerProxy extends ResultFormatter {

    private static final String TAB = "\t";
    private final ResultListener listener;
    
    public CSPARQLResultListenerProxy(ResultListener listener) {
        this.listener = listener;
    }

    @Override
    public void update(Observable o, Object table) {
        final RDFTable rdfTable = (RDFTable) table;
        final List<Binding> bindings = new ArrayList<>();
        final String[] vars = rdfTable.getNames().toArray(new String[]{});
        rdfTable.stream().forEach((t) -> {
            bindings.add(NodeUtils.toBinding(vars, t.toString(), TAB));
        });
        listener.update(vars, bindings);
    }
    
}
