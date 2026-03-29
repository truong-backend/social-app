import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Link } from 'react-router-dom'
import { useRegister } from '@features/auth/hooks/useAuthMutations'
import { Input } from '@components/ui/Input'
import { Button } from '@components/ui/Button'
import { PASSWORD_MIN_LENGTH, PASSWORD_MAX_LENGTH } from '@features/auth/constants/auth.constants'

const registerSchema = z.object({
  email:      z.string().email('Email không hợp lệ'),
  password:   z.string()
                .min(PASSWORD_MIN_LENGTH, `Tối thiểu ${PASSWORD_MIN_LENGTH} ký tự`)
                .max(PASSWORD_MAX_LENGTH, `Tối đa ${PASSWORD_MAX_LENGTH} ký tự`),
  familyName: z.string().min(1, 'Vui lòng nhập họ'),
  givenName:  z.string().min(1, 'Vui lòng nhập tên'),
  birthdate:  z.string().min(1, 'Vui lòng chọn ngày sinh'),
})

type RegisterFormValues = z.infer<typeof registerSchema>

export const RegisterPage = () => {
  const register_ = useRegister()

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
  })

  return (
    <div className="register-page">
      <h2 className="register-page__title">Tạo tài khoản</h2>

      <form
        className="register-page__form"
        onSubmit={handleSubmit((data) => register_.mutate(data))}
      >
        <div className="register-page__name-row">
          <Input
            label="Họ"
            autoComplete="family-name"
            errorMessage={errors.familyName?.message}
            {...register('familyName')}
          />
          <Input
            label="Tên"
            autoComplete="given-name"
            errorMessage={errors.givenName?.message}
            {...register('givenName')}
          />
        </div>

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
          autoComplete="new-password"
          errorMessage={errors.password?.message}
          {...register('password')}
        />

        <Input
          label="Ngày sinh"
          type="date"
          errorMessage={errors.birthdate?.message}
          {...register('birthdate')}
        />

        <Button
          type="submit"
          fullWidth
          isLoading={register_.isPending}
        >
          Đăng ký
        </Button>
      </form>

      <p className="register-page__login-link">
        Đã có tài khoản?{' '}
        <Link to="/login" className="register-page__link">
          Đăng nhập
        </Link>
      </p>
    </div>
  )
}