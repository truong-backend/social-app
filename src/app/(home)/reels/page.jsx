"use client";
import { useRef } from "react";
import { RefreshCw, Film, Clock, Users } from "lucide-react";
import useReels from "@/hooks/useReels";
import StoryBar from "@/components/social-app-component/StoryBar";
import StoryViewer from "@/components/social-app-component/StoryViewer";
import CreateStoryModal from "@/components/social-app-component/CreateStoryModal";
import StoryViewersModal from "@/components/social-app-component/StoryViewersModal";

export default function ReelsPage() {
  const {
    friendGroups,
    myStories,
    isLoading,
    error,
    viewerOpen,
    activeGroupIndex,
    activeStoryIndex,
    viewingMyStory,
    currentGroup,
    currentStory,
    openViewer,
    closeViewer,
    goNext,
    goPrev,
    handleReact,
    handleReply,
    handleDeleteStory,
    createOpen,
    setCreateOpen,
    isCreating,
    handleCreateStory,
    viewersOpen,
    setViewersOpen,
    viewers,
    openViewers,
    loadStories,
  } = useReels();

  const totalStories = friendGroups.reduce((sum, g) => sum + g.stories.length, 0) + myStories.length;
  const newStories = friendGroups.filter((g) => g.stories.some((s) => !s.isViewed)).length;

  return (
    <div className="w-full min-h-screen bg-[var(--background)] text-[var(--foreground)]">
      {/* ─ Page header ─ */}
      <div className="sticky top-0 z-30 bg-[var(--background)] border-b border-[var(--border)] px-4 py-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Film size={22} className="text-blue-500" />
            <h1 className="font-bold text-xl">Reels</h1>
          </div>
          <button
            onClick={loadStories}
            disabled={isLoading}
            className="w-9 h-9 rounded-full hover:bg-[var(--card)] flex items-center justify-center transition-colors"
            title="Làm mới"
          >
            <RefreshCw size={18} className={isLoading ? "animate-spin opacity-50" : ""} />
          </button>
        </div>

        {!isLoading && (
          <div className="flex gap-4 mt-2">
            <StatChip icon={<Film size={13} />} label={`${totalStories} story`} />
            {newStories > 0 && (
              <StatChip
                icon={<Clock size={13} />}
                label={`${newStories} chưa xem`}
                highlight
              />
            )}
            <StatChip icon={<Users size={13} />} label={`${friendGroups.length} bạn bè`} />
          </div>
        )}
      </div>

      {/* ─ Content ─ */}
      <div className="px-4 py-5 space-y-6">
        {/* Loading */}
        {isLoading && (
          <div className="space-y-4">
            <div className="h-4 w-32 bg-[var(--card)] rounded-full animate-pulse" />
            <div className="flex gap-3">
              {[...Array(5)].map((_, i) => (
                <div
                  key={i}
                  className="w-[110px] h-[190px] rounded-2xl bg-[var(--card)] animate-pulse flex-shrink-0"
                />
              ))}
            </div>
          </div>
        )}

        {/* Error */}
        {error && !isLoading && (
          <div className="text-center py-12">
            <p className="text-red-500 mb-3">{error}</p>
            <button
              onClick={loadStories}
              className="px-4 py-2 rounded-xl bg-blue-500 text-white text-sm font-semibold hover:bg-blue-600 transition-colors"
            >
              Thử lại
            </button>
          </div>
        )}

        {/* Stories section */}
        {!isLoading && !error && (
          <>
            <section>
              <SectionTitle>Story của bạn bè</SectionTitle>
              <div className="mt-3">
                <StoryBar
                  myStories={myStories}
                  friendGroups={friendGroups}
                  onOpenCreate={() => setCreateOpen(true)}
                  onOpenMyStory={() => openViewer(0, 0, true)}
                  onOpenFriendStory={(idx) => openViewer(idx, 0, false)}
                />
              </div>
            </section>

            {friendGroups.length === 0 && myStories.length === 0 && (
              <EmptyState onCreateStory={() => setCreateOpen(true)} />
            )}

            {friendGroups.length > 0 && (
              <section>
                <SectionTitle>Tất cả story</SectionTitle>
                <div className="mt-3 grid grid-cols-2 sm:grid-cols-3 gap-3">
                  {friendGroups.map((group, groupIdx) =>
                    group.stories.map((story, storyIdx) => (
                      <StoryGridCard
                        key={story.id}
                        story={story}
                        group={group}
                        onClick={() => openViewer(groupIdx, storyIdx, false)}
                      />
                    ))
                  )}
                </div>
              </section>
            )}

            {myStories.length > 0 && (
              <section>
                <SectionTitle>Story của tôi</SectionTitle>
                <div className="mt-3 grid grid-cols-2 sm:grid-cols-3 gap-3">
                  {myStories.map((story, idx) => (
                    <StoryGridCard
                      key={story.id}
                      story={story}
                      group={{ displayName: "Tôi", avatar: null }}
                      isMyStory
                      onClick={() => openViewer(0, idx, true)}
                    />
                  ))}
                </div>
              </section>
            )}
          </>
        )}
      </div>

      {/* ─ Modals ─ */}
      {viewerOpen && (
        <StoryViewer
          friendGroups={friendGroups}
          myStories={myStories}
          activeGroupIndex={activeGroupIndex}
          activeStoryIndex={activeStoryIndex}
          viewingMyStory={viewingMyStory}
          onClose={closeViewer}
          onNext={goNext}
          onPrev={goPrev}
          onReact={handleReact}
          onReply={handleReply}
          onDelete={handleDeleteStory}
          onOpenViewers={openViewers}
        />
      )}

      <CreateStoryModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onSubmit={handleCreateStory}
        isCreating={isCreating}
      />

      <StoryViewersModal
        open={viewersOpen}
        onClose={() => setViewersOpen(false)}
        viewers={viewers}
      />
    </div>
  );
}

