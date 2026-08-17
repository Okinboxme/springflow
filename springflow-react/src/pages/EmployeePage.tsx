import { useCallback, useState } from "react";
import type { Employee } from "../springflow/generated/dev/springflow/demo/employees/Employee";
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
