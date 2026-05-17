"use client";
import { useState, useRef } from "react";
import {
  X, Upload, Image, Type, Loader2, Play,
  CheckCircle, Plus, ChevronLeft,
} from "lucide-react";

const BG_COLORS = [
  "#1a1a2e", "#16213e", "#0f3460",
  "#e94560", "#533483", "#2b2d42",
  "#ef233c", "#8d99ae", "#06d6a0",
  "#118ab2", "#ffd166", "#ef476f",
];

const EMPTY_DRAFT = {
  tab: "photo",
  preview: null,
  previewType: null,
  mediaFile: null,
  caption: "",
  bgColor: BG_COLORS[0],
};

export default function CreateStoryModal({ open, onClose, onSubmit, isCreating }) {
  const [queue, setQueue] = useState([]);
  const [draft, setDraft] = useState(EMPTY_DRAFT);
  const [previewIdx, setPreviewIdx] = useState(null);
  const [postingIdx, setPostingIdx] = useState(null);
  const [postedCount, setPostedCount] = useState(0);
  const [allDone, setAllDone] = useState(false);
  const fileInputRef = useRef(null);

  // ← KHÔNG return null trước hooks nữa, dùng conditional render bên dưới
  if (!open) return null;

  const updateDraft = (patch) => setDraft((d) => ({ ...d, ...patch }));

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const type = file.type.startsWith("video/") ? "video" : "image";
    const reader = new FileReader();
    reader.onload = (ev) =>
      updateDraft({ mediaFile: file, preview: ev.target.result, previewType: type, tab: "photo" });
    reader.readAsDataURL(file);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const canAddDraft =
    (draft.tab === "photo" && (draft.mediaFile || draft.preview)) ||
    (draft.tab === "text" && draft.caption.trim());

  const addToQueue = () => {
    if (!canAddDraft) return;
    setQueue((q) => [...q, { ...draft }]);
    setDraft(EMPTY_DRAFT);
    setPreviewIdx(null);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const removeFromQueue = (idx) => {
    setQueue((q) => q.filter((_, i) => i !== idx));
    if (previewIdx === idx) setPreviewIdx(null);
    else if (previewIdx !== null && previewIdx > idx) setPreviewIdx((p) => p - 1);
  };

  const handleSubmitAll = async () => {
    const finalQueue = canAddDraft ? [...queue, { ...draft }] : [...queue];
    if (finalQueue.length === 0) return;

    setPostedCount(0);
    setAllDone(false);

    for (let i = 0; i < finalQueue.length; i++) {
      const item = finalQueue[i];
      setPostingIdx(i);
      await new Promise((resolve) => {
        onSubmit(
          {
            mediaFile: item.tab === "photo" ? item.mediaFile : null,
            caption: item.caption,
            bgColor: item.tab === "text" ? item.bgColor : null,
          },
          () => resolve()
        );
        setTimeout(resolve, 8000);
      });
      setPostedCount((c) => c + 1);
    }

    setPostingIdx(null);
    setAllDone(true);
    setQueue([]);
    setDraft(EMPTY_DRAFT);
    setTimeout(() => {
      setAllDone(false);
      onClose();
    }, 1500);
  };

  const handleClose = () => {
    setQueue([]);
    setDraft(EMPTY_DRAFT);
    setPreviewIdx(null);
    setPostingIdx(null);
    setPostedCount(0);
    setAllDone(false);
    onClose();
  };

  const totalToPost = queue.length + (canAddDraft ? 1 : 0);
  const isPosting = postingIdx !== null;
  const viewing = previewIdx !== null ? queue[previewIdx] : null;

  return (
    <div
      className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/70 backdrop-blur-sm"
      onClick={(e) => e.target === e.currentTarget && !isPosting && handleClose()}
    >
      <div className="bg-[var(--background)] rounded-2xl shadow-2xl w-full max-w-lg mx-4 overflow-hidden border border-[var(--border)]">
        {/* Header */}
        <div className="flex items-center justify-between px-5 py-4 border-b border-[var(--border)]">
          <div className="flex items-center gap-2">
            <h2 className="font-bold text-lg text-[var(--foreground)]">Tạo story</h2>
            {allDone && (
              <span className="flex items-center gap-1 text-green-500 text-sm font-semibold">
                <CheckCircle size={15} /> Đã đăng {postedCount} story!
              </span>
            )}
            {isPosting && (
              <span className="text-blue-500 text-sm font-semibold">
                Đang đăng {postingIdx + 1}/{queue.length + (canAddDraft ? 1 : 0)}...
              </span>
            )}
          </div>
          <button
            onClick={handleClose}
            disabled={isPosting}
            className="w-8 h-8 rounded-full hover:bg-[var(--card)] flex items-center justify-center text-[var(--foreground)] transition-colors disabled:opacity-40"
          >
            <X size={18} />
          </button>
        </div>

        {/* Queue thumbnails */}
        {(queue.length > 0 || canAddDraft) && (
          <div className="px-5 pt-3 pb-0">
            <div className="flex items-center gap-1 mb-2">
              <span className="text-xs font-semibold text-[var(--foreground)] opacity-60">Hàng chờ</span>
              <span className="ml-1 text-xs font-bold text-blue-500">{totalToPost} story</span>
            </div>
            <div className="flex gap-2 overflow-x-auto pb-2" style={{ scrollbarWidth: "none" }}>
              {queue.map((item, idx) => (
                <QueueThumb
                  key={idx}
                  item={item}
                  index={idx}
                  isActive={previewIdx === idx}
                  isPosting={postingIdx === idx}
                  isDone={postingIdx !== null && idx < postingIdx}
                  onSelect={() => setPreviewIdx(previewIdx === idx ? null : idx)}
                  onRemove={() => removeFromQueue(idx)}
                  disabled={isPosting}
                />
              ))}
              {canAddDraft && (
                <QueueThumb
                  item={draft}
                  index={queue.length}
                  isActive={previewIdx === null}
                  isPosting={isPosting && postingIdx === queue.length}
                  isDone={false}
                  onSelect={() => setPreviewIdx(null)}
                  onRemove={null}
                  isCurrent
                  disabled={isPosting}
                />
              )}
            </div>
          </div>
        )}

        {/* Body — soạn draft mới */}
        {previewIdx === null && (
          <>
            <div className="flex border-b border-[var(--border)] mt-2">
              <TabBtn active={draft.tab === "photo"} onClick={() => updateDraft({ tab: "photo" })} icon={<Image size={16} />} label="Ảnh / Video" />
              <TabBtn active={draft.tab === "text"} onClick={() => updateDraft({ tab: "text" })} icon={<Type size={16} />} label="Văn bản" />
            </div>

            <div className="p-5">
              {draft.tab === "photo" && (
                <div className="space-y-4">
                  {draft.preview ? (
                    <div className="relative rounded-xl overflow-hidden bg-black aspect-[9/16] max-h-72 flex items-center justify-center">
                      {draft.previewType === "video" ? (
                        <video src={draft.preview} className="w-full h-full object-cover" controls muted playsInline />
                      ) : (
                        <img src={draft.preview} alt="preview" className="w-full h-full object-cover" />
                      )}
                      <button
                        onClick={() => updateDraft({ preview: null, mediaFile: null, previewType: null })}
                        className="absolute top-2 right-2 w-7 h-7 rounded-full bg-black/60 flex items-center justify-center text-white z-10"
                      >
                        <X size={14} />
                      </button>
                      {draft.previewType === "video" && (
                        <div className="absolute bottom-2 left-2 bg-black/60 text-white text-xs px-2 py-1 rounded flex items-center gap-1">
                          <Play size={10} /> Video
                        </div>
                      )}
                    </div>
                  ) : (
                    <button
                      onClick={() => fileInputRef.current?.click()}
                      className="w-full aspect-video rounded-xl border-2 border-dashed border-[var(--border)] flex flex-col items-center justify-center gap-3 hover:border-blue-500 hover:bg-blue-50/5 transition-colors text-[var(--foreground)]"
                    >
                      <Upload size={32} className="opacity-50" />
                      <div className="text-center">
                        <p className="font-semibold text-sm">Chọn ảnh hoặc video</p>
                        <p className="text-xs opacity-50 mt-0.5">PNG, JPG, MP4, MOV — tối đa 200MB</p>
                      </div>
                    </button>
                  )}
                  <input ref={fileInputRef} type="file" accept="image/*,video/*" className="hidden" onChange={handleFileChange} />
                  <div>
                    <label className="block text-xs font-semibold text-[var(--foreground)] opacity-60 mb-1.5">
                      Chú thích (tuỳ chọn)
                    </label>
                    <textarea
                      value={draft.caption}
                      onChange={(e) => updateDraft({ caption: e.target.value })}
                      placeholder="Thêm chú thích cho story..."
                      maxLength={150}
                      rows={2}
                      className="w-full rounded-xl border border-[var(--border)] bg-[var(--card)] text-[var(--foreground)] px-3 py-2 text-sm resize-none outline-none focus:border-blue-500 transition-colors"
                    />
                  </div>
                </div>
              )}

              {draft.tab === "text" && (
                <div className="space-y-4">
                  <div
                    className="w-full rounded-xl aspect-[9/16] max-h-64 flex items-center justify-center p-6"
                    style={{ backgroundColor: draft.bgColor }}
                  >
                    <p className="text-white text-xl font-bold text-center leading-snug break-words max-w-full">
                      {draft.caption || "Nhập văn bản..."}
                    </p>
                  </div>
                  <textarea
                    value={draft.caption}
                    onChange={(e) => updateDraft({ caption: e.target.value })}
                    placeholder="Nhập nội dung story..."
                    maxLength={200}
                    rows={3}
                    className="w-full rounded-xl border border-[var(--border)] bg-[var(--card)] text-[var(--foreground)] px-3 py-2 text-sm resize-none outline-none focus:border-blue-500 transition-colors"
                  />
                  <div>
                    <p className="text-xs font-semibold text-[var(--foreground)] opacity-60 mb-2">Màu nền</p>
                    <div className="flex flex-wrap gap-2">
                      {BG_COLORS.map((c) => (
                        <button
                          key={c}
                          onClick={() => updateDraft({ bgColor: c })}
                          className="w-8 h-8 rounded-full border-2 transition-all"
                          style={{
                            backgroundColor: c,
                            borderColor: draft.bgColor === c ? "#fff" : "transparent",
                            boxShadow: draft.bgColor === c ? "0 0 0 2px " + c : "none",
                            transform: draft.bgColor === c ? "scale(1.2)" : "scale(1)",
                          }}
                        />
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>
          </>
        )}

        {/* Body — xem preview story trong queue */}
        {viewing !== null && (
          <div className="p-5">
            <button
              onClick={() => setPreviewIdx(null)}
              className="text-xs text-blue-500 font-semibold flex items-center gap-1 hover:underline mb-3"
            >
              <ChevronLeft size={14} /> Quay lại soạn
            </button>
            <div className="relative rounded-xl overflow-hidden bg-black aspect-[9/16] max-h-72 flex items-center justify-center">
              {viewing.tab === "photo" && viewing.preview ? (
                viewing.previewType === "video" ? (
                  <video src={viewing.preview} className="w-full h-full object-cover" controls muted playsInline />
                ) : (
                  <img src={viewing.preview} alt="preview" className="w-full h-full object-cover" />
                )
              ) : (
                <div
                  className="absolute inset-0 flex items-center justify-center p-6"
                  style={{ backgroundColor: viewing.bgColor }}
                >
                  <p className="text-white text-xl font-bold text-center leading-snug">{viewing.caption}</p>
                </div>
              )}
            </div>
            {viewing.caption && viewing.tab === "photo" && (
              <p className="mt-2 text-sm text-[var(--foreground)] opacity-60 text-center">{viewing.caption}</p>
            )}
          </div>
        )}

        {/* Footer */}
        <div className="px-5 pb-1">
          <p className="text-xs text-[var(--foreground)] opacity-40 text-center">
            Story tự động ẩn sau 24 giờ · Thêm nhiều story rồi đăng cùng lúc
          </p>
        </div>

        {/* Buttons */}
        <div className="flex gap-2 px-5 pb-5 pt-2">
          <button
            onClick={handleClose}
            disabled={isPosting}
            className="flex-1 py-2.5 rounded-xl border border-[var(--border)] text-sm font-semibold text-[var(--foreground)] hover:bg-[var(--card)] transition-colors disabled:opacity-40"
          >
            Đóng
          </button>

          {canAddDraft && previewIdx === null && (
            <button
              onClick={addToQueue}
              disabled={isPosting}
              className="flex-1 py-2.5 rounded-xl border border-blue-500 text-blue-500 text-sm font-semibold hover:bg-blue-500/10 transition-colors flex items-center justify-center gap-2 disabled:opacity-40"
            >
              <Plus size={16} />
              Thêm story
            </button>
          )}

          <button
            onClick={handleSubmitAll}
            disabled={isPosting || totalToPost === 0}
            className="flex-1 py-2.5 rounded-xl bg-blue-500 text-white text-sm font-semibold hover:bg-blue-600 disabled:opacity-40 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2"
          >
            {isPosting ? (
              <>
                <Loader2 size={16} className="animate-spin" />
                {postingIdx + 1}/{queue.length + (canAddDraft ? 1 : 0)}
              </>
            ) : (
              `Đăng ${totalToPost > 1 ? `${totalToPost} story` : "story"}`
            )}
          </button>
        </div>
      </div>
    </div>
  );
}

function QueueThumb({ item, index, isActive, isPosting, isDone, onSelect, onRemove, isCurrent, disabled }) {
  return (
    <div
      className={`relative flex-shrink-0 w-14 h-20 rounded-xl overflow-hidden cursor-pointer border-2 transition-all ${
        isActive ? "border-blue-500 scale-105" : "border-transparent opacity-80 hover:opacity-100"
      }`}
      onClick={!disabled ? onSelect : undefined}
    >
      {item.tab === "photo" && item.preview ? (
        item.previewType === "video" ? (
          <video src={item.preview} className="w-full h-full object-cover" muted playsInline preload="metadata" />
        ) : (
          <img src={item.preview} alt="" className="w-full h-full object-cover" />
        )
      ) : (
        <div className="w-full h-full flex items-center justify-center p-1" style={{ backgroundColor: item.bgColor || "#1a1a2e" }}>
          <p className="text-white text-[8px] font-semibold text-center line-clamp-3 leading-tight">{item.caption}</p>
        </div>
      )}

      {isPosting && (
        <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
          <Loader2 size={16} className="text-white animate-spin" />
        </div>
      )}
      {isDone && (
        <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
          <CheckCircle size={16} className="text-green-400" />
        </div>
      )}

      <div className="absolute top-0.5 left-0.5 bg-black/60 text-white text-[8px] font-bold rounded px-1">
        {index + 1}
      </div>
      {isCurrent && (
        <div className="absolute bottom-0.5 right-0.5 bg-blue-500 text-white text-[8px] font-bold rounded px-1">
          mới
        </div>
      )}
      {onRemove && !disabled && (
        <button
          onClick={(e) => { e.stopPropagation(); onRemove(); }}
          className="absolute top-0.5 right-0.5 w-4 h-4 rounded-full bg-black/70 flex items-center justify-center text-white hover:bg-red-500 transition-colors"
        >
          <X size={9} />
        </button>
      )}
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