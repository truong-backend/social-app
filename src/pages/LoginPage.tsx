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

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
  })

  return (
    <div className="login-page">
      <h2 className="login-page__title">Đăng nhập</h2>

      <form className="login-page__form" onSubmit={handleSubmit((data) => login.mutate(data))}>
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

        <Button
          type="submit"
          fullWidth
          isLoading={login.isPending}
        >
          Đăng nhập
        </Button>
      </form>

      <div className="login-page__links">
        <Link to="/reset-password" className="login-page__link">
          Quên mật khẩu?
        </Link>
        <span className="login-page__separator">·</span>
        <Link to="/register" className="login-page__link">
          Tạo tài khoản mới
        </Link>
      </div>
    </div>
  )
}