import { useEffect, useState } from "react";
import { EmployeeEndpoint } from "../springflow/generated/dev/springflow/demo/employees/EmployeeEndpoint";
import type { Employee } from "../springflow/generated/dev/springflow/demo/employees/Employee";

function EmployeePage() {
  const [employees, setEmployees] = useState<Employee[]>([]);

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [department, setDepartment] = useState("");
  const [active, setActive] = useState(true);

  const [editingId, setEditingId] = useState<number | null>(null);

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    loadEmployees();
  }, []);

  async function loadEmployees() {
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
  }

  function resetForm() {
    setName("");
    setEmail("");
    setDepartment("");
    setActive(true);
    setEditingId(null);
  }

  function editEmployee(employee: Employee) {
    setEditingId(employee.id);
    setName(employee.name);
    setEmail(employee.email);
    setDepartment(employee.department);
    setActive(employee.active);
  }

  async function saveEmployee() {
    if (!name.trim()) {
      setError("Employee name is required.");
      return;
    }

    if (!email.trim()) {
      setError("Employee email is required.");
      return;
    }

    if (!department.trim()) {
      setError("Department is required.");
      return;
    }

    setSaving(true);
    setError("");

    try {
      if (editingId === null) {
        await EmployeeEndpoint.create(
          name,
          email,
          department
        );
      } else {
        await EmployeeEndpoint.update(
          editingId,
          name,
          email,
          department,
          active
        );
      }

      resetForm();

      await loadEmployees();
    } catch (err) {
      console.error(err);

      setError(
        err instanceof Error
          ? err.message
          : "Failed to save employee"
      );
    } finally {
      setSaving(false);
    }
  }

  async function deleteEmployee(id: number) {
    const confirmed = window.confirm(
      "Are you sure you want to delete this employee?"
    );

    if (!confirmed) {
      return;
    }

    setError("");

    try {
      await EmployeeEndpoint.delete(id);

      await loadEmployees();
    } catch (err) {
      console.error(err);

      setError(
        err instanceof Error
          ? err.message
          : "Failed to delete employee"
      );
    }
  }

  return (
    <div className="min-h-screen bg-slate-50 p-8">

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

        {/* Error */}
        {error && (
          <div className="mb-6 rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
            {error}
          </div>
        )}

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
                  onChange={(e) => setName(e.target.value)}
                  placeholder="John Doe"
                  className="w-full rounded-lg border border-slate-300 px-4 py-3 outline-none focus:border-blue-500"
                />
              </div>

              {/* Email */}
              <div>
                <label className="mb-1 block text-sm font-medium text-slate-700">
                  Email
                </label>

                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="john@example.com"
                  className="w-full rounded-lg border border-slate-300 px-4 py-3 outline-none focus:border-blue-500"
                />
              </div>

              {/* Department */}
              <div>
                <label className="mb-1 block text-sm font-medium text-slate-700">
                  Department
                </label>

                <input
                  value={department}
                  onChange={(e) => setDepartment(e.target.value)}
                  placeholder="ICT"
                  className="w-full rounded-lg border border-slate-300 px-4 py-3 outline-none focus:border-blue-500"
                />
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
                  onClick={saveEmployee}
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

            <div className="flex items-center justify-between border-b p-6">

              <div>
                <h2 className="text-xl font-semibold text-slate-900">
                  Employee List
                </h2>

                <p className="mt-1 text-sm text-slate-500">
                  {employees.length} employee
                  {employees.length === 1 ? "" : "s"}
                </p>
              </div>

              <button
                onClick={loadEmployees}
                disabled={loading}
                className="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50"
              >
                {loading ? "Loading..." : "Refresh"}
              </button>

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
                        No employees found.
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

                        <span
                          className={
                            employee.active
                              ? "rounded-full bg-green-100 px-3 py-1 text-xs font-medium text-green-700"
                              : "rounded-full bg-slate-100 px-3 py-1 text-xs font-medium text-slate-600"
                          }
                        >
                          {employee.active
                            ? "Active"
                            : "Inactive"}
                        </span>

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
                              deleteEmployee(employee.id)
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