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
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: 'Mật khẩu không khớp',
    path:    ['confirmPassword'],
  })

export const ResetPasswordPage = () => {
  const [step, setStep] = useState<Step>('request-code')
  const navigate = useNavigate()

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

  const emailForm = useForm<z.infer<typeof emailSchema>>({
    resolver: zodResolver(emailSchema),
  })
  const codeForm = useForm<z.infer<typeof codeSchema>>({
    resolver: zodResolver(codeSchema),
  })
  const passwordForm = useForm<z.infer<typeof newPasswordSchema>>({
    resolver: zodResolver(newPasswordSchema),
  })

  return (
    <div className="reset-password-page">
      <h2 className="reset-password-page__title">Đặt lại mật khẩu</h2>

      {step === 'request-code' && (
        <form onSubmit={emailForm.handleSubmit((data) => requestCode.mutate(data))}>
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
        <form onSubmit={codeForm.handleSubmit((data) => confirmCode.mutate(data))}>
          <p className="reset-password-page__description">
            Nhập mã xác thực đã được gửi đến email của bạn.
          </p>
          <Input
            label="Mã xác thực"
            maxLength={6}
            errorMessage={codeForm.formState.errors.code?.message}
            {...codeForm.register('code')}
          />
          <Button type="submit" fullWidth isLoading={confirmCode.isPending}>
            Xác nhận
          </Button>
        </form>
      )}

      {step === 'new-password' && (
        <form onSubmit={passwordForm.handleSubmit((data) => updatePassword.mutate(data))}>
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