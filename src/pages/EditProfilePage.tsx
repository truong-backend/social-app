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
import { useRef } from 'react'

const editProfileSchema = z.object({
  familyName: z.string().min(1, 'Vui lòng nhập họ'),
  givenName:  z.string().min(1, 'Vui lòng nhập tên'),
  username:   z.string().min(3).max(30),
  bio:        z.string().max(500).optional(),
})

type EditProfileFormValues = z.infer<typeof editProfileSchema>

export const EditProfilePage = () => {
  const { data: profile, isLoading } = useMyProfile()
  const fileInputRef = useRef<HTMLInputElement>(null)

  const changeName    = useChangeName()
  const changeUsername = useChangeUsername()
  const changeBio     = useChangeBio()
  const updatePicture = useUpdateProfilePicture()

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

  if (isLoading) {
    return <div className="edit-profile-page__loading"><Spinner size="lg" /></div>
  }

  const isSaving = changeName.isPending || changeUsername.isPending || changeBio.isPending

  return (
    <div className="edit-profile-page">
      <h1 className="edit-profile-page__title">Chỉnh sửa trang cá nhân</h1>

      {/* Avatar upload */}
      <div className="edit-profile-page__avatar-section">
        <img
          src={profile?.profilePictureUrl ?? '/default-avatar.png'}
          alt={profile?.username}
          className="edit-profile-page__current-avatar"
        />
        <Button
          variant="secondary"
          size="sm"
          isLoading={updatePicture.isPending}
          onClick={() => fileInputRef.current?.click()}
        >
          Đổi ảnh đại diện
        </Button>
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          className="edit-profile-page__file-input"
          onChange={(e) => {
            const file = e.target.files?.[0]
            if (file) updatePicture.mutate(file)
          }}
        />
      </div>

      <form className="edit-profile-page__form" onSubmit={handleSubmit(onSubmit)}>
        <div className="edit-profile-page__name-row">
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
          label="Tiểu sử"
          rows={3}
          placeholder="Giới thiệu bản thân..."
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