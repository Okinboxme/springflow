package dev.springflow.cli.util;

import dev.springflow.cli.commands.CliException;

import java.io.IOException;
import java.nio.file.Path;

public final class Processes {

    private Processes() {
    }

    public static boolean isWindows() {
        return System.getProperty("os.name", "")
                .toLowerCase()
                .contains("win");
    }

    public static String mavenCommand() {
        return isWindows() ? "mvn.cmd" : "mvn";
    }

    public static String npmCommand() {
        return isWindows() ? "npm.cmd" : "npm";
    }

    public static boolean isAvailable(String command) {

        try {

            Process process =
                    new ProcessBuilder(command, "--version")
                            .redirectErrorStream(true)
                            .start();

            return process.waitFor() == 0;

        } catch (Exception e) {
            return false;
        }
    }

    public static boolean mavenAvailable() {
        return isAvailable(mavenCommand());
    }

    public static boolean npmAvailable() {
        return isAvailable(npmCommand());
    }

    public static void requireMaven() {

        if (!mavenAvailable()) {
            throw new CliException(
                    "Maven is required to build the SpringFlow backend.\n"
                            + "Please install Maven and try again."
            );
        }
    }

    public static void requireNpm() {

        if (!npmAvailable()) {
            throw new CliException(
                    "Node.js/npm is required to run the SpringFlow frontend.\n"
                            + "Please install Node.js and try again."
            );
        }
    }

    public static int run(
            Path directory,
            String... command) {

        try {

            Process process =
                    new ProcessBuilder(command)
                            .directory(directory.toFile())
                            .inheritIO()
                            .start();

            return process.waitFor();

        } catch (IOException | InterruptedException e) {

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            throw new CliException(
                    "Failed to execute: "
                            + String.join(" ", command)
                            + "\n"
                            + e.getMessage()
            );
        }
    }

    public static int run(String... command) {
        return run(Path.of("."), command);
    }
}
