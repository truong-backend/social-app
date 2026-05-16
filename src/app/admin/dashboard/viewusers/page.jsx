"use client"

import React, { useState, useEffect, useCallback, useRef } from 'react';
import { User, Loader2, Mail, Calendar, Users, MessageCircle, FileText, Phone, Shield, ShieldCheck, Clock, UserCheck, UserX, Send, Inbox, ArrowLeft, Upload, ThumbsUp, MessageSquareText, Trash2, RotateCcw, AlertTriangle, X } from 'lucide-react';
import api from "@/utils/axios";
import UserHeader from '@/components/social-app-component/UserHeader';
import { useRouter } from 'next/navigation';
import adminApi from "@/utils/adminInterception";

// ==================== CONFIRM DIALOG ====================
const ConfirmDialog = ({ open, title, message, confirmLabel, confirmClass, onConfirm, onCancel, loading }) => {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
      <div className="bg-card border border-border rounded-2xl shadow-xl p-6 w-full max-w-sm mx-4">
        <div className="flex items-center gap-3 mb-3">
          <div className="w-10 h-10 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center flex-shrink-0">
            <AlertTriangle className="w-5 h-5 text-red-500" />
          </div>
          <h3 className="text-lg font-bold text-card-foreground">{title}</h3>
        </div>
        <p className="text-sm text-muted-foreground mb-6 leading-relaxed">{message}</p>
        <div className="flex gap-3">
          <button
            onClick={onCancel}
            disabled={loading}
            className="flex-1 px-4 py-2 rounded-lg border border-border text-card-foreground hover:bg-muted transition-colors text-sm font-medium disabled:opacity-50"
          >
            Hủy
          </button>
          <button
            onClick={onConfirm}
            disabled={loading}
            className={`flex-1 px-4 py-2 rounded-lg text-white text-sm font-medium transition-colors disabled:opacity-50 flex items-center justify-center gap-2 ${confirmClass}`}
          >
            {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : null}
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
};
// ========================================================

