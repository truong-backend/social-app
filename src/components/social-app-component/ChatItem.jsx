"use client";

import dayjs from "dayjs";
import "dayjs/locale/vi";
import relativeTime from "dayjs/plugin/relativeTime";
import { memo } from "react";
import { Users } from "lucide-react";
import Avatar from "../ui-components/Avatar";
import Badge from "../ui-components/Badge";

dayjs.extend(relativeTime);
dayjs.locale("vi");

function ChatItem({ chat, onClick, selected }) {
  const {
    chatId,
    latestMessage,
    target,
    notReadMessageCount,
    isGroup,
    name,
    groupAvatarUrl,
    memberCount,
    myRole,
  } = chat;

  // ── Display name ──────────────────────────────────────────────────────────
  const displayName = isGroup
    ? name || "Nhóm không tên"
    : `${target?.givenName || ""} ${target?.familyName || ""}`.trim() ||
      target?.username ||
      "Unknown User";

  // ── Online status (chỉ cho direct chat) ──────────────────────────────────
  const isOnline = !isGroup && (target?.isOnline || false);
  const isUnread = notReadMessageCount > 0;

  // ── Latest message preview ────────────────────────────────────────────────
  let content = "Chưa có tin nhắn nào";
  let sentTime = "";

  if (latestMessage) {
    const {
      type,
      callAt,
      endAt,
      deleted,
      attachment,
      content: msgContent,
      sentAt,
      sender,
    } = latestMessage;

    // Prefix người gửi
    let senderPrefix = "";
    if (isGroup && sender) {
      const currentUserId =
        typeof window !== "undefined" ? localStorage.getItem("userId") : null;
      const isSelf = sender.id === currentUserId;
      senderPrefix = isSelf
        ? "Bạn: "
        : `${sender.givenName || sender.username || ""}: `;
    } else if (!isGroup) {
      const isSenderTarget = sender?.id === target?.id;
      senderPrefix = isSenderTarget ? "" : "Bạn: ";
    }

    if (type === "CALL") {
      if (callAt && endAt) {
        const durationSec = dayjs(endAt).diff(dayjs(callAt), "second");
        const min = Math.floor(durationSec / 60);
        const sec = durationSec % 60;
        content = `📞 Cuộc gọi đã kết thúc (${min}:${sec.toString().padStart(2, "0")})`;
      } else {
        content = "📞 Cuộc gọi nhỡ";
      }
    } else {
      if (deleted) {
        content = "Tin nhắn đã bị thu hồi";
      } else if (type === "VOICE") {
        content = "🎤 Tin nhắn thoại";
      } else if (attachment) {
        content = "📎 Tệp đính kèm";
      } else if (type === "GIF") {
        content = "🎞️ GIF";
      } else {
        content = msgContent?.slice(0, 60) || "Tin nhắn đã bị xoá";
      }
      content = senderPrefix + content;
    }

    sentTime = dayjs(sentAt).fromNow();
  }

  return (
    <div
      onClick={onClick}
      className={`flex items-center gap-3 p-3 rounded-xl cursor-pointer transition-colors
        hover:bg-[var(--accent)] ${selected ? "bg-[var(--accent)]" : ""}`}
      data-chat-id={chatId}
    >
      {/* ── Avatar ─────────────────────────────────────────────────────────── */}
      <div className="relative flex-shrink-0">
        {isGroup ? (
          // Group avatar
          <div className="w-12 h-12 rounded-full overflow-hidden bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
            {groupAvatarUrl ? (
              <img
                src={groupAvatarUrl}
                alt={displayName}
                className="w-full h-full object-cover"
              />
            ) : (
              <span className="text-white font-bold text-lg">
                {displayName?.[0]?.toUpperCase() || "G"}
              </span>
            )}
          </div>
        ) : (
          // Direct chat avatar
          <Avatar
            src={target?.profilePictureUrl}
            alt={displayName}
            className="w-12 h-12"
          />
        )}

        {/* Online dot — chỉ direct chat */}
        {!isGroup && (
          <div className="absolute bottom-0 right-0">
            <div
              className={`w-3.5 h-3.5 rounded-full border-2 border-[var(--background)]
                ${isOnline ? "bg-green-500" : "bg-gray-400"}`}
            >
              {isOnline && (
                <div className="absolute inset-0 w-3.5 h-3.5 bg-green-500 rounded-full animate-pulse opacity-75" />
              )}
            </div>
          </div>
        )}

        {/* Group badge */}
        {isGroup && (
          <div className="absolute bottom-0 right-0 w-4 h-4 bg-blue-500 rounded-full border-2 border-[var(--background)] flex items-center justify-center">
            <Users className="w-2.5 h-2.5 text-white" />
          </div>
        )}
      </div>

      {/* ── Text info ──────────────────────────────────────────────────────── */}
      <div className="flex-1 min-w-0 flex flex-col hide-between-630-768">
        <div className="flex justify-between items-center mb-0.5">
          <div className="flex items-center gap-1.5 min-w-0">
            <p className={`truncate ${isUnread ? "font-bold" : "font-medium"}`}>
              {displayName}
            </p>
            {/* Group role badge (optional, nhỏ thôi) */}
            {isGroup && myRole === "OWNER" && (
              <span className="text-[10px] px-1 py-0.5 rounded bg-yellow-100 text-yellow-700 font-medium flex-shrink-0">
                Owner
              </span>
            )}
          </div>
          {sentTime && (
            <span
              className={`text-xs text-[var(--muted-foreground)] shrink-0 ml-1
                ${isUnread ? "font-bold" : ""}`}
            >
              {sentTime}
            </span>
          )}
        </div>

        <div className="flex justify-between items-center">
          <p
            className={`text-sm text-[var(--muted-foreground)] truncate
              ${isUnread ? "font-semibold text-[var(--foreground)]" : ""}`}
          >
            {content}
          </p>
          {notReadMessageCount > 0 && (
            <Badge
              variant="secondary"
              className="rounded-full border px-2 text-xs ml-2 shrink-0 bg-blue-600 text-white border-blue-600"
            >
              {notReadMessageCount > 99 ? "99+" : notReadMessageCount}
            </Badge>
          )}
        </div>

        {/* Group member count */}
        {isGroup && memberCount > 0 && (
          <p className="text-[11px] text-[var(--muted-foreground)] mt-0.5 flex items-center gap-1">
            <Users className="w-2.5 h-2.5" />
            {memberCount} thành viên
          </p>
        )}
      </div>
    </div>
  );
}

