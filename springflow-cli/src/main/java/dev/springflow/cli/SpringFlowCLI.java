package dev.springflow.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SpringFlowCLI {

    public static void main(String[] args) {

        if (args.length == 0) {
            printHelp();
            return;
        }

        String command = args[0];

        switch (command) {

            case "create" -> {

                if (args.length < 2) {
                    System.out.println(
                            "Usage: springflow create <project-name>"
                    );
                    return;
                }

                createProject(args[1]);
            }

            case "--version", "-v" -> {

                System.out.println(
                        "SpringFlow CLI 0.1.0"
                );
            }

            case "--help", "-h", "help" -> {

                printHelp();
            }

            default -> {

                System.out.println(
                        "Unknown command: " + command
                );

                System.out.println();

                printHelp();
            }
        }
    }

    private static void printHelp() {

        System.out.println();
        System.out.println("SpringFlow CLI");
        System.out.println();
        System.out.println("Usage:");
        System.out.println();
        System.out.println(
                "  springflow create <project-name>"
        );
        System.out.println(
                "      Create a new SpringFlow application"
        );
        System.out.println();
        System.out.println(
                "  springflow --version"
        );
        System.out.println(
                "      Show SpringFlow version"
        );
        System.out.println();
    }

    private static void createProject(
            String projectName) {

        System.out.println();
        System.out.println(
                "Creating SpringFlow application: "
                        + projectName
        );

        Path projectDirectory =
                Paths.get(projectName)
                        .toAbsolutePath()
                        .normalize();

        if (Files.exists(projectDirectory)) {

            System.out.println();

            System.out.println(
                    "ERROR: Directory already exists:"
            );

            System.out.println(
                    projectDirectory
            );

            return;
        }

        try {

            createDirectories(
                    projectDirectory
            );

            createPom(
                    projectDirectory,
                    projectName
            );

            createApplication(
                    projectDirectory,
                    projectName
            );

            createFrontend(
                    projectDirectory,
                    projectName
            );

            createSpringFlowConfig(
                    projectDirectory,
                    projectName
            );

            System.out.println();
            System.out.println(
                    "SpringFlow application created successfully."
            );

            System.out.println();

            System.out.println(
                    "Project:"
            );

            System.out.println(
                    "  "
                            + projectDirectory
            );

            System.out.println();

            System.out.println(
                    "Next steps:"
            );

            System.out.println(
                    "  cd "
                            + projectName
            );

            System.out.println(
                    "  mvn spring-boot:run"
            );

            System.out.println();

            System.out.println(
                    "Frontend:"
            );

            System.out.println(
                    "  cd frontend"
            );

            System.out.println(
                    "  npm install"
            );

            System.out.println(
                    "  npm run dev"
            );

            System.out.println();

        } catch (IOException e) {

            System.err.println();

            System.err.println(
                    "Failed to create SpringFlow project:"
            );

            System.err.println(
                    e.getMessage()
            );
        }
    }

    private static void createDirectories(
            Path project) throws IOException {

        Files.createDirectories(
                project.resolve(
                        "src/main/java"
                )
        );

        Files.createDirectories(
                project.resolve(
                        "src/main/resources"
                )
        );

        Files.createDirectories(
                project.resolve(
                        "frontend/src"
                )
        );

        Files.createDirectories(
                project.resolve(
                        "frontend/src/springflow/generated"
                )
        );
    }

    private static void createPom(
            Path project,
            String projectName)
            throws IOException {

        String artifactId =
                sanitizeArtifactId(
                        projectName
                );

        String content =
                """
                <?xml version="1.0" encoding="UTF-8"?>

                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="
                           http://maven.apache.org/POM/4.0.0
                           https://maven.apache.org/xsd/maven-4.0.0.xsd">

                    <modelVersion>4.0.0</modelVersion>

                    <groupId>com.example</groupId>
                    <artifactId>%s</artifactId>
                    <version>0.1.0-SNAPSHOT</version>

                    <properties>
                        <java.version>21</java.version>
                        <spring-boot.version>3.5.7</spring-boot.version>
                        <springflow.version>0.1.0-SNAPSHOT</springflow.version>
                    </properties>

                    <dependencies>

                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-web</artifactId>
                            <version>${spring-boot.version}</version>
                        </dependency>

                        <dependency>
                            <groupId>dev.springflow</groupId>
                            <artifactId>springflow-core</artifactId>
                            <version>${springflow.version}</version>
                        </dependency>

                        <dependency>
                            <groupId>dev.springflow</groupId>
                            <artifactId>springflow-runtime</artifactId>
                            <version>${springflow.version}</version>
                        </dependency>

                        <dependency>
                            <groupId>dev.springflow</groupId>
                            <artifactId>springflow-boot-starter</artifactId>
                            <version>${springflow.version}</version>
                        </dependency>

                    </dependencies>

                    <build>

                        <plugins>

                            <plugin>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-maven-plugin</artifactId>
                                <version>${spring-boot.version}</version>
                            </plugin>

                            <plugin>
                                <groupId>org.apache.maven.plugins</groupId>
                                <artifactId>maven-compiler-plugin</artifactId>
                                <version>3.14.0</version>

                                <configuration>

                                    <release>21</release>

                                    <parameters>true</parameters>

                                    <annotationProcessorPaths>

                                        <path>
                                            <groupId>dev.springflow</groupId>
                                            <artifactId>springflow-processor</artifactId>
                                            <version>${springflow.version}</version>
                                        </path>

                                    </annotationProcessorPaths>

                                </configuration>

                            </plugin>

                        </plugins>

                    </build>

                </project>
                """.formatted(
                        artifactId
                );

        write(
                project.resolve("pom.xml"),
                content
        );
    }

    private static void createApplication(
            Path project,
            String projectName)
            throws IOException {

        String packageName =
                "com.example."
                        + sanitizeJavaName(
                                projectName
                        );

        String content =
                """
                package %s;

                import org.springframework.boot.SpringApplication;
                import org.springframework.boot.autoconfigure.SpringBootApplication;

                @SpringBootApplication
                public class Application {

                    public static void main(String[] args) {

                        SpringApplication.run(
                                Application.class,
                                args
                        );
                    }
                }
                """.formatted(
                        packageName
                );

        Path packageDirectory =
                project.resolve(
                        "src/main/java/"
                                + packageName.replace(
                                        '.',
                                        '/'
                                )
                );

        Files.createDirectories(
                packageDirectory
        );

        write(
                packageDirectory.resolve(
                        "Application.java"
                ),
                content
        );
    }

    private static void createFrontend(
            Path project,
            String projectName)
            throws IOException {

        Path frontend =
                project.resolve("frontend");

        write(
                frontend.resolve("package.json"),
                """
                {
                  "name": "%s-frontend",
                  "private": true,
                  "version": "0.1.0",
                  "type": "module",
                  "scripts": {
                    "dev": "vite",
                    "build": "tsc -b && vite build",
                    "preview": "vite preview"
                  },
                  "dependencies": {
                    "@vitejs/plugin-react": "^5.0.0",
                    "react": "^19.1.0",
                    "react-dom": "^19.1.0"
                  },
                  "devDependencies": {
                    "typescript": "^5.8.3",
                    "vite": "^7.0.0"
                  }
                }
                """.formatted(
                        sanitizeNpmName(
                                projectName
                        )
                )
        );

        write(
                frontend.resolve("index.html"),
                """
                <!doctype html>
                <html lang="en">

                <head>

                    <meta charset="UTF-8" />

                    <meta
                        name="viewport"
                        content="width=device-width, initial-scale=1.0"
                    />

                    <title>SpringFlow</title>

                </head>

                <body>

                    <div id="root"></div>

                    <script
                        type="module"
                        src="/src/main.tsx">
                    </script>

                </body>

                </html>
                """
        );

        write(
                frontend.resolve("src/main.tsx"),
                """
                import React from "react";
                import ReactDOM from "react-dom/client";

                import App from "./App";

                ReactDOM.createRoot(
                    document.getElementById("root")!
                ).render(
                    <React.StrictMode>
                        <App />
                    </React.StrictMode>
                );
                """
        );

        write(
                frontend.resolve("src/App.tsx"),
                """
                function App() {

                    return (
                        <div
                            style={{
                                minHeight: "100vh",
                                padding: "40px",
                                fontFamily: "Arial"
                            }}
                        >

                            <h1>
                                SpringFlow
                            </h1>

                            <p>
                                Java → TypeScript → React
                            </p>

                            <p>
                                Your SpringFlow application is ready.
                            </p>

                        </div>
                    );
                }

                export default App;
                """
        );

        write(
                frontend.resolve("vite.config.ts"),
                """
                import { defineConfig } from "vite";
                import react from "@vitejs/plugin-react";

                export default defineConfig({

                    plugins: [
                        react()
                    ],

                    server: {

                        port: 5173,

                        proxy: {

                            "/springflow": {

                                target:
                                    "http://localhost:8080",

                                changeOrigin: true
                            }
                        }
                    }
                });
                """
        );

        write(
                frontend.resolve("tsconfig.json"),
                """
                {
                  "files": [],
                  "references": [
                    {
                      "path": "./tsconfig.app.json"
                    },
                    {
                      "path": "./tsconfig.node.json"
                    }
                  ]
                }
                """
        );

        write(
                frontend.resolve("tsconfig.app.json"),
                """
                {
                  "compilerOptions": {
                    "tsBuildInfoFile": "./node_modules/.tmp/tsconfig.app.tsbuildinfo",
                    "target": "ES2022",
                    "useDefineForClassFields": true,
                    "lib": ["ES2022", "DOM", "DOM.Iterable"],
                    "module": "ESNext",
                    "skipLibCheck": true,
                    "moduleResolution": "Bundler",
                    "allowImportingTsExtensions": true,
                    "verbatimModuleSyntax": true,
                    "moduleDetection": "force",
                    "noEmit": true,
                    "jsx": "react-jsx",
                    "strict": true,
                    "noUnusedLocals": false,
                    "noUnusedParameters": false
                  },
                  "include": ["src"]
                }
                """
        );

        write(
                frontend.resolve("tsconfig.node.json"),
                """
                {
                  "compilerOptions": {
                    "tsBuildInfoFile": "./node_modules/.tmp/tsconfig.node.tsbuildinfo",
                    "target": "ES2023",
                    "lib": ["ES2023"],
                    "module": "ESNext",
                    "skipLibCheck": true,
                    "moduleResolution": "Bundler",
                    "allowImportingTsExtensions": true,
                    "verbatimModuleSyntax": true,
                    "moduleDetection": "force",
                    "noEmit": true,
                    "strict": true
                  },
                  "include": ["vite.config.ts"]
                }
                """
        );
    }

    private static void createSpringFlowConfig(
            Path project,
            String projectName)
            throws IOException {

        String content =
                """
                {
                  "name": "%s",
                  "springflow": "0.1.0-SNAPSHOT",
                  "frontend": "frontend",
                  "generated": "frontend/src/springflow/generated"
                }
                """.formatted(
                        projectName
                );

        write(
                project.resolve(
                        "springflow.config.json"
                ),
                content
        );
    }

    private static void write(
            Path path,
            String content)
            throws IOException {

        Files.createDirectories(
                path.getParent()
        );

        Files.writeString(
                path,
                content,
                StandardCharsets.UTF_8
        );
    }

    private static String sanitizeArtifactId(
            String name) {

        return name
                .toLowerCase()
                .replaceAll(
                        "[^a-z0-9-]",
                        "-"
                )
                .replaceAll(
                        "-+",
                        "-"
                )
                .replaceAll(
                        "^-|-$",
                        ""
                );
    }

    private static String sanitizeJavaName(
            String name) {

        String result =
                name.replaceAll(
                        "[^a-zA-Z0-9]",
                        ""
                );

        if (result.isEmpty()) {
            result = "app";
        }

        if (!Character.isJavaIdentifierStart(
                result.charAt(0))) {

            result = "app" + result;
        }

        return result.toLowerCase();
    }

    private static String sanitizeNpmName(
            String name) {

        return name
                .toLowerCase()
                .replaceAll(
                        "[^a-z0-9-_]",
                        "-"
                );
    }
}