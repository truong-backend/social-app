import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { confirmEmailApi } from '@features/auth/api/auth.api'
import { useSessionStore } from '@stores/session.store'
import { Input } from '@components/ui/Input'
import { Button } from '@components/ui/Button'
import { extractErrorMessage } from '@utils/api-response'

const confirmSchema = z.object({
  code: z.string().length(6, 'Mã xác thực gồm 6 ký tự'),
})

type ConfirmFormValues = z.infer<typeof confirmSchema>

export const ConfirmEmailPage = () => {
  const navigate   = useNavigate()
  const setSession = useSessionStore((state) => state.setSession)

  const confirm = useMutation({
    mutationFn: confirmEmailApi,
    onSuccess: (data) => {
      setSession({
        accountId:    data.accountId,
        userId:       data.userId,
        role:         data.role,
        accessToken:  data.accessToken,
        refreshToken: data.refreshToken,
      })
      toast.success('Email đã được xác thực!')
      navigate('/feed')
    },
    onError: (error) => toast.error(extractErrorMessage(error)),
  })

  const { register, handleSubmit, formState: { errors } } = useForm<ConfirmFormValues>({
    resolver: zodResolver(confirmSchema),
  })

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2
          className="text-3xl font-extrabold tracking-tight text-on-surface"
          style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
        >
          Xác thực email
        </h2>
        <p className="mt-2 text-sm text-on-surface-variant leading-relaxed">
          Chúng tôi đã gửi mã xác thực đến email của bạn. Vui lòng nhập mã bên dưới.
        </p>
      </div>

      {/* OTP hint visual */}
      <div className="flex items-center gap-2 px-4 py-3 bg-surface-container rounded-xl border border-outline-variant/30">
        <span className="material-symbols-outlined text-primary text-lg">mark_email_read</span>
        <span className="text-sm text-on-surface-variant">Kiểm tra hộp thư đến của bạn</span>
      </div>

      <form
        className="flex flex-col gap-4"
        onSubmit={handleSubmit((data) => confirm.mutate(data))}
      >
        <Input
          label="Mã xác thực"
          maxLength={6}
          placeholder="Nhập mã 6 chữ số"
          errorMessage={errors.code?.message}
          {...register('code')}
        />

        <Button type="submit" fullWidth isLoading={confirm.isPending}>
          Xác nhận
        </Button>
      </form>
    </div>
  )
}