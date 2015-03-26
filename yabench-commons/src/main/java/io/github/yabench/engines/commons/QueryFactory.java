package io.github.yabench.engines.commons;

import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Set;
import org.reflections.Reflections;

public class QueryFactory {

    public Query create(String queryString) throws Exception {
        Reflections reflections = new Reflections("io.github.yabench.engines");
        Set<Class<? extends Query>> classes = reflections.getSubTypesOf(Query.class);
        Optional<Class<? extends Query>> r = classes.stream()
                .filter((clazz) -> !Modifier.isAbstract(clazz.getModifiers()))
                .findFirst();
        if (r.isPresent()) {
            return r.get()
                    .getConstructor(String.class)
                    .newInstance(queryString);
        } else {
            return null;
        }
    }

}
