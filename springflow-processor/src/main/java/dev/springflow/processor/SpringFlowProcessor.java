package dev.springflow.processor;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import dev.springflow.core.annotation.Endpoint;
import dev.springflow.core.annotation.EndpointMethod;
import dev.springflow.processor.generator.SpringFlowClientGenerator;
import dev.springflow.processor.generator.TypeScriptGenerator;

@SupportedAnnotationTypes({
        "dev.springflow.core.annotation.Endpoint"
})
@SupportedSourceVersion(
        javax.lang.model.SourceVersion.RELEASE_21
)
public class SpringFlowProcessor
        extends AbstractProcessor {

    private static final String GENERATED_DIRECTORY =
            "springflow/generated";

    /*
     * Shared TypeScript client is generated once
     * per compilation.
     */
    private boolean clientGenerated = false;

    /*
     * Physical generated files.
     *
     * Protects against Java Filer attempting to
     * create the same resource twice.
     */
    private final Set<String> generatedFiles =
            new HashSet<>();

    /*
     * Java model qualified names that have already
     * been processed.
     *
     * Example:
     *
     * dev.springflow.demo.employees.Employee
     */
    private final Set<String> generatedModels =
            new HashSet<>();

    @Override
    public boolean process(
            Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {

        if (roundEnv.processingOver()) {
            return false;
        }

        TypeScriptGenerator generator =
                new TypeScriptGenerator(
                        processingEnv.getTypeUtils(),
                        processingEnv.getElementUtils()
                );

        /*
         * Generate shared SpringFlow client once.
         */
        if (!clientGenerated) {

            generateSpringFlowClient();

            clientGenerated = true;
        }

        /*
         * Process every @Endpoint.
         */
        for (Element element :
                roundEnv.getElementsAnnotatedWith(
                        Endpoint.class)) {

            if (!(element instanceof TypeElement endpoint)) {
                continue;
            }

            processingEnv.getMessager()
                    .printMessage(
                            Diagnostic.Kind.NOTE,
                            "SpringFlow generating: "
                                    + endpoint.getQualifiedName()
                    );

            /*
             * Generate endpoint.
             */
            String endpointSource =
                    generator.generate(endpoint);

            generateEndpointFile(
                    endpoint,
                    endpoint.getSimpleName().toString()
                            + ".ts",
                    endpointSource
            );

            /*
             * Generate all custom models referenced
             * by the endpoint.
             */
            generateReferencedModels(
                    generator,
                    endpoint
            );
        }

        return true;
    }

    /**
     * Generate models referenced by an endpoint.
     */
    private void generateReferencedModels(
            TypeScriptGenerator generator,
            TypeElement endpoint) {

        for (Element element :
                endpoint.getEnclosedElements()) {

            if (!(element instanceof ExecutableElement method)) {
                continue;
            }

            if (method.getAnnotation(
                    EndpointMethod.class) == null) {

                continue;
            }

            /*
             * Return type.
             */
            generateModelIfNeeded(
                    generator,
                    method.getReturnType()
            );

            /*
             * Parameters.
             */
            for (var parameter :
                    method.getParameters()) {

                generateModelIfNeeded(
                        generator,
                        parameter.asType()
                );
            }
        }
    }

    /**
     * Generate a model if it is a custom Java class.
     *
     * Models are tracked by fully-qualified Java name,
     * so the same model is generated only once.
     */
    private void generateModelIfNeeded(
            TypeScriptGenerator generator,
            TypeMirror type) {

        if (type == null) {
            return;
        }

        /*
         * Array:
         *
         * Employee[]
         */
        if (type.getKind() == TypeKind.ARRAY) {

            ArrayType array =
                    (ArrayType) type;

            generateModelIfNeeded(
                    generator,
                    array.getComponentType()
            );

            return;
        }

        /*
         * Declared type:
         *
         * Employee
         * List<Employee>
         * Optional<Employee>
         */
        if (!(type instanceof DeclaredType declaredType)) {
            return;
        }

        Element element =
                declaredType.asElement();

        if (!(element instanceof TypeElement model)) {
            return;
        }

        String qualifiedName =
                model.getQualifiedName()
                        .toString();

        /*
         * Java/Jakarta standard types are not
         * SpringFlow models.
         *
         * We still inspect generic arguments.
         */
        if (qualifiedName.startsWith("java.")
                || qualifiedName.startsWith("javax.")
                || qualifiedName.startsWith("jakarta.")) {

            for (TypeMirror argument :
                    declaredType.getTypeArguments()) {

                generateModelIfNeeded(
                        generator,
                        argument
                );
            }

            return;
        }

        /*
         * IMPORTANT:
         *
         * If this model was already generated,
         * stop here.
         *
         * This prevents:
         *
         * Employee
         * Employee
         * Employee
         * Employee
         *
         * from being generated repeatedly.
         */
        if (!generatedModels.add(qualifiedName)) {

            return;
        }

        processingEnv.getMessager()
                .printMessage(
                        Diagnostic.Kind.NOTE,
                        "SpringFlow generating model: "
                                + qualifiedName
                );

        /*
         * Generate the model.
         */
        String modelSource =
                generator.generateModel(model);

        generateModelFile(
                model,
                model.getSimpleName().toString()
                        + ".ts",
                modelSource
        );

        /*
         * Inspect generic arguments too.
         *
         * Example:
         *
         * List<Address>
         *
         * generates Address as well.
         */
        for (TypeMirror argument :
                declaredType.getTypeArguments()) {

            generateModelIfNeeded(
                    generator,
                    argument
            );
        }
    }

    /**
     * Generate endpoint under its Java package.
     *
     * Java:
     *
     * dev.springflow.demo.employees
     *
     * becomes:
     *
     * springflow/generated/
     * dev/springflow/demo/employees/
     */
    private void generateEndpointFile(
            TypeElement endpoint,
            String fileName,
            String content) {

        String packagePath =
                processingEnv.getElementUtils()
                        .getPackageOf(endpoint)
                        .getQualifiedName()
                        .toString()
                        .replace('.', '/');

        generateFile(
                packagePath + "/" + fileName,
                content,
                "endpoint generation"
        );
    }

    /**
     * Generate model under its Java package.
     */
    private void generateModelFile(
            TypeElement model,
            String fileName,
            String content) {

        String packagePath =
                processingEnv.getElementUtils()
                        .getPackageOf(model)
                        .getQualifiedName()
                        .toString()
                        .replace('.', '/');

        generateFile(
                packagePath + "/" + fileName,
                content,
                "model generation"
        );
    }

    /**
     * Generate shared SpringFlowClient.
     */
    private void generateSpringFlowClient() {

        String typescript =
                new SpringFlowClientGenerator()
                        .generate();

        generateFile(
                "SpringFlowClient.ts",
                typescript,
                "client generation"
        );
    }

    /**
     * Write generated TypeScript file.
     */
    private void generateFile(
            String fileName,
            String content,
            String description) {

        String relativePath =
                GENERATED_DIRECTORY
                        + "/"
                        + fileName;

        /*
         * Second safety layer.
         *
         * Even if model discovery somehow
         * encounters the same physical file,
         * don't reopen it.
         */
        if (!generatedFiles.add(relativePath)) {

            processingEnv.getMessager()
                    .printMessage(
                            Diagnostic.Kind.NOTE,
                            "SpringFlow skipped duplicate generation: "
                                    + relativePath
                    );

            return;
        }

        try {

            Filer filer =
                    processingEnv.getFiler();

            FileObject file =
                    filer.createResource(
                            StandardLocation.CLASS_OUTPUT,
                            "",
                            relativePath
                    );

            try (Writer writer =
                         file.openWriter()) {

            writer.write(content);

processingEnv.getMessager().printMessage(
        Diagnostic.Kind.NOTE,
        "SpringFlow generated: "
                + relativePath
);

            }

            processingEnv.getMessager()
                    .printMessage(
                            Diagnostic.Kind.NOTE,
                            "SpringFlow generated: "
                                    + relativePath
                    );

        } catch (IOException e) {

            processingEnv.getMessager()
                    .printMessage(
                            Diagnostic.Kind.ERROR,
                            "SpringFlow "
                                    + description
                                    + " failed: "
                                    + e.getMessage()
                    );
        }
    }
}