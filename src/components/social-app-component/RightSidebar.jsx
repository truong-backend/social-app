"use client";
import { useEffect, useState } from "react";
import Link from "next/link";
import Avatar from "@/components/ui-components/Avatar";
import api from "@/utils/axios";
import ChatList from "./ChatList";
import Chatbox from "./ChatBox";

export default function RightSidebar({ token }) {
  const [currentUser, setCurrentUser] = useState(null);
  const [suggested, setSuggested] = useState([]);
  const [loadingSuggested, setLoadingSuggested] = useState(true);
  const [followingIds, setFollowingIds] = useState({});
  const [activeChatId, setActiveChatId] = useState(null);
  const [activeTargetUser, setActiveTargetUser] = useState(null);

  useEffect(() => {
    if (typeof window !== "undefined") {
      const userName = localStorage.getItem("userName");
      const userId = localStorage.getItem("userId");
      const avatar = localStorage.getItem("profilePictureUrl") || null;
      const givenName = localStorage.getItem("givenName") || "";
      const familyName = localStorage.getItem("familyName") || "";
      const fullName = (familyName + " " + givenName).trim() || userName;
      if (userName)
        setCurrentUser({ username: userName, id: userId, avatar, fullName });
    }
  }, []);

  useEffect(() => {
    const fetchSuggested = async () => {
      try {
        const res = await api.get("/v1/friends/suggested?limit=5");
        const data = res.data?.body || res.data || [];
        setSuggested(Array.isArray(data) ? data.slice(0, 5) : []);
      } catch (e) {
        setSuggested([]);
      } finally {
        setLoadingSuggested(false);
      }
    };
    fetchSuggested();
  }, []);

  const handleFollow = async (userId) => {
    setFollowingIds((prev) => ({ ...prev, [userId]: "loading" }));
    try {
      await api.post("/v1/friend-request/send", { receiverId: userId });
      setFollowingIds((prev) => ({ ...prev, [userId]: "sent" }));
    } catch (e) {
      setFollowingIds((prev) => ({ ...prev, [userId]: null }));
    }
  };

  const handleSelectChat = (chatId, user) => {
    setActiveChatId(chatId);
    setActiveTargetUser(user);
  };

  const handleBack = () => {
    setActiveChatId(null);
    setActiveTargetUser(null);
  };

  const handleChatCreated = (id, user) => {
    setActiveChatId(id);
    setActiveTargetUser(user);
  };

  const getUserDisplayName = (user) => {
    const family = user?.familyName || "";
    const given = user?.givenName || "";
    const full = (family + " " + given).trim();
    return full || user?.username || "Người dùng";
  };

  const getMutualText = (user) => {
    if (user?.mutualFriendsCount > 0)
      return user.mutualFriendsCount + " bạn chung";
    return "Gợi ý cho bạn";
  };

  const getInitial = (name) => {
    if (!name) return "?";
    return name.charAt(0).toUpperCase();
  };

  return (
    <div className="flex flex-col gap-4 w-full pt-4">
      {/* Current user */}
      {currentUser && (
        <div className="flex items-center justify-between px-2 mb-2">
          <Link
            href={"/profile/" + currentUser.username}
            className="flex items-center gap-3 group"
          >
            {currentUser.avatar ? (
              <img
                src={currentUser.avatar}
                alt={currentUser.username}
                className="w-11 h-11 rounded-full object-cover flex-shrink-0 border border-[var(--border)]"
                onError={(e) => {
                  e.target.style.display = "none";
                  e.target.nextSibling.style.display = "flex";
                }}
              />
            ) : null}
            <div
              className="w-11 h-11 rounded-full flex-shrink-0 flex items-center justify-center text-sm font-bold border border-[var(--border)]"
              style={{
                display: currentUser.avatar ? "none" : "flex",
                background:
                  "linear-gradient(135deg, #833ab4, #fd1d1d, #fcb045)",
                color: "#fff",
              }}
            >
              {getInitial(currentUser.fullName || currentUser.username)}
            </div>
            <div className="min-w-0">
              <p className="text-sm font-semibold text-[var(--foreground)] truncate leading-tight">
                {currentUser.fullName || currentUser.username}
              </p>
              <p className="text-xs text-[var(--muted-foreground)] truncate">
                {currentUser.username}
              </p>
            </div>
          </Link>
          {/* <Link
            href="/settings/personalinfo"
            className="text-xs font-semibold text-blue-500 hover:text-blue-400 transition-colors flex-shrink-0"
          >
            Cài đặt
          </Link> */}
        </div>
      )}

      {/* Suggested for you */}
      <div className="px-2">
        <div className="flex items-center justify-between mb-3">
          <span className="text-sm font-semibold text-[var(--muted-foreground)]">
            Gợi ý cho bạn
          </span>
          <Link
            href="/friends"
            className="text-xs font-semibold text-[var(--foreground)] hover:text-[var(--muted-foreground)] transition-colors"
          >
            Xem tất cả
          </Link>
        </div>

        {loadingSuggested ? (
          <div className="space-y-3">
            {[1, 2, 3].map((i) => (
              <div key={i} className="flex items-center gap-3 animate-pulse">
                <div className="w-10 h-10 rounded-full bg-gray-200 dark:bg-[#2a2a2a] flex-shrink-0" />
                <div className="flex-1 space-y-1.5">
                  <div className="h-3 bg-gray-200 dark:bg-[#2a2a2a] rounded w-24" />
                  <div className="h-2.5 bg-gray-200 dark:bg-[#2a2a2a] rounded w-16" />
                </div>
              </div>
            ))}
          </div>
        ) : suggested.length === 0 ? (
          <p className="text-xs text-[var(--muted-foreground)] text-center py-2">
            Không có gợi ý nào
          </p>
        ) : (
          <div className="space-y-3">
            {suggested.map((user) => {
              const uid = user?.id || user?.userId || user?.username;
              const followState = followingIds[uid];
              return (
                <div
                  key={uid}
                  className="flex items-center justify-between gap-2"
                >
                  <Link
                    href={"/profile/" + (user?.username || "")}
                    className="flex items-center gap-3 min-w-0 flex-1 group"
                  >
                    {user?.profilePictureUrl ? (
                      <img
                        src={user.profilePictureUrl}
                        alt={getUserDisplayName(user)}
                        className="w-10 h-10 rounded-full object-cover flex-shrink-0 border border-[var(--border)]"
                      />
                    ) : (
                      <div
                        className="w-10 h-10 rounded-full flex-shrink-0 flex items-center justify-center text-sm font-bold border border-[var(--border)]"
                        style={{
                          background:
                            "linear-gradient(135deg, #833ab4, #fd1d1d, #fcb045)",
                          color: "#fff",
                        }}
                      >
                        {getInitial(getUserDisplayName(user))}
                      </div>
                    )}
                    <div className="min-w-0">
                      <p className="text-sm font-semibold text-[var(--foreground)] truncate leading-tight group-hover:underline">
                        {getUserDisplayName(user)}
                      </p>
                      <p className="text-xs text-[var(--muted-foreground)] truncate">
                        {getMutualText(user)}
                      </p>
                    </div>
                  </Link>
                  {/* <button
                    onClick={() => handleFollow(uid)}
                    disabled={
                      followState === "loading" || followState === "sent"
                    }
                    className={
                      "text-xs font-semibold flex-shrink-0 transition-colors " +
                      (followState === "sent"
                        ? "text-[var(--muted-foreground)] cursor-default"
                        : followState === "loading"
                          ? "text-[var(--muted-foreground)] cursor-wait"
                          : "text-blue-500 hover:text-blue-400")
                    }
                  >
                    {followState === "sent"
                      ? "Đã gửi"
                      : followState === "loading"
                        ? "..."
                        : "Theo dõi"}
                  </button> */}
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Divider */}
      <div className="border-t border-[var(--border)] mx-2" />

      {/* Messages */}
      <div className="flex-1 min-h-0 px-0">
        {activeChatId && activeTargetUser ? (
          <Chatbox
            chatId={activeChatId}
            targetUser={activeTargetUser}
            onBack={handleBack}
            onChatCreated={handleChatCreated}
            beToken={token}
            recipientId={activeTargetUser?.id || activeTargetUser?.userId}
          />
        ) : (
          <ChatList
            onSelectChat={(chatId, user) => handleSelectChat(chatId, user)}
            selectedChatId={activeChatId}
          />
        )}
      </div>

      {/* Footer */}
      <div className="px-2 pb-4">
        <div className="flex flex-wrap gap-x-2 gap-y-1 text-[10px] text-[var(--muted-foreground)]">
          {[
            "Giới thiệu",
            "Trợ giúp",
            "Báo chí",
            "API",
            "Việc làm",
            "Quyền riêng tư",
          ].map((item) => (
            <span key={item} className="cursor-pointer hover:underline">
              {item}
            </span>
          ))}
        </div>
        <p className="text-[10px] text-[var(--muted-foreground)] mt-2">
          © 2026 POCPOC
        </p>
      </div>
    </div>
  );
}
