"use client";

import { ArrowLeft, Phone, Video, Users, Info } from "lucide-react";
import Avatar from "../ui-components/Avatar";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import { useRouter } from "next/navigation";
import { useState } from "react";
import GroupInfoModal from "./GroupInfoModal";

dayjs.extend(relativeTime);

export default function ChatHeader({
  targetUser,
  chat,           // full chat object (for group)
  isGroup,
  onBack,
  onCall,
  onVideoCall,
  onChatUpdated,
}) {
  const router = useRouter();
  const [showGroupInfo, setShowGroupInfo] = useState(false);

  const handleProfileClick = (e, user) => {
    e.stopPropagation();
    router.push(`/profile/${user.username}`);
  };

  // Direct chat status text
  let statusText = "Offline 🔴";
  if (!isGroup) {
    if (targetUser?.isOnline) statusText = "Online 🟢";
    else if (targetUser?.lastOnline) statusText = dayjs(targetUser.lastOnline).fromNow();
  }

  return (
    <>
      <div className="flex items-center justify-between gap-3 p-3 py-1 border-b border-[var(--border)]">
        <div className="flex items-center gap-3 min-w-0">
          <button onClick={onBack} className="text-[var(--muted-foreground)] hover:text-foreground flex-shrink-0">
            <ArrowLeft className="w-3 h-3" />
          </button>

          {isGroup ? (
            // Group chat header
            <div className="flex items-center gap-2 cursor-pointer" onClick={() => setShowGroupInfo(true)}>
              <Avatar src={chat?.groupAvatarUrl} size="sm" fallback={chat?.name?.[0]} />
              <div className="flex-1 min-w-0">
                <div className="font-semibold text-base truncate">{chat?.name}</div>
                <div className="text-xs text-[var(--muted-foreground)] flex items-center gap-1">
                  <Users className="w-3 h-3" />
                  {chat?.memberCount || "?"} thành viên
                </div>
              </div>
            </div>
          ) : (
            // Direct chat header
            <div className="flex items-center cursor-pointer" onClick={(e) => handleProfileClick(e, targetUser)}>
              <Avatar src={targetUser?.profilePictureUrl} size="sm" />
              <div className="flex-1 px-2 min-w-0">
                <div className="font-semibold text-base truncate">{targetUser?.givenName}</div>
                <div className="text-sm text-[var(--muted-foreground)]">{statusText}</div>
              </div>
            </div>
          )}
        </div>

        {/* Action buttons */}
        <div className="flex items-center gap-1 flex-shrink-0">
          {!isGroup && (
            <>
              <button onClick={() => onCall?.()}
                className="p-2 text-[var(--muted-foreground)] hover:text-foreground hover:bg-[var(--accent)] rounded-full transition-colors"
                title="Voice call">
                <Phone className="w-5 h-5" />
              </button>
              <button onClick={() => onVideoCall?.()}
                className="p-2 text-[var(--muted-foreground)] hover:text-foreground hover:bg-[var(--accent)] rounded-full transition-colors"
                title="Video call">
                <Video className="w-5 h-5" />
              </button>
            </>
          )}

          {isGroup && (
            <button onClick={() => setShowGroupInfo(true)}
              className="p-2 text-[var(--muted-foreground)] hover:text-foreground hover:bg-[var(--accent)] rounded-full transition-colors"
              title="Thông tin nhóm">
              <Info className="w-5 h-5" />
            </button>
          )}
        </div>
      </div>

      {/* Group Info Modal */}
      {showGroupInfo && (
        <GroupInfoModal
          chat={chat}
          onClose={() => setShowGroupInfo(false)}
          onChatUpdated={onChatUpdated}
        />
      )}
    </>
  );
}