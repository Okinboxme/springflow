package dev.springflow.runtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@RestController
@RequestMapping("/springflow")
@CrossOrigin(
        origins = {
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        }
)
public class SpringFlowController {

    private final EndpointRegistry registry;
    private final ObjectMapper objectMapper;

    public SpringFlowController(
            EndpointRegistry registry,
            ObjectMapper objectMapper) {

        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    // ============================================================
    // GET
    // ============================================================

    @GetMapping("/{endpoint}/{method}")
    public Object invokeGet(
            @PathVariable("endpoint") String endpoint,
            @PathVariable("method") String method,
            @RequestParam(name = "") Map<String, String> params) {

        Object target = getEndpoint(endpoint);

        Method selected =
                findGetMethod(target, method, params);

        if (selected == null) {
            throw new IllegalArgumentException(
                    "SpringFlow method not found: "
                            + endpoint + "." + method
            );
        }

        Object[] arguments =
                buildArguments(selected, params);

        return invoke(
                target,
                selected,
                arguments
        );
    }

    // ============================================================
    // POST JSON
    // ============================================================

    @PostMapping(
            value = "/{endpoint}/{method}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public Object invokePost(
            @PathVariable("endpoint") String endpoint,
            @PathVariable("method") String method,
            @RequestBody(required = false) String body) {

        Object target = getEndpoint(endpoint);

        Method selected =
                findPostMethod(target, method);

        if (selected == null) {
            throw new IllegalArgumentException(
                    "SpringFlow POST method not found: "
                            + endpoint + "." + method
            );
        }

        Object[] arguments =
                buildBodyArguments(
                        selected,
                        body
                );

        return invoke(
                target,
                selected,
                arguments
        );
    }

    // ============================================================
    // ENDPOINT LIST
    // ============================================================

    @GetMapping("/endpoints")
    public Map<String, Object> endpoints() {
        return registry.all();
    }

    // ============================================================
    // FIND ENDPOINT
    // ============================================================

    private Object getEndpoint(String endpoint) {

        Object target =
                registry.get(endpoint);

        if (target == null) {
            throw new IllegalArgumentException(
                    "SpringFlow endpoint not found: "
                            + endpoint
            );
        }

        return target;
    }

    // ============================================================
    // FIND GET METHOD
    // ============================================================

    private Method findGetMethod(
            Object target,
            String name,
            Map<String, String> params) {

        for (Method method :
                target.getClass().getMethods()) {

            if (!method.getName().equals(name)) {
                continue;
            }

            if (method.getParameterCount()
                    != params.size()) {

                continue;
            }

            return method;
        }

        return null;
    }

    // ============================================================
    // FIND POST METHOD
    // ============================================================

    private Method findPostMethod(
            Object target,
            String name) {

        for (Method method :
                target.getClass().getMethods()) {

            if (!method.getName().equals(name)) {
                continue;
            }

            if (method.getParameterCount() == 1) {
                return method;
            }
        }

        return null;
    }

    // ============================================================
    // GET PARAMETERS
    // ============================================================

    private Object[] buildArguments(
            Method method,
            Map<String, String> params) {

        var parameters =
                method.getParameters();

        Object[] arguments =
                new Object[parameters.length];

        for (int i = 0;
             i < parameters.length;
             i++) {

            var parameter =
                    parameters[i];

            String name =
                    parameter.getName();

            String value =
                    params.get(name);

            if (value == null) {
                throw new IllegalArgumentException(
                        "Missing parameter: "
                                + name
                );
            }

            arguments[i] =
                    convert(
                            value,
                            parameter.getType()
                    );
        }

        return arguments;
    }

    // ============================================================
    // POST BODY
    // ============================================================

    private Object[] buildBodyArguments(
            Method method,
            String body) {

        var parameters =
                method.getParameters();

        if (parameters.length == 0) {
            return new Object[0];
        }

        if (parameters.length != 1) {
            throw new IllegalArgumentException(
                    "SpringFlow POST methods currently "
                            + "support one JSON body parameter"
            );
        }

        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException(
                    "Request body is required"
            );
        }

        Class<?> parameterType =
                parameters[0].getType();

        try {

            Object argument =
                    objectMapper.readValue(
                            body,
                            parameterType
                    );

            return new Object[]{
                    argument
            };

        } catch (JsonProcessingException e) {

            throw new IllegalArgumentException(
                    "Invalid JSON request body",
                    e
            );
        }
    }

    // ============================================================
    // INVOKE JAVA METHOD
    // ============================================================

    private Object invoke(
            Object target,
            Method method,
            Object[] arguments) {

        try {

            return method.invoke(
                    target,
                    arguments
            );

        } catch (InvocationTargetException e) {

            Throwable cause =
                    e.getCause();

            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }

            throw new RuntimeException(cause);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    // ============================================================
    // TYPE CONVERSION
    // ============================================================

    private Object convert(
            String value,
            Class<?> type) {

        if (type == String.class) {
            return value;
        }

        if (type == int.class
                || type == Integer.class) {

            return Integer.parseInt(value);
        }

        if (type == long.class
                || type == Long.class) {

            return Long.parseLong(value);
        }

        if (type == double.class
                || type == Double.class) {

            return Double.parseDouble(value);
        }

        if (type == float.class
                || type == Float.class) {

            return Float.parseFloat(value);
        }

        if (type == short.class
                || type == Short.class) {

            return Short.parseShort(value);
        }

        if (type == byte.class
                || type == Byte.class) {

            return Byte.parseByte(value);
        }

        if (type == boolean.class
                || type == Boolean.class) {

            return Boolean.parseBoolean(value);
        }

        if (type == char.class
                || type == Character.class) {

            if (value.length() != 1) {
                throw new IllegalArgumentException(
                        "Invalid character value: "
                                + value
                );
            }

            return value.charAt(0);
        }

        throw new IllegalArgumentException(
                "Unsupported parameter type: "
                        + type.getName()
        );
    }
}