package dev.springflow.cli.config;

import dev.springflow.cli.util.Json;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public final class ConfigLoader {

    private static final String CONFIG_FILE =
            "springflow.config.json";

    private ConfigLoader() {
    }

    public static SpringFlowConfig load() {
        return load(Paths.get(CONFIG_FILE));
    }

    public static SpringFlowConfig load(Path configPath) {

        SpringFlowConfig config =
                SpringFlowConfig.defaults();

        if (!Files.exists(configPath)) {
            return config;
        }

        try {

            Map<String, Object> root =
                    Json.parse(
                            Files.readString(configPath)
                    );

            String name =
                    string(root.get("name"));

            int backendPort =
                    port(nested(root, "backend"), "port");

            Object frontend =
                    root.get("frontend");

            String frontendDirectory =
                    SpringFlowConfig.DEFAULT_FRONTEND_DIRECTORY;

            int frontendPort =
                    SpringFlowConfig.DEFAULT_FRONTEND_PORT;

            if (frontend instanceof Map<?, ?> frontendMap) {

                frontendDirectory =
                        string(frontendMap.get("directory"));

                frontendPort =
                        port(frontendMap, "port");

            } else if (frontend instanceof String legacy) {

                // Legacy config: "frontend": "frontend"
                frontendDirectory = legacy;
            }

            String apiDirectory =
                    string(nested(root, "springflow")
                            .get("apiDirectory"));

            if (apiDirectory == null) {

                // Legacy config: "generated": "frontend/src/..."
                Object generated =
                        root.get("generated");

                if (generated instanceof String legacy) {
                    apiDirectory = legacy;
                }
            }

            return new SpringFlowConfig(
                    name,
                    backendPort,
                    frontendDirectory,
                    frontendPort,
                    apiDirectory
            ).withDefaults();

        } catch (Exception e) {

            return SpringFlowConfig.defaults();
        }
    }

    private static String string(Object value) {
        return value instanceof String s ? s : null;
    }

    private static int port(
            Map<?, ?> map,
            String key) {

        Object value =
                map.get(key);

        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value instanceof String text) {

            try {
                return Integer.parseInt(
                        text.trim()
                );
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        return 0;
    }

    @SuppressWarnings("unchecked")
    private static Map<?, ?> nested(
            Map<String, Object> root,
            String key) {

        Object value =
                root.get(key);

        return value instanceof Map
                ? (Map<?, ?>) value
                : Map.of();
    }
}
