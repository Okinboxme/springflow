package dev.springflow.demo.employees;

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

    // CREATE
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

    // READ ALL
    @EndpointMethod
    public List<Employee> findAll() {
        return new ArrayList<>(employees);
    }

    // READ ONE
    @EndpointMethod
    public Employee findById(Long id) {

        return employees.stream()
                .filter(employee -> employee.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // UPDATE
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

    // DELETE
    @EndpointMethod
    public boolean delete(Long id) {

        return employees.removeIf(
                employee -> employee.getId().equals(id)
        );
    }
}