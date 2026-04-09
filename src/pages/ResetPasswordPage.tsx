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
  'request-code': { icon: 'lock_reset',      title: 'Reset Password',         subtitle: 'Enter your email to receive a verification code' },
  'confirm-code': { icon: 'mark_email_read', title: 'Enter Code',             subtitle: 'Check your inbox for the code' },
  'new-password': { icon: 'key',             title: 'New Password',           subtitle: 'Create a strong password for your account' },
}

export const ResetPasswordPage = () => {
  const [step, setStep] = useState<Step>('request-code')
  const navigate = useNavigate()
  const meta = STEP_META[step]

  const requestCode = useMutation({
    mutationFn: prepareResetPasswordApi,
    onSuccess:  () => { toast.success('Verification code sent'); setStep('confirm-code') },
    onError:    (err) => toast.error(extractErrorMessage(err)),
  })
  const confirmCode = useMutation({
    mutationFn: confirmResetCodeApi,
    onSuccess:  () => setStep('new-password'),
    onError:    (err) => toast.error(extractErrorMessage(err)),
  })
  const updatePassword = useMutation({
    mutationFn: updatePasswordApi,
    onSuccess:  () => { toast.success('Password updated successfully'); navigate('/login') },
    onError:    (err) => toast.error(extractErrorMessage(err)),
  })

  const emailForm    = useForm<z.infer<typeof emailSchema>>({ resolver: zodResolver(emailSchema) })
  const codeForm     = useForm<z.infer<typeof codeSchema>>({ resolver: zodResolver(codeSchema) })
  const passwordForm = useForm<z.infer<typeof newPasswordSchema>>({ resolver: zodResolver(newPasswordSchema) })

  const steps: Step[] = ['request-code', 'confirm-code', 'new-password']

  return (
    <div className="flex flex-col gap-8">
      {/* Header */}
      <div className="flex flex-col gap-3">
        <div className="w-12 h-12 rounded-2xl bg-primary/10 flex items-center justify-center">
          <span className="material-symbols-outlined text-primary text-2xl">{meta.icon}</span>
        </div>
        <div>
          <h2
            className="text-3xl font-extrabold tracking-tight text-on-surface"
            style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
          >
            {meta.title}
          </h2>
          <p className="mt-1 text-sm text-on-surface-variant">{meta.subtitle}</p>
        </div>
      </div>

      {/* Step progress bar */}
      <div className="flex gap-2">
        {steps.map((s) => (
          <div
            key={s}
            className={`h-1.5 flex-1 rounded-full transition-all duration-300 ${
              s === step
                ? 'bg-primary'
                : steps.indexOf(s) < steps.indexOf(step)
                ? 'bg-primary/40'
                : 'bg-surface-container-high'
            }`}
          />
        ))}
      </div>

      {/* Step: Request Code */}
      {step === 'request-code' && (
        <form className="flex flex-col gap-5" onSubmit={emailForm.handleSubmit((d) => requestCode.mutate(d))}>
          <Input
            label="Email Address"
            type="email"
            placeholder="Enter your email"
            errorMessage={emailForm.formState.errors.email?.message}
            {...emailForm.register('email')}
          />
          <Button type="submit" fullWidth isLoading={requestCode.isPending}>
            Send Verification Code
          </Button>
        </form>
      )}

      {/* Step: Confirm Code */}
      {step === 'confirm-code' && (
        <form className="flex flex-col gap-5" onSubmit={codeForm.handleSubmit((d) => confirmCode.mutate(d))}>
          <div className="flex items-center gap-3 px-4 py-3 bg-surface-container-low rounded-xl border border-outline-variant/20">
            <span className="material-symbols-outlined text-primary text-lg">inbox</span>
            <span className="text-sm text-on-surface-variant">Check your inbox for the 6-digit code</span>
          </div>
          <Input
            label="Verification Code"
            maxLength={6}
            placeholder="6-digit code"
            errorMessage={codeForm.formState.errors.code?.message}
            {...codeForm.register('code')}
          />
          <Button type="submit" fullWidth isLoading={confirmCode.isPending}>
            Verify Code
          </Button>
        </form>
      )}

      {/* Step: New Password */}
      {step === 'new-password' && (
        <form className="flex flex-col gap-5" onSubmit={passwordForm.handleSubmit((d) => updatePassword.mutate(d))}>
          <Input
            label="New Password"
            type="password"
            placeholder="••••••••"
            errorMessage={passwordForm.formState.errors.newPassword?.message}
            {...passwordForm.register('newPassword')}
          />
          <Input
            label="Confirm Password"
            type="password"
            placeholder="••••••••"
            errorMessage={passwordForm.formState.errors.confirmPassword?.message}
            {...passwordForm.register('confirmPassword')}
          />
          <Button type="submit" fullWidth isLoading={updatePassword.isPending}>
            Update Password
          </Button>
        </form>
      )}
    </div>
  )
}
