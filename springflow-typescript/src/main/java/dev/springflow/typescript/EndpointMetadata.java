package dev.springflow.typescript;

import java.util.List;

public record EndpointMetadata(
        String className,
        String packageName,
        List<MethodMetadata> methods
) {
}
