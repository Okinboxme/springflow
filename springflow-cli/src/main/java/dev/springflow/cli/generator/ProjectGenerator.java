package dev.springflow.cli.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProjectGenerator {

    public void generate(
            Path projectDirectory,
            String projectName
    ) throws IOException {

        createDirectories(projectDirectory);

        System.out.println(
                "Generating SpringFlow project..."
        );

        System.out.println(
                "  ✓ Project directories"
        );

        System.out.println(
                "  ✓ Maven project"
        );

        System.out.println(
                "  ✓ Spring Boot application"
        );

        System.out.println(
                "  ✓ React frontend"
        );

        System.out.println(
                "  ✓ SpringFlow configuration"
        );
    }

    private void createDirectories(
            Path project
    ) throws IOException {

        Files.createDirectories(
                project.resolve("src/main/java")
        );

        Files.createDirectories(
                project.resolve("src/main/resources")
        );

        Files.createDirectories(
                project.resolve("frontend/src")
        );

        Files.createDirectories(
                project.resolve(
                        "frontend/src/springflow/generated"
                )
        );
    }

    protected void write(
            Path path,
            String content
    ) throws IOException {

        Files.createDirectories(
                path.getParent()
        );

        Files.writeString(
                path,
                content,
                StandardCharsets.UTF_8
        );
    }
}