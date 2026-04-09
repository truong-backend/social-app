import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMyProfile } from '@features/user/hooks/useProfile'
import {
  useChangeName,
  useChangeUsername,
  useChangeBio,
  useUpdateProfilePicture,
} from '@features/user/hooks/useUpdateProfile'
import { Input } from '@components/ui/Input'
import { Textarea } from '@components/ui/Textarea'
import { Button } from '@components/ui/Button'
import { Spinner } from '@components/feedback/Spinner'
import { useRef, useState } from 'react'

const editProfileSchema = z.object({
  familyName: z.string().min(1, 'Vui lòng nhập họ'),
  givenName:  z.string().min(1, 'Vui lòng nhập tên'),
  username:   z.string().min(3).max(30),
  bio:        z.string().max(500).optional(),
})

type EditProfileFormValues = z.infer<typeof editProfileSchema>

export const EditProfilePage = () => {
  const { data: profile, isLoading } = useMyProfile()
  const fileInputRef  = useRef<HTMLInputElement>(null)
  const [previewUrl, setPreviewUrl] = useState<string | null>(null)

  const changeName     = useChangeName()
  const changeUsername = useChangeUsername()
  const changeBio      = useChangeBio()
  const updatePicture  = useUpdateProfilePicture()

  const { register, handleSubmit, formState: { errors } } = useForm<EditProfileFormValues>({
    resolver: zodResolver(editProfileSchema),
    values: {
      familyName: profile?.familyName ?? '',
      givenName:  profile?.givenName ?? '',
      username:   profile?.username ?? '',
      bio:        profile?.bio ?? '',
    },
  })

  const onSubmit = (data: EditProfileFormValues) => {
    if (data.familyName !== profile?.familyName || data.givenName !== profile?.givenName) {
      changeName.mutate({ familyName: data.familyName, givenName: data.givenName })
    }
    if (data.username !== profile?.username) {
      changeUsername.mutate({ username: data.username })
    }
    if (data.bio !== profile?.bio) {
      changeBio.mutate({ bio: data.bio ?? '' })
    }
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    // Preview ngay lập tức
    const url = URL.createObjectURL(file)
    setPreviewUrl(url)
    updatePicture.mutate(file, {
      onError: () => setPreviewUrl(null),
    })
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[60vh]">
        <Spinner size="lg" />
      </div>
    )
  }

  const isSaving = changeName.isPending || changeUsername.isPending || changeBio.isPending
  const currentAvatarUrl = previewUrl ?? profile?.profilePictureUrl ?? null

  return (
    <div className="max-w-2xl mx-auto px-4 py-8 pb-24 md:pb-8 flex flex-col gap-8">
      {/* Header */}
      <div>
        <h1
          className="text-3xl font-extrabold tracking-tight text-on-surface"
          style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
        >
          Chỉnh sửa hồ sơ
        </h1>
        <p className="mt-1 text-sm text-on-surface-variant">Cập nhật thông tin cá nhân của bạn</p>
      </div>

      {/* Avatar section */}
      <div className="flex items-center gap-6 p-6 bg-surface-container-lowest rounded-2xl shadow-sm border border-outline-variant/10">
        <div className="relative flex-shrink-0">
          {currentAvatarUrl ? (
            <img
              src={currentAvatarUrl}
              alt={profile?.username}
              className="w-24 h-24 rounded-full object-cover border-4 border-surface shadow-lg"
            />
          ) : (
            <div className="w-24 h-24 rounded-full bg-gradient-to-br from-primary to-primary-container text-on-primary flex items-center justify-center text-3xl font-bold border-4 border-surface shadow-lg">
              {profile?.username?.charAt(0).toUpperCase()}
            </div>
          )}
          {updatePicture.isPending && (
            <div className="absolute inset-0 flex items-center justify-center bg-black/40 rounded-full">
              <Spinner size="sm" />
            </div>
          )}
          {/* Overlay click to change */}
          <button
            type="button"
            onClick={() => fileInputRef.current?.click()}
            className="absolute inset-0 flex items-center justify-center bg-black/0 hover:bg-black/30 rounded-full transition-colors group"
          >
            <span className="material-symbols-outlined text-white opacity-0 group-hover:opacity-100 transition-opacity">
              photo_camera
            </span>
          </button>
        </div>

        <div className="flex flex-col gap-2">
          <p className="font-bold text-on-surface">Ảnh đại diện</p>
          <p className="text-xs text-on-surface-variant">JPG, PNG — tối đa 5MB</p>
          <Button
            variant="secondary"
            size="sm"
            isLoading={updatePicture.isPending}
            onClick={() => fileInputRef.current?.click()}
          >
            <span className="material-symbols-outlined text-sm mr-1">photo_camera</span>
            Thay ảnh đại diện
          </Button>
          {updatePicture.isSuccess && !updatePicture.isPending && (
            <p className="text-xs text-green-600 font-medium flex items-center gap-1">
              <span className="material-symbols-outlined text-sm">check_circle</span>
              Đã cập nhật ảnh đại diện
            </p>
          )}
        </div>

        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          className="hidden"
          onChange={handleFileChange}
        />
      </div>

      {/* Form */}
      <form className="flex flex-col gap-5" onSubmit={handleSubmit(onSubmit)}>
        <div className="grid grid-cols-2 gap-3">
          <Input
            label="Họ"
            errorMessage={errors.familyName?.message}
            {...register('familyName')}
          />
          <Input
            label="Tên"
            errorMessage={errors.givenName?.message}
            {...register('givenName')}
          />
        </div>

        <Input
          label="Username"
          errorMessage={errors.username?.message}
          helperText="Chỉ được thay đổi mỗi 30 ngày"
          {...register('username')}
        />

        <Textarea
          label="Bio"
          rows={3}
          placeholder="Giới thiệu về bản thân..."
          errorMessage={errors.bio?.message}
          {...register('bio')}
        />

        <Button type="submit" fullWidth isLoading={isSaving}>
          Lưu thay đổi
        </Button>
      </form>
    </div>
  )
}
