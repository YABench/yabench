package io.github.yabench.engines.cqels;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import io.github.yabench.engines.commons.ResultListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.deri.cqels.data.Mapping;
import org.deri.cqels.engine.ContinuousListener;
import org.deri.cqels.engine.ExecContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CQELSResultListenerProxy implements ContinuousListener {

    private static final Logger logger = LoggerFactory.getLogger(
            CQELSResultListenerProxy.class);
    private final ResultListener listener;
    private final ExecContext execContext;

    public CQELSResultListenerProxy(final ExecContext execContext,
            final ResultListener listener) {
        this.listener = listener;
        this.execContext = execContext;
    }

    @Override
    public void update(Mapping mapping) {
        final List<String> variables = new ArrayList<>();
        final BindingMap binding = BindingFactory.create();

        for (Iterator<Var> vars = mapping.vars(); vars.hasNext();) {
            final Var var = vars.next();
            variables.add(var.getVarName());

            final long id = mapping.get(var);
            if (id > 0) {
                binding.add(var, execContext.engine().decode(id));
            } else {
                logger.error("Can't decode the value of {}!", var);
                binding.add(var, null);
            }
        }

        listener.update(variables.toArray(new String[]{}),
                new ArrayList<Binding>() {
                    {
                        add(binding);
                    }
                });
    }

}
