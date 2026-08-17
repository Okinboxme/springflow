import { useCallback, useEffect, useMemo, useState } from "react";
import { EmployeeEndpoint } from "../springflow/generated/dev/springflow/demo/employees/EmployeeEndpoint";
import type { Employee } from "../springflow/generated/dev/springflow/demo/employees/Employee";

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
