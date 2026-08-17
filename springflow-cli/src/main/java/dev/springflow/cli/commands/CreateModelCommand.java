package dev.springflow.cli.commands;

import dev.springflow.cli.model.FieldDefinition;
import dev.springflow.cli.model.ModelDefinition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CreateModelCommand implements Command {

    @Override
    public void run(String[] args) {

        if (args.length < 3
                || !"model".equals(args[1])) {

            System.out.println(
                    "Usage: springflow create model <ModelName>"
            );

            return;
        }

        String modelName = args[2];

        createModel(modelName);
    }

    private static void createModel(String modelName) {

        System.out.println();
        System.out.println(
                "Creating model: " + modelName
        );

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

        Path modelsDirectory =
                Paths.get("models");

        Path modelFile =
                modelsDirectory.resolve(
                        modelName + ".json"
                );

        if (Files.exists(modelFile)) {

            System.out.println();
            System.out.println(
                    "Model already exists: "
                            + modelFile
            );

            return;
        }

        try {

            Files.createDirectories(modelsDirectory);

            ModelDefinition model =
                    interactiveModel(modelName);

            model.writeToFile(modelFile);

            System.out.println();
            System.out.println(
                    "Model created: " + modelFile
            );

            System.out.println();
            System.out.println(
                    "Generated fields:"
            );

            for (FieldDefinition field :
                    model.getFields()) {

                System.out.println(
                        "  - "
                                + field.getName()
                                + " ("
                                + field.getType()
                                + ")"
                );
            }

            System.out.println();
            System.out.println("Next steps:");
            System.out.println();
            System.out.println(
                    "  Edit: " + modelFile
            );
            System.out.println(
                    "  Generate: springflow generate all "
                            + modelName
            );
            System.out.println();

        } catch (IOException e) {

            throw new CliException(
                    "Failed to create model: "
                            + e.getMessage()
            );
        }
    }

    /**
     * Interactive model builder.
     */
    private static ModelDefinition interactiveModel(
            String modelName) {

        Scanner scanner =
                new Scanner(System.in);

        List<FieldDefinition> fields =
                new ArrayList<>();

        // Always add an id field
        FieldDefinition idField =
                new FieldDefinition("id", "Long");
        idField.setId(true);
        fields.add(idField);

        System.out.println();
        System.out.println(
                "Define fields for "
                        + modelName
                        + " (empty name to finish):"
        );
        System.out.println();

        while (true) {

            System.out.print(
                    "  Field name (or Enter to finish): "
            );

            String fieldName =
                    scanner.nextLine().trim();

            if (fieldName.isEmpty()) {
                break;
            }

            if (fieldName.contains(" ")
                    || fieldName.contains("-")) {

                System.out.println(
                        "  Invalid name. Use camelCase."
                );

                continue;
            }

            // Check for duplicate
            if (fields.stream()
                    .anyMatch(
                            f -> f.getName().equals(
                                    fieldName
                            ))) {

                System.out.println(
                        "  Field already exists."
                );

                continue;
            }

            System.out.print(
                    "  Type (String/Long/Integer/Double/boolean/LocalDate) [String]: "
            );

            String fieldType =
                    scanner.nextLine().trim();

            if (fieldType.isEmpty()) {
                fieldType = "String";
            }

            if (!isValidType(fieldType)) {

                System.out.println(
                        "  Unknown type: "
                                + fieldType
                );

                continue;
            }

            FieldDefinition field =
                    new FieldDefinition(
                            fieldName,
                            fieldType
                    );

            if (!"Long".equals(fieldType)
                    && !"boolean".equals(fieldType)) {

                System.out.print(
                        "  Required? (y/N): "
                );

                String req =
                        scanner.nextLine().trim();

                field.setRequired(
                        "y".equalsIgnoreCase(req)
                );
            }

            if ("String".equals(fieldType)) {

                System.out.print(
                        "  Max length (or Enter to skip): "
                );

                String maxStr =
                        scanner.nextLine().trim();

                if (!maxStr.isEmpty()) {

                    try {
                        field.setMax(
                                Integer.parseInt(maxStr)
                        );
                    } catch (NumberFormatException e) {
                        // skip
                    }
                }
            }

            fields.add(field);

            System.out.println(
                    "  Added: "
                            + fieldName
                            + " ("
                            + fieldType
                            + ")"
            );

            System.out.println();
        }

        return new ModelDefinition(
                modelName,
                fields
        );
    }

    private static boolean isValidType(String type) {

        return switch (type) {
            case "String",
                    "Long",
                    "Integer",
                    "Double",
                    "Float",
                    "boolean",
                    "Boolean",
                    "LocalDate",
                    "LocalDateTime",
                    "Instant" -> true;
            default -> false;
        };
    }
}
