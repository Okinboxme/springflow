import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router';

import type { ViewConfig } from '@vaadin/hilla-file-router/types.js';

import EmployeeEndpoint from 'Frontend/generated/EmployeeEndpoint.js';
import type Employee from 'Frontend/generated/dev/springflow/demo/Employee.js';

export const config: ViewConfig = {
    title: 'Employees',
    menu: {
        title: 'Employees',
    },
};

export default function EmployeesView() {
    const navigate = useNavigate();

    const [employees, setEmployees] = useState<Employee[]>([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');

    async function loadEmployees() {
        try {
            setLoading(true);

            const result = await EmployeeEndpoint.findAll();

            setEmployees(result ?? []);
        } catch (error) {
            console.error('Failed to load employees:', error);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadEmployees();
    }, []);

    async function handleDelete(id: number) {
        const confirmed = window.confirm(
            'Are you sure you want to delete this employee?'
        );

        if (!confirmed) {
            return;
        }

        try {
            await EmployeeEndpoint.delete(id);
            await loadEmployees();
        } catch (error) {
            console.error('Failed to delete employee:', error);
            window.alert('Failed to delete employee.');
        }
    }

    const filteredEmployees = useMemo(() => {
        const value = search.trim().toLowerCase();

        if (!value) {
            return employees;
        }

        return employees.filter((employee) =>
            [
                employee.name,
                employee.email,
                employee.department,
            ].some((field) =>
                field?.toLowerCase().includes(value)
            )
        );
    }, [employees, search]);

    const activeCount = employees.filter(
        (employee) => employee.active
    ).length;

    const inactiveCount =
        employees.length - activeCount;

    const departmentCount = new Set(
        employees
            .map((employee) => employee.department)
            .filter(Boolean)
    ).size;

    return (
        <div className="min-h-screen bg-slate-50 p-6">

            {/* Header */}
            <div className="mb-6 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">

                <div>
                    <h1 className="text-3xl font-bold text-slate-900">
                        Employees
                    </h1>

                    <p className="mt-1 text-sm text-slate-500">
                        Manage your organization employees
                    </p>
                </div>

                <div className="flex gap-3">

                    <input
                        type="text"
                        value={search}
                        onChange={(event) =>
                            setSearch(event.target.value)
                        }
                        placeholder="Search employees..."
                        className="w-64 rounded-lg border border-slate-200 bg-white px-4 py-2.5 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                    />

                    <button
                        type="button"
                        onClick={() =>
                            navigate('/employees/new')
                        }
                        className="rounded-lg bg-blue-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-blue-700"
                    >
                        + Add Employee
                    </button>

                </div>
            </div>

            {/* Statistics */}
            <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">

                <StatCard
                    title="Total Employees"
                    value={employees.length}
                    icon="👥"
                />

                <StatCard
                    title="Active"
                    value={activeCount}
                    icon="✓"
                />

                <StatCard
                    title="Inactive"
                    value={inactiveCount}
                    icon="○"
                />

                <StatCard
                    title="Departments"
                    value={departmentCount}
                    icon="▦"
                />

            </div>

            {/* Employee table */}
            <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">

                <div className="border-b border-slate-200 px-6 py-4">
                    <h2 className="font-semibold text-slate-900">
                        Employee Directory
                    </h2>
                </div>

                {loading ? (

                    <div className="flex h-64 items-center justify-center text-sm text-slate-500">
                        Loading employees...
                    </div>

                ) : filteredEmployees.length === 0 ? (

                    <div className="flex h-64 flex-col items-center justify-center">
                        <div className="mb-3 text-4xl">
                            👥
                        </div>

                        <p className="font-medium text-slate-900">
                            No employees found
                        </p>

                        <p className="mt-1 text-sm text-slate-500">
                            Add your first employee.
                        </p>
                    </div>

                ) : (

                    <div className="overflow-x-auto">

                        <table className="w-full">

                            <thead className="bg-slate-50">

                                <tr className="text-left text-xs font-semibold uppercase tracking-wide text-slate-500">

                                    <th className="px-6 py-4">
                                        ID
                                    </th>

                                    <th className="px-6 py-4">
                                        Employee
                                    </th>

                                    <th className="px-6 py-4">
                                        Email
                                    </th>

                                    <th className="px-6 py-4">
                                        Department
                                    </th>

                                    <th className="px-6 py-4">
                                        Status
                                    </th>

                                    <th className="px-6 py-4 text-right">
                                        Actions
                                    </th>

                                </tr>

                            </thead>

                            <tbody className="divide-y divide-slate-100">

                                {filteredEmployees.map(
                                    (employee) => (

                                        <tr
                                            key={employee.id}
                                            className="transition hover:bg-slate-50"
                                        >

                                            <td className="px-6 py-4 text-sm text-slate-500">
                                                #{employee.id}
                                            </td>

                                            <td className="px-6 py-4">

                                                <button
                                                    type="button"
                                                    onClick={() =>
                                                        navigate(
                                                            `/employees/${employee.id}`
                                                        )
                                                    }
                                                    className="flex items-center gap-3 text-left"
                                                >

                                                    <div className="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100 font-semibold text-blue-700">
                                                        {employee.name
                                                            ?.charAt(0)
                                                            .toUpperCase()}
                                                    </div>

                                                    <div>
                                                        <div className="font-medium text-slate-900">
                                                            {employee.name}
                                                        </div>

                                                        <div className="text-xs text-slate-400">
                                                            Employee #{employee.id}
                                                        </div>
                                                    </div>

                                                </button>

                                            </td>

                                            <td className="px-6 py-4 text-sm text-slate-600">
                                                {employee.email}
                                            </td>

                                            <td className="px-6 py-4 text-sm text-slate-600">
                                                {employee.department}
                                            </td>

                                            <td className="px-6 py-4">

                                                {employee.active ? (

                                                    <span className="inline-flex rounded-full bg-green-50 px-3 py-1 text-xs font-semibold text-green-700">
                                                        ● Active
                                                    </span>

                                                ) : (

                                                    <span className="inline-flex rounded-full bg-red-50 px-3 py-1 text-xs font-semibold text-red-700">
                                                        ● Inactive
                                                    </span>

                                                )}

                                            </td>

                                            <td className="px-6 py-4">

                                                <div className="flex justify-end gap-2">

                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            navigate(
                                                                `/employees/${employee.id}/edit`
                                                            )
                                                        }
                                                        className="rounded-lg bg-blue-50 px-3 py-2 text-sm font-medium text-blue-600 hover:bg-blue-100"
                                                    >
                                                        Edit
                                                    </button>

                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            handleDelete(
                                                                employee.id!
                                                            )
                                                        }
                                                        className="rounded-lg bg-red-50 px-3 py-2 text-sm font-medium text-red-600 hover:bg-red-100"
                                                    >
                                                        Delete
                                                    </button>

                                                </div>

                                            </td>

                                        </tr>

                                    )
                                )}

                            </tbody>

                        </table>

                    </div>

                )}

                <div className="border-t border-slate-200 px-6 py-4 text-sm text-slate-500">
                    Showing {filteredEmployees.length} of{' '}
                    {employees.length} employees
                </div>

            </div>

        </div>
    );
}

function StatCard({
    title,
    value,
    icon,
}: {
    title: string;
    value: number;
    icon: string;
}) {
    return (
        <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">

            <div className="flex items-center justify-between">

                <div>
                    <p className="text-sm text-slate-500">
                        {title}
                    </p>

                    <p className="mt-2 text-2xl font-bold text-slate-900">
                        {value}
                    </p>
                </div>

                <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-blue-50 text-lg text-blue-600">
                    {icon}
                </div>

            </div>

        </div>
    );
}