// ─ Sub-components ─────────────────────────────────────────────────────────────

function SectionTitle({ children }) {
  return (
    <h2 className="font-bold text-base text-[var(--foreground)]">{children}</h2>
  );
}

function StatChip({ icon, label, highlight }) {
  return (
    <div
      className={`flex items-center gap-1 text-xs font-medium px-2.5 py-1 rounded-full ${
        highlight
          ? "bg-blue-500/10 text-blue-500"
          : "bg-[var(--card)] text-[var(--foreground)] opacity-70"
      }`}
    >
      {icon}
      <span>{label}</span>
    </div>
  );
}

function StoryGridCard({ story, group, isMyStory = false, onClick }) {
  const timeLeft = () => {
    const created = new Date(story.createdAt);
    const expires = new Date(created.getTime() + 24 * 60 * 60 * 1000);
    const diff = expires - Date.now();
    const hours = Math.floor(diff / 3600000);
    const mins = Math.floor((diff % 3600000) / 60000);
    if (hours > 0) return `${hours}g`;
    return `${mins}p`;
  };

  return (
    <div
      className="relative aspect-[9/16] max-h-64 rounded-2xl overflow-hidden cursor-pointer group"
      onClick={onClick}
    >
      {/* Media */}
      {story.mediaUrl ? (
        story.mediaType === "video" ? (
          <video
            src={story.mediaUrl}
            className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
            muted
            playsInline
            preload="metadata"
          />
        ) : (
          <img
            src={story.mediaUrl}
            alt="story"
            className="absolute inset-0 w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
          />
        )
      ) : (
        <div
          className="absolute inset-0 flex items-center justify-center p-3"
          style={{ backgroundColor: story.bgColor || "#1a1a2e" }}
        >
          <p className="text-white text-sm font-semibold text-center leading-snug line-clamp-4">
            {story.caption}
          </p>
        </div>
      )}

      {/* Overlay */}
      <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-transparent to-black/20" />

      {/* Viewed indicator */}
      {story.isViewed && !isMyStory && (
        <div className="absolute inset-0 ring-2 ring-inset ring-white/20 rounded-2xl pointer-events-none" />
      )}

      {/* New story ring */}
      {!story.isViewed && !isMyStory && (
        <div className="absolute inset-0 ring-2 ring-inset ring-blue-500 rounded-2xl pointer-events-none" />
      )}

      {/* Avatar */}
      {group.avatar && (
        <div className="absolute top-2 left-2">
          <img
            src={group.avatar}
            alt={group.displayName}
            className="w-8 h-8 rounded-full border-2 border-white object-cover"
          />
        </div>
      )}

      {/* Timer badge */}
      <div className="absolute top-2 right-2 bg-black/50 text-white text-[10px] font-bold rounded-full px-2 py-0.5">
        {timeLeft()}
      </div>

      {/* Footer */}
      <div className="absolute bottom-2 left-2 right-2">
        <p className="text-white text-xs font-semibold truncate">{group.displayName}</p>
        {story.caption && story.mediaUrl && (
          <p className="text-white/70 text-[10px] truncate mt-0.5">{story.caption}</p>
        )}
        {Object.keys(story.reactions || {}).length > 0 && (
          <div className="flex gap-1 mt-1">
            {Object.entries(story.reactions)
              .slice(0, 3)
              .map(([emoji, count]) => (
                <span key={emoji} className="text-[11px]">
                  {emoji} {count}
                </span>
              ))}
          </div>
        )}
      </div>
    </div>
  );
}

function EmptyState({ onCreateStory }) {
  return (
    <div className="text-center py-16">
      <div className="w-20 h-20 rounded-full bg-gradient-to-tr from-blue-400 to-purple-500 flex items-center justify-center mx-auto mb-4 opacity-70">
        <Film size={36} className="text-white" />
      </div>
      <h3 className="font-bold text-lg text-[var(--foreground)] mb-2">Chưa có story nào</h3>
      <p className="text-[var(--foreground)] opacity-50 text-sm mb-6 max-w-xs mx-auto">
        Bạn bè chưa đăng story. Hãy là người đầu tiên chia sẻ khoảnh khắc!
      </p>
      <button
        onClick={onCreateStory}
        className="px-6 py-2.5 rounded-xl bg-blue-500 text-white font-semibold hover:bg-blue-600 transition-colors text-sm"
      >
        Tạo story đầu tiên
      </button>
    </div>
  );
}