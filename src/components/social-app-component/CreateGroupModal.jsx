"use client";

import { useState } from "react";
import { X, Search, Users, Check } from "lucide-react";
import toast from "react-hot-toast";
import Avatar from "../ui-components/Avatar";
import { groupApi } from "@/utils/groupApi";
import api from "@/utils/axios";
import useAppStore from "@/store/ZustandStore";

export default function CreateGroupModal({ onClose, onCreated }) {
  const [step, setStep] = useState(1); // 1=name, 2=members
  const [groupName, setGroupName] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searching, setSearching] = useState(false);
  const { fetchChatList } = useAppStore();

  const handleSearch = async (q) => {
    setSearchQuery(q);
    if (!q.trim()) {
      setSearchResults([]);
      return;
    }
    setSearching(true);
    try {
      const res = await api.get(`/v1/search`, {
        params: { query: q, type: "USER" },
      });
      const body = res.data.body ?? res.data;
      setSearchResults(body?.USER || body?.user || []);
    } catch {
      setSearchResults([]);
    } finally {
      setSearching(false);
    }
  };

  const toggleUser = (user) => {
    setSelectedUsers((prev) =>
      prev.find((u) => u.username === user.username)
        ? prev.filter((u) => u.username !== user.username)
        : [...prev, user],
    );
  };

  const handleCreate = async () => {
    if (!groupName.trim()) {
      toast.error("Nhập tên nhóm!");
      return;
    }
    if (selectedUsers.length < 1) {
      toast.error("Chọn ít nhất 1 thành viên!");
      return;
    }

    setLoading(true);
    try {
      await groupApi.createGroup(
        groupName.trim(),
        selectedUsers.map((u) => u.username),
      );
      toast.success("Tạo nhóm thành công!");
      await fetchChatList();
      onCreated?.();
      onClose();
    } catch (e) {
      toast.error(e.response?.data?.message || "Lỗi tạo nhóm");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-[var(--card)] rounded-2xl w-full max-w-md mx-4 shadow-xl">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-[var(--border)]">
          <h2 className="text-lg font-semibold">Tạo nhóm chat</h2>
          <button
            onClick={onClose}
            className="p-1 hover:bg-[var(--accent)] rounded-full"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-4 space-y-4">
          {/* Group name input */}
          <div>
            <label className="text-sm font-medium text-[var(--muted-foreground)] mb-1 block">
              Tên nhóm
            </label>
            <input
              type="text"
              value={groupName}
              onChange={(e) => setGroupName(e.target.value)}
              placeholder="Nhập tên nhóm..."
              maxLength={100}
              className="w-full px-3 py-2 rounded-lg border border-[var(--border)] bg-[var(--background)] text-[var(--foreground)] focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
            />
          </div>

          {/* Selected users */}
          {selectedUsers.length > 0 && (
            <div className="flex flex-wrap gap-2">
              {selectedUsers.map((u) => (
                <div
                  key={u.username}
                  className="flex items-center gap-1 bg-blue-100 text-blue-800 rounded-full px-2 py-1 text-xs"
                >
                  <Avatar src={u.profilePictureUrl} size="xs" />
                  <span>{u.givenName}</span>
                  <button
                    onClick={() => toggleUser(u)}
                    className="ml-1 hover:text-red-600"
                  >
                    <X className="w-3 h-3" />
                  </button>
                </div>
              ))}
            </div>
          )}

          {/* Search members */}
          <div>
            <label className="text-sm font-medium text-[var(--muted-foreground)] mb-1 block">
              Thêm thành viên ({selectedUsers.length} đã chọn)
            </label>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[var(--muted-foreground)]" />
              <input
                type="text"
                value={searchQuery}
                onChange={(e) => handleSearch(e.target.value)}
                placeholder="Tìm kiếm bạn bè..."
                className="w-full pl-9 pr-3 py-2 rounded-lg border border-[var(--border)] bg-[var(--background)] text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          {/* Search results */}
          <div className="max-h-48 overflow-y-auto space-y-1">
            {searching && (
              <p className="text-xs text-center text-[var(--muted-foreground)] py-2">
                Đang tìm...
              </p>
            )}
            {searchResults.map((user) => {
              const selected = selectedUsers.find(
                (u) => u.username === user.username,
              );
              return (
                <div
                  key={user.username}
                  onClick={() => toggleUser(user)}
                  className={`flex items-center gap-3 p-2 rounded-lg cursor-pointer transition-colors
                    ${selected ? "bg-blue-50" : "hover:bg-[var(--accent)]"}`}
                >
                  <Avatar src={user.profilePictureUrl} size="sm" />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">
                      {user.givenName} {user.familyName}
                    </p>
                    <p className="text-xs text-[var(--muted-foreground)]">
                      @{user.username}
                    </p>
                  </div>
                  {selected && (
                    <Check className="w-4 h-4 text-blue-600 flex-shrink-0" />
                  )}
                </div>
              );
            })}
          </div>
        </div>

        {/* Footer */}
        <div className="flex gap-2 p-4 border-t border-[var(--border)]">
          <button
            onClick={onClose}
            className="flex-1 py-2 rounded-lg border border-[var(--border)] text-sm hover:bg-[var(--accent)] transition-colors"
          >
            Hủy
          </button>
          <button
            onClick={handleCreate}
            disabled={loading || !groupName.trim() || selectedUsers.length < 1}
            className="flex-1 py-2 rounded-lg bg-blue-600 text-white text-sm font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {loading
              ? "Đang tạo..."
              : `Tạo nhóm (${selectedUsers.length + 1} người)`}
          </button>
        </div>
      </div>
    </div>
  );
}
