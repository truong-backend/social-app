import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link } from 'react-router-dom'
import { useLogin } from '@features/auth/hooks/useLogin'
import { Input } from '@components/ui/Input'
import { Button } from '@components/ui/Button'
import { PASSWORD_MIN_LENGTH } from '@features/auth/constants/auth.constants'

const loginSchema = z.object({
  email:    z.string().email('Email không hợp lệ'),
  password: z.string().min(PASSWORD_MIN_LENGTH, `Mật khẩu tối thiểu ${PASSWORD_MIN_LENGTH} ký tự`),
})

type LoginFormValues = z.infer<typeof loginSchema>

export const LoginPage = () => {
  const login = useLogin()

  const { register, handleSubmit, formState: { errors } } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
  })

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h2
          className="text-3xl font-extrabold tracking-tight text-on-surface"
          style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
        >
          Đăng nhập
        </h2>
        <p className="mt-1 text-sm text-on-surface-variant">Chào mừng trở lại ✨</p>
      </div>

      <form className="flex flex-col gap-4" onSubmit={handleSubmit((data) => login.mutate(data))}>
        <Input
          label="Email"
          type="email"
          autoComplete="email"
          errorMessage={errors.email?.message}
          {...register('email')}
        />

        <Input
          label="Mật khẩu"
          type="password"
          autoComplete="current-password"
          errorMessage={errors.password?.message}
          {...register('password')}
        />

        <Button type="submit" fullWidth isLoading={login.isPending}>
          Đăng nhập
        </Button>
      </form>

      <div className="flex items-center justify-center gap-3 text-sm">
        <Link
          to="/reset-password"
          className="text-primary font-semibold hover:underline transition-colors"
        >
          Quên mật khẩu?
        </Link>
        <span className="text-outline-variant">·</span>
        <Link
          to="/register"
          className="text-primary font-semibold hover:underline transition-colors"
        >
          Tạo tài khoản mới
        </Link>
      </div>
    </div>
  )
}