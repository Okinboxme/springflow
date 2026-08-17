package dev.springflow.cli.config;

public record SpringFlowConfig(
        String name,
        int backendPort,
        String frontendDirectory,
        int frontendPort,
        String apiDirectory
) {

    public static final int DEFAULT_BACKEND_PORT = 8080;
    public static final int DEFAULT_FRONTEND_PORT = 5173;
    public static final String DEFAULT_FRONTEND_DIRECTORY = "frontend";
    public static final String DEFAULT_API_DIRECTORY = "frontend/src/api";

    public static SpringFlowConfig defaults() {
        return new SpringFlowConfig(
                "",
                DEFAULT_BACKEND_PORT,
                DEFAULT_FRONTEND_DIRECTORY,
                DEFAULT_FRONTEND_PORT,
                DEFAULT_API_DIRECTORY
        );
    }

    public SpringFlowConfig withDefaults() {

        String name =
                this.name == null ? "" : this.name;

        int backendPort =
                this.backendPort <= 0
                        ? DEFAULT_BACKEND_PORT
                        : this.backendPort;

        String frontendDirectory =
                this.frontendDirectory == null
                        || this.frontendDirectory.isBlank()
                        ? DEFAULT_FRONTEND_DIRECTORY
                        : this.frontendDirectory;

        int frontendPort =
                this.frontendPort <= 0
                        ? DEFAULT_FRONTEND_PORT
                        : this.frontendPort;

        String apiDirectory =
                this.apiDirectory == null
                        || this.apiDirectory.isBlank()
                        ? frontendDirectory + "/src/api"
                        : this.apiDirectory;

        return new SpringFlowConfig(
                name,
                backendPort,
                frontendDirectory,
                frontendPort,
                apiDirectory
        );
    }
}
