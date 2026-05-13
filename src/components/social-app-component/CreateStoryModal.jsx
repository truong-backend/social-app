"use client";
import { useState, useRef } from "react";
import { X, Upload, Image, Type, Loader2 } from "lucide-react";

const BG_COLORS = [
  "#1a1a2e", "#16213e", "#0f3460",
  "#e94560", "#533483", "#2b2d42",
  "#ef233c", "#8d99ae", "#06d6a0",
  "#118ab2", "#ffd166", "#ef476f",
];

/**
 * CreateStoryModal — modal tạo story mới
 */
export default function CreateStoryModal({ open, onClose, onSubmit, isCreating }) {
  const [tab, setTab] = useState("photo"); // "photo" | "text"
  const [preview, setPreview] = useState(null);
  const [mediaFile, setMediaFile] = useState(null);
  const [caption, setCaption] = useState("");
  const [bgColor, setBgColor] = useState(BG_COLORS[0]);
  const fileInputRef = useRef(null);

  if (!open) return null;

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setMediaFile(file);
    const reader = new FileReader();
    reader.onload = (ev) => setPreview(ev.target.result);
    reader.readAsDataURL(file);
    setTab("photo");
  };

  const handleSubmit = () => {
    if (tab === "photo" && !mediaFile && !preview) return;
    if (tab === "text" && !caption.trim()) return;
    onSubmit({
      mediaFile: tab === "photo" ? mediaFile : null,
      mediaUrl: null,
      caption,
      bgColor: tab === "text" ? bgColor : null,
    });
  };

  const handleClose = () => {
    setPreview(null);
    setMediaFile(null);
    setCaption("");
    setBgColor(BG_COLORS[0]);
    setTab("photo");
    onClose();
  };

  const canSubmit =
    !isCreating &&
    ((tab === "photo" && (mediaFile || preview)) ||
      (tab === "text" && caption.trim()));

  return (
    <div
      className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/70 backdrop-blur-sm"
      onClick={(e) => e.target === e.currentTarget && handleClose()}
    >
      <div className="bg-[var(--background)] rounded-2xl shadow-2xl w-full max-w-lg mx-4 overflow-hidden border border-[var(--border)]">
        {/* Header */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-[var(--border)]">
          <h2 className="font-bold text-lg text-[var(--foreground)]">Tạo story mới</h2>
          <button
            onClick={handleClose}
            className="w-8 h-8 rounded-full hover:bg-[var(--card)] flex items-center justify-center text-[var(--foreground)] transition-colors"
          >
            <X size={18} />
          </button>
        </div>

        {/* Tabs */}
        <div className="flex border-b border-[var(--border)]">
          <TabBtn active={tab === "photo"} onClick={() => setTab("photo")} icon={<Image size={16} />} label="Ảnh / Video" />
          <TabBtn active={tab === "text"} onClick={() => setTab("text")} icon={<Type size={16} />} label="Văn bản" />
        </div>

        {/* Body */}
        <div className="p-5">
          {tab === "photo" && (
            <div className="space-y-4">
              {/* Preview */}
              {preview ? (
                <div className="relative rounded-xl overflow-hidden bg-black aspect-[9/16] max-h-72 flex items-center justify-center">
                  <img src={preview} alt="preview" className="w-full h-full object-contain" />
                  <button
                    onClick={() => { setPreview(null); setMediaFile(null); }}
                    className="absolute top-2 right-2 w-7 h-7 rounded-full bg-black/60 flex items-center justify-center text-white"
                  >
                    <X size={14} />
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => fileInputRef.current?.click()}
                  className="w-full aspect-video rounded-xl border-2 border-dashed border-[var(--border)] flex flex-col items-center justify-center gap-3 hover:border-blue-500 hover:bg-blue-50/5 transition-colors text-[var(--foreground)]"
                >
                  <Upload size={32} className="opacity-50" />
                  <div className="text-center">
                    <p className="font-semibold text-sm">Chọn ảnh hoặc video</p>
                    <p className="text-xs opacity-50 mt-0.5">PNG, JPG, MP4 — tối đa 50MB</p>
                  </div>
                </button>
              )}
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*,video/*"
                className="hidden"
                onChange={handleFileChange}
              />

              {/* Caption */}
              <div>
                <label className="block text-xs font-semibold text-[var(--foreground)] opacity-60 mb-1.5">
                  Chú thích (tuỳ chọn)
                </label>
                <textarea
                  value={caption}
                  onChange={(e) => setCaption(e.target.value)}
                  placeholder="Thêm chú thích cho story..."
                  maxLength={150}
                  rows={2}
                  className="w-full rounded-xl border border-[var(--border)] bg-[var(--card)] text-[var(--foreground)] px-3 py-2 text-sm resize-none outline-none focus:border-blue-500 transition-colors"
                />
              </div>
            </div>
          )}

          {tab === "text" && (
            <div className="space-y-4">
              {/* Live preview */}
              <div
                className="w-full rounded-xl aspect-[9/16] max-h-64 flex items-center justify-center p-6"
                style={{ backgroundColor: bgColor }}
              >
                <p className="text-white text-xl font-bold text-center leading-snug break-words max-w-full">
                  {caption || "Nhập văn bản..."}
                </p>
              </div>

              {/* Text input */}
              <textarea
                value={caption}
                onChange={(e) => setCaption(e.target.value)}
                placeholder="Nhập nội dung story..."
                maxLength={200}
                rows={3}
                className="w-full rounded-xl border border-[var(--border)] bg-[var(--card)] text-[var(--foreground)] px-3 py-2 text-sm resize-none outline-none focus:border-blue-500 transition-colors"
              />

              {/* Color picker */}
              <div>
                <p className="text-xs font-semibold text-[var(--foreground)] opacity-60 mb-2">Màu nền</p>
                <div className="flex flex-wrap gap-2">
                  {BG_COLORS.map((c) => (
                    <button
                      key={c}
                      onClick={() => setBgColor(c)}
                      className="w-8 h-8 rounded-full border-2 transition-all"
                      style={{
                        backgroundColor: c,
                        borderColor: bgColor === c ? "#fff" : "transparent",
                        boxShadow: bgColor === c ? "0 0 0 2px " + c : "none",
                        transform: bgColor === c ? "scale(1.2)" : "scale(1)",
                      }}
                    />
                  ))}
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Note về 24h */}
        <div className="px-5 pb-2">
          <p className="text-xs text-[var(--foreground)] opacity-40 text-center">
            Story tự động ẩn sau 24 giờ
          </p>
        </div>

        {/* Actions */}
        <div className="flex gap-2 px-5 pb-5">
          <button
            onClick={handleClose}
            className="flex-1 py-2.5 rounded-xl border border-[var(--border)] text-sm font-semibold text-[var(--foreground)] hover:bg-[var(--card)] transition-colors"
          >
            Huỷ
          </button>
          <button
            onClick={handleSubmit}
            disabled={!canSubmit}
            className="flex-1 py-2.5 rounded-xl bg-blue-500 text-white text-sm font-semibold hover:bg-blue-600 disabled:opacity-40 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2"
          >
            {isCreating ? (
              <>
                <Loader2 size={16} className="animate-spin" />
                Đang đăng...
              </>
            ) : (
              "Đăng story"
            )}
          </button>
        </div>
      </div>
    </div>
  );
}

function TabBtn({ active, onClick, icon, label }) {
  return (
    <button
      onClick={onClick}
      className={
        "flex-1 flex items-center justify-center gap-2 py-3 text-sm font-semibold border-b-2 transition-colors " +
        (active
          ? "border-blue-500 text-blue-500"
          : "border-transparent text-[var(--foreground)] opacity-50 hover:opacity-80")
      }
    >
      {icon}
      {label}
    </button>
  );
}