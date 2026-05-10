"use client";

import { useState, useEffect, useRef } from "react";
import Image from "next/image";
import Avatar from "@/components/ui-components/Avatar";
import api from "@/utils/axios";
import toast from "react-hot-toast";

// A single editable row, Instagram-style
function SettingRow({ label, name, value, onChange, type = "text", maxLength, readOnly = false, multiline = false }) {
  const inputClass = `
    w-full bg-transparent text-sm text-[var(--foreground)] outline-none
    placeholder:text-[var(--muted-foreground)]
    ${readOnly ? "opacity-50 cursor-not-allowed select-none" : ""}
  `;

  return (
    <div className="flex flex-col sm:flex-row sm:items-start gap-1 sm:gap-0 py-4 border-b border-[var(--border)] last:border-0">
      <label className="sm:w-48 sm:min-w-[12rem] text-sm font-semibold text-[var(--foreground)] pt-0.5 shrink-0">
        {label}
      </label>
      <div className="flex-1">
        {multiline ? (
          <>
            <textarea
              name={name}
              value={value || ""}
              onChange={onChange}
              maxLength={maxLength}
              rows={3}
              readOnly={readOnly}
              className={`${inputClass} resize-none`}
              placeholder={`Thêm ${label.toLowerCase()}...`}
            />
            {maxLength && (
              <p className="text-xs text-[var(--muted-foreground)] mt-1 text-right">
                {(value || "").length} / {maxLength}
              </p>
            )}
          </>
        ) : (
          <input
            type={type}
            name={name}
            value={value || ""}
            onChange={onChange}
            maxLength={maxLength}
            readOnly={readOnly}
            className={inputClass}
            placeholder={`Thêm ${label.toLowerCase()}...`}
          />
        )}
      </div>
    </div>
  );
}

