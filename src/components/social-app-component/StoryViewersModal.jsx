"use client";
import { X, Eye } from "lucide-react";
import { normalizeFileUrl } from "@/utils/normalizeFileUrl";

/**
 * StoryViewersModal — hiển thị danh sách người đã xem story
 */
export default function StoryViewersModal({ open, onClose, viewers = [] }) {
  if (!open) return null;

  const formatTime = (iso) => {
    const d = new Date(iso);
    const now = Date.now();
    const diff = now - d.getTime();
    const mins = Math.floor(diff / 60000);
    if (mins < 60) return `${mins} phút trước`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return `${hrs} giờ trước`;
    return `${Math.floor(hrs / 24)} ngày trước`;
  };

  return (
    <div
      className="fixed inset-0 z-[10000] flex items-end sm:items-center justify-center bg-black/50 backdrop-blur-sm"
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      <div className="bg-[var(--background)] rounded-t-2xl sm:rounded-2xl w-full max-w-sm border border-[var(--border)] shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-[var(--border)]">
          <div className="flex items-center gap-2">
            <Eye size={18} className="text-blue-500" />
            <span className="font-semibold text-[var(--foreground)]">
              {viewers.length} lượt xem
            </span>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full hover:bg-[var(--card)] flex items-center justify-center text-[var(--foreground)] transition-colors"
          >
            <X size={16} />
          </button>
        </div>

        {/* List */}
        <div className="max-h-80 overflow-y-auto">
          {viewers.length === 0 ? (
            <div className="py-12 text-center">
              <Eye size={32} className="mx-auto text-[var(--foreground)] opacity-20 mb-3" />
              <p className="text-[var(--foreground)] opacity-50 text-sm">Chưa có ai xem story này</p>
            </div>
          ) : (
            viewers.map((v) => {
              const avatarUrl = normalizeFileUrl(v.avatar);
              return (
                <div key={v.userId} className="flex items-center gap-3 px-5 py-3 hover:bg-[var(--card)] transition-colors">
                  {avatarUrl ? (
                    <img
                      src={avatarUrl}
                      alt={v.displayName}
                      className="w-10 h-10 rounded-full object-cover flex-shrink-0"
                      onError={(e) => {
                        e.target.style.display = "none";
                        e.target.nextSibling && (e.target.nextSibling.style.display = "flex");
                      }}
                    />
                  ) : null}
                  <div
                    className="w-10 h-10 rounded-full flex-shrink-0 flex items-center justify-center text-sm font-bold text-white"
                    style={{
                      display: avatarUrl ? "none" : "flex",
                      background: "linear-gradient(135deg, #833ab4, #fd1d1d, #fcb045)",
                    }}
                  >
                    {v.displayName?.charAt(0)?.toUpperCase() || "?"}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-sm text-[var(--foreground)] truncate">{v.displayName}</p>
                    <p className="text-xs text-[var(--foreground)] opacity-50">@{v.username}</p>
                  </div>
                  <span className="text-xs text-[var(--foreground)] opacity-40 flex-shrink-0">
                    {formatTime(v.viewedAt)}
                  </span>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}