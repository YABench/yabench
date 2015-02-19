import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import io.github.yabench.commons.NodeUtils;
import org.junit.Test;
import static org.junit.Assert.*;

public class NodeUtilsTest {
    
    @Test
    public void test() {
        Node node = NodeUtils.toNode("\"98.0\"^^<http://www.w3.org/2001/XMLSchema#float>");
        assertEquals(true, node.isLiteral());
        assertEquals(98.0f, node.getLiteralValue());
        assertEquals(XSDDatatype.XSDfloat, node.getLiteralDatatype());
    }
}