export default function PersonalInfoPage() {
  const [user, setUser] = useState(null);
  const [originalUser, setOriginalUser] = useState(null);
  const [avatarFile, setAvatarFile] = useState(null);
  const [avatarPreview, setAvatarPreview] = useState(null);
  const [loadingUser, setLoadingUser] = useState(true);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [token, setToken] = useState(null);
  const fileInputRef = useRef(null);

  useEffect(() => {
    const t = localStorage.getItem("accessToken");
    setToken(t);
    const username = localStorage.getItem("userName");

    const fetchProfile = async () => {
      try {
        const res = await api.get(`/v1/users/${username}`, {
          headers: { Authorization: `Bearer ${t}` },
        });
        if (res.data.code === 200) {
          setUser(res.data.body);
          setOriginalUser(res.data.body);
        }
      } catch {
        setErrors({ fetch: "Không tải được thông tin người dùng" });
      } finally {
        setLoadingUser(false);
      }
    };
    fetchProfile();
  }, []);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setUser((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors((prev) => ({ ...prev, [name]: null }));
  };

  const handleAvatarChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (e2) => {
      setAvatarPreview(e2.target.result);
      setAvatarFile(file);
    };
    reader.readAsDataURL(file);
  };

  const handleSave = async () => {
    if (!originalUser) return;
    setLoading(true);
    setErrors({});

    const updates = [
      {
        check: user.givenName !== originalUser.givenName || user.familyName !== originalUser.familyName,
        request: () =>
          api.patch(
            `/v1/users/update-name?givenName=${encodeURIComponent(user.givenName)}&familyName=${encodeURIComponent(user.familyName)}`,
            {},
            { headers: { Authorization: `Bearer ${token}` } }
          ),
        errorKey: "name",
        label: "Tên",
      },
      {
        check: user.username !== originalUser.username,
        request: () =>
          api.patch(`/v1/users/update-username?username=${encodeURIComponent(user.username)}`, {}, {
            headers: { Authorization: `Bearer ${token}` },
          }),
        errorKey: "username",
        label: "Username",
      },
      {
        check: user.birthdate !== originalUser.birthdate,
        request: () =>
          api.patch(`/v1/users/update-birthday?birthdate=${encodeURIComponent(user.birthdate)}`, {}, {
            headers: { Authorization: `Bearer ${token}` },
          }),
        errorKey: "birthday",
        label: "Ngày sinh",
      },
      {
        check: user.bio !== originalUser.bio,
        request: () =>
          fetch(`${process.env.NEXT_PUBLIC_API_URL}/v1/users/update-bio?bio=${encodeURIComponent(user.bio)}`, {
            method: "PATCH",
            headers: { Authorization: `Bearer ${token}` },
          }).then((r) => {
            if (!r.ok) return r.json().then((err) => Promise.reject(err));
            return r.json();
          }),
        errorKey: "bio",
        label: "Tiểu sử",
      },
      {
        check: !!avatarFile,
        request: () => {
          const form = new FormData();
          form.append("file", avatarFile);
          return api.patch("/v1/users/update-profile-picture", form, {
            headers: { "Content-Type": "multipart/form-data", Authorization: `Bearer ${token}` },
          });
        },
        errorKey: "avatar",
        label: "Ảnh đại diện",
      },
    ];

    let hasError = false;
    let successCount = 0;

    for (const item of updates) {
      if (item.check) {
        try {
          await item.request();
          successCount++;
        } catch (err) {
          hasError = true;
          setErrors((prev) => ({
            ...prev,
            [item.errorKey]: err?.response?.data?.message || `Lỗi cập nhật ${item.label.toLowerCase()}`,
          }));
        }
      }
    }

    setLoading(false);
    if (successCount > 0) {
      setOriginalUser({ ...user });
      toast.success("Đã lưu thay đổi");
    }
    if (!hasError && successCount === 0) {
      toast("Không có thay đổi nào cần lưu");
    }
  };

  if (loadingUser) {
    return (
      <div className="max-w-xl mx-auto px-4 py-12 text-center text-sm text-[var(--muted-foreground)]">
        Đang tải...
      </div>
    );
  }

  if (!user) {
    return (
      <div className="max-w-xl mx-auto px-4 py-12 text-center text-sm text-red-500">
        {errors.fetch || "Không tải được thông tin"}
      </div>
    );
  }

  return (
    <div className="w-full max-w-2xl px-6 sm:px-10 py-8">
      {/* Header */}
      <div className="mb-8 hidden md:block">
        <h1 className="text-xl font-bold">Chỉnh sửa trang cá nhân</h1>
      </div>

      {/* Avatar row */}
      <div className="flex items-center gap-4 mb-6 py-4 border-b border-[var(--border)]">
        <div className="w-14 h-14 rounded-full overflow-hidden shrink-0">
          {avatarPreview ? (
            <img src={avatarPreview} alt="avatar" className="w-full h-full object-cover" />
          ) : (
            <Avatar src={user.profilePictureUrl} className="w-full h-full object-cover" />
          )}
        </div>
        <div className="flex-1 min-w-0">
          <p className="text-sm font-semibold truncate">{user.username}</p>
          <p className="text-xs text-[var(--muted-foreground)] truncate">
            {user.familyName} {user.givenName}
          </p>
        </div>
        <button
          onClick={() => fileInputRef.current?.click()}
          className="text-sm font-semibold text-blue-500 hover:text-blue-400 transition-colors shrink-0"
        >
          Thay đổi ảnh
        </button>
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          onChange={handleAvatarChange}
          className="hidden"
        />
      </div>

      {/* Error messages */}
      {Object.keys(errors).filter(k => k !== "fetch" && errors[k]).length > 0 && (
        <div className="mb-6 p-3 rounded-xl bg-red-50 border border-red-100 text-red-600 text-xs space-y-1">
          {Object.keys(errors).map((k) =>
            errors[k] ? <p key={k}>• {errors[k]}</p> : null
          )}
        </div>
      )}

      {/* Form fields */}
      <div className="mb-8">
        <SettingRow
          label="Tên"
          name="givenName"
          value={user.givenName}
          onChange={handleInputChange}
        />
        <SettingRow
          label="Họ"
          name="familyName"
          value={user.familyName}
          onChange={handleInputChange}
        />
        <SettingRow
          label="Tên người dùng"
          name="username"
          value={user.username}
          onChange={handleInputChange}
          readOnly
        />
        <SettingRow
          label="Ngày sinh"
          name="birthdate"
          value={user.birthdate}
          onChange={handleInputChange}
          type="date"
        />
        <SettingRow
          label="Tiểu sử"
          name="bio"
          value={user.bio}
          onChange={handleInputChange}
          multiline
          maxLength={256}
        />
      </div>

      {/* Save button */}
      <button
        onClick={handleSave}
        disabled={loading}
        className={`
          w-full py-2.5 rounded-xl text-sm font-semibold
          bg-blue-500 text-white
          hover:bg-blue-600 active:scale-[0.98]
          transition-all duration-150
          ${loading ? "opacity-60 cursor-not-allowed" : ""}
        `}
      >
        {loading ? "Đang lưu..." : "Gửi"}
      </button>
    </div>
  );
}