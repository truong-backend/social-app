"use client";
import { useState, useEffect, useRef, useCallback } from "react";
import {
  X,
  ChevronLeft,
  ChevronRight,
  MoreHorizontal,
  Send,
  Eye,
  Trash2,
  Heart,
  Smile,
} from "lucide-react";

const STORY_DURATION = 5000; // 5 giây mỗi story
const REACTIONS = ["❤️", "😍", "😂", "😮", "😢", "👏", "🔥"];

/**
 * StoryViewer — full-screen story viewer kiểu Instagram
 */
export default function StoryViewer({
  friendGroups = [],
  myStories = [],
  activeGroupIndex,
  activeStoryIndex,
  viewingMyStory,
  onClose,
  onNext,
  onPrev,
  onReact,
  onReply,
  onDelete,
  onOpenViewers,
}) {
  const [progress, setProgress] = useState(0);
  const [paused, setPaused] = useState(false);
  const [replyText, setReplyText] = useState("");
  const [showReactions, setShowReactions] = useState(false);
  const [showMenu, setShowMenu] = useState(false);
  const [sentReaction, setSentReaction] = useState(null);
  const [showReplySent, setShowReplySent] = useState(false);
  const progressInterval = useRef(null);
  const touchStartX = useRef(null);
  const inputRef = useRef(null);

  const currentGroup = viewingMyStory ? null : friendGroups[activeGroupIndex];
  const currentStory = viewingMyStory
    ? myStories[activeStoryIndex]
    : currentGroup?.stories?.[activeStoryIndex];

  const stories = viewingMyStory
    ? myStories
    : currentGroup?.stories || [];

  // ── Progress bar ──────────────────────────────────────────────────────────

  const startProgress = useCallback(() => {
    setProgress(0);
    clearInterval(progressInterval.current);
    if (paused) return;
    const step = 100 / (STORY_DURATION / 100);
    progressInterval.current = setInterval(() => {
      setProgress((p) => {
        if (p >= 100) {
          clearInterval(progressInterval.current);
          onNext();
          return 100;
        }
        return p + step;
      });
    }, 100);
  }, [paused, onNext]);

  useEffect(() => {
    startProgress();
    return () => clearInterval(progressInterval.current);
  }, [activeStoryIndex, activeGroupIndex, viewingMyStory]);

  useEffect(() => {
    if (paused) {
      clearInterval(progressInterval.current);
    } else {
      startProgress();
    }
  }, [paused]);

  // ── Keyboard ──────────────────────────────────────────────────────────────

  useEffect(() => {
    const handleKey = (e) => {
      if (e.key === "Escape") onClose();
      if (e.key === "ArrowRight") onNext();
      if (e.key === "ArrowLeft") onPrev();
    };
    window.addEventListener("keydown", handleKey);
    return () => window.removeEventListener("keydown", handleKey);
  }, [onClose, onNext, onPrev]);

  // ── Touch swipe ───────────────────────────────────────────────────────────

  const handleTouchStart = (e) => {
    touchStartX.current = e.touches[0].clientX;
    setPaused(true);
  };

  const handleTouchEnd = (e) => {
    if (touchStartX.current === null) return;
    const diff = touchStartX.current - e.changedTouches[0].clientX;
    if (Math.abs(diff) > 50) {
      diff > 0 ? onNext() : onPrev();
    }
    setPaused(false);
    touchStartX.current = null;
  };

  // ── Reaction ──────────────────────────────────────────────────────────────

  const handleReact = async (emoji) => {
    setSentReaction(emoji);
    setShowReactions(false);
    await onReact(emoji);
    setTimeout(() => setSentReaction(null), 2000);
  };

  // ── Reply ─────────────────────────────────────────────────────────────────

  const handleReply = async () => {
    if (!replyText.trim()) return;
    await onReply(replyText);
    setReplyText("");
    setShowReplySent(true);
    setTimeout(() => setShowReplySent(false), 2000);
  };

  if (!currentStory) return null;

  const authorName = viewingMyStory ? "Story của bạn" : currentGroup?.displayName;
  const authorAvatar = viewingMyStory ? null : currentGroup?.avatar;

  // Thời gian còn lại
  const timeLeft = () => {
    const created = new Date(currentStory.createdAt);
    const expires = new Date(created.getTime() + 24 * 60 * 60 * 1000);
    const diff = expires - Date.now();
    const hours = Math.floor(diff / 3600000);
    const mins = Math.floor((diff % 3600000) / 60000);
    if (hours > 0) return `${hours}h ${mins}m`;
    return `${mins}m`;
  };

  return (
    <div
      className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/90 backdrop-blur-sm"
      onClick={(e) => e.target === e.currentTarget && onClose()}
    >
      {/* Main story card */}
      <div
        className="relative w-full max-w-sm h-full max-h-[90vh] sm:max-h-[680px] rounded-none sm:rounded-2xl overflow-hidden shadow-2xl select-none"
        onTouchStart={handleTouchStart}
        onTouchEnd={handleTouchEnd}
        onMouseDown={() => setPaused(true)}
        onMouseUp={() => setPaused(false)}
      >
        {/* Media */}
        {currentStory.mediaUrl ? (
          <img
            src={currentStory.mediaUrl}
            alt="story"
            className="absolute inset-0 w-full h-full object-cover"
            draggable={false}
          />
        ) : (
          <div
            className="absolute inset-0 flex items-center justify-center"
            style={{ backgroundColor: currentStory.bgColor || "#1a1a2e" }}
          >
            <p className="text-white text-2xl font-bold text-center px-8 leading-snug">
              {currentStory.caption}
            </p>
          </div>
        )}

        {/* Gradient overlays */}
        <div className="absolute inset-0 bg-gradient-to-b from-black/50 via-transparent to-black/60 pointer-events-none" />

        {/* ─ Progress bars ─ */}
        <div className="absolute top-3 left-3 right-3 flex gap-1 z-10">
          {stories.map((s, i) => (
            <div
              key={s.id}
              className="flex-1 h-0.5 bg-white/30 rounded-full overflow-hidden"
            >
              <div
                className="h-full bg-white rounded-full transition-none"
                style={{
                  width:
                    i < activeStoryIndex
                      ? "100%"
                      : i === activeStoryIndex
                      ? `${progress}%`
                      : "0%",
                  transition: i === activeStoryIndex ? "none" : undefined,
                }}
              />
            </div>
          ))}
        </div>

        {/* ─ Header ─ */}
        <div className="absolute top-7 left-3 right-3 flex items-center gap-2 z-10">
          {authorAvatar && (
            <img
              src={authorAvatar}
              alt={authorName}
              className="w-9 h-9 rounded-full object-cover border-2 border-white"
            />
          )}
          <div className="flex-1 min-w-0">
            <p className="text-white font-semibold text-sm truncate">{authorName}</p>
            <p className="text-white/70 text-[11px]">
              còn {timeLeft()} · {currentStory.viewCount} lượt xem
            </p>
          </div>

          {/* Menu */}
          <div className="relative">
            <button
              className="text-white p-1 hover:bg-white/20 rounded-full transition-colors"
              onClick={(e) => { e.stopPropagation(); setShowMenu(!showMenu); }}
            >
              <MoreHorizontal size={20} />
            </button>
            {showMenu && (
              <div className="absolute right-0 top-8 bg-white dark:bg-[#262626] rounded-xl shadow-xl border border-gray-200 dark:border-[#3a3a3a] py-1 w-44 z-20">
                {viewingMyStory && (
                  <>
                    <button
                      className="w-full flex items-center gap-2 px-4 py-2.5 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-[#363636]"
                      onClick={() => { setShowMenu(false); onOpenViewers(currentStory.id); }}
                    >
                      <Eye size={16} />
                      Xem ai đã xem
                    </button>
                    <button
                      className="w-full flex items-center gap-2 px-4 py-2.5 text-sm text-red-500 hover:bg-gray-50 dark:hover:bg-[#363636]"
                      onClick={() => { setShowMenu(false); onDelete(currentStory.id); }}
                    >
                      <Trash2 size={16} />
                      Xoá story
                    </button>
                  </>
                )}
                {!viewingMyStory && (
                  <button className="w-full flex items-center gap-2 px-4 py-2.5 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-50 dark:hover:bg-[#363636]">
                    Báo cáo
                  </button>
                )}
              </div>
            )}
          </div>

          {/* Close */}
          <button
            className="text-white p-1 hover:bg-white/20 rounded-full transition-colors"
            onClick={onClose}
          >
            <X size={20} />
          </button>
        </div>

        {/* ─ Caption ─ */}
        {currentStory.caption && currentStory.mediaUrl && (
          <div className="absolute bottom-20 left-3 right-3 z-10">
            <p className="text-white text-sm font-medium drop-shadow-lg leading-relaxed">
              {currentStory.caption}
            </p>
          </div>
        )}

        {/* ─ Reply / Reaction bar ─ */}
        {!viewingMyStory && (
          <div className="absolute bottom-4 left-3 right-3 z-10 flex items-center gap-2">
            {/* Reply input */}
            <div className="flex-1 flex items-center bg-white/10 backdrop-blur-sm border border-white/30 rounded-full px-4 py-2 gap-2">
              <input
                ref={inputRef}
                type="text"
                placeholder="Trả lời..."
                value={replyText}
                onChange={(e) => setReplyText(e.target.value)}
                onFocus={() => setPaused(true)}
                onBlur={() => setPaused(false)}
                onKeyDown={(e) => e.key === "Enter" && handleReply()}
                className="flex-1 bg-transparent text-white placeholder-white/60 text-sm outline-none min-w-0"
              />
              {replyText && (
                <button onClick={handleReply} className="text-white">
                  <Send size={16} />
                </button>
              )}
            </div>

            {/* Reaction button */}
            <div className="relative">
              <button
                className="w-10 h-10 rounded-full bg-white/10 backdrop-blur-sm border border-white/30 flex items-center justify-center text-white hover:bg-white/20 transition-colors"
                onClick={(e) => { e.stopPropagation(); setShowReactions(!showReactions); setPaused(true); }}
              >
                <Heart size={18} />
              </button>
              {showReactions && (
                <div className="absolute bottom-12 right-0 flex gap-1 bg-white dark:bg-[#262626] rounded-2xl px-3 py-2 shadow-2xl border border-gray-200 dark:border-[#3a3a3a] z-20">
                  {REACTIONS.map((emoji) => (
                    <button
                      key={emoji}
                      onClick={() => handleReact(emoji)}
                      className="text-xl hover:scale-125 transition-transform"
                    >
                      {emoji}
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}

        {/* Reaction sent toast */}
        {sentReaction && (
          <div className="absolute inset-0 flex items-center justify-center pointer-events-none z-20">
            <span className="text-7xl animate-bounce">{sentReaction}</span>
          </div>
        )}

        {/* Reply sent toast */}
        {showReplySent && (
          <div className="absolute bottom-20 left-1/2 -translate-x-1/2 bg-white/20 backdrop-blur-sm text-white text-sm px-4 py-2 rounded-full z-20">
            Đã gửi trả lời ✓
          </div>
        )}

        {/* ─ Nav tap zones ─ */}
        <button
          className="absolute left-0 top-10 bottom-20 w-1/3 z-5 cursor-pointer"
          onClick={(e) => { e.stopPropagation(); onPrev(); }}
        />
        <button
          className="absolute right-0 top-10 bottom-20 w-1/3 z-5 cursor-pointer"
          onClick={(e) => { e.stopPropagation(); onNext(); }}
        />
      </div>

      {/* Prev / Next group buttons (desktop) */}
      {!viewingMyStory && activeGroupIndex > 0 && (
        <button
          className="hidden sm:flex absolute left-4 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/20 backdrop-blur-sm items-center justify-center text-white hover:bg-white/30 transition-colors"
          onClick={onPrev}
        >
          <ChevronLeft size={20} />
        </button>
      )}
      {!viewingMyStory && activeGroupIndex < friendGroups.length - 1 && (
        <button
          className="hidden sm:flex absolute right-4 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full bg-white/20 backdrop-blur-sm items-center justify-center text-white hover:bg-white/30 transition-colors"
          onClick={onNext}
        >
          <ChevronRight size={20} />
        </button>
      )}
    </div>
  );
}