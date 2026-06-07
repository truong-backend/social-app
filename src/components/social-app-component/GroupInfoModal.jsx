"use client";

import { useState, useEffect, useRef } from "react";
import { X, Crown, Shield, User, MoreVertical, Edit2, UserPlus, LogOut, Trash2, Pin, ChevronRight } from "lucide-react";
import toast from "react-hot-toast";
import Avatar from "../ui-components/Avatar";
import { groupApi } from "@/utils/groupApi";
import useAppStore from "@/store/ZustandStore";

const ROLE_ICON = {
  OWNER: <Crown className="w-3.5 h-3.5 text-yellow-500" />,
  ADMIN: <Shield className="w-3.5 h-3.5 text-blue-500" />,
  MEMBER: <User className="w-3.5 h-3.5 text-gray-400" />,
};

const ROLE_LABEL = { OWNER: "Trưởng nhóm", ADMIN: "Phó nhóm", MEMBER: "Thành viên" };

export default function GroupInfoModal({ chat, onClose, onChatUpdated }) {
  const [members, setMembers] = useState([]);
  const [pinnedMessages, setPinnedMessages] = useState([]);
  const [editingName, setEditingName] = useState(false);
  const [newName, setNewName] = useState(chat?.name || "");
  const [loading, setLoading] = useState(false);
  const [activeMenu, setActiveMenu] = useState(null);
  const [tab, setTab] = useState("members"); // members | pins
  const avatarInputRef = useRef(null);
  const { fetchChatList } = useAppStore();
  const myRole = chat?.myRole || "MEMBER";

  useEffect(() => {
    if (!chat?.chatId) return;
    groupApi.getMembers(chat.chatId).then(r => setMembers(r.data.body || []));
    groupApi.getPinnedMessages(chat.chatId).then(r => setPinnedMessages(r.data.body || []));
  }, [chat?.chatId]);

  // ── Rename ────────────────────────────────────────────────────────────────
  const handleRename = async () => {
    if (!newName.trim()) return;
    setLoading(true);
    try {
      await groupApi.updateGroup(chat.chatId, newName.trim(), null);
      toast.success("Đã đổi tên nhóm");
      setEditingName(false);
      onChatUpdated?.();
      fetchChatList();
    } catch { toast.error("Lỗi đổi tên nhóm"); }
    finally { setLoading(false); }
  };

  // ── Avatar ────────────────────────────────────────────────────────────────
  const handleAvatarChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setLoading(true);
    try {
      await groupApi.updateGroup(chat.chatId, null, file);
      toast.success("Đã cập nhật ảnh nhóm");
      onChatUpdated?.();
      fetchChatList();
    } catch { toast.error("Lỗi cập nhật ảnh"); }
    finally { setLoading(false); }
  };

  // ── Member actions ────────────────────────────────────────────────────────
  const handleRemoveMember = async (username) => {
    try {
      await groupApi.removeMember(chat.chatId, username);
      setMembers(prev => prev.filter(m => m.username !== username));
      toast.success("Đã xóa thành viên");
      setActiveMenu(null);
    } catch (e) { toast.error(e.response?.data?.message || "Lỗi xóa thành viên"); }
  };

  const handlePromote = async (username) => {
    try {
      await groupApi.promoteToAdmin(chat.chatId, username);
      setMembers(prev => prev.map(m => m.username === username ? { ...m, role: "ADMIN" } : m));
      toast.success("Đã nâng lên Admin");
      setActiveMenu(null);
    } catch (e) { toast.error(e.response?.data?.message || "Lỗi"); }
  };

  const handleDemote = async (username) => {
    try {
      await groupApi.demoteToMember(chat.chatId, username);
      setMembers(prev => prev.map(m => m.username === username ? { ...m, role: "MEMBER" } : m));
      toast.success("Đã hạ xuống Member");
      setActiveMenu(null);
    } catch (e) { toast.error(e.response?.data?.message || "Lỗi"); }
  };

  const handleTransferOwnership = async (username) => {
    if (!confirm("Bạn có chắc muốn chuyển quyền trưởng nhóm?")) return;
    try {
      await groupApi.transferOwnership(chat.chatId, username);
      toast.success("Đã chuyển quyền trưởng nhóm");
      onChatUpdated?.();
      fetchChatList();
      setActiveMenu(null);
    } catch (e) { toast.error(e.response?.data?.message || "Lỗi"); }
  };

  const handleLeave = async () => {
    if (!confirm("Bạn có chắc muốn rời nhóm?")) return;
    try {
      await groupApi.leaveGroup(chat.chatId);
      toast.success("Đã rời nhóm");
      fetchChatList();
      onClose();
    } catch (e) { toast.error(e.response?.data?.message || "Lỗi"); }
  };

  const handleDissolve = async () => {
    if (!confirm("Giải tán nhóm sẽ xóa toàn bộ dữ liệu. Bạn chắc chắn?")) return;
    try {
      await groupApi.dissolveGroup(chat.chatId);
      toast.success("Đã giải tán nhóm");
      fetchChatList();
      onClose();
    } catch (e) { toast.error(e.response?.data?.message || "Lỗi"); }
  };

  const handleUnpin = async (messageId) => {
    try {
      await groupApi.unpinMessage(chat.chatId, messageId);
      setPinnedMessages(prev => prev.filter(m => m.id !== messageId));
      toast.success("Đã bỏ ghim");
    } catch { toast.error("Lỗi bỏ ghim"); }
  };

  const canManage = myRole === "OWNER" || myRole === "ADMIN";

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-[var(--card)] rounded-2xl w-full max-w-md mx-4 max-h-[90vh] overflow-hidden shadow-xl flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-[var(--border)] flex-shrink-0">
          <h2 className="text-lg font-semibold">Thông tin nhóm</h2>
          <button onClick={onClose} className="p-1 hover:bg-[var(--accent)] rounded-full">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Group Avatar & Name */}
        <div className="flex flex-col items-center py-5 gap-3 border-b border-[var(--border)] flex-shrink-0">
          <div className="relative">
            <Avatar src={chat?.groupAvatarUrl} size="xl" fallback={chat?.name?.[0]} />
            {canManage && (
              <>
                <button
                  onClick={() => avatarInputRef.current?.click()}
                  className="absolute bottom-0 right-0 bg-blue-600 text-white rounded-full p-1 hover:bg-blue-700 shadow"
                >
                  <Edit2 className="w-3 h-3" />
                </button>
                <input ref={avatarInputRef} type="file" className="hidden" accept="image/*"
                  onChange={handleAvatarChange} />
              </>
            )}
          </div>

          {editingName ? (
            <div className="flex items-center gap-2">
              <input
                value={newName}
                onChange={e => setNewName(e.target.value)}
                className="border border-[var(--border)] rounded-lg px-3 py-1 text-sm bg-[var(--background)] focus:outline-none focus:ring-2 focus:ring-blue-500"
                maxLength={100}
                autoFocus
              />
              <button onClick={handleRename} disabled={loading}
                className="text-xs text-blue-600 font-medium hover:underline">
                Lưu
              </button>
              <button onClick={() => { setEditingName(false); setNewName(chat?.name); }}
                className="text-xs text-[var(--muted-foreground)] hover:underline">
                Hủy
              </button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <span className="font-semibold text-lg">{chat?.name}</span>
              {canManage && (
                <button onClick={() => setEditingName(true)} className="text-[var(--muted-foreground)] hover:text-[var(--foreground)]">
                  <Edit2 className="w-4 h-4" />
                </button>
              )}
            </div>
          )}
          <span className="text-sm text-[var(--muted-foreground)]">{members.length} thành viên</span>
        </div>

        {/* Tabs */}
        <div className="flex border-b border-[var(--border)] flex-shrink-0">
          {["members", "pins"].map(t => (
            <button key={t} onClick={() => setTab(t)}
              className={`flex-1 py-2.5 text-sm font-medium transition-colors
                ${tab === t ? "border-b-2 border-blue-600 text-blue-600" : "text-[var(--muted-foreground)] hover:text-[var(--foreground)]"}`}>
              {t === "members" ? "Thành viên" : "Tin nhắn ghim"}
            </button>
          ))}
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-3">
          {tab === "members" && (
            <div className="space-y-1">
              {members.map(member => (
                <div key={member.username}
                  className="flex items-center gap-3 p-2 rounded-lg hover:bg-[var(--accent)] group">
                  <div className="relative flex-shrink-0">
                    <Avatar src={member.profilePictureUrl} size="sm" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-1.5">
                      <span className="text-sm font-medium truncate">
                        {member.givenName} {member.familyName}
                      </span>
                      {ROLE_ICON[member.role]}
                    </div>
                    <p className="text-xs text-[var(--muted-foreground)]">
                      {ROLE_LABEL[member.role]} · @{member.username}
                    </p>
                  </div>

                  {/* Actions (Owner only or Admin for members) */}
                  {myRole === "OWNER" && member.role !== "OWNER" && (
                    <div className="relative">
                      <button
                        onClick={() => setActiveMenu(activeMenu === member.username ? null : member.username)}
                        className="p-1 rounded-full hover:bg-[var(--border)] opacity-0 group-hover:opacity-100 transition-opacity"
                      >
                        <MoreVertical className="w-4 h-4" />
                      </button>
                      {activeMenu === member.username && (
                        <div className="absolute right-0 top-8 z-10 bg-[var(--card)] border border-[var(--border)] rounded-lg shadow-lg py-1 min-w-40">
                          {member.role === "MEMBER" && (
                            <button onClick={() => handlePromote(member.username)}
                              className="w-full text-left px-3 py-2 text-sm hover:bg-[var(--accent)]">
                              Nâng lên Admin
                            </button>
                          )}
                          {member.role === "ADMIN" && (
                            <button onClick={() => handleDemote(member.username)}
                              className="w-full text-left px-3 py-2 text-sm hover:bg-[var(--accent)]">
                              Hạ xuống Member
                            </button>
                          )}
                          <button onClick={() => handleTransferOwnership(member.username)}
                            className="w-full text-left px-3 py-2 text-sm hover:bg-[var(--accent)] text-yellow-600">
                            Chuyển quyền trưởng nhóm
                          </button>
                          <button onClick={() => handleRemoveMember(member.username)}
                            className="w-full text-left px-3 py-2 text-sm hover:bg-[var(--accent)] text-red-600">
                            Xóa khỏi nhóm
                          </button>
                        </div>
                      )}
                    </div>
                  )}
                  {myRole === "ADMIN" && member.role === "MEMBER" && (
                    <button onClick={() => handleRemoveMember(member.username)}
                      className="p-1 rounded-full hover:bg-red-100 text-red-500 opacity-0 group-hover:opacity-100 transition-opacity">
                      <X className="w-4 h-4" />
                    </button>
                  )}
                </div>
              ))}
            </div>
          )}

          {tab === "pins" && (
            <div className="space-y-2">
              {pinnedMessages.length === 0 ? (
                <p className="text-center text-sm text-[var(--muted-foreground)] py-6">
                  Chưa có tin nhắn nào được ghim
                </p>
              ) : (
                pinnedMessages.map(msg => (
                  <div key={msg.id}
                    className="bg-[var(--accent)] rounded-lg p-3 border border-[var(--border)]">
                    <div className="flex items-start justify-between gap-2">
                      <div className="flex-1 min-w-0">
                        <p className="text-xs text-[var(--muted-foreground)] mb-1">
                          <Pin className="w-3 h-3 inline mr-1" />
                          @{msg.senderUsername}
                        </p>
                        <p className="text-sm truncate">{msg.content}</p>
                      </div>
                      {canManage && (
                        <button onClick={() => handleUnpin(msg.id)}
                          className="text-[var(--muted-foreground)] hover:text-red-500 flex-shrink-0">
                          <X className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>
          )}
        </div>

        {/* Actions Footer */}
        <div className="p-4 border-t border-[var(--border)] flex-shrink-0 space-y-2">
          {myRole !== "OWNER" && (
            <button onClick={handleLeave}
              className="w-full flex items-center justify-center gap-2 py-2 rounded-lg border border-red-200 text-red-600 text-sm hover:bg-red-50 transition-colors">
              <LogOut className="w-4 h-4" />
              Rời nhóm
            </button>
          )}
          {myRole === "OWNER" && (
            <button onClick={handleDissolve}
              className="w-full flex items-center justify-center gap-2 py-2 rounded-lg bg-red-600 text-white text-sm hover:bg-red-700 transition-colors">
              <Trash2 className="w-4 h-4" />
              Giải tán nhóm
            </button>
          )}
        </div>
      </div>
    </div>
  );
}