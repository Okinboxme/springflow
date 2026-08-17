package dev.springflow.cli.commands;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateCommand implements Command {

    @Override
    public void run(String[] args) {

        if (args.length < 2) {

            System.out.println(
                    "Usage: springflow create <project-name>"
            );

            return;
        }

        createProject(args[1]);
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

            String packageName =
                    "com.example."
                            + sanitizeJavaName(projectName);

            String packagePath =
                    packageName.replace('.', '/');

            createDirectories(
                    projectDirectory,
                    packagePath
            );

            createPom(
                    projectDirectory,
                    projectName
            );

            createApplication(
                    projectDirectory,
                    packageName
            );

            createEmployeeModel(
                    projectDirectory,
                    packagePath
            );

            createEmployeeEndpoint(
                    projectDirectory,
                    packagePath
            );

            createResources(
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
                    "  What was generated:"
            );
            System.out.println();
            System.out.println(
                    "    Backend:"
            );
            System.out.println(
                    "      - Application.java"
            );
            System.out.println(
                    "      - Employee.java (model)"
            );
            System.out.println(
                    "      - EmployeeEndpoint.java (CRUD)"
            );
            System.out.println();
            System.out.println(
                    "    Frontend:"
            );
            System.out.println(
                    "      - Employee CRUD page"
            );
            System.out.println(
                    "      - Search, form validation"
            );
            System.out.println(
                    "      - Confirm dialogs, toasts"
            );
            System.out.println(
                    "      - Tailwind CSS"
            );

            System.out.println();
            System.out.println(
                    "Project:"
            );
            System.out.println(
                    "  " + projectDirectory
            );

            System.out.println();
            System.out.println(
                    "Next steps:"
            );
            System.out.println();
            System.out.println(
                    "  cd " + projectName
            );
            System.out.println(
                    "  npm install --prefix frontend"
            );
            System.out.println(
                    "  springflow dev"
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
            Path project,
            String packagePath)
            throws IOException {

        Files.createDirectories(
                project.resolve("src/main/java/" + packagePath + "/employees")
        );

        Files.createDirectories(
                project.resolve("src/main/resources")
        );

        Files.createDirectories(
                project.resolve("frontend/src/components")
        );

        Files.createDirectories(
                project.resolve("frontend/src/hooks")
        );

        Files.createDirectories(
                project.resolve("frontend/src/pages")
        );
    }

    private static void createPom(
            Path project,
            String projectName)
            throws IOException {

        String artifactId =
                sanitizeArtifactId(projectName);

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
                    <version>0.1.0</version>

                    <properties>
                        <java.version>21</java.version>
                        <spring-boot.version>3.5.7</spring-boot.version>
                        <springflow.version>0.1.0</springflow.version>
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
            String packageName)
            throws IOException {

        String content =
                """
                package %s;

                import org.springframework.boot.SpringApplication;
                import org.springframework.boot.autoconfigure.SpringBootApplication;

                @SpringBootApplication(scanBasePackages = {
                        "%s",
                        "dev.springflow.runtime"
                })
                public class Application {

                    public static void main(String[] args) {

                        SpringApplication.run(
                                Application.class,
                                args
                        );
                    }
                }
                """.formatted(
                        packageName,
                        packageName
                );

        Path packageDirectory =
                project.resolve(
                        "src/main/java/"
                                + packageName.replace('.', '/')
                );

        Files.createDirectories(
                packageDirectory
        );

        write(
                packageDirectory.resolve("Application.java"),
                content
        );
    }

    private static void createEmployeeModel(
            Path project,
            String packagePath)
            throws IOException {

        String packageName = packagePath.replace('/', '.');

        String content =
                """
                package %s.employees;

                public class Employee {

                    private Long id;
                    private String name;
                    private String email;
                    private String department;
                    private boolean active;

                    public Employee() {
                    }

                    public Employee(Long id, String name, String email,
                                    String department, boolean active) {
                        this.id = id;
                        this.name = name;
                        this.email = email;
                        this.department = department;
                        this.active = active;
                    }

                    public Long getId() {
                        return id;
                    }

                    public void setId(Long id) {
                        this.id = id;
                    }

                    public String getName() {
                        return name;
                    }

                    public void setName(String name) {
                        this.name = name;
                    }

                    public String getEmail() {
                        return email;
                    }

                    public void setEmail(String email) {
                        this.email = email;
                    }

                    public String getDepartment() {
                        return department;
                    }

                    public void setDepartment(String department) {
                        this.department = department;
                    }

                    public boolean isActive() {
                        return active;
                    }

                    public void setActive(boolean active) {
                        this.active = active;
                    }
                }
                """.formatted(
                        packageName
                );

        write(
                project.resolve(
                        "src/main/java/" + packagePath + "/employees/Employee.java"
                ),
                content
        );
    }

    private static void createEmployeeEndpoint(
            Path project,
            String packagePath)
            throws IOException {

        String packageName = packagePath.replace('/', '.');

        String content =
                """
                package %s.employees;

                import dev.springflow.core.annotation.Endpoint;
                import dev.springflow.core.annotation.EndpointMethod;

                import java.util.ArrayList;
                import java.util.List;
                import java.util.concurrent.atomic.AtomicLong;

                @Endpoint
                public class EmployeeEndpoint {

                    private final List<Employee> employees = new ArrayList<>();

                    private final AtomicLong idGenerator = new AtomicLong(1);

                    public EmployeeEndpoint() {
                        employees.add(
                                new Employee(
                                        idGenerator.getAndIncrement(),
                                        "Frank",
                                        "frank@example.com",
                                        "ICT",
                                        true
                                )
                        );

                        employees.add(
                                new Employee(
                                        idGenerator.getAndIncrement(),
                                        "Jane",
                                        "jane@example.com",
                                        "Finance",
                                        true
                                )
                        );
                    }

                    @EndpointMethod
                    public Employee create(
                            String name,
                            String email,
                            String department
                    ) {
                        Employee employee = new Employee(
                                idGenerator.getAndIncrement(),
                                name,
                                email,
                                department,
                                true
                        );

                        employees.add(employee);

                        return employee;
                    }

                    @EndpointMethod
                    public List<Employee> findAll() {
                        return new ArrayList<>(employees);
                    }

                    @EndpointMethod
                    public Employee findById(Long id) {

                        return employees.stream()
                                .filter(employee -> employee.getId().equals(id))
                                .findFirst()
                                .orElse(null);
                    }

                    @EndpointMethod
                    public Employee update(
                            Long id,
                            String name,
                            String email,
                            String department,
                            boolean active
                    ) {

                        Employee employee = findById(id);

                        if (employee == null) {
                            return null;
                        }

                        employee.setName(name);
                        employee.setEmail(email);
                        employee.setDepartment(department);
                        employee.setActive(active);

                        return employee;
                    }

                    @EndpointMethod
                    public boolean delete(Long id) {

                        return employees.removeIf(
                                employee -> employee.getId().equals(id)
                        );
                    }
                }
                """.formatted(
                        packageName
                );

        write(
                project.resolve(
                        "src/main/java/" + packagePath + "/employees/EmployeeEndpoint.java"
                ),
                content
        );
    }

    private static void createResources(
            Path project,
            String projectName)
            throws IOException {

        String content =
                """
                spring.application.name=%s
                """.formatted(
                        sanitizeArtifactId(projectName)
                );

        write(
                project.resolve(
                        "src/main/resources/application.properties"
                ),
                content
        );
    }

    private static void createFrontend(
            Path project,
            String projectName)
            throws IOException {

        Path frontend = project.resolve("frontend");

        String npmName = sanitizeNpmName(projectName);
        String javaName = sanitizeJavaName(projectName);

        // package.json
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
                    "react": "^19.1.0",
                    "react-dom": "^19.1.0"
                  },
                  "devDependencies": {
                    "@tailwindcss/vite": "^4.3.3",
                    "@types/react": "^19.1.0",
                    "@types/react-dom": "^19.1.0",
                    "@vitejs/plugin-react": "^5.0.0",
                    "tailwindcss": "^4.3.3",
                    "typescript": "^5.8.3",
                    "vite": "^7.0.0"
                  }
                }
                """.formatted(npmName)
        );

        // index.html
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

        // vite.config.ts
        write(
                frontend.resolve("vite.config.ts"),
                """
                import { defineConfig } from "vite";
                import react from "@vitejs/plugin-react";
                import tailwindcss from "@tailwindcss/vite";

                export default defineConfig({

                    plugins: [
                        react(),
                        tailwindcss()
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

        // src/index.css
        write(
                frontend.resolve("src/index.css"),
                """
                @import "tailwindcss";

                :root {
                  font-family:
                    Inter,
                    ui-sans-serif,
                    system-ui,
                    -apple-system,
                    BlinkMacSystemFont,
                    "Segoe UI",
                    sans-serif;

                  background: #f8fafc;
                  color: #0f172a;
                }

                body {
                  margin: 0;
                  min-width: 320px;
                  min-height: 100vh;
                }

                button,
                input,
                select {
                  font: inherit;
                }
                """
        );

        // src/main.tsx
        write(
                frontend.resolve("src/main.tsx"),
                """
                import { StrictMode } from "react";
                import { createRoot } from "react-dom/client";
                import App from "./App";
                import "./index.css";

                createRoot(document.getElementById("root")!).render(
                  <StrictMode>
                    <App />
                  </StrictMode>
                );
                """
        );

        // src/App.tsx
        write(
                frontend.resolve("src/App.tsx"),
                """
                import EmployeePage from "./pages/EmployeePage";

                function App() {
                  return <EmployeePage />;
                }

                export default App;
                """
        );

        // tsconfig.json
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

        // tsconfig.app.json
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

        // tsconfig.node.json
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

        // -- Frontend components --

        // src/components/ConfirmDialog.tsx
        write(
                frontend.resolve("src/components/ConfirmDialog.tsx"),
                """
                import { useEffect, useRef } from "react";

                interface ConfirmDialogProps {
                  open: boolean;
                  title: string;
                  message: string;
                  confirmLabel?: string;
                  destructive?: boolean;
                  onConfirm: () => void;
                  onCancel: () => void;
                }

                export function ConfirmDialog({
                  open,
                  title,
                  message,
                  confirmLabel = "Confirm",
                  destructive = false,
                  onConfirm,
                  onCancel,
                }: ConfirmDialogProps) {
                  const dialogRef = useRef<HTMLDialogElement>(null);

                  useEffect(() => {
                    const dialog = dialogRef.current;
                    if (!dialog) return;

                    if (open && !dialog.open) {
                      dialog.showModal();
                    } else if (!open && dialog.open) {
                      dialog.close();
                    }
                  }, [open]);

                  return (
                    <dialog
                      ref={dialogRef}
                      onClose={onCancel}
                      className="rounded-xl border bg-white p-0 shadow-xl backdrop:bg-black/40"
                    >
                      <div className="p-6">
                        <h2 className="text-lg font-semibold text-slate-900">
                          {title}
                        </h2>

                        <p className="mt-2 text-sm text-slate-600">
                          {message}
                        </p>
                      </div>

                      <div className="flex justify-end gap-3 border-t px-6 py-4">
                        <button
                          onClick={onCancel}
                          className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
                        >
                          Cancel
                        </button>

                        <button
                          onClick={onConfirm}
                          className={
                            destructive
                              ? "rounded-lg bg-red-600 px-4 py-2 text-sm font-medium text-white hover:bg-red-700"
                              : "rounded-lg bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
                          }
                        >
                          {confirmLabel}
                        </button>
                      </div>
                    </dialog>
                  );
                }
                """
        );

        // src/components/Toast.tsx
        write(
                frontend.resolve("src/components/Toast.tsx"),
                """
                import { useEffect } from "react";

                export interface ToastItem {
                  id: number;
                  type: "success" | "error";
                  message: string;
                }

                interface ToastContainerProps {
                  toasts: ToastItem[];
                  onDismiss: (id: number) => void;
                }

                export function ToastContainer({
                  toasts,
                  onDismiss,
                }: ToastContainerProps) {
                  if (toasts.length === 0) return null;

                  return (
                    <div className="fixed top-4 right-4 z-50 flex flex-col gap-2">
                      {toasts.map((toast) => (
                        <Toast key={toast.id} toast={toast} onDismiss={onDismiss} />
                      ))}
                    </div>
                  );
                }

                function Toast({
                  toast,
                  onDismiss,
                }: {
                  toast: ToastItem;
                  onDismiss: (id: number) => void;
                }) {
                  useEffect(() => {
                    const timer = setTimeout(() => {
                      onDismiss(toast.id);
                    }, 3000);

                    return () => clearTimeout(timer);
                  }, [toast.id, onDismiss]);

                  return (
                    <div
                      className={
                        toast.type === "success"
                          ? "flex items-center gap-3 rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-800 shadow-lg"
                          : "flex items-center gap-3 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800 shadow-lg"
                      }
                    >
                      <span className="flex-1">{toast.message}</span>

                      <button
                        onClick={() => onDismiss(toast.id)}
                        className="ml-2 text-current opacity-60 hover:opacity-100"
                      >
                        &times;
                      </button>
                    </div>
                  );
                }
                """
        );

        // src/components/SearchInput.tsx
        write(
                frontend.resolve("src/components/SearchInput.tsx"),
                """
                interface SearchInputProps {
                  value: string;
                  onChange: (value: string) => void;
                  placeholder?: string;
                }

                export function SearchInput({
                  value,
                  onChange,
                  placeholder = "Search...",
                }: SearchInputProps) {
                  return (
                    <div className="relative">
                      <svg
                        className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                        />
                      </svg>

                      <input
                        type="text"
                        value={value}
                        onChange={(e) => onChange(e.target.value)}
                        placeholder={placeholder}
                        className="w-full rounded-lg border border-slate-300 py-2.5 pl-10 pr-10 text-sm outline-none focus:border-blue-500"
                      />

                      {value && (
                        <button
                          onClick={() => onChange("")}
                          className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                        >
                          &times;
                        </button>
                      )}
                    </div>
                  );
                }
                """
        );

        // src/components/StatusBadge.tsx
        write(
                frontend.resolve("src/components/StatusBadge.tsx"),
                """
                interface StatusBadgeProps {
                  active: boolean;
                }

                export function StatusBadge({ active }: StatusBadgeProps) {
                  return (
                    <span
                      className={
                        active
                          ? "rounded-full bg-green-100 px-3 py-1 text-xs font-medium text-green-700"
                          : "rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-600"
                      }
                    >
                      {active ? "Active" : "Inactive"}
                    </span>
                  );
                }
                """
        );

        // -- Frontend hooks --

        String apiImport =
                "com/example/" + javaName + "/employees";

        // src/hooks/useEmployees.ts
        write(
                frontend.resolve("src/hooks/useEmployees.ts"),
                """
                import { useCallback, useEffect, useMemo, useState } from "react";
                import { EmployeeEndpoint } from "../api/%s/EmployeeEndpoint";
                import type { Employee } from "../api/%s/Employee";

                export function useEmployees() {
                  const [employees, setEmployees] = useState<Employee[]>([]);
                  const [loading, setLoading] = useState(false);
                  const [error, setError] = useState("");
                  const [searchQuery, setSearchQuery] = useState("");

                  const filteredEmployees = useMemo(() => {
                    if (!searchQuery.trim()) {
                      return employees;
                    }

                    const query = searchQuery.toLowerCase();

                    return employees.filter(
                      (e) =>
                        e.name.toLowerCase().includes(query) ||
                        e.email.toLowerCase().includes(query) ||
                        e.department.toLowerCase().includes(query)
                    );
                  }, [employees, searchQuery]);

                  const loadEmployees = useCallback(async () => {
                    setLoading(true);
                    setError("");

                    try {
                      const result = await EmployeeEndpoint.findAll();
                      setEmployees(result);
                    } catch (err) {
                      console.error(err);

                      setError(
                        err instanceof Error
                          ? err.message
                          : "Failed to load employees"
                      );
                    } finally {
                      setLoading(false);
                    }
                  }, []);

                  const createEmployee = useCallback(
                    async (
                      name: string,
                      email: string,
                      department: string
                    ): Promise<boolean> => {
                      setError("");

                      try {
                        await EmployeeEndpoint.create(name, email, department);
                        await loadEmployees();
                        return true;
                      } catch (err) {
                        console.error(err);

                        setError(
                          err instanceof Error
                            ? err.message
                            : "Failed to create employee"
                        );
                        return false;
                      }
                    },
                    [loadEmployees]
                  );

                  const updateEmployee = useCallback(
                    async (
                      id: number,
                      name: string,
                      email: string,
                      department: string,
                      active: boolean
                    ): Promise<boolean> => {
                      setError("");

                      try {
                        await EmployeeEndpoint.update(id, name, email, department, active);
                        await loadEmployees();
                        return true;
                      } catch (err) {
                        console.error(err);

                        setError(
                          err instanceof Error
                            ? err.message
                            : "Failed to update employee"
                        );
                        return false;
                      }
                    },
                    [loadEmployees]
                  );

                  const deleteEmployee = useCallback(
                    async (id: number): Promise<boolean> => {
                      setError("");

                      try {
                        await EmployeeEndpoint.delete(id);
                        await loadEmployees();
                        return true;
                      } catch (err) {
                        console.error(err);

                        setError(
                          err instanceof Error
                            ? err.message
                            : "Failed to delete employee"
                        );
                        return false;
                      }
                    },
                    [loadEmployees]
                  );

                  useEffect(() => {
                    // eslint-disable-next-line react-hooks/set-state-in-effect
                    void loadEmployees();
                  }, [loadEmployees]);

                  return {
                    employees: filteredEmployees,
                    totalCount: employees.length,
                    loading,
                    error,
                    searchQuery,
                    setSearchQuery,
                    loadEmployees,
                    createEmployee,
                    updateEmployee,
                    deleteEmployee,
                  };
                }
                """.formatted(apiImport, apiImport)
        );

        // -- Frontend pages --

        // src/pages/EmployeePage.tsx
        write(
                frontend.resolve("src/pages/EmployeePage.tsx"),
                """
                import { useCallback, useState } from "react";
                import type { Employee } from "../api/%s/Employee";
                import { useEmployees } from "../hooks/useEmployees";
                import { ConfirmDialog } from "../components/ConfirmDialog";
                import { SearchInput } from "../components/SearchInput";
                import { StatusBadge } from "../components/StatusBadge";
                import { ToastContainer, type ToastItem } from "../components/Toast";

                interface FormErrors {
                  name?: string;
                  email?: string;
                  department?: string;
                }

                function validate(
                  name: string,
                  email: string,
                  department: string
                ): FormErrors {
                  const errors: FormErrors = {};

                  if (!name.trim()) {
                    errors.name = "Name is required.";
                  } else if (name.trim().length < 2) {
                    errors.name = "Name must be at least 2 characters.";
                  }

                  if (!email.trim()) {
                    errors.email = "Email is required.";
                  } else if (!email.includes("@")) {
                    errors.email = "Email must be valid.";
                  }

                  if (!department.trim()) {
                    errors.department = "Department is required.";
                  }

                  return errors;
                }

                function EmployeePage() {
                  const {
                    employees,
                    totalCount,
                    loading,
                    searchQuery,
                    setSearchQuery,
                    createEmployee,
                    updateEmployee,
                    deleteEmployee,
                  } = useEmployees();

                  const [name, setName] = useState("");
                  const [email, setEmail] = useState("");
                  const [department, setDepartment] = useState("");
                  const [active, setActive] = useState(true);
                  const [editingId, setEditingId] = useState<number | null>(null);
                  const [saving, setSaving] = useState(false);
                  const [errors, setErrors] = useState<FormErrors>({});

                  const [toasts, setToasts] = useState<ToastItem[]>([]);
                  const [deleteTarget, setDeleteTarget] = useState<Employee | null>(null);

                  const addToast = useCallback(
                    (type: "success" | "error", message: string) => {
                      setToasts((prev) => [
                        ...prev,
                        { id: Date.now(), type, message },
                      ]);
                    },
                    []
                  );

                  const dismissToast = useCallback((id: number) => {
                    setToasts((prev) => prev.filter((t) => t.id !== id));
                  }, []);

                  function resetForm() {
                    setName("");
                    setEmail("");
                    setDepartment("");
                    setActive(true);
                    setEditingId(null);
                    setErrors({});
                  }

                  function editEmployee(employee: Employee) {
                    setEditingId(employee.id);
                    setName(employee.name);
                    setEmail(employee.email);
                    setDepartment(employee.department);
                    setActive(employee.active);
                    setErrors({});
                  }

                  async function handleSave() {
                    const validationErrors = validate(name, email, department);
                    setErrors(validationErrors);

                    if (Object.keys(validationErrors).length > 0) {
                      return;
                    }

                    setSaving(true);

                    const success =
                      editingId === null
                        ? await createEmployee(
                            name.trim(),
                            email.trim(),
                            department.trim()
                          )
                        : await updateEmployee(
                            editingId,
                            name.trim(),
                            email.trim(),
                            department.trim(),
                            active
                          );

                    setSaving(false);

                    if (success) {
                      resetForm();
                      addToast(
                        "success",
                        editingId === null
                          ? "Employee created."
                          : "Employee updated."
                      );
                    }
                  }

                  async function handleDeleteConfirm() {
                    if (!deleteTarget) return;

                    setDeleteTarget(null);

                    const success = await deleteEmployee(deleteTarget.id);

                    if (success) {
                      addToast("success", "Employee deleted.");
                    }
                  }

                  return (
                    <div className="min-h-screen bg-slate-50 p-8">

                      <ToastContainer toasts={toasts} onDismiss={dismissToast} />

                      <ConfirmDialog
                        open={deleteTarget !== null}
                        title="Delete Employee"
                        message={
                          deleteTarget
                            ? `Are you sure you want to delete ${deleteTarget.name}? This action cannot be undone.`
                            : ""
                        }
                        confirmLabel="Delete"
                        destructive
                        onConfirm={handleDeleteConfirm}
                        onCancel={() => setDeleteTarget(null)}
                      />

                      <div className="mx-auto max-w-7xl">

                        {/* Header */}
                        <div className="mb-8">

                          <div className="flex items-center justify-between">

                            <div>
                              <h1 className="text-3xl font-bold text-slate-900">
                                Employees
                              </h1>

                              <p className="mt-2 text-slate-500">
                                Manage employees using the generated SpringFlow TypeScript API.
                              </p>
                            </div>

                            <button
                              onClick={resetForm}
                              className="rounded-lg bg-blue-600 px-5 py-3 text-sm font-medium text-white hover:bg-blue-700"
                            >
                              + New Employee
                            </button>

                          </div>

                        </div>

                        <div className="grid gap-6 lg:grid-cols-3">

                          {/* Employee form */}
                          <div className="rounded-xl border bg-white p-6 shadow-sm">

                            <h2 className="text-xl font-semibold text-slate-900">
                              {editingId === null
                                ? "Add Employee"
                                : "Edit Employee"}
                            </h2>

                            <p className="mt-1 text-sm text-slate-500">
                              {editingId === null
                                ? "Create a new employee."
                                : `Editing employee #${editingId}`}
                            </p>

                            <div className="mt-6 space-y-4">

                              {/* Name */}
                              <div>
                                <label className="mb-1 block text-sm font-medium text-slate-700">
                                  Name
                                </label>

                                <input
                                  value={name}
                                  onChange={(e) => {
                                    setName(e.target.value);
                                    if (errors.name) {
                                      setErrors((prev) => ({
                                        ...prev,
                                        name: undefined,
                                      }));
                                    }
                                  }}
                                  placeholder="John Doe"
                                  className={
                                    errors.name
                                      ? "w-full rounded-lg border border-red-300 px-4 py-3 outline-none focus:border-red-500"
                                      : "w-full rounded-lg border border-slate-300 px-4 py-3 outline-none focus:border-blue-500"
                                  }
                                />

                                {errors.name && (
                                  <p className="mt-1 text-xs text-red-600">
                                    {errors.name}
                                  </p>
                                )}
                              </div>

                              {/* Email */}
                              <div>
                                <label className="mb-1 block text-sm font-medium text-slate-700">
                                  Email
                                </label>

                                <input
                                  type="email"
                                  value={email}
                                  onChange={(e) => {
                                    setEmail(e.target.value);
                                    if (errors.email) {
                                      setErrors((prev) => ({
                                        ...prev,
                                        email: undefined,
                                      }));
                                    }
                                  }}
                                  placeholder="john@example.com"
                                  className={
                                    errors.email
                                      ? "w-full rounded-lg border border-red-300 px-4 py-3 outline-none focus:border-red-500"
                                      : "w-full rounded-lg border border-slate-300 px-4 py-3 outline-none focus:border-blue-500"
                                  }
                                />

                                {errors.email && (
                                  <p className="mt-1 text-xs text-red-600">
                                    {errors.email}
                                  </p>
                                )}
                              </div>

                              {/* Department */}
                              <div>
                                <label className="mb-1 block text-sm font-medium text-slate-700">
                                  Department
                                </label>

                                <input
                                  value={department}
                                  onChange={(e) => {
                                    setDepartment(e.target.value);
                                    if (errors.department) {
                                      setErrors((prev) => ({
                                        ...prev,
                                        department: undefined,
                                      }));
                                    }
                                  }}
                                  placeholder="ICT"
                                  className={
                                    errors.department
                                      ? "w-full rounded-lg border border-red-300 px-4 py-3 outline-none focus:border-red-500"
                                      : "w-full rounded-lg border border-slate-300 px-4 py-3 outline-none focus:border-blue-500"
                                  }
                                />

                                {errors.department && (
                                  <p className="mt-1 text-xs text-red-600">
                                    {errors.department}
                                  </p>
                                )}
                              </div>

                              {/* Active */}
                              <div className="flex items-center gap-3">

                                <input
                                  id="active"
                                  type="checkbox"
                                  checked={active}
                                  onChange={(e) => setActive(e.target.checked)}
                                  className="h-4 w-4"
                                />

                                <label
                                  htmlFor="active"
                                  className="text-sm font-medium text-slate-700"
                                >
                                  Active employee
                                </label>

                              </div>

                              {/* Buttons */}
                              <div className="flex gap-3 pt-2">

                                <button
                                  onClick={handleSave}
                                  disabled={saving}
                                  className="flex-1 rounded-lg bg-blue-600 px-4 py-3 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-50"
                                >
                                  {saving
                                    ? "Saving..."
                                    : editingId === null
                                      ? "Create Employee"
                                      : "Update Employee"}
                                </button>

                                {editingId !== null && (
                                  <button
                                    onClick={resetForm}
                                    className="rounded-lg border border-slate-300 px-4 py-3 text-sm font-medium text-slate-700 hover:bg-slate-50"
                                  >
                                    Cancel
                                  </button>
                                )}

                              </div>

                            </div>

                          </div>

                          {/* Employee table */}
                          <div className="rounded-xl border bg-white shadow-sm lg:col-span-2">

                            <div className="border-b p-6">

                              <div className="flex items-center justify-between">

                                <div>
                                  <h2 className="text-xl font-semibold text-slate-900">
                                    Employee List
                                  </h2>

                                  <p className="mt-1 text-sm text-slate-500">
                                    {searchQuery
                                      ? `${employees.length} of ${totalCount} employees`
                                      : `${totalCount} employee${totalCount === 1 ? "" : "s"}`}
                                  </p>
                                </div>

                                <button
                                  onClick={() => {}}
                                  disabled={loading}
                                  className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50"
                                >
                                  {loading ? "Loading..." : "Refresh"}
                                </button>

                              </div>

                              <div className="mt-4">
                                <SearchInput
                                  value={searchQuery}
                                  onChange={setSearchQuery}
                                  placeholder="Search employees..."
                                />
                              </div>

                            </div>

                            <div className="overflow-x-auto">

                              <table className="w-full text-left">

                                <thead className="bg-slate-50 text-sm text-slate-500">

                                  <tr>
                                    <th className="px-6 py-4 font-medium">
                                      ID
                                    </th>

                                    <th className="px-6 py-4 font-medium">
                                      Name
                                    </th>

                                    <th className="px-6 py-4 font-medium">
                                      Email
                                    </th>

                                    <th className="px-6 py-4 font-medium">
                                      Department
                                    </th>

                                    <th className="px-6 py-4 font-medium">
                                      Status
                                    </th>

                                    <th className="px-6 py-4 font-medium">
                                      Actions
                                    </th>
                                  </tr>

                                </thead>

                                <tbody className="divide-y">

                                  {loading && employees.length === 0 && (
                                    <tr>
                                      <td
                                        colSpan={6}
                                        className="px-6 py-10 text-center text-sm text-slate-500"
                                      >
                                        Loading employees...
                                      </td>
                                    </tr>
                                  )}

                                  {!loading && employees.length === 0 && (
                                    <tr>
                                      <td
                                        colSpan={6}
                                        className="px-6 py-10 text-center text-sm text-slate-500"
                                      >
                                        {searchQuery
                                          ? "No employees match your search."
                                          : "No employees found."}
                                      </td>
                                    </tr>
                                  )}

                                  {employees.map((employee) => (
                                    <tr
                                      key={employee.id}
                                      className="hover:bg-slate-50"
                                    >

                                      <td className="px-6 py-4 text-sm text-slate-600">
                                        {employee.id}
                                      </td>

                                      <td className="px-6 py-4 font-medium text-slate-900">
                                        {employee.name}
                                      </td>

                                      <td className="px-6 py-4 text-sm text-slate-600">
                                        {employee.email}
                                      </td>

                                      <td className="px-6 py-4 text-sm text-slate-600">
                                        {employee.department}
                                      </td>

                                      <td className="px-6 py-4">
                                        <StatusBadge active={employee.active} />
                                      </td>

                                      <td className="px-6 py-4">

                                        <div className="flex gap-2">

                                          <button
                                            onClick={() =>
                                              editEmployee(employee)
                                            }
                                            className="rounded-lg border border-slate-300 px-3 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50"
                                          >
                                            Edit
                                          </button>

                                          <button
                                            onClick={() =>
                                              setDeleteTarget(employee)
                                            }
                                            className="rounded-lg border border-red-200 px-3 py-2 text-xs font-medium text-red-600 hover:bg-red-50"
                                          >
                                            Delete
                                          </button>

                                        </div>

                                      </td>

                                    </tr>
                                  ))}

                                </tbody>

                              </table>

                            </div>

                          </div>

                        </div>

                      </div>

                    </div>
                  );
                }

                export default EmployeePage;
                """.formatted(apiImport)
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
                  "backend": {
                    "port": 8080
                  },
                  "frontend": {
                    "directory": "frontend",
                    "port": 5173
                  },
                  "springflow": {
                    "apiDirectory": "frontend/src/api"
                  }
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
