"use client";
import { useRef } from "react";
import { Plus, ChevronLeft, ChevronRight } from "lucide-react";
import { normalizeFileUrl } from "@/utils/normalizeFileUrl";

export default function StoryBar({
  myStories = [],
  friendGroups = [],
  onOpenCreate,
  onOpenMyStory,
  onOpenFriendStory,
}) {
  const scrollRef = useRef(null);

  const scroll = (dir) => {
    if (!scrollRef.current) return;
    scrollRef.current.scrollBy({ left: dir * 220, behavior: "smooth" });
  };

  const hasMyStory = myStories.length > 0;

  return (
    <div className="relative w-full">
      <button
        onClick={() => scroll(-1)}
        className="absolute left-0 top-1/2 -translate-y-1/2 z-10 w-8 h-8 rounded-full bg-[var(--background)] border border-[var(--border)] shadow flex items-center justify-center text-[var(--foreground)] hover:bg-[var(--card)] transition-colors"
        style={{ display: friendGroups.length > 3 ? undefined : "none" }}
      >
        <ChevronLeft size={16} />
      </button>

      <div
        ref={scrollRef}
        className="flex gap-3 overflow-x-auto scroll-smooth pb-1 px-1"
        style={{ scrollbarWidth: "none", msOverflowStyle: "none" }}
      >
        <MyStoryCard
          myStories={myStories}
          hasMyStory={hasMyStory}
          onOpenCreate={onOpenCreate}
          onOpenMyStory={onOpenMyStory}
        />

        {friendGroups.map((group, index) => (
          <FriendStoryCard
            key={group.userId}
            group={group}
            onClick={() => onOpenFriendStory(index)}
          />
        ))}
      </div>

      <button
        onClick={() => scroll(1)}
        className="absolute right-0 top-1/2 -translate-y-1/2 z-10 w-8 h-8 rounded-full bg-[var(--background)] border border-[var(--border)] shadow flex items-center justify-center text-[var(--foreground)] hover:bg-[var(--card)] transition-colors"
        style={{ display: friendGroups.length > 3 ? undefined : "none" }}
      >
        <ChevronRight size={16} />
      </button>
    </div>
  );
}

function MyStoryCard({ myStories, hasMyStory, onOpenCreate, onOpenMyStory }) {
  const firstStory = myStories[0];

  return (
    <div className="flex-shrink-0 w-[110px] cursor-pointer group" onClick={hasMyStory ? onOpenMyStory : onOpenCreate}>
      <div className="relative w-[110px] h-[190px] rounded-2xl overflow-hidden bg-[var(--card)] border border-[var(--border)]">
        {/* Background media nếu đã có story */}
        {hasMyStory && firstStory?.mediaUrl ? (
          firstStory.mediaType === "video" ? (
            <video
              src={normalizeFileUrl(firstStory.mediaUrl)}
              className="absolute inset-0 w-full h-full object-cover"
              muted
              playsInline
              preload="metadata"
            />
          ) : (
            <img
              src={normalizeFileUrl(firstStory.mediaUrl)}
              alt="My story"
              className="absolute inset-0 w-full h-full object-cover"
            />
          )
        ) : hasMyStory && firstStory?.bgColor ? (
          <div
            className="absolute inset-0 flex items-center justify-center p-2"
            style={{ backgroundColor: firstStory.bgColor }}
          >
            <p className="text-white text-[10px] font-semibold text-center line-clamp-3">
              {firstStory.caption}
            </p>
          </div>
        ) : (
          <div className="absolute inset-0 bg-gradient-to-b from-blue-400 to-purple-500 opacity-20" />
        )}

        {/* Overlay tối phía dưới */}
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent" />

        {/* Plus button */}
        <div className="absolute bottom-8 left-1/2 -translate-x-1/2">
          <div className="w-9 h-9 rounded-full bg-blue-500 border-4 border-[var(--background)] flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform">
            <Plus size={18} className="text-white" />
          </div>
        </div>

        {/* Label */}
        <p className="absolute bottom-2 left-0 right-0 text-center text-[11px] font-semibold text-white leading-tight px-1">
          {hasMyStory ? "Story của bạn" : "Tạo story"}
        </p>

        {/* Count badge */}
        {hasMyStory && (
          <div className="absolute top-2 right-2 bg-blue-500 text-white text-[10px] font-bold rounded-full w-5 h-5 flex items-center justify-center">
            {myStories.length}
          </div>
        )}
      </div>
    </div>
  );
}

function FriendStoryCard({ group, onClick }) {
  const firstStory = group.stories[0];
  const hasNew = group.hasNewStory || group.stories.some((s) => !s.isViewed);
  const allViewed = group.stories.every((s) => s.isViewed);
  const avatarUrl = normalizeFileUrl(group.avatar);

  return (
    <div className="flex-shrink-0 w-[110px] cursor-pointer group" onClick={onClick}>
      <div className="relative w-[110px] h-[190px] rounded-2xl overflow-hidden bg-[var(--card)] border border-[var(--border)]">
        {/* Background media */}
        {firstStory?.mediaUrl ? (
          firstStory.mediaType === "video" ? (
            <video
              src={normalizeFileUrl(firstStory.mediaUrl)}
              className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
              muted
              playsInline
              preload="metadata"
            />
          ) : (
            <img
              src={normalizeFileUrl(firstStory.mediaUrl)}
              alt={group.displayName}
              className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            />
          )
        ) : firstStory?.bgColor ? (
          <div
            className="absolute inset-0 flex items-center justify-center p-2"
            style={{ backgroundColor: firstStory.bgColor }}
          >
            <p className="text-white text-[10px] font-semibold text-center line-clamp-3">
              {firstStory.caption}
            </p>
          </div>
        ) : null}

        {/* Overlay gradient */}
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/10 to-transparent" />

        {/* Avatar với ring chỉ trạng thái */}
        <div className="absolute top-2 left-2">
          <div
            className={`rounded-full p-[2px] ${
              hasNew && !allViewed
                ? "bg-gradient-to-tr from-yellow-400 via-pink-500 to-purple-600"
                : "bg-gray-400"
            }`}
          >
            <div className="bg-[var(--background)] rounded-full p-[2px]">
              {avatarUrl ? (
                <img
                  src={avatarUrl}
                  alt={group.displayName}
                  className="w-9 h-9 rounded-full object-cover"
                  onError={(e) => {
                    e.target.style.display = "none";
                    e.target.nextSibling && (e.target.nextSibling.style.display = "flex");
                  }}
                />
              ) : null}
              <div
                className="w-9 h-9 rounded-full flex items-center justify-center text-xs font-bold text-white"
                style={{
                  display: avatarUrl ? "none" : "flex",
                  background: "linear-gradient(135deg, #833ab4, #fd1d1d, #fcb045)",
                }}
              >
                {group.displayName?.charAt(0)?.toUpperCase() || "?"}
              </div>
            </div>
          </div>
        </div>

        {/* Multiple story indicator */}
        {group.stories.length > 1 && (
          <div className="absolute top-2 right-2 flex gap-0.5">
            {group.stories.slice(0, 3).map((_, i) => (
              <div
                key={i}
                className={`w-1.5 h-1.5 rounded-full ${
                  group.stories[i]?.isViewed ? "bg-white/40" : "bg-white"
                }`}
              />
            ))}
          </div>
        )}

        {/* Username */}
        <p className="absolute bottom-2 left-2 right-2 text-[11px] font-semibold text-white leading-tight truncate">
          {group.displayName}
        </p>
      </div>
    </div>
  );
}