const UsersPage = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [currentSkip, setCurrentSkip] = useState(0);
  const [error, setError] = useState("");
  const router = useRouter();

  // Dialog state
  const [dialog, setDialog] = useState({
    open: false,
    type: null,       // 'delete' | 'restore'
    userId: null,
    userName: "",
    loading: false,
  });

  const abortControllerRef = useRef(null);
  const LIMIT = 20;

  const goToProfile = (username) => {
    if (username) router.push(`/profile/${username}`);
  };

  const goBackToAdmin = () => {
    router.push('/admin/dashboard/users');
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    try {
      return new Date(dateString).toLocaleDateString('vi-VN', {
        year: 'numeric', month: 'long', day: 'numeric'
      });
    } catch { return "N/A"; }
  };

  const calculateAge = (birthdate) => {
    if (!birthdate) return "N/A";
    try {
      const today = new Date();
      const birth = new Date(birthdate);
      let age = today.getFullYear() - birth.getFullYear();
      const monthDiff = today.getMonth() - birth.getMonth();
      if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) age--;
      return age;
    } catch { return "N/A"; }
  };

  const formatLastOnline = (lastOnline, isOnline) => {
    if (isOnline) return "Trực tuyến";
    if (!lastOnline) return "Rất lâu trước đây";
    try {
      const cleanDateString = typeof lastOnline === 'string'
        ? lastOnline.replace(/\[[^\]]+\]$/, '') : lastOnline;
      const date = new Date(cleanDateString);
      const diffMs = new Date() - date;
      const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
      const diffDays = Math.floor(diffHours / 24);
      if (diffHours < 1) return "Just now";
      if (diffHours < 24) return `${diffHours}h ago`;
      if (diffDays < 7) return `${diffDays}d ago`;
      return date.toLocaleDateString('vi-VN');
    } catch { return "N/A"; }
  };

  const fetchUsers = useCallback(async (skipValue = 0, isLoadMore = false) => {
    const token = localStorage.getItem("admin_accessToken");
    if (!token) { console.warn("Không có token đăng nhập"); return; }
    if (abortControllerRef.current) abortControllerRef.current.abort();
    abortControllerRef.current = new AbortController();

    try {
      isLoadMore ? setLoadingMore(true) : setLoading(true);
      setError("");
      const res = await adminApi.get(`/v1/users?skip=${skipValue}&limit=${LIMIT}`,
        { signal: abortControllerRef.current.signal });

      if (res.data.code === 200) {
        const newUsers = res.data.body || [];
        setUsers(prevUsers => {
          if (isLoadMore) {
            const existingIds = new Set(prevUsers.map(u => u.id));
            return [...prevUsers, ...newUsers.filter(u => !existingIds.has(u.id))];
          }
          return newUsers;
        });
        setHasMore(newUsers.length === LIMIT);
        setCurrentSkip(skipValue + newUsers.length);
      }
    } catch (err) {
      if (!abortControllerRef.current.signal.aborted) {
        setError(`Không thể tải danh sách users: ${err.message}`);
      }
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  }, []);

  const handleLoadMore = useCallback(() => {
    if (!loadingMore && hasMore) fetchUsers(currentSkip, true);
  }, [currentSkip, hasMore, loadingMore, fetchUsers]);

  useEffect(() => {
    fetchUsers(0, false);
    return () => { if (abortControllerRef.current) abortControllerRef.current.abort(); };
  }, [fetchUsers]);

  // ==================== DELETE / RESTORE HANDLERS ====================
  const openDeleteDialog = (e, user) => {
    e.stopPropagation();
    setDialog({ open: true, type: 'delete', userId: user.id, userName: `${user.givenName} ${user.familyName}`, loading: false });
  };

  const openRestoreDialog = (e, user) => {
    e.stopPropagation();
    setDialog({ open: true, type: 'restore', userId: user.id, userName: `${user.givenName} ${user.familyName}`, loading: false });
  };

  const closeDialog = () => {
    if (dialog.loading) return;
    setDialog(d => ({ ...d, open: false }));
  };

  const handleConfirm = async () => {
    setDialog(d => ({ ...d, loading: true }));
    try {
      if (dialog.type === 'delete') {
        await adminApi.delete(`/v1/users/admin/${dialog.userId}/soft-delete`);
        // Cập nhật state local: đánh dấu isDeleted = true
        setUsers(prev => prev.map(u =>
          u.id === dialog.userId ? { ...u, isDeleted: true, deletedAt: new Date().toISOString() } : u
        ));
      } else {
        await adminApi.patch(`/v1/users/admin/${dialog.userId}/restore`);
        // Cập nhật state local: bỏ isDeleted
        setUsers(prev => prev.map(u =>
          u.id === dialog.userId ? { ...u, isDeleted: false, deletedAt: null } : u
        ));
      }
      setDialog(d => ({ ...d, open: false, loading: false }));
    } catch (err) {
      setDialog(d => ({ ...d, loading: false }));
      setError(`Thao tác thất bại: ${err.response?.data?.message || err.message}`);
    }
  };
  // ==================================================================

  // User Card Component
  const UserCard = ({ user }) => (
    <div
      className={`bg-[var(--card)] rounded-xl shadow-sm border p-6 hover:shadow-md transition-shadow relative
        ${user.isDeleted
          ? 'border-red-300 dark:border-red-800 opacity-75'
          : 'border-border cursor-pointer'
        }`}
      onClick={() => !user.isDeleted && goToProfile(user.username)}
    >
      {/* Deleted Banner */}
      {user.isDeleted && (
        <div className="absolute top-0 left-0 right-0 bg-red-500 text-white text-xs font-semibold text-center py-1 rounded-t-xl flex items-center justify-center gap-1">
          <Trash2 className="w-3 h-3" />
          Đã xóa {user.deletedAt ? `— ${formatDate(user.deletedAt)}` : ""}
        </div>
      )}

      <div className={`flex items-start gap-4 mb-4 ${user.isDeleted ? 'mt-6' : ''}`}>
        <div className="relative">
          {user.profilePictureUrl ? (
            <img
              src={user.profilePictureUrl}
              alt={`${user.givenName} ${user.familyName}`}
              className={`w-16 h-16 rounded-full object-cover border-2 border-border ${user.isDeleted ? 'grayscale' : ''}`}
            />
          ) : (
            <div className={`w-16 h-16 rounded-full flex items-center justify-center
              ${user.isDeleted
                ? 'bg-gray-400'
                : 'bg-gradient-to-br from-blue-500 to-purple-600'
              }`}>
              <span className="text-white font-bold text-xl">
                {user.givenName?.charAt(0)}{user.familyName?.charAt(0)}
              </span>
            </div>
          )}
          <div className={`absolute -bottom-1 -right-1 w-5 h-5 rounded-full border-2 border-card ${user.isOnline && !user.isDeleted ? 'bg-green-500' : 'bg-gray-400'}`}></div>
        </div>

        <div className="flex-1 min-w-0">
          <div className="flex items-center gap-2 mb-1">
            <h3 className="text-lg font-bold text-card-foreground truncate">
              {user.givenName} {user.familyName}
            </h3>
            {user.verified && <ShieldCheck className="w-5 h-5 text-blue-500 flex-shrink-0" />}
            {user.isDeleted && <Trash2 className="w-4 h-4 text-red-500 flex-shrink-0" />}
          </div>
          <p className="text-sm text-muted-foreground truncate">@{user.username}</p>
          <div className="flex items-center gap-1 mt-1">
            <Clock className="w-4 h-4 text-muted-foreground" />
            <span className="text-sm text-muted-foreground">
              {user.isDeleted ? "Tài khoản bị xóa" : formatLastOnline(user.lastOnline, user.isOnline)}
            </span>
          </div>
        </div>
      </div>

      <div className="mb-4">
        <p className="text-foreground text-sm leading-relaxed line-clamp-2 h-12">
          {user.bio ? user.bio : "Không có tiểu sử"}
        </p>
      </div>

      <div className="gap-8 my-2">
        <div className="flex gap-2 justify-between">
          <div className="flex items-center gap-2">
            <Mail className="w-4 h-4 text-muted-foreground" />
            <span className="text-sm text-muted-foreground truncate">{user.email}</span>
          </div>
          <div className="flex items-center gap-2">
            <Calendar className="w-4 h-4 text-muted-foreground" />
            <span className="text-sm text-muted-foreground">{calculateAge(user.birthdate)} tuổi</span>
          </div>
        </div>
        <div className="flex items-center gap-2 py-2">
          <UserCheck className="w-4 h-4 text-muted-foreground" />
          <span className="text-sm text-muted-foreground">Tham gia: {formatDate(user.registrationDate)}</span>
        </div>
        <div className="flex items-center gap-2">
          <Calendar className="w-4 h-4 text-muted-foreground" />
          <span className="text-sm text-muted-foreground">Sinh: {formatDate(user.birthdate)}</span>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-4 pt-4 border-t border-border">
        <div className="text-center">
          <div className="flex items-center justify-center gap-1 mb-1">
            <Users className="w-4 h-4 text-blue-500" />
            <span className="font-bold text-card-foreground">{user.friendCount || 0}</span>
          </div>
          <p className="text-xs text-muted-foreground">Bạn bè</p>
        </div>
        <div className="text-center">
          <div className="flex items-center justify-center gap-1 mb-1">
            <FileText className="w-4 h-4 text-green-500" />
            <span className="font-bold text-card-foreground">{user.postCount || 0}</span>
          </div>
          <p className="text-xs text-muted-foreground">Bài viết</p>
        </div>
        <div className="text-center">
          <div className="flex items-center justify-center gap-1 mb-1">
            <MessageCircle className="w-4 h-4 text-purple-500" />
            <span className="font-bold text-card-foreground">{user.messageCount || 0}</span>
          </div>
          <p className="text-xs text-muted-foreground">Tin nhắn</p>
        </div>
      </div>

      <div className="grid grid-cols-5 gap-2 mt-3 pt-3 border-t border-border">
        <div className="text-center">
          <div className="flex items-center justify-center gap-1">
            <MessageSquareText className="w-3 h-3 text-blue-400" />
            <span className="text-xs font-medium text-muted-foreground">{user.commentCount || 0}</span>
          </div>
          <p className="text-xs text-muted-foreground">Bình luận</p>
        </div>
        <div className="text-center">
          <div className="flex items-center justify-center gap-1">
            <Phone className="w-3 h-3 text-green-400" />
            <span className="text-xs font-medium text-muted-foreground">{user.callCount || 0}</span>
          </div>
          <p className="text-xs text-muted-foreground">Cuộc gọi</p>
        </div>
        <div className="text-center">
          <div className="flex items-center justify-center gap-1">
            <Send className="w-3 h-3 text-blue-400" />
            <span className="text-xs font-medium text-muted-foreground">{user.requestSentCount || 0}</span>
          </div>
          <p className="text-xs text-muted-foreground">Đã gửi</p>
        </div>
        <div className="text-center">
          <div className="flex items-center justify-center gap-1">
            <Inbox className="w-3 h-3 text-orange-400" />
            <span className="text-xs font-medium text-muted-foreground">{user.requestReceivedCount || 0}</span>
          </div>
          <p className="text-xs text-muted-foreground">Đã nhận</p>
        </div>
        <div className="text-center">
          <div className="flex items-center justify-center gap-1">
            <Upload className="w-3 h-3 text-indigo-400" />
            <span className="text-xs font-medium text-muted-foreground">{user.uploadedFileCount || 0}</span>
          </div>
          <p className="text-xs text-muted-foreground">Tệp tải lên</p>
        </div>
      </div>

      {user.blockCount > 0 && (
        <div className="mt-3 pt-3 border-t border-border">
          <div className="flex items-center justify-center gap-1">
            <UserX className="w-4 h-4 text-red-500" />
            <span className="text-sm text-red-600 dark:text-red-400">{user.blockCount} người bị chặn</span>
          </div>
        </div>
      )}

      {/* ===== ACTION BUTTONS ===== */}
      <div className="mt-4 pt-3 border-t border-border flex gap-2">
        {user.isDeleted ? (
          <button
            onClick={(e) => openRestoreDialog(e, user)}
            className="flex-1 flex items-center justify-center gap-2 px-3 py-2 rounded-lg bg-green-500 hover:bg-green-600 text-white text-sm font-medium transition-colors"
          >
            <RotateCcw className="w-4 h-4" />
            Khôi phục
          </button>
        ) : (
          <button
            onClick={(e) => openDeleteDialog(e, user)}
            className="flex-1 flex items-center justify-center gap-2 px-3 py-2 rounded-lg bg-red-500 hover:bg-red-600 text-white text-sm font-medium transition-colors"
          >
            <Trash2 className="w-4 h-4" />
            Xóa tài khoản
          </button>
        )}
      </div>
      {/* ========================= */}
    </div>
  );

  return (
    <>
      {/* Confirm Dialog */}
      <ConfirmDialog
        open={dialog.open}
        loading={dialog.loading}
        type={dialog.type}
        title={dialog.type === 'delete' ? 'Xác nhận xóa tài khoản' : 'Xác nhận khôi phục tài khoản'}
        message={
          dialog.type === 'delete'
            ? `Bạn có chắc muốn xóa mềm tài khoản của "${dialog.userName}"? Tài khoản sẽ không thể đăng nhập nhưng vẫn có thể khôi phục sau.`
            : `Bạn có chắc muốn khôi phục tài khoản của "${dialog.userName}"? Người dùng sẽ có thể đăng nhập lại.`
        }
        confirmLabel={dialog.type === 'delete' ? 'Xóa tài khoản' : 'Khôi phục'}
        confirmClass={dialog.type === 'delete' ? 'bg-red-500 hover:bg-red-600' : 'bg-green-500 hover:bg-green-600'}
        onConfirm={handleConfirm}
        onCancel={closeDialog}
      />

      <main className="max-w-6xl mx-auto mt-4 px-4">
        <div className="space-y-6">
          {/* Header */}
          <div className="flex items-center justify-between bg-card rounded-xl shadow-sm border border-border p-6">
            <div className="flex items-center gap-4">
              <button
                onClick={goBackToAdmin}
                className="flex items-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors"
              >
                <ArrowLeft className="w-4 h-4" />
                Quay lại Admin
              </button>
              <div className="w-10 h-10 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full flex items-center justify-center">
                <Users className="w-5 h-5 text-white" />
              </div>
              <h2 className="text-2xl font-bold text-card-foreground">
                Tất cả người dùng ({users.length})
              </h2>
            </div>
            {/* Legend */}
            <div className="hidden md:flex items-center gap-4 text-xs text-muted-foreground">
              <div className="flex items-center gap-1">
                <div className="w-3 h-3 rounded-full bg-green-500"></div>
                <span>Hoạt động</span>
              </div>
              <div className="flex items-center gap-1">
                <div className="w-3 h-3 rounded-full bg-red-400"></div>
                <span>Đã xóa</span>
              </div>
            </div>
          </div>

          {/* Error State */}
          {error && (
            <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-xl p-4 flex items-center justify-between">
              <p className="text-red-600 dark:text-red-400">{error}</p>
              <button onClick={() => setError("")} className="text-red-400 hover:text-red-600">
                <X className="w-4 h-4" />
              </button>
            </div>
          )}

          {/* Users List */}
          <section>
            {loading && users.length === 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {[...Array(6)].map((_, i) => (
                  <div key={i} className="bg-card rounded-xl shadow-sm border border-border p-6">
                    <div className="animate-pulse">
                      <div className="flex items-center gap-4 mb-4">
                        <div className="w-16 h-16 bg-muted rounded-full"></div>
                        <div className="flex-1">
                          <div className="h-4 bg-muted rounded w-3/4 mb-2"></div>
                          <div className="h-3 bg-muted rounded w-1/2"></div>
                        </div>
                      </div>
                      <div className="space-y-2">
                        <div className="h-3 bg-muted rounded"></div>
                        <div className="h-3 bg-muted rounded w-5/6"></div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : users.length > 0 ? (
              <>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {users.map((user) => (
                    <UserCard key={user.id} user={user} />
                  ))}
                </div>

                {loadingMore && (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mt-6">
                    {[...Array(3)].map((_, i) => (
                      <div key={i} className="bg-card rounded-xl shadow-sm border border-border p-6">
                        <div className="animate-pulse">
                          <div className="flex items-center gap-4 mb-4">
                            <div className="w-16 h-16 bg-muted rounded-full"></div>
                            <div className="flex-1">
                              <div className="h-4 bg-muted rounded w-3/4 mb-2"></div>
                              <div className="h-3 bg-muted rounded w-1/2"></div>
                            </div>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                <div className="flex justify-center py-8">
                  {hasMore ? (
                    <button
                      onClick={handleLoadMore}
                      disabled={loadingMore}
                      className="flex items-center gap-2 px-6 py-3 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 text-white rounded-lg font-medium transition-colors disabled:cursor-not-allowed"
                    >
                      {loadingMore ? (
                        <><Loader2 className="w-4 h-4 animate-spin" />Đang tải...</>
                      ) : (
                        <>Tải thêm người dùng <span className="text-sm opacity-80">({users.length})</span></>
                      )}
                    </button>
                  ) : (
                    <div className="bg-card rounded-full px-6 py-3 shadow-sm border border-border">
                      <p className="text-muted-foreground text-sm font-medium">🎉 Đã hiển thị hết người dùng!</p>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <div className="flex flex-col items-center justify-center py-16">
                <div className="bg-card rounded-xl shadow-sm border border-border p-8 text-center max-w-md">
                  <div className="w-16 h-16 bg-muted rounded-full flex items-center justify-center mx-auto mb-4">
                    <User className="w-8 h-8 text-muted-foreground" />
                  </div>
                  <h3 className="text-lg font-semibold text-card-foreground mb-2">Không có người dùng nào</h3>
                  <p className="text-muted-foreground">Hiện tại chưa có người dùng nào để hiển thị.</p>
                </div>
              </div>
            )}
          </section>
        </div>
      </main>
    </>
  );
};

export default UsersPage;