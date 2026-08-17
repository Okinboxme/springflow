package dev.springflow.cli.commands;

import dev.springflow.cli.config.ConfigLoader;
import dev.springflow.cli.config.SpringFlowConfig;
import dev.springflow.cli.util.Processes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BuildCommand implements Command {

    @Override
    public void run(String[] args) {

        System.out.println();
        System.out.println("SpringFlow Build");
        System.out.println();

        System.out.println("✓ Validating project");

        GenerateCommand.runGeneration();

        SpringFlowConfig config =
                ConfigLoader.load();

        Path frontendDirectory =
                Path.of(config.frontendDirectory());

        Path packageJson =
                frontendDirectory.resolve("package.json");

        if (Files.exists(packageJson)) {

            System.out.println();
            System.out.println("Building frontend...");

            Processes.requireNpm();

            if (needsInstall(frontendDirectory, packageJson)) {

                System.out.println("Installing frontend dependencies...");

                int installExit =
                        Processes.run(
                                frontendDirectory,
                                Processes.npmCommand(),
                                "install"
                        );

                if (installExit != 0) {
                    throw new CliException(
                            "Frontend dependency installation failed with exit code "
                                    + installExit
                    );
                }
            }

            int frontendExit =
                    Processes.run(
                            frontendDirectory,
                            Processes.npmCommand(),
                            "run",
                            "build"
                    );

            if (frontendExit != 0) {
                throw new CliException(
                        "Frontend build failed with exit code "
                                + frontendExit
                );
            }

        } else {

            System.out.println();
            System.out.println(
                    "No frontend found, skipping frontend build."
            );
        }

        System.out.println();
        System.out.println("Building Spring Boot backend...");

        Processes.requireMaven();

        int backendExit =
                Processes.run(
                        Path.of("."),
                        Processes.mavenCommand(),
                        "package"
                );

        if (backendExit != 0) {
            throw new CliException(
                    "Backend build failed with exit code "
                            + backendExit
            );
        }

        System.out.println();
        System.out.println("✓ Build complete");
    }

    private static boolean needsInstall(
            Path frontendDirectory,
            Path packageJson) {

        Path nodeModules =
                frontendDirectory.resolve("node_modules");

        if (!Files.isDirectory(nodeModules)) {
            return true;
        }

        try {

            return Files.getLastModifiedTime(packageJson).toMillis()
                    > Files.getLastModifiedTime(nodeModules).toMillis();

        } catch (IOException e) {
            return true;
        }
    }
}
