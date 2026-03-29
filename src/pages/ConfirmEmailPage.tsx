import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation } from '@tanstack/react-query'
// import { useNavigate, useLocation } from 'react-router-dom'
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
  const navigate  = useNavigate()
  // const location  = useLocation()
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
    <div className="confirm-email-page">
      <h2 className="confirm-email-page__title">Xác thực email</h2>
      <p className="confirm-email-page__description">
        Chúng tôi đã gửi mã xác thực đến email của bạn. Vui lòng nhập mã bên dưới.
      </p>

      <form
        className="confirm-email-page__form"
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