// ── Memo comparison ───────────────────────────────────────────────────────────
const areEqual = (prevProps, nextProps) => {
  const prev = prevProps.chat;
  const next = nextProps.chat;

  if (prevProps.selected !== nextProps.selected) return false;
  if (prev.chatId !== next.chatId) return false;
  if (prev.notReadMessageCount !== next.notReadMessageCount) return false;
  if (prev.isGroup !== next.isGroup) return false;
  if (prev.name !== next.name) return false;
  if (prev.groupAvatarUrl !== next.groupAvatarUrl) return false;
  if (prev.memberCount !== next.memberCount) return false;
  if (prev.myRole !== next.myRole) return false;

  // Direct chat fields
  if (prev.target?.isOnline !== next.target?.isOnline) return false;
  if (prev.target?.profilePictureUrl !== next.target?.profilePictureUrl) return false;
  if (prev.target?.givenName !== next.target?.givenName) return false;
  if (prev.target?.familyName !== next.target?.familyName) return false;
  if (prev.target?.username !== next.target?.username) return false;

  // Latest message
  const prevMsg = prev.latestMessage;
  const nextMsg = next.latestMessage;
  if (!prevMsg && !nextMsg) return true;
  if (!prevMsg || !nextMsg) return false;
  if (prevMsg.id !== nextMsg.id) return false;
  if (prevMsg.content !== nextMsg.content) return false;
  if (prevMsg.sentAt !== nextMsg.sentAt) return false;
  if (prevMsg.type !== nextMsg.type) return false;
  if (prevMsg.deleted !== nextMsg.deleted) return false;
  if (prevMsg.attachment !== nextMsg.attachment) return false;
  if (prevMsg.callAt !== nextMsg.callAt) return false;
  if (prevMsg.endAt !== nextMsg.endAt) return false;
  if (prevMsg.sender?.id !== nextMsg.sender?.id) return false;

  return true;
};

export default memo(ChatItem, areEqual);