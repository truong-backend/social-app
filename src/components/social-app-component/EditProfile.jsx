"use client"

import { useRef, useState } from "react"
import Avatar from "../ui-components/Avatar"
import api, { refreshTokenManually, setUserName } from "@/utils/axios"
import toast from "react-hot-toast"

function Row({ label, children, error }) {
  return (
    <div className="flex flex-col sm:flex-row sm:items-start gap-0.5 sm:gap-0 py-3.5 border-b border-[var(--border)] last:border-0">
      <span className="sm:w-44 sm:min-w-[11rem] text-sm font-semibold text-[var(--foreground)] pt-1 shrink-0">
        {label}
      </span>
      <div className="flex-1 min-w-0">
        {children}
        {error && <p className="text-xs text-red-500 mt-1">{error}</p>}
      </div>
    </div>
  )
}

const inputClass =
  "w-full bg-transparent text-sm text-[var(--foreground)] outline-none placeholder:text-[var(--muted-foreground)]"

export default function EditProfileModal({ profileData, onSave }) {
  const [formData, setFormData] = useState({
    firstname: profileData.givenName || "",
    lastname: profileData.familyName || "",
    username: profileData.username || "",
    birthday: profileData.birthdate || "",
    bio: profileData.bio || "",
    avatar: profileData.profilePictureUrl || "/avatar-placeholder.png",
  })

  const token = localStorage.getItem("accessToken")
  const [avatarFile, setAvatarFile] = useState(null)
  const [avatarPreview, setAvatarPreview] = useState(null)
  const [loading, setLoading] = useState(false)
  const [errors, setErrors] = useState({})
  const fileInputRef = useRef(null)

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
    if (errors[name]) setErrors((prev) => ({ ...prev, [name]: null }))
  }

  const handleAvatarChange = (e) => {
    const file = e.target.files[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = (e2) => {
      setAvatarPreview(e2.target.result)
      setAvatarFile(file)
    }
    reader.readAsDataURL(file)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setErrors({})

    const updates = [
      {
        label: "Name",
        check: formData.firstname !== profileData.givenName || formData.lastname !== profileData.familyName,
        request: () =>
          api.patch(
            `/v1/users/update-name?givenName=${encodeURIComponent(formData.firstname)}&familyName=${encodeURIComponent(formData.lastname)}`
          ),
        errorKey: "name",
      },
      {
        label: "Bio",
        check: formData.bio !== profileData.bio,
        request: () =>
          fetch(`${process.env.NEXT_PUBLIC_API_URL}/v1/users/update-bio?bio=${encodeURIComponent(formData.bio)}`, {
            method: "PATCH",
            headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
          }).then((r) => { if (!r.ok) return r.json().then((err) => Promise.reject(err)); return r.json() }),
        errorKey: "bio",
      },
      {
        label: "Avatar",
        check: !!avatarFile,
        request: () => {
          const form = new FormData()
          form.append("file", avatarFile)
          return api.patch("/v1/users/update-profile-picture", form, {
            headers: { "Content-Type": "multipart/form-data" },
          })
        },
        errorKey: "avatar",
      },
    ]

    let successCount = 0
    const newErrors = {}

    for (const item of updates) {
      if (item.check) {
        try {
          await item.request()
          successCount++
        } catch (err) {
          newErrors[item.errorKey] = err?.response?.data?.message || `Lỗi cập nhật ${item.label.toLowerCase()}`
        }
      }
    }

    setErrors(newErrors)
    setLoading(false)

    if (successCount > 0) {
      toast.success("Đã lưu thay đổi")
      const updatedData = {
        ...formData,
        givenName: formData.firstname,
        familyName: formData.lastname,
        profilePictureUrl: avatarPreview || formData.avatar,
      }
      onSave(updatedData, { usernameChanged: false, oldUsername: profileData.username, newUsername: formData.username })
    } else if (Object.keys(newErrors).length === 0) {
      toast("Không có thay đổi nào")
    }
  }

  return (
    <div className="h-full flex flex-col bg-[var(--card)] text-[var(--card-foreground)]">
      {/* Header */}
      <div className="px-6 py-4 border-b border-[var(--border)] shrink-0">
        <h2 className="text-base font-bold">Chỉnh sửa trang cá nhân</h2>
      </div>

      <form onSubmit={handleSubmit} className="flex-1 overflow-y-auto">
        <div className="px-6 py-2">

          {/* Avatar row */}
          <div className="flex items-center gap-4 py-4 border-b border-[var(--border)]">
            <div className="w-12 h-12 rounded-full overflow-hidden shrink-0">
              {avatarPreview ? (
                <img src={avatarPreview} alt="avatar" className="w-full h-full object-cover" />
              ) : (
                <Avatar src={formData.avatar} className="w-full h-full object-cover" />
              )}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold truncate">{formData.username}</p>
              <p className="text-xs text-[var(--muted-foreground)] truncate">
                {formData.lastname} {formData.firstname}
              </p>
            </div>
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              className="text-sm font-semibold text-blue-500 hover:text-blue-400 transition-colors shrink-0"
            >
              Thay đổi ảnh
            </button>
            <input ref={fileInputRef} type="file" accept="image/*" onChange={handleAvatarChange} className="hidden" />
          </div>
          {errors.avatar && <p className="text-xs text-red-500 mt-1">{errors.avatar}</p>}

          {/* Fields */}
          <Row label="Tên" error={errors.name}>
            <input
              name="firstname"
              value={formData.firstname}
              onChange={handleInputChange}
              placeholder="Tên"
              className={inputClass}
            />
          </Row>

          <Row label="Họ">
            <input
              name="lastname"
              value={formData.lastname}
              onChange={handleInputChange}
              placeholder="Họ"
              className={inputClass}
            />
          </Row>

          <Row label="Tên người dùng">
            <p className="text-sm text-[var(--foreground)] opacity-50 select-none">{formData.username}</p>
          </Row>

          <Row label="Ngày sinh">
            <input
              name="birthday"
              type="date"
              value={formData.birthday}
              onChange={handleInputChange}
              className={inputClass}
            />
          </Row>

          <Row label="Tiểu sử" error={errors.bio}>
            <textarea
              name="bio"
              value={formData.bio}
              onChange={handleInputChange}
              rows={3}
              maxLength={256}
              placeholder="Giới thiệu về bạn..."
              className={`${inputClass} resize-none`}
            />
            <p className="text-xs text-[var(--muted-foreground)] text-right mt-0.5">
              {formData.bio.length} / 256
            </p>
          </Row>

        </div>

        {/* Submit */}
        <div className="px-6 pb-6 pt-2">
          <button
            type="submit"
            disabled={loading}
            className={`w-full py-2.5 rounded-xl text-sm font-semibold bg-blue-500 text-white hover:bg-blue-600 active:scale-[0.98] transition-all duration-150 ${
              loading ? "opacity-60 cursor-not-allowed" : ""
            }`}
          >
            {loading ? "Đang lưu..." : "Gửi"}
          </button>
        </div>
      </form>
    </div>
  )
}