package io.github.yabench.engines;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultSerializer implements ResultListener {

    private static final String NEWLINE = "\n";
    private static final String TAB = "\t";
    private final Writer writer;
    private boolean initialized = false;
    private boolean firstResult = true;
    private final static Logger log = LoggerFactory.getLogger(ResultSerializer.class);


    public ResultSerializer(Writer writer) {
        this.writer = writer;
    }

    public void initialize() throws IOException {
        if (!initialized) {
            writer.write(System.currentTimeMillis() + NEWLINE);
            initialized = true;
        }
    }

    @Override
    public void update(String[] vars, List<Binding> bindings) {
        try {
            if (firstResult) {
                for(String var : vars) {
                    writer.write(var + TAB);
                }
                writer.write(NEWLINE);
                firstResult = false;
            }
            
            final long timestamp = System.currentTimeMillis();
            writeln(timestamp);
            
            for(Binding binding : bindings) {
                for(String varName : vars) {
                    final Var var  = Var.alloc(varName);
                    writer.write(binding.get(var).toString(false) + TAB);
                }
                writer.write(NEWLINE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void writeln(final Object string) throws IOException {
        writer.write(string + NEWLINE);
    }

}
