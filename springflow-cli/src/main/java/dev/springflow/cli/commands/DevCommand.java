package dev.springflow.cli.commands;

import dev.springflow.cli.config.ConfigLoader;
import dev.springflow.cli.config.SpringFlowConfig;
import dev.springflow.cli.util.Processes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DevCommand implements Command {

    @Override
    public void run(String[] args) {

        System.out.println();
        System.out.println("SpringFlow Development Server");
        System.out.println();

        GenerateCommand.runGeneration();

        SpringFlowConfig config =
                ConfigLoader.load();

        Path frontendDirectory =
                Path.of(config.frontendDirectory());

        if (!Files.isDirectory(frontendDirectory)) {

            throw new CliException(
                    "Error: Frontend directory does not exist.\n\n"
                            + "Expected:\n"
                            + "  "
                            + frontendDirectory
                            + "/\n\n"
                            + "Run:\n"
                            + "  springflow create <project-name>"
            );
        }

        Processes.requireNpm();

        System.out.println();
        System.out.println("Backend:");
        System.out.println(
                "  http://localhost:"
                        + config.backendPort()
        );
        System.out.println();
        System.out.println("Frontend:");
        System.out.println(
                "  http://localhost:"
                        + config.frontendPort()
        );
        System.out.println();
        System.out.println(
                "SpringFlow development server running..."
        );
        System.out.println();

        List<Process> processes =
                new ArrayList<>();

        Runtime.getRuntime().addShutdownHook(
                new Thread(() ->
                        processes.forEach(Process::destroy)
                )
        );

        Process backend =
                start(
                        Path.of("."),
                        Processes.mavenCommand(),
                        "spring-boot:run",
                        "-Dspring-boot.run.arguments=--server.port="
                                + config.backendPort()
                );

        processes.add(backend);

        Process frontend =
                start(
                        frontendDirectory,
                        Processes.npmCommand(),
                        "run",
                        "dev",
                        "--",
                        "--port",
                        String.valueOf(config.frontendPort())
                );

        processes.add(frontend);

        try {

            backend.waitFor();
            frontend.waitFor();

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            processes.forEach(Process::destroy);
        }
    }

    private static Process start(
            Path directory,
            String... command) {

        try {

            return new ProcessBuilder(command)
                    .directory(directory.toFile())
                    .inheritIO()
                    .start();

        } catch (IOException e) {

            throw new CliException(
                    "Failed to start: "
                            + String.join(" ", command)
                            + "\n"
                            + e.getMessage()
            );
        }
    }
}
