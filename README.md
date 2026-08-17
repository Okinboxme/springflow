# SpringFlow

> A Spring Boot framework for building type-safe React frontends from Java endpoints.

SpringFlow connects **Spring Boot**, **Java**, and **React + TypeScript** by automatically generating TypeScript API clients and model interfaces from annotated Java endpoints.

The goal is to provide a development experience similar to frameworks such as Hilla while keeping Spring Boot at the center of the application.

---

## 🚀 What is SpringFlow?

SpringFlow allows you to define your backend API using normal Java methods and annotations.

For example:

```java
@Endpoint
public class EmployeeEndpoint {

    @EndpointMethod
    public List<Employee> findAll() {
        return employees;
    }

    @EndpointMethod
    public Employee findById(Long id) {
        return findEmployee(id);
    }

    @EndpointMethod
    public Employee create(
            String name,
            String email,
            String department
    ) {
        // ...
    }
}

---

## 🛠️ Command-Line Interface

The CLI orchestrates the full application lifecycle. Inside a project it
generates TypeScript and keeps `frontend/src/api/` synchronized with
your Java endpoints.

```
springflow create my-app     # scaffold a new full-stack project
springflow generate          # generate + sync TypeScript into frontend/src/api
springflow dev               # generate, sync, then run backend + frontend
springflow build             # generate, sync, build frontend, package backend
springflow version           # show version
springflow help              # show help
```

```bash
springflow create my-app
cd my-app
springflow dev
```
