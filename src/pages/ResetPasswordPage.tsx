import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import {
  prepareResetPasswordApi,
  confirmResetCodeApi,
  updatePasswordApi,
} from '@features/auth/api/auth.api'
import { Input } from '@components/ui/Input'
import { Button } from '@components/ui/Button'
import { extractErrorMessage } from '@utils/api-response'
import { PASSWORD_MIN_LENGTH } from '@features/auth/constants/auth.constants'

type Step = 'request-code' | 'confirm-code' | 'new-password'

const emailSchema = z.object({
  email: z.string().email('Email không hợp lệ'),
})
const codeSchema = z.object({
  code: z.string().length(6, 'Mã gồm 6 ký tự'),
})
const newPasswordSchema = z
  .object({
    newPassword:     z.string().min(PASSWORD_MIN_LENGTH),
    confirmPassword: z.string().min(PASSWORD_MIN_LENGTH),
  })
  .refine((d) => d.newPassword === d.confirmPassword, {
    message: 'Mật khẩu không khớp',
    path:    ['confirmPassword'],
  })

const STEP_META: Record<Step, { icon: string; title: string; subtitle: string }> = {
  'request-code': { icon: 'lock_reset',      title: 'Đặt lại mật khẩu',    subtitle: 'Nhập email để nhận mã xác thực' },
  'confirm-code': { icon: 'mark_email_read', title: 'Nhập mã xác thực',    subtitle: 'Kiểm tra hộp thư đến của bạn' },
  'new-password': { icon: 'key',             title: 'Mật khẩu mới',        subtitle: 'Tạo mật khẩu mạnh cho tài khoản' },
}

export const ResetPasswordPage = () => {
  const [step, setStep] = useState<Step>('request-code')
  const navigate = useNavigate()
  const meta = STEP_META[step]

  const requestCode = useMutation({
    mutationFn: prepareResetPasswordApi,
    onSuccess:  () => { toast.success('Đã gửi mã xác thực'); setStep('confirm-code') },
    onError:    (err) => toast.error(extractErrorMessage(err)),
  })
  const confirmCode = useMutation({
    mutationFn: confirmResetCodeApi,
    onSuccess:  () => setStep('new-password'),
    onError:    (err) => toast.error(extractErrorMessage(err)),
  })
  const updatePassword = useMutation({
    mutationFn: updatePasswordApi,
    onSuccess:  () => { toast.success('Đổi mật khẩu thành công'); navigate('/login') },
    onError:    (err) => toast.error(extractErrorMessage(err)),
  })

  const emailForm    = useForm<z.infer<typeof emailSchema>>({ resolver: zodResolver(emailSchema) })
  const codeForm     = useForm<z.infer<typeof codeSchema>>({ resolver: zodResolver(codeSchema) })
  const passwordForm = useForm<z.infer<typeof newPasswordSchema>>({ resolver: zodResolver(newPasswordSchema) })

  /* Step indicator */
  const steps: Step[] = ['request-code', 'confirm-code', 'new-password']

  return (
    <div className="flex flex-col gap-6">
      {/* Header */}
      <div className="flex flex-col gap-2">
        <div className="w-12 h-12 rounded-2xl bg-primary/10 flex items-center justify-center">
          <span className="material-symbols-outlined text-primary text-2xl">{meta.icon}</span>
        </div>
        <h2
          className="text-3xl font-extrabold tracking-tight text-on-surface"
          style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
        >
          {meta.title}
        </h2>
        <p className="text-sm text-on-surface-variant">{meta.subtitle}</p>
      </div>

      {/* Step dots */}
      <div className="flex gap-2">
        {steps.map((s) => (
          <div
            key={s}
            className={`h-1.5 flex-1 rounded-full transition-all duration-300 ${
              s === step ? 'bg-primary' : steps.indexOf(s) < steps.indexOf(step) ? 'bg-primary/40' : 'bg-surface-container-high'
            }`}
          />
        ))}
      </div>

      {/* Forms */}
      {step === 'request-code' && (
        <form className="flex flex-col gap-4" onSubmit={emailForm.handleSubmit((d) => requestCode.mutate(d))}>
          <Input
            label="Email tài khoản"
            type="email"
            errorMessage={emailForm.formState.errors.email?.message}
            {...emailForm.register('email')}
          />
          <Button type="submit" fullWidth isLoading={requestCode.isPending}>
            Gửi mã xác thực
          </Button>
        </form>
      )}

      {step === 'confirm-code' && (
        <form className="flex flex-col gap-4" onSubmit={codeForm.handleSubmit((d) => confirmCode.mutate(d))}>
          <Input
            label="Mã xác thực"
            maxLength={6}
            placeholder="6 chữ số"
            errorMessage={codeForm.formState.errors.code?.message}
            {...codeForm.register('code')}
          />
          <Button type="submit" fullWidth isLoading={confirmCode.isPending}>
            Xác nhận
          </Button>
        </form>
      )}

      {step === 'new-password' && (
        <form className="flex flex-col gap-4" onSubmit={passwordForm.handleSubmit((d) => updatePassword.mutate(d))}>
          <Input
            label="Mật khẩu mới"
            type="password"
            errorMessage={passwordForm.formState.errors.newPassword?.message}
            {...passwordForm.register('newPassword')}
          />
          <Input
            label="Xác nhận mật khẩu"
            type="password"
            errorMessage={passwordForm.formState.errors.confirmPassword?.message}
            {...passwordForm.register('confirmPassword')}
          />
          <Button type="submit" fullWidth isLoading={updatePassword.isPending}>
            Cập nhật mật khẩu
          </Button>
        </form>
      )}
    </div>
  )
}