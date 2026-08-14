# SpringFlow

> A Spring Boot framework for building backend APIs with automatically
> generated TypeScript clients and React frontends.

SpringFlow connects:

Java → Spring Boot → TypeScript → React

The goal is to provide a development experience similar to modern
full-stack frameworks while keeping Spring Boot at the core.

---

## Features

- Spring Boot integration
- Java endpoint annotations
- Automatic TypeScript client generation
- Automatic TypeScript model generation
- React frontend project generation
- Vite frontend
- TypeScript support
- Automatic Java package → TypeScript folder mapping
- Generated API clients
- CLI project creation
- Maven integration

---

## Architecture

SpringFlow consists of several modules:

springflow-core
    Core annotations and API definitions.

springflow-processor
    Java annotation processor responsible for generating TypeScript.

springflow-runtime
    Runtime responsible for exposing SpringFlow endpoints.

springflow-boot-starter
    Spring Boot integration.

springflow-typescript
    TypeScript client/runtime support.

springflow-cli
    Command-line project generator.

---

## Example

Java:

```java
package com.example.demo.employees;

import dev.springflow.core.annotation.Endpoint;
import dev.springflow.core.annotation.EndpointMethod;

@Endpoint
public class EmployeeEndpoint {

    @EndpointMethod
    public List<Employee> findAll() {
        return employees;
    }
}