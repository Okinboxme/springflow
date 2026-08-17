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
