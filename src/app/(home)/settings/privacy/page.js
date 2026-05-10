"use client";

import React, { useState, useEffect } from "react";
import { ChevronRight } from "lucide-react";
import Link from "next/link";
import toast from "react-hot-toast";

function Toggle({ checked, onChange }) {
  return (
    <button
      role="switch"
      aria-checked={checked}
      onClick={() => onChange(!checked)}
      className={`relative inline-flex h-7 w-12 items-center rounded-full transition-colors duration-200 focus:outline-none shrink-0 ${
        checked ? "bg-blue-500" : "bg-[var(--muted-foreground)]/25"
      }`}
    >
      <span
        className={`inline-block h-6 w-6 transform rounded-full bg-white shadow-md transition-transform duration-200 ${
          checked ? "translate-x-5" : "translate-x-0.5"
        }`}
      />
    </button>
  );
}

// Section label
function SectionLabel({ children }) {
  return (
    <p className="text-[11px] font-semibold text-[var(--muted-foreground)] uppercase tracking-widest mb-1 mt-7 first:mt-0">
      {children}
    </p>
  );
}

// Plain row — no card, just a divider below
function Row({ label, description, action, href }) {
  const inner = (
    <div className="flex items-center justify-between py-3.5 border-b border-[var(--border)] gap-4">
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-[var(--foreground)] leading-snug">{label}</p>
        {description && (
          <p className="text-xs text-[var(--muted-foreground)] mt-0.5 leading-relaxed">{description}</p>
        )}
      </div>
      <div className="shrink-0">{action}</div>
    </div>
  );

  if (href) {
    return (
      <Link href={href} className="block hover:opacity-70 transition-opacity">
        {inner}
      </Link>
    );
  }
  return inner;
}

export default function PrivacySettings() {
  const [privacy, setPrivacy] = useState({
    isPrivateAccount: false,
    defaultPostPrivacy: "PUBLIC",
    allowFriendRequest: true,
    emailVerified: true,
    isLocked: false,
  });

  useEffect(() => {
    const stored = localStorage.getItem("defaultPrivacy");
    if (stored) setPrivacy((prev) => ({ ...prev, defaultPostPrivacy: stored }));
  }, []);

  const handleToggle = (key) => (val) => {
    setPrivacy((prev) => ({ ...prev, [key]: val }));
  };

  const handleSave = () => {
    localStorage.setItem("defaultPrivacy", privacy.defaultPostPrivacy);
    toast.success("Đã lưu cài đặt quyền riêng tư");
  };

  return (
    <div className="w-full max-w-2xl px-6 sm:px-10 py-8">
      <div className="mb-7 hidden md:block">
        <h1 className="text-xl font-bold">Quyền riêng tư tài khoản</h1>
      </div>

      <SectionLabel>Tài khoản</SectionLabel>
      <Row
        label="Tài khoản riêng tư"
        description="Khi tài khoản ở chế độ riêng tư, chỉ người bạn chấp thuận mới xem được ảnh và video."
        action={<Toggle checked={privacy.isPrivateAccount} onChange={handleToggle("isPrivateAccount")} />}
      />
      <Row
        label="Cho phép gửi lời mời kết bạn"
        action={<Toggle checked={privacy.allowFriendRequest} onChange={handleToggle("allowFriendRequest")} />}
      />

      <SectionLabel>Bài viết</SectionLabel>
      <Row
        label="Quyền riêng tư mặc định"
        description="Áp dụng cho bài viết mới"
        action={
          <select
            value={privacy.defaultPostPrivacy}
            onChange={(e) => setPrivacy((p) => ({ ...p, defaultPostPrivacy: e.target.value }))}
            className="text-xs bg-[var(--muted)] text-[var(--foreground)] px-3 py-1.5 rounded-lg border-0 outline-none cursor-pointer"
          >
            <option value="PUBLIC">Công khai</option>
            <option value="FRIEND">Chỉ bạn bè</option>
            <option value="PRIVATE">Chỉ mình tôi</option>
          </select>
        }
      />

      <SectionLabel>Trạng thái tài khoản</SectionLabel>
      <Row
        label="Email đã xác minh"
        action={
          <span className={`text-xs font-semibold ${privacy.emailVerified ? "text-green-500" : "text-red-500"}`}>
            {privacy.emailVerified ? "Đã xác minh" : "Chưa xác minh"}
          </span>
        }
      />
      <Row
        label="Tài khoản bị khóa"
        action={
          <span className={`text-xs font-semibold ${privacy.isLocked ? "text-red-500" : "text-green-500"}`}>
            {privacy.isLocked ? "Đang bị khóa" : "Bình thường"}
          </span>
        }
      />
      <Row
        label="Nội dung bạn đã ẩn"
        href="/settings/blockedlist"
        action={<ChevronRight className="w-4 h-4 text-[var(--muted-foreground)]" />}
      />

      <button
        onClick={handleSave}
        className="mt-8 w-full py-2.5 rounded-xl text-sm font-semibold bg-blue-500 text-white hover:bg-blue-600 active:scale-[0.98] transition-all duration-150"
      >
        Lưu cài đặt
      </button>
    </div>
  );
}