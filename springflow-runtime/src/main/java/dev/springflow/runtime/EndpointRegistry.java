package dev.springflow.runtime;

import dev.springflow.core.annotation.Endpoint;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EndpointRegistry {

    private final ApplicationContext applicationContext;

    private final Map<String, Object> endpoints =
            new ConcurrentHashMap<>();

    public EndpointRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        discover();
    }

    private void discover() {

        Map<String, Object> beans =
                applicationContext.getBeansWithAnnotation(
                        Endpoint.class
                );

        beans.values().forEach(bean -> {

            String name =
                    bean.getClass()
                            .getSimpleName()
                            .replace("$$SpringCGLIB$$", "");

            endpoints.put(name, bean);
        });
    }

    public Object get(String name) {
        return endpoints.get(name);
    }

    public boolean contains(String name) {
        return endpoints.containsKey(name);
    }

    public Map<String, Object> all() {
        return Map.copyOf(endpoints);
    }
}
