"use client";

export default function ConfirmDialog({ isOpen, title, message, confirmText = "Xác nhận", cancelText = "Hủy", confirmStyle = "danger", onConfirm, onCancel }) {
  if (!isOpen) return null;

  const confirmBtnClass =
    confirmStyle === "danger"
      ? "px-4 py-2 rounded-lg text-sm font-medium bg-red-600 text-white hover:bg-red-700 transition-colors"
      : "px-4 py-2 rounded-lg text-sm font-medium bg-blue-600 text-white hover:bg-blue-700 transition-colors";

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/40" onClick={onCancel} />
      <div
        className="relative z-10 w-full max-w-sm mx-4 p-6 rounded-xl shadow-xl"
        style={{ backgroundColor: "var(--card)", color: "var(--card-foreground)", border: "1px solid var(--border)" }}
      >
        <h3 className="text-lg font-semibold mb-2">{title}</h3>
        <p className="text-sm mb-6" style={{ color: "var(--muted-foreground)" }}>{message}</p>
        <div className="flex justify-end gap-3">
          <button
            onClick={onCancel}
            className="px-4 py-2 rounded-lg text-sm font-medium transition-colors"
            style={{ backgroundColor: "var(--accent)", color: "var(--accent-foreground)", border: "1px solid var(--border)" }}
          >
            {cancelText}
          </button>
          <button onClick={onConfirm} className={confirmBtnClass}>
            {confirmText}
          </button>
        </div>
      </div>
    </div>
  );
}