"use client";

import { useState } from "react";
import { MessageCircle, UserPlus, UserMinus, UserCheck, UserX, Shield, MoreHorizontal, Grid3x3, Film } from "lucide-react";
import Avatar from "../ui-components/Avatar";
import Modal from "../ui-components/Modal";
import EditProfileModal from "./EditProfile";
import api from "@/utils/axios";
import toast from "react-hot-toast";
import { useParams, useRouter } from "next/navigation";
import useAppStore from "@/store/ZustandStore";
import FriendsListModal from "./FriendsListModal";

export default function ProfileHeader({
  profileData,
  isOwnProfile = true,
  activeTab = "posts",
  onTabChange,
  onProfileUpdate,
  onUsernameChange,
}) {
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isFriendsModalOpen, setIsFriendsModalOpen] = useState(false);
  const [friendsList, setFriendsList] = useState([]);
  const [isLoadingFriends, setIsLoadingFriends] = useState(false);
  const [initialModalTab, setInitialModalTab] = useState("friends");
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);

  const avatar = profileData.profilePictureUrl;
  const { username: routeUsername } = useParams();
  const router = useRouter();
  const username = profileData.username;

  const selectChat = useAppStore((s) => s.selectChat);
  const showVirtualChat = useAppStore((s) => s.showVirtualChat);
  const chatList = useAppStore((s) => s.chatList);

  const handleBlockUser = async () => {
    if (!window.confirm(`Bạn có chắc muốn chặn ${routeUsername}?`)) return;
    try {
      const res = await api.post(`/v1/blocks/${routeUsername}`);
      if (res.data.code === 200) { toast.success(`Đã chặn ${routeUsername}`); setIsDropdownOpen(false); }
    } catch { toast.error("Có lỗi xảy ra khi chặn người dùng."); }
  };

  const handleSaveProfile = (newData, changeInfo) => {
    if (onProfileUpdate) onProfileUpdate(newData);
    if (changeInfo?.usernameChanged && onUsernameChange)
      onUsernameChange(changeInfo.oldUsername, changeInfo.newUsername);
    setIsEditModalOpen(false);
  };

  const handleChatClick = () => {
    const targetUserId = profileData.id;
    if (!targetUserId) { toast.error("Không thể tìm thấy thông tin người dùng"); return; }
    const existing = chatList.find(c => c.target?.id === targetUserId || c.target?.username === username);
    if (existing) { selectChat(existing.chatId); router.push("/chats"); return; }
    showVirtualChat(targetUserId, {
      username: profileData.username, givenName: profileData.givenName,
      familyName: profileData.familyName, profilePictureUrl: profileData.profilePictureUrl,
      online: profileData.online || false,
    });
    router.push("/chats");
  };

  const cancelFriendRequest = async () => {
    try { await api.delete(`/v1/friend-request/delete/${username}`); toast.success("Đã hủy lời mời kết bạn"); onProfileUpdate({ ...profileData, request: "NONE" }); }
    catch { toast.error("Lỗi khi hủy lời mời"); }
  };

  const declineFriendRequest = async () => {
    try { await api.delete(`/v1/friend-request/delete/${username}`); toast.success("Đã từ chối lời mời"); onProfileUpdate({ ...profileData, request: "NONE" }); }
    catch { toast.error("Lỗi khi từ chối lời mời"); }
  };

  const sendFriendRequest = async () => {
    try {
      const res = await api.post(`/v1/friend-request/send/${username}`);
      if (res.data.code === 200) { toast.success("Gửi lời mời thành công"); onProfileUpdate({ ...profileData, request: "OUT" }); }
    } catch { toast.error("Lỗi gửi lời mời"); }
  };

  const acceptFriendRequest = async () => {
    onProfileUpdate({ ...profileData, isFriend: true, request: "NONE", friendCount: profileData.friendCount + 1 });
    toast.success("Đã chấp nhận kết bạn");
    try {
      const res = await api.post(`/v1/friend-request/accept/${username}`);
      if (res.data.code !== 200) onProfileUpdate({ ...profileData, isFriend: false, request: "IN", friendCount: profileData.friendCount });
    } catch { onProfileUpdate({ ...profileData, isFriend: false, request: "IN", friendCount: profileData.friendCount }); }
  };

  const unfriend = async () => {
    try {
      await api.delete(`/v1/friends/${username}`);
      toast.success("Đã hủy kết bạn");
      onProfileUpdate({ ...profileData, isFriend: false, request: "NONE", friendCount: profileData.friendCount - 1 });
      setIsDropdownOpen(false);
    } catch { toast.error("Lỗi khi hủy kết bạn"); }
  };

  const handleGetListFriend = async () => {
    if (profileData.friendCount === 0) { setFriendsList([]); setInitialModalTab("friends"); setIsFriendsModalOpen(true); return; }
    setIsLoadingFriends(true);
    try {
      const res = await api.get(`/v1/friends/${username}`);
      if (res.data.code === 200) { setFriendsList(res.data.body || []); setInitialModalTab("friends"); setIsFriendsModalOpen(true); }
      else toast.error("Không thể tải danh sách bạn bè");
    } catch { toast.error("Có lỗi xảy ra"); } finally { setIsLoadingFriends(false); }
  };

  const handleGetMutualFriends = async () => {
    setIsLoadingFriends(true);
    try {
      const res = await api.get(`/v1/friends/${username}`);
      if (res.data.code === 200) { setFriendsList(res.data.body || []); setInitialModalTab("mutual"); setIsFriendsModalOpen(true); }
      else toast.error("Không thể tải danh sách bạn bè");
    } catch { toast.error("Có lỗi xảy ra"); } finally { setIsLoadingFriends(false); }
  };

  // --- Render action buttons (Instagram style: filled primary + ghost secondary) ---
  const renderActionButtons = () => {
    if (profileData.request === "OUT") {
      return (
        <button onClick={cancelFriendRequest}
          className="px-5 py-1.5 rounded-lg text-sm font-semibold bg-[var(--muted)] text-[var(--foreground)] hover:bg-[var(--muted)]/80 transition-colors">
          Đã gửi lời mời
        </button>
      );
    }
    if (profileData.request === "IN") {
      return (
        <div className="flex gap-2">
          <button onClick={acceptFriendRequest}
            className="px-5 py-1.5 rounded-lg text-sm font-semibold bg-blue-500 text-white hover:bg-blue-600 transition-colors">
            Xác nhận
          </button>
          <button onClick={declineFriendRequest}
            className="px-5 py-1.5 rounded-lg text-sm font-semibold bg-[var(--muted)] text-[var(--foreground)] hover:bg-[var(--muted)]/80 transition-colors">
            Xóa
          </button>
        </div>
      );
    }
    if (!profileData.isFriend && (profileData.request === "NONE" || profileData.request === null)) {
      return (
        <button onClick={sendFriendRequest}
          className="px-5 py-1.5 rounded-lg text-sm font-semibold bg-blue-500 text-white hover:bg-blue-600 transition-colors">
          Thêm bạn bè
        </button>
      );
    }
    return null;
  };

  return (
    <div className="w-full">
      {/* ── Top section ── */}
      <div className="flex items-start gap-8 sm:gap-16 px-4 sm:px-8 py-8">
        {/* Avatar */}
        <div className="shrink-0">
          <div className="w-20 h-20 sm:w-36 sm:h-36 rounded-full overflow-hidden ring-1 ring-[var(--border)]">
            <Avatar src={avatar} alt="Avatar" className="w-full h-full object-cover" />
          </div>
        </div>

        {/* Info */}
        <div className="flex-1 min-w-0 pt-1">
          {/* Username row + buttons */}
          <div className="flex flex-wrap items-center gap-3 mb-4">
            <h2 className="text-xl font-light text-[var(--foreground)] truncate">
              {profileData?.username}
            </h2>

            {isOwnProfile ? (
              <button
                onClick={() => setIsEditModalOpen(true)}
                className="px-5 py-1.5 rounded-lg text-sm font-semibold bg-[var(--muted)] text-[var(--foreground)] hover:bg-[var(--muted)]/80 transition-colors"
              >
                Chỉnh sửa trang cá nhân
              </button>
            ) : (
              <div className="flex items-center gap-2">
                {renderActionButtons()}
                <button onClick={handleChatClick}
                  className="px-5 py-1.5 rounded-lg text-sm font-semibold bg-[var(--muted)] text-[var(--foreground)] hover:bg-[var(--muted)]/80 transition-colors">
                  Nhắn tin
                </button>
                <div className="relative">
                  <button
                    onClick={() => setIsDropdownOpen(!isDropdownOpen)}
                    className="flex items-center justify-center w-8 h-8 rounded-lg bg-[var(--muted)] hover:bg-[var(--muted)]/80 transition-colors"
                  >
                    <MoreHorizontal size={18} />
                  </button>
                  {isDropdownOpen && (
                    <div className="absolute right-0 top-full mt-2 w-44 bg-[var(--card)] rounded-xl shadow-xl border border-[var(--border)] z-50 overflow-hidden">
                      {profileData.isFriend && (
                        <button onClick={unfriend}
                          className="w-full px-4 py-3 text-left text-sm text-red-500 hover:bg-[var(--muted)] flex items-center gap-2 font-medium">
                          <UserMinus size={15} /> Hủy kết bạn
                        </button>
                      )}
                      <button onClick={handleBlockUser}
                        className="w-full px-4 py-3 text-left text-sm text-red-500 hover:bg-[var(--muted)] flex items-center gap-2 font-medium">
                        <Shield size={15} /> Chặn
                      </button>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>

          {/* Stats row */}
          <div className="flex gap-6 mb-4 text-sm">
            <span className="text-[var(--foreground)]">
              <strong className="font-semibold">{profileData.postCount || 0}</strong>
              <span className="text-[var(--muted-foreground)] ml-1">bài viết</span>
            </span>
            <button onClick={handleGetListFriend} disabled={isLoadingFriends}
              className="text-[var(--foreground)] hover:opacity-70 transition-opacity disabled:opacity-50">
              <strong className="font-semibold">{profileData?.friendCount || 0}</strong>
              <span className="text-[var(--muted-foreground)] ml-1">bạn bè</span>
            </button>
            <button onClick={handleGetMutualFriends} disabled={isLoadingFriends}
              className="text-[var(--foreground)] hover:opacity-70 transition-opacity disabled:opacity-50">
              <strong className="font-semibold">{profileData?.mutualFriendsCount || 0}</strong>
              <span className="text-[var(--muted-foreground)] ml-1">bạn chung</span>
            </button>
          </div>

          {/* Full name + bio */}
          <div className="text-sm space-y-0.5">
            <p className="font-semibold text-[var(--foreground)]">
              {profileData?.givenName} {profileData?.familyName}
            </p>
            {profileData?.bio && (
              <p className="text-[var(--foreground)] whitespace-pre-wrap leading-snug">{profileData.bio}</p>
            )}
          </div>
        </div>
      </div>

      {/* ── Tab bar ── */}
      <div className="flex border-t border-[var(--border)]">
        <button
          onClick={() => onTabChange?.("posts")}
          className={`flex-1 flex items-center justify-center gap-2 py-3 text-xs font-semibold uppercase tracking-widest border-t-2 transition-colors ${
            activeTab === "posts"
              ? "border-[var(--foreground)] text-[var(--foreground)]"
              : "border-transparent text-[var(--muted-foreground)] hover:text-[var(--foreground)]"
          }`}
        >
          <Grid3x3 size={14} />
          Bài viết
        </button>
        <button
          onClick={() => onTabChange?.("file")}
          className={`flex-1 flex items-center justify-center gap-2 py-3 text-xs font-semibold uppercase tracking-widest border-t-2 transition-colors ${
            activeTab === "file"
              ? "border-[var(--foreground)] text-[var(--foreground)]"
              : "border-transparent text-[var(--muted-foreground)] hover:text-[var(--foreground)]"
          }`}
        >
          <Film size={14} />
          Ảnh & video
        </button>
      </div>

      {/* Overlay */}
      {isDropdownOpen && (
        <div className="fixed inset-0 z-40" onClick={() => setIsDropdownOpen(false)} />
      )}

      <Modal isOpen={isEditModalOpen} onClose={() => setIsEditModalOpen(false)}>
        <EditProfileModal profileData={profileData} onSave={handleSaveProfile} />
      </Modal>

      <Modal isOpen={isFriendsModalOpen} onClose={() => setIsFriendsModalOpen(false)} size="small">
        <FriendsListModal username={username} initialFriends={friendsList} initialTab={initialModalTab} />
      </Modal>
    </div>
  );
}