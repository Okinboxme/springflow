package dev.springflow.cli.commands;

import dev.springflow.cli.generator.FileGenerator;
import dev.springflow.cli.model.ModelDefinition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class GenerateAllCommand implements Command {

    @Override
    public void run(String[] args) {

        if (args.length < 3
                || !"all".equals(args[1])) {

            System.out.println(
                    "Usage: springflow generate all <ModelName> [--dry-run] [--no-backup]"
            );

            return;
        }

        String modelName = args[2];

        boolean dryRun = hasFlag(args, "--dry-run");
        boolean noBackup = hasFlag(args, "--no-backup");

        generateAll(
                modelName,
                dryRun,
                !noBackup
        );
    }

    private static void generateAll(
            String modelName,
            boolean dryRun,
            boolean backupEnabled) {

        System.out.println();
        System.out.println(
                "SpringFlow Model-Driven Generation"
        );
        System.out.println();

        if (!Files.exists(Paths.get("pom.xml"))
                && !Files.exists(
                        Paths.get(
                                "springflow.config.json"
                        ))) {

            throw new CliException(
                    "No SpringFlow project found.\n\n"
                            + "Run this command from a "
                            + "SpringFlow project root."
            );
        }

        Path modelFile =
                Paths.get("models")
                        .resolve(modelName + ".json");

        if (!Files.exists(modelFile)) {

            throw new CliException(
                    "Model not found: "
                            + modelFile
                            + "\n\n"
                            + "Create it with:\n"
                            + "  springflow create model "
                            + modelName
            );
        }

        try {

            System.out.println(
                    "Model: " + modelFile
            );

            if (dryRun) {
                System.out.println(
                        "Mode: dry-run (no files modified)"
                );
            }

            if (!backupEnabled) {
                System.out.println(
                        "Backup: disabled"
                );
            }

            System.out.println();

            ModelDefinition model =
                    ModelDefinition.load(modelFile);

            System.out.println(
                    "Loaded model: "
                            + model.getName()
                            + " ("
                            + model.getFields().size()
                            + " fields)"
            );

            System.out.println();

            FileGenerator generator =
                    new FileGenerator(
                            Paths.get("."),
                            backupEnabled,
                            dryRun
                    );

            List<FileGenerator.PlannedFile> files =
                    generator.generate(model);

            System.out.println();

            if (dryRun) {

                System.out.println(
                        "Files that would be generated:"
                );

                for (FileGenerator.PlannedFile file :
                        files) {

                    System.out.println(
                            "  + " + file.relativePath()
                    );
                }

                System.out.println();
                System.out.println(
                        "Total: "
                                + files.size()
                                + " files"
                );

            } else {

                System.out.println(
                        "Generated "
                                + files.size()
                                + " files"
                );
            }

            System.out.println();
            System.out.println("Next steps:");
            System.out.println();
            System.out.println(
                    "  springflow generate"
            );
            System.out.println(
                    "  springflow dev"
            );
            System.out.println();

        } catch (IOException e) {

            throw new CliException(
                    "Generation failed: "
                            + e.getMessage()
            );
        }
    }

    private static boolean hasFlag(
            String[] args,
            String flag) {

        for (String arg : args) {
            if (flag.equals(arg)) {
                return true;
            }
        }

        return false;
    }
}
