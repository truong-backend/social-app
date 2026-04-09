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

  const { register, handleSubmit, formState: { errors } } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
  })

  return (
    <div className="flex flex-col gap-8">
      {/* Header */}
      <header>
        <h2
          className="text-3xl font-extrabold tracking-tight text-on-surface mb-2"
          style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
        >
          Create Account
        </h2>
        <p className="text-on-surface-variant">Experience the pulse of the network. Register to get started.</p>
      </header>

      <form
        className="flex flex-col gap-5"
        onSubmit={handleSubmit((data) => register_.mutate(data))}
      >
        {/* Full Name */}
        <div className="flex flex-col gap-2">
          <label className="block text-sm font-semibold text-on-surface ml-1">Full Name</label>
          <div className="relative group">
            <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline-variant group-focus-within:text-primary transition-colors">
              person
            </span>
            <div className="grid grid-cols-2 gap-3">
              <div className="relative">
                <input
                  autoComplete="family-name"
                  placeholder="Họ"
                  {...register('familyName')}
                  className="w-full pl-4 pr-4 py-4 bg-surface-container-low border-transparent rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary focus:bg-surface-container-lowest transition-all placeholder:text-outline-variant/60 outline-none"
                />
                {errors.familyName && (
                  <p className="text-xs text-error mt-1">{errors.familyName.message}</p>
                )}
              </div>
              <div className="relative">
                <input
                  autoComplete="given-name"
                  placeholder="Tên"
                  {...register('givenName')}
                  className="w-full pl-4 pr-4 py-4 bg-surface-container-low border-transparent rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary focus:bg-surface-container-lowest transition-all placeholder:text-outline-variant/60 outline-none"
                />
                {errors.givenName && (
                  <p className="text-xs text-error mt-1">{errors.givenName.message}</p>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Email */}
        <div className="flex flex-col gap-2">
          <label className="block text-sm font-semibold text-on-surface ml-1">Email Address</label>
          <div className="relative group">
            <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline-variant group-focus-within:text-primary transition-colors">
              mail
            </span>
            <input
              type="email"
              autoComplete="email"
              placeholder="name@company.com"
              {...register('email')}
              className="w-full pl-12 pr-4 py-4 bg-surface-container-low border-transparent rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary focus:bg-surface-container-lowest transition-all placeholder:text-outline-variant/60 outline-none"
            />
          </div>
          {errors.email && <p className="text-xs text-error ml-1">{errors.email.message}</p>}
        </div>

        {/* Password */}
        <div className="flex flex-col gap-2">
          <label className="block text-sm font-semibold text-on-surface ml-1">Password</label>
          <div className="relative group">
            <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline-variant group-focus-within:text-primary transition-colors">
              lock
            </span>
            <input
              type="password"
              autoComplete="new-password"
              placeholder="••••••••"
              {...register('password')}
              className="w-full pl-12 pr-4 py-4 bg-surface-container-low border-transparent rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary focus:bg-surface-container-lowest transition-all placeholder:text-outline-variant/60 outline-none"
            />
          </div>
          {errors.password && <p className="text-xs text-error ml-1">{errors.password.message}</p>}
        </div>

        {/* Birthdate */}
        <div className="flex flex-col gap-2">
          <label className="block text-sm font-semibold text-on-surface ml-1">Date of Birth</label>
          <div className="relative group">
            <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline-variant group-focus-within:text-primary transition-colors">
              calendar_today
            </span>
            <input
              type="date"
              {...register('birthdate')}
              className="w-full pl-12 pr-4 py-4 bg-surface-container-low border-transparent rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary focus:bg-surface-container-lowest transition-all outline-none"
            />
          </div>
          {errors.birthdate && <p className="text-xs text-error ml-1">{errors.birthdate.message}</p>}
        </div>

        {/* Terms */}
        <div className="flex items-start gap-3 py-1">
          <input
            type="checkbox"
            className="w-5 h-5 text-primary border-outline-variant rounded focus:ring-primary/20 focus:ring-offset-0 bg-surface-container-low mt-0.5"
          />
          <label className="text-sm text-on-surface-variant leading-tight">
            I agree to the{' '}
            <a href="#" className="text-primary font-semibold hover:underline">Terms of Service</a>
            {' '}and{' '}
            <a href="#" className="text-primary font-semibold hover:underline">Privacy Policy</a>.
          </label>
        </div>

        {/* Submit */}
        <button
          type="submit"
          disabled={register_.isPending}
          className="w-full py-4 bg-gradient-to-br from-primary to-primary-container text-on-primary font-bold rounded-xl shadow-lg shadow-primary/20 hover:scale-[1.02] active:scale-[0.98] transition-all flex items-center justify-center gap-2 group disabled:opacity-60 disabled:pointer-events-none"
        >
          {register_.isPending ? 'Đang tạo tài khoản...' : 'Create Account'}
          {!register_.isPending && (
            <span className="material-symbols-outlined text-[20px] group-hover:translate-x-1 transition-transform">
              arrow_forward
            </span>
          )}
        </button>
      </form>

      <div className="pt-6 border-t border-outline-variant/10">
        <div className="flex items-center justify-center gap-2 text-sm">
          <span className="text-on-surface-variant">Already have an account?</span>
          <Link to="/login" className="text-primary font-bold hover:underline">
            Login here
          </Link>
        </div>
      </div>
    </div>
  )
}
