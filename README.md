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
