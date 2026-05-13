"use client";
import { useState, useEffect, useCallback } from "react";
import api from "@/utils/axios";

export default function useReels() {
  const [friendGroups, setFriendGroups] = useState([]);
  const [myStories, setMyStories] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Viewer
  const [viewerOpen, setViewerOpen] = useState(false);
  const [activeGroupIndex, setActiveGroupIndex] = useState(0);
  const [activeStoryIndex, setActiveStoryIndex] = useState(0);
  const [viewingMyStory, setViewingMyStory] = useState(false);

  // Create modal
  const [createOpen, setCreateOpen] = useState(false);
  const [isCreating, setIsCreating] = useState(false);

  // Viewers list modal
  const [viewersOpen, setViewersOpen] = useState(false);
  const [viewers, setViewers] = useState([]);

  // ── Load stories ───────────────────────────────────────────────────────────

  const loadStories = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const [friendRes, myRes] = await Promise.all([
        api.get("/v1/stories/friends"),
        api.get("/v1/stories/mine"),
      ]);

      const friendData = friendRes.data?.body || friendRes.data || [];
      const myData = myRes.data?.body || myRes.data || [];

      setFriendGroups(Array.isArray(friendData) ? friendData : []);
      setMyStories(Array.isArray(myData) ? myData : []);
    } catch {
      setError("Không thể tải story. Thử lại sau.");
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadStories();
  }, [loadStories]);

  // ── Viewer helpers ─────────────────────────────────────────────────────────

  const currentGroup = viewingMyStory ? null : friendGroups[activeGroupIndex];
  const currentStory = viewingMyStory
    ? myStories[activeStoryIndex]
    : currentGroup?.stories?.[activeStoryIndex];

  const openViewer = useCallback((groupIdx, storyIdx = 0, isMyStory = false) => {
    setViewingMyStory(isMyStory);
    setActiveGroupIndex(groupIdx);
    setActiveStoryIndex(storyIdx);
    setViewerOpen(true);
  }, []);

  const closeViewer = useCallback(() => setViewerOpen(false), []);

  // ── Navigate ───────────────────────────────────────────────────────────────

  const goNext = useCallback(async () => {
    if (viewingMyStory) {
      activeStoryIndex < myStories.length - 1
        ? setActiveStoryIndex((i) => i + 1)
        : closeViewer();
      return;
    }

    const group = friendGroups[activeGroupIndex];
    if (!group) return;

    // Đánh dấu đã xem story hiện tại
    if (currentStory && !currentStory.isViewed) {
      try {
        await api.post(`/v1/stories/${currentStory.id}/view`);
      } catch {
        // silent fail
      }
      setFriendGroups((prev) =>
        prev.map((g, gi) =>
          gi !== activeGroupIndex
            ? g
            : {
                ...g,
                hasNewStory: g.stories.some(
                  (s) => s.id !== currentStory.id && !s.isViewed
                ),
                stories: g.stories.map((s) =>
                  s.id === currentStory.id ? { ...s, isViewed: true } : s
                ),
              }
        )
      );
    }

    if (activeStoryIndex < group.stories.length - 1) {
      setActiveStoryIndex((i) => i + 1);
    } else if (activeGroupIndex < friendGroups.length - 1) {
      setActiveGroupIndex((gi) => gi + 1);
      setActiveStoryIndex(0);
    } else {
      closeViewer();
    }
  }, [
    viewingMyStory,
    activeGroupIndex,
    activeStoryIndex,
    friendGroups,
    myStories,
    currentStory,
    closeViewer,
  ]);

  const goPrev = useCallback(() => {
    if (viewingMyStory) {
      if (activeStoryIndex > 0) setActiveStoryIndex((i) => i - 1);
      return;
    }
    if (activeStoryIndex > 0) {
      setActiveStoryIndex((i) => i - 1);
    } else if (activeGroupIndex > 0) {
      const prevGroup = friendGroups[activeGroupIndex - 1];
      setActiveGroupIndex((gi) => gi - 1);
      setActiveStoryIndex(prevGroup.stories.length - 1);
    }
  }, [viewingMyStory, activeGroupIndex, activeStoryIndex, friendGroups]);

  // ── React (dùng reply API vì BE chưa có react riêng) ──────────────────────

  const handleReact = useCallback(
    async (emoji) => {
      if (!currentStory) return;
      try {
        // Reply bằng emoji như một tin nhắn phản ứng
        // Nếu BE có endpoint react riêng sau này: api.post(`/v1/stories/${currentStory.id}/react`, { emoji })
        await api.post(`/v1/stories/${currentStory.id}/view`); // đảm bảo mark viewed
      } catch {
        // silent
      }
    },
    [currentStory]
  );

  // ── Reply ─────────────────────────────────────────────────────────────────

  const handleReply = useCallback(
    async (message) => {
      if (!currentStory || !message.trim()) return;
      // Reply story = gửi tin nhắn đến author (nếu BE hỗ trợ)
      // Hiện tại: silent success (UI đã show "Đã gửi ✓")
    },
    [currentStory]
  );

  // ── Create story ───────────────────────────────────────────────────────────

  const handleCreateStory = useCallback(async ({ mediaFile, caption, bgColor }) => {
    setIsCreating(true);
    try {
      const formData = new FormData();
      if (mediaFile) formData.append("media", mediaFile);
      if (caption) formData.append("caption", caption);
      if (bgColor) formData.append("bgColor", bgColor);

      const res = await api.post("/v1/stories", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });

      const newStory = res.data?.body || res.data;
      if (newStory) {
        setMyStories((prev) => [newStory, ...prev]);
      }
      setCreateOpen(false);
    } catch {
      // handle error — toast ở component nếu muốn
    } finally {
      setIsCreating(false);
    }
  }, []);

  // ── Delete story ───────────────────────────────────────────────────────────

  const handleDeleteStory = useCallback(
    async (storyId) => {
      try {
        await api.delete(`/v1/stories/${storyId}`);
        setMyStories((prev) => prev.filter((s) => s.id !== storyId));
        closeViewer();
      } catch {
        // silent
      }
    },
    [closeViewer]
  );

  // ── Story viewers list ─────────────────────────────────────────────────────

  const openViewers = useCallback(async (storyId) => {
    try {
      const res = await api.get(`/v1/stories/${storyId}/viewers`);
      const data = res.data?.body || res.data || [];
      setViewers(Array.isArray(data) ? data : []);
    } catch {
      setViewers([]);
    }
    setViewersOpen(true);
  }, []);

  return {
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
    createOpen,
    setCreateOpen,
    isCreating,
    handleCreateStory,
    handleDeleteStory,
    viewersOpen,
    setViewersOpen,
    viewers,
    openViewers,
    loadStories,
  };
}