package dev.springflow.cli;

import dev.springflow.cli.commands.BuildCommand;
import dev.springflow.cli.commands.CliException;
import dev.springflow.cli.commands.Command;
import dev.springflow.cli.commands.CreateCommand;
import dev.springflow.cli.commands.CreateModelCommand;
import dev.springflow.cli.commands.DevCommand;
import dev.springflow.cli.commands.GenerateAllCommand;
import dev.springflow.cli.commands.GenerateCommand;
import dev.springflow.cli.commands.HelpCommand;
import dev.springflow.cli.commands.VersionCommand;

public class SpringFlowCLI {

    public static void main(String[] args) {

        if (args.length == 0) {

            new HelpCommand().run(args);

            return;
        }

        String command = args[0];

        Command handler =
                switch (command) {

                    case "create" ->

                            isModelCreate(args)
                                    ? new CreateModelCommand()
                                    : new CreateCommand();

                    case "dev" -> new DevCommand();
                    case "build" -> new BuildCommand();

                    case "generate" ->

                            isGenerateAll(args)
                                    ? new GenerateAllCommand()
                                    : new GenerateCommand();

                    case "version", "--version", "-v" ->
                            new VersionCommand();

                    case "help", "--help", "-h" ->
                            new HelpCommand();

                    default -> null;
                };

        if (handler == null) {

            System.out.println(
                    "Unknown command: " + command
            );

            System.out.println();

            new HelpCommand().run(args);

            return;
        }

        try {

            handler.run(args);

        } catch (CliException e) {

            System.out.println();
            System.out.println(
                    "Error: " + e.getMessage()
            );

            System.exit(1);
        }
    }

    private static boolean isModelCreate(
            String[] args) {

        return args.length >= 2
                && "model".equals(args[1]);
    }

    private static boolean isGenerateAll(
            String[] args) {

        return args.length >= 2
                && "all".equals(args[1]);
    }
}
