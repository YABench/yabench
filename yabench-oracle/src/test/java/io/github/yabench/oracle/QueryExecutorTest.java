package io.github.yabench.oracle;

import com.hp.hpl.jena.sparql.core.Var;
import io.github.yabench.commons.TemporalRDFReader;
import io.github.yabench.commons.TemporalTriple;
import io.github.yabench.commons.tests.TestUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import static org.junit.Assert.*;
import org.junit.Test;

public class QueryExecutorTest {

    private static final TestUtils utils = new TestUtils(QueryExecutorTest.class);

    @Test
    public void testAvgOneVar() throws IOException {
        final String TEST_PREFIX = "testAvgOneVar/";
        QueryExecutor qexec = new QueryExecutor(
                utils.readToString(TEST_PREFIX + "query.template"),
                new Properties() {
                    {
                        put("TEMP", "50.0");
                    }
                });

        TripleWindow inputs = readToTripleWindow(
                TEST_PREFIX + "input.stream", 60000, 75445);

        BindingWindow results = qexec.executeSelect(inputs);

        System.out.println(results.getBindings().size());
        System.out.println(results.getBindings().isEmpty());

        assertEquals(1, results.getBindings().size());
        assertEquals(null, results.getBindings().get(0).get(Var.alloc("avg")));
    }

    @Test
    public void testAvgTwoVar() throws IOException {
        final String TEST_PREFIX = "testAvgTwoVar/";
        QueryExecutor qexec = new QueryExecutor(
                utils.readToString(TEST_PREFIX + "query.template"),
                new Properties() {
                    {
                        put("TEMP", "50.0");
                    }
                });

        TripleWindow inputs = readToTripleWindow(
                TEST_PREFIX + "input.stream", 60000, 75445);

        BindingWindow results = qexec.executeSelect(inputs);

        System.out.println(results);

        assertEquals(1, results.getBindings().size());
        assertEquals(null, results.getBindings().get(0).get(Var.alloc("avg")));
    }

    private TripleWindow readToTripleWindow(String fileName, long start, long end)
            throws IOException {
        TemporalRDFReader reader = new TemporalRDFReader(utils.readToReader(fileName));

        List<TemporalTriple> triples = new ArrayList<>();
        TemporalTriple triple;
        while ((triple = reader.readNextTriple()) != null) {
            if (triple.getTime() >= start) {
                if (triple.getTime() <= end) {
                    triples.add(triple);
                } else {
                    break;
                }
            }
        }

        return new TripleWindow(triples, start, end);
    }

}
