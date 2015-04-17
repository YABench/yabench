package io.github.yabench.commons;

import com.hp.hpl.jena.rdf.model.Statement;
import java.util.Comparator;

public class StatementComparator implements Comparator<Statement> {

    @Override
    public int compare(Statement s1, Statement s2) {
        if (s1.getPredicate() == null && s2.getPredicate() == null) {
            return 0;
        }

        if (s1.getPredicate().toString().compareTo(
                s2.getPredicate().toString()) == 0) {
            return s1.getObject().toString().compareTo(
                    s2.getObject().toString());
        } else {
            return s1.getPredicate().toString().compareTo(
                    s2.getPredicate().toString());
        }
    }

}
