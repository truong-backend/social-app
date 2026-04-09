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
    <div className="flex flex-col gap-8">
      {/* Header */}
      <header>
        <h2
          className="text-3xl font-extrabold tracking-tight text-on-surface mb-2"
          style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
        >
          Welcome Back
        </h2>
        <p className="text-on-surface-variant font-medium">Please enter your details to continue.</p>
      </header>

      <form className="flex flex-col gap-5" onSubmit={handleSubmit((data) => login.mutate(data))}>
        {/* Email */}
        <div className="flex flex-col gap-2">
          <label className="text-sm font-semibold text-on-surface ml-1">Email or Username</label>
          <div className="relative">
            <input
              type="email"
              autoComplete="email"
              placeholder="Enter your email"
              {...register('email')}
              className="w-full px-5 py-4 bg-surface-container-lowest border-0 rounded-xl focus:ring-2 focus:ring-primary-fixed transition-all outline-none text-on-surface placeholder:text-outline-variant shadow-sm"
            />
          </div>
          {errors.email && (
            <p className="text-xs text-error ml-1">{errors.email.message}</p>
          )}
        </div>

        {/* Password */}
        <div className="flex flex-col gap-2">
          <div className="flex justify-between items-center px-1">
            <label className="text-sm font-semibold text-on-surface">Password</label>
            <Link to="/reset-password" className="text-sm font-bold text-primary hover:text-primary-dim transition-colors">
              Forgot password?
            </Link>
          </div>
          <input
            type="password"
            autoComplete="current-password"
            placeholder="••••••••"
            {...register('password')}
            className="w-full px-5 py-4 bg-surface-container-lowest border-0 rounded-xl focus:ring-2 focus:ring-primary-fixed transition-all outline-none text-on-surface placeholder:text-outline-variant shadow-sm"
          />
          {errors.password && (
            <p className="text-xs text-error ml-1">{errors.password.message}</p>
          )}
        </div>

        {/* Remember me */}
        <div className="flex items-center px-1 gap-3">
          <input
            type="checkbox"
            id="remember"
            className="w-5 h-5 rounded border-outline-variant text-primary focus:ring-primary bg-surface-container-lowest"
          />
          <label htmlFor="remember" className="text-sm font-medium text-on-surface-variant">
            Remember me for 30 days
          </label>
        </div>

        {/* Submit */}
        <button
          type="submit"
          disabled={login.isPending}
          className="w-full py-4 bg-gradient-to-r from-primary to-primary-container text-on-primary font-bold rounded-xl shadow-lg shadow-primary/20 hover:scale-[1.02] active:scale-[0.98] transition-all disabled:opacity-60 disabled:pointer-events-none"
        >
          {login.isPending ? 'Đang đăng nhập...' : 'Login to Pulse'}
        </button>
      </form>

      {/* Divider */}
      <div className="relative flex items-center gap-4">
        <div className="flex-grow h-px bg-surface-container-high" />
        <span className="text-xs font-bold text-outline-variant uppercase tracking-widest">Or continue with</span>
        <div className="flex-grow h-px bg-surface-container-high" />
      </div>

      {/* Social */}
      <div className="grid grid-cols-2 gap-4">
        <button
          type="button"
          className="flex items-center justify-center gap-3 py-3 px-4 bg-surface-container-lowest rounded-xl border border-outline-variant/10 hover:bg-surface-container-low transition-colors font-semibold text-on-surface shadow-sm"
        >
          <svg className="w-5 h-5" viewBox="0 0 24 24">
            <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4" />
            <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853" />
            <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z" fill="#FBBC05" />
            <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335" />
          </svg>
          Google
        </button>
        <button
          type="button"
          className="flex items-center justify-center gap-3 py-3 px-4 bg-surface-container-lowest rounded-xl border border-outline-variant/10 hover:bg-surface-container-low transition-colors font-semibold text-on-surface shadow-sm"
        >
          <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
            <path d="M17.05 20.28c-.98.95-2.05 1.83-3.14 1.83-1.05 0-1.39-.65-2.64-.65-1.26 0-1.64.63-2.64.63-1.07 0-2.27-.99-3.26-1.97-2.03-2.02-3.56-5.71-3.56-9.15 0-3.39 2.09-5.18 4.07-5.18 1.05 0 2.04.73 2.68.73.64 0 1.77-.88 3-.88.94 0 3.59.34 5.3 2.84-4.29 2.52-3.59 8.29.19 10.31zm-4.71-15.54c.64-.77 1.07-1.84.95-2.91-1.1.04-2.18.73-2.97 1.66-.65.76-1.12 1.87-.95 2.91 1.2.09 2.33-.89 2.97-1.66z" />
          </svg>
          Apple
        </button>
      </div>

      <footer className="text-center">
        <p className="text-on-surface-variant font-medium text-sm">
          Don't have an account?{' '}
          <Link to="/register" className="text-primary font-bold hover:underline ml-1">
            Sign up for free
          </Link>
        </p>
      </footer>
    </div>
  )
}
