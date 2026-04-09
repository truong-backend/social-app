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
    <div className="flex flex-col gap-8">
      {/* Header */}
      <div className="flex flex-col gap-3">
        <div className="w-12 h-12 rounded-2xl bg-primary/10 flex items-center justify-center">
          <span className="material-symbols-outlined text-primary text-2xl">mark_email_read</span>
        </div>
        <div>
          <h2
            className="text-3xl font-extrabold tracking-tight text-on-surface"
            style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
          >
            Verify Email
          </h2>
          <p className="mt-2 text-sm text-on-surface-variant leading-relaxed">
            We sent a verification code to your email. Please enter the 6-digit code below.
          </p>
        </div>
      </div>

      {/* OTP hint */}
      <div className="flex items-center gap-3 px-4 py-3 bg-surface-container-low rounded-xl border border-outline-variant/20">
        <span className="material-symbols-outlined text-primary text-lg">inbox</span>
        <span className="text-sm text-on-surface-variant">Check your inbox for the code</span>
      </div>

      <form
        className="flex flex-col gap-5"
        onSubmit={handleSubmit((data) => confirm.mutate(data))}
      >
        <Input
          label="Verification Code"
          maxLength={6}
          placeholder="Enter 6-digit code"
          errorMessage={errors.code?.message}
          {...register('code')}
        />

        <Button type="submit" fullWidth isLoading={confirm.isPending}>
          Verify Email
        </Button>
      </form>
    </div>
  )
}
