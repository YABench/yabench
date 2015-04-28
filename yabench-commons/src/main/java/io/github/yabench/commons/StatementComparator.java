package io.github.yabench.commons;

import com.hp.hpl.jena.rdf.model.Statement;
import java.util.Comparator;

public class StatementComparator implements Comparator<Statement> {

    @Override
    public int compare(Statement s1, Statement s2) {
        if(s1.getSubject().equals(s2.getSubject())) {
            if(s1.getPredicate().equals(s2.getPredicate())) {
                return s1.getObject().toString()
                        .compareToIgnoreCase(s2.getObject().toString());
            } else {
                return s1.getPredicate().toString()
                        .compareToIgnoreCase(s2.getPredicate().toString());
            }
        } else {
            return s1.getSubject().toString()
                    .compareToIgnoreCase(s2.getSubject().toString());
        }
    }

}
