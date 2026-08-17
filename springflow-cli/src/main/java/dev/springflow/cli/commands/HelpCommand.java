package dev.springflow.cli.commands;

public class HelpCommand implements Command {

    @Override
    public void run(String[] args) {

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
                "  springflow create model <ModelName>"
        );
        System.out.println(
                "      Create a model definition (interactive)"
        );
        System.out.println();
        System.out.println(
                "  springflow generate"
        );
        System.out.println(
                "      Generate TypeScript clients from SpringFlow annotations"
        );
        System.out.println();
        System.out.println(
                "  springflow generate all <ModelName> [--dry-run] [--no-backup]"
        );
        System.out.println(
                "      Generate full stack from a model definition"
        );
        System.out.println();
        System.out.println(
                "  springflow dev"
        );
        System.out.println(
                "      Run the backend and frontend in development mode"
        );
        System.out.println();
        System.out.println(
                "  springflow build"
        );
        System.out.println(
                "      Build the backend and frontend"
        );
        System.out.println();
        System.out.println(
                "  springflow version"
        );
        System.out.println(
                "      Show SpringFlow version"
        );
        System.out.println();
        System.out.println(
                "  springflow help"
        );
        System.out.println(
                "      Show this help"
        );
        System.out.println();
    }
}
