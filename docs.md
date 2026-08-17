# SpringFlow Documentation

> A Spring Boot framework for building backend APIs with automatically
> generated TypeScript clients and React frontends.

SpringFlow connects:

```
Java → Spring Boot → TypeScript → React
```

The goal is to provide a development experience similar to modern
full-stack frameworks while keeping Spring Boot at the core.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Module Overview](#module-overview)
- [Getting Started](#getting-started)
- [Core Concepts](#core-concepts)
- [How It Works](#how-it-works)
- [HTTP API](#http-api)
- [TypeScript Generation](#typescript-generation)
- [Type Mapping](#type-mapping)
- [Frontend Integration](#frontend-integration)
- [Command-Line Interface](#command-line-interface)
- [Demo Application](#demo-application)
- [Configuration](#configuration)
- [Repository Layout](#repository-layout)
- [Limitations](#limitations)

---

## Features

- Spring Boot integration
- Java endpoint annotations
- Automatic TypeScript client generation via annotation processing
- Automatic TypeScript model (interface) generation
- Automatic Java package → TypeScript folder mapping
- Generated API clients (`SpringFlowClient`)
- React frontend project generation
- Vite frontend with TypeScript
- CLI project creation
- Maven integration
- CORS support for local development

---

## Architecture

SpringFlow is a multi-module Maven project. The core flow is:

1. You annotate a plain Java class with `@Endpoint` and methods with
   `@EndpointMethod`.
2. During compilation, the annotation processor generates TypeScript
   clients and model interfaces.
3. At runtime, the runtime module exposes those Java methods over HTTP.
4. Your React frontend calls the generated TypeScript clients.

```
Java sources
   │  (compile)
   ▼
springflow-processor ──► generated TypeScript clients + models
   │
   ▼
springflow-runtime ──► REST endpoints under /springflow/...
   │
   ▼
springflow-react / frontend ──► calls SpringFlowClient
```

---

## Module Overview

| Module | Purpose |
| --- | --- |
| `springflow-core` | Annotations (`@Endpoint`, `@EndpointMethod`, etc.) and API definitions. |
| `springflow-processor` | Java annotation processor that generates TypeScript. |
| `springflow-runtime` | Runtime that discovers endpoints and exposes them over HTTP. |
| `springflow-boot-starter` | Spring Boot integration glue. |
| `springflow-typescript` | TypeScript generation utilities and metadata records. |
| `springflow-cli` | Command-line project generator. |
| `springflow-demo` | Runnable example application. |
| `springflow-react` | React + Vite frontend for the demo. |
| `springflow-ui` | UI package metadata (`@springflow/ui`). |

### Build requirements

- JDK 21
- Spring Boot 3.5.7
- Maven (multi-module reactor)

Build the whole project:

```bash
mvn clean install
```

> The `maven-compiler-plugin` must be configured with
> `<parameters>true</parameters>` and the `springflow-processor`
> declared under `annotationProcessorPaths`. The runtime relies on
> reflection parameter names, and the processor runs during compilation.

---

## Getting Started

### 1. Create a project with the CLI

```bash
java -jar springflow-cli/target/springflow-cli-0.1.0-SNAPSHOT.jar create my-app
```

Or run the CLI from the module:

```bash
mvn -pl springflow-cli exec:java -Dexec.mainClass="dev.springflow.cli.SpringFlowCLI" -Dexec.args="create my-app"
```

This creates a project containing:

```
my-app/
├── pom.xml                      # Spring Boot + SpringFlow dependencies + processor
├── springflow.config.json       # SpringFlow configuration
├── src/main/java/com/example/myapp/Application.java
└── frontend/                    # Vite + React + TypeScript frontend
```

### 2. Run the backend

```bash
cd my-app
mvn spring-boot:run
```

The SpringFlow API is served from `http://localhost:8080/springflow`.

### 3. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend runs at `http://localhost:5173` and proxies
`/springflow` to the backend on port `8080`.

---

## Core Concepts

### @Endpoint

Marks a class as a SpringFlow endpoint. It is a Spring `@Component`, so
every `@Endpoint` class becomes a Spring bean automatically.

```java
@Endpoint
public class EmployeeEndpoint {
    // ...
}
```

### @EndpointMethod

Marks a public method as an invokable endpoint method. Methods with
zero parameters are invoked via HTTP `GET`. Methods with a single
parameter can be invoked via `POST` with a JSON body.

```java
@EndpointMethod
public List<Employee> findAll() {
    return employees;
}
```

### Other annotations

| Annotation | Target | Purpose |
| --- | --- | --- |
| `@Endpoint` | Class | Marks an endpoint bean. |
| `@EndpointMethod` | Method | Marks an endpoint method. |
| `@SpringFlowEndpoint` | Class | Alternative endpoint marker with a `value()` (optional name). |
| `@SpringFlowMethod` | Method | Alternative method marker with a `value()` (optional name). |
| `@Allow` | Method | `SOURCE`-retained annotation accepting `roles()` (declared for future authorization use). |

> `@Endpoint` + `@EndpointMethod` are the primary annotations used by
> the processor and runtime. `@SpringFlowEndpoint` / `@SpringFlowMethod`
> are early-stage alternatives and are not processed by
> `SpringFlowProcessor`.

---

## How It Works

### Compile time — annotation processing

`SpringFlowProcessor` (`springflow-processor`) runs during compilation
and, for every `@Endpoint` class:

1. Generates a shared `SpringFlowClient.ts` once per compilation.
2. Generates an endpoint client class (`EmployeeEndpoint.ts`) that
   mirrors the Java endpoint's public `@EndpointMethod` methods.
3. Recursively discovers custom model types referenced by return types
   and parameters (including generic arguments such as `List<Employee>`)
   and generates TypeScript interfaces for them.
4. Writes generated files under `springflow/generated/...`, preserving
   the Java package structure:
   ```
   dev.springflow.demo.employees  →  springflow/generated/dev/springflow/demo/employees/
   ```
   This is a build artifact on the classpath; the CLI is responsible for
   synchronizing it into the frontend's API directory.

Models are generated once per qualified name and duplicate physical
files are skipped.

### Run time — endpoint discovery and invocation

`EndpointRegistry` (`springflow-runtime`) scans the Spring
`ApplicationContext` for beans annotated with `@Endpoint` and registers
them under their simple class name.

`SpringFlowController` is a `@RestController` mapped at `/springflow`
that:

- `GET /springflow/{endpoint}/{method}` — invokes a method using query
  parameters. Parameters are matched by name and converted to the
  declared Java types.
- `POST /springflow/{endpoint}/{method}` — invokes a method with one
  JSON body parameter (currently only a single body parameter is
  supported).
- `GET /springflow/endpoints` — lists all registered endpoints.

Both GET and POST invocation delegate to the endpoint bean via
reflection.

---

## HTTP API

### List endpoints

```
GET /springflow/endpoints
```

Returns a map of endpoint simple-name → bean instance.

### Invoke a no-argument method

```
GET /springflow/EmployeeEndpoint/findAll
```

### Invoke a method with parameters

```
GET /springflow/EmployeeEndpoint/findById?id=1
```

### Invoke a method with a JSON body

```
POST /springflow/EmployeeEndpoint/save
Content-Type: application/json

{"name": "Frank"}
```

### Supported parameter types (query parameters)

`String`, `int`/`Integer`, `long`/`Long`, `double`/`Double`,
`float`/`Float`, `short`/`Short`, `byte`/`Byte`, `boolean`/`Boolean`,
`char`/`Character`.

> GET method selection requires the query parameter count to exactly
> match the method's parameter count.

### CORS

The controller allows cross-origin requests from:

- `http://localhost:5173`
- `http://127.0.0.1:5173`

---

## TypeScript Generation

### SpringFlowClient

A shared client used by all generated endpoint clients:

```typescript
export class SpringFlowClient {

    static async get<T>(
        url: string,
        params?: Record<string, unknown>
    ): Promise<T> {
        // builds query string and fetch(url)
        // throws on !response.ok
        // returns response.json()
    }
}
```

### Endpoint client

Each `@Endpoint` class produces a TypeScript class with the same name
and one static async method per `@EndpointMethod`:

```typescript
/* Generated by SpringFlow. DO NOT EDIT. */

import { SpringFlowClient } from "../../../../SpringFlowClient";
import type { Employee } from "./Employee";

export class EmployeeEndpoint {

    static async findAll(): Promise<Employee[]> {
        return SpringFlowClient.get<Employee[]>(
            "/springflow/EmployeeEndpoint/findAll"
        );
    }
}
```

### Model interface

Each referenced custom Java class produces a TypeScript interface:

```typescript
/* Generated by SpringFlow. DO NOT EDIT. */

export interface Employee {
    id: number;
    name: string;
    email: string;
    department: string;
    active: boolean;
}
```

Static fields and compiler-generated fields are ignored.

---

## Type Mapping

| Java | TypeScript |
| --- | --- |
| `void` | `void` |
| `int`, `long`, `double`, `float`, `short`, `byte` and boxed forms | `number` |
| `BigDecimal`, `BigInteger` | `number` |
| `boolean` / `Boolean` | `boolean` |
| `char` / `Character` | `string` |
| `String` | `string` |
| `Object` | `unknown` |
| `T[]` | `T[]` |
| `List<T>`, `Set<T>` | `T[]` |
| `Optional<T>` | `T \| null` |
| `Map<K,V>` | `Record<K,V>` |
| Custom model | Same-name imported interface |

---

## Frontend Integration

### Generated API directory

The authoritative location for generated frontend API files is the
project's API directory — `frontend/src/api/` by default. Every
command that depends on generated TypeScript (`generate`, `dev`,
`build`) synchronizes the generated clients there, so the frontend is
never left stale:

```text
<project>/frontend/src/api/
├── index.ts              # re-exports every generated module
├── SpringFlowClient.ts
└── dev/springflow/.../   # Java package structure preserved
```

`frontend/src/api/index.ts` is generated and re-exports every module:

```typescript
/* Generated by SpringFlow. DO NOT EDIT. */

export * from "./SpringFlowClient";
export * from "./dev/springflow/demo/employees/EmployeeEndpoint";
```

Consumers can import everything from the generated API:

```tsx
import { EmployeeEndpoint } from "../api";
import type { Employee } from "../api/dev/springflow/demo/employees/Employee";

async function load() {
  const employees = await EmployeeEndpoint.findAll();
  setEmployees(employees);
}
```

### Vite proxy

`vite.config.ts` proxies `/springflow` requests to the backend:

```ts
server: {
  port: 5173,
  proxy: {
    "/springflow": {
      target: "http://localhost:8080",
      changeOrigin: true,
    },
  },
},
```

`springflow-react` additionally uses Tailwind CSS 4 via
`@tailwindcss/vite`.

---

## Command-Line Interface

```
Usage:

  springflow create <project-name>
      Create a new SpringFlow application

  springflow generate
      Generate TypeScript clients from SpringFlow annotations
      and synchronize them into the frontend API directory

  springflow dev
      Generate TypeScript, synchronize the frontend API,
      then run the backend and frontend in development mode

  springflow build
      Generate TypeScript, synchronize the frontend API,
      build the frontend, then package the Spring Boot backend

  springflow version
      Show SpringFlow version

  springflow help
      Show this help
```

### `springflow generate`

Validates the project, runs `mvn compile` (which triggers the
annotation processor), copies every generated `.ts` file from
`target/classes/springflow/generated` into the API directory
(`frontend/src/api/` by default), removes stale generated files, and
writes `index.ts`:

```text
SpringFlow Generate

✓ Scanning endpoints
✓ Generating TypeScript
✓ Updating frontend/src/api
✓ Synchronized 6 generated files
✓ Generated index.ts

Generation complete.
```

Generation is deterministic — running it twice without Java changes
produces identical output.

### `springflow dev`

Pipeline:

```text
validate → generate → sync frontend/src/api → start Spring Boot → start Vite
```

It prints the backend and frontend URLs (from `springflow.config.json`,
defaults `8080` / `5173`) and manages the child processes so they are
terminated when the dev server is stopped:

```text
SpringFlow Development Server

Backend:
  http://localhost:8080

Frontend:
  http://localhost:5173

SpringFlow development server running...
```

### `springflow build`

Pipeline:

```text
validate → generate → sync frontend/src/api → npm build → mvn package
```

The frontend build runs first; `npm install` is run automatically when
`node_modules` is missing. A failing frontend or backend build exits
with a non-zero code.

### Project validation and tool detection

`generate`, `dev`, and `build` require a `pom.xml` or a
`springflow.config.json` in the current directory. Missing Maven or
Node.js/npm produces a friendly error instead of a stack trace, and
`dev`/`build` fail gracefully when the frontend directory does not
exist.

The `create` command scaffolds:

- Maven `pom.xml` with Spring Boot, SpringFlow dependencies, the
  annotation processor, and `<parameters>true</parameters>`.
- A Spring Boot `Application` class and `application.properties`.
- A Vite + React + TypeScript frontend with a `/springflow` proxy.
- A `springflow.config.json`.

Project names are sanitized for the Maven artifactId, Java package
name, and npm package name.

---

## Demo Application

`springflow-demo` is a runnable Spring Boot application:

```bash
mvn -pl springflow-demo spring-boot:run
```

It includes:

- `dev.springflow.demo.HelloEndpoint` — `hello`, `add`, `active`
  examples.
- `dev.springflow.demo.employees.EmployeeEndpoint` — a full
  create/read/update/delete example using an in-memory `List<Employee>`.
- `dev.springflow.demo.employees.Employee` — a model class.

The Spring Boot application scans both `dev.springflow.demo` and
`dev.springflow.runtime` so the runtime beans are picked up:

```java
@SpringBootApplication(scanBasePackages = {
    "dev.springflow.demo",
    "dev.springflow.runtime"
})
```

The matching frontend (`springflow-react`) shows an employee
management page backed by the generated TypeScript API.

---

## Configuration

### `springflow.config.json`

Created by the CLI in each generated project:

```json
{
  "name": "my-app",
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
```

| Field | Description |
| --- | --- |
| `name` | Project name. |
| `backend.port` | Backend port (default `8080`). |
| `frontend.directory` | Frontend directory (default `frontend`). |
| `frontend.port` | Frontend port (default `5173`). |
| `springflow.apiDirectory` | Where generated TypeScript files are synchronized (default `<frontend.directory>/src/api`). |

Legacy configurations using a flat `"frontend"` string and a
`"generated"` path are still understood by the CLI.

### Required Maven compiler options

For SpringFlow to work, the application's `pom.xml` must configure the
compiler plugin with:

```xml
<configuration>
    <release>21</release>
    <parameters>true</parameters>
    <annotationProcessorPaths>
        <path>
            <groupId>dev.springflow</groupId>
            <artifactId>springflow-processor</artifactId>
            <version>0.1.0-SNAPSHOT</version>
        </path>
    </annotationProcessorPaths>
</configuration>
```

- `<parameters>true</parameters>` ensures method parameter names are
  retained for query-parameter matching.
- The processor path registers `SpringFlowProcessor` as an annotation
  processor for the compilation.

---

## Repository Layout

```
springflow/
├── pom.xml                        # Parent reactor POM
├── README.md
├── LICENSE.txt
├── springflow-core/               # Annotations
├── springflow-processor/          # Annotation processor (TypeScript generation)
├── springflow-runtime/            # HTTP runtime (controller + registry)
├── springflow-boot-starter/       # Spring Boot integration
├── springflow-typescript/         # TypeScript generator utilities
├── springflow-cli/                # Project scaffolding CLI
├── springflow-demo/               # Example backend application
├── springflow-react/              # Example React frontend
├── springflow-ui/                 # UI package metadata
└── test-app/                      # Generated-project smoke test
```

---

## Limitations

- POST endpoint methods currently accept exactly one JSON body
  parameter (or zero).
- GET method resolution requires the number of query parameters to
  match the method signature exactly; overloaded methods with the same
  name are not distinguished.
- The generated `SpringFlowClient` uses relative URLs; the React demo
  keeps a hand-maintained variant with a `BASE_URL` constant.
- `@Allow`, `@SpringFlowEndpoint`, and `@SpringFlowMethod` are declared
  but not yet enforced by the processor or runtime.
- No authentication or authorization is applied to endpoints at runtime
  beyond Spring's default behavior.
