"use client";

import { useState, useEffect } from "react";
import UserHeader from "@/components/social-app-component/UserHeader";
import ConfirmDialog from "@/components/social-app-component/ConfirmDialog";
import api from "@/utils/axios";
import toast from "react-hot-toast";

export default function BlockedUsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState({});
  const [confirmDialog, setConfirmDialog] = useState({ isOpen: false, userId: null, userName: "" });

  useEffect(() => {
    const fetchBlockedUsers = async () => {
      setLoading(true);
      try {
        const response = await api.get("/v1/blocks");
        setUsers(response.data.body || []);
      } catch (error) {
        console.error("Lỗi khi tải danh sách chặn:", error);
        toast.error("Lỗi khi tải danh sách chặn");
      } finally {
        setLoading(false);
      }
    };
    fetchBlockedUsers();
  }, []);

  const openConfirm = (user) => {
    setConfirmDialog({
      isOpen: true,
      userId: user.username,
      userName: user.givenName ? `${user.givenName} ${user.familyName || ""}`.trim() : user.username,
    });
  };

  const handleConfirm = async () => {
    const userId = confirmDialog.userId;
    setConfirmDialog({ isOpen: false, userId: null, userName: "" });
    const previousUsers = [...users];
    setUsers((prev) => prev.filter((user) => user.username !== userId));
    setActionLoading((prev) => ({ ...prev, [userId]: true }));
    try {
      await api.delete(`/v1/blocks/${userId}`);
      toast.success("Đã bỏ chặn thành công");
    } catch (error) {
      setUsers(previousUsers);
      toast.error(`Lỗi: ${error.response?.data?.message || error.message}`);
    } finally {
      setActionLoading((prev) => ({ ...prev, [userId]: false }));
    }
  };

  const handleCancel = () => {
    setConfirmDialog({ isOpen: false, userId: null, userName: "" });
  };

  return (
    <div className="container mx-auto px-4 py-6 max-w-4xl">
      <ConfirmDialog
        isOpen={confirmDialog.isOpen}
        title="Bỏ chặn người dùng?"
        message={`Bạn có chắc muốn bỏ chặn ${confirmDialog.userName} không?`}
        confirmText="Bỏ chặn"
        cancelText="Hủy"
        confirmStyle="danger"
        onConfirm={handleConfirm}
        onCancel={handleCancel}
      />
      <h1 className="text-2xl font-bold mb-6 text-[var(--foreground)]">Đã chặn</h1>
      <div className="bg-[var(--card)] rounded-lg shadow">
        {loading ? (
          <div className="p-8 text-center text-[var(--muted-foreground)]">
            <div className="animate-pulse flex flex-col items-center gap-2">
              <div className="h-4 w-32 bg-[var(--muted)] rounded"></div>
              <div className="h-4 w-64 bg-[var(--muted)] rounded"></div>
            </div>
          </div>
        ) : users.length === 0 ? (
          <div className="p-8 text-center text-[var(--muted-foreground)]">Bạn chưa chặn ai</div>
        ) : (
          <ul className="divide-y divide-[var(--border)]">
            {users.map((user) => (
              <li key={user.username} className="p-4 hover:bg-[var(--accent)] transition-colors">
                <div className="flex items-center justify-between gap-4">
                  <UserHeader user={user} className="flex-grow min-w-0" />
                  <button
                    onClick={() => openConfirm(user)}
                    disabled={actionLoading[user.username]}
                    className="px-3 py-1 text-sm rounded-md bg-gray-50 text-gray-600 hover:bg-gray-100 min-w-[100px]"
                  >
                    {actionLoading[user.username] ? "..." : "Bỏ chặn"}
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}