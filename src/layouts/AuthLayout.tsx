import { Outlet, Navigate } from 'react-router-dom'
import { useSessionStore } from '@stores/session.store'

export const AuthLayout = () => {
  const isAuthenticated = useSessionStore((state) => state.isAuthenticated)

  if (isAuthenticated) {
    return <Navigate to="/feed" replace />
  }

  return (
    <div className="min-h-screen bg-background flex items-center justify-center px-4 relative overflow-hidden">
      {/* Decorative blobs */}
      <div className="absolute top-[-10%] left-[-5%] w-[500px] h-[500px] rounded-full bg-gradient-to-br from-primary/20 to-tertiary/10 blur-3xl pointer-events-none" />
      <div className="absolute bottom-[-10%] right-[-5%] w-[400px] h-[400px] rounded-full bg-gradient-to-tl from-secondary/15 to-primary-container/20 blur-3xl pointer-events-none" />

      <div className="relative z-10 w-full max-w-[1000px] flex flex-col md:flex-row items-center gap-12 md:gap-20">
        {/* Brand side */}
        <div className="flex-1 text-center md:text-left">
          <h1
            className="text-5xl md:text-6xl font-extrabold tracking-tight text-on-surface"
            style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
          >
            Social Z
          </h1>
          <p className="mt-3 text-lg text-on-surface-variant font-medium" style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}>
            Digital Gallery
          </p>
          <p className="mt-6 text-on-surface-variant leading-relaxed max-w-sm mx-auto md:mx-0">
            Kết nối với những người bạn yêu thích. Chia sẻ khoảnh khắc. Khám phá thế giới.
          </p>

          {/* Decorative tags */}
          <div className="hidden md:flex flex-wrap gap-2 mt-10">
            {['#Creativity', '#Connection', '#Community', '#Stories'].map((tag) => (
              <span
                key={tag}
                className="px-4 py-2 bg-surface-container-highest text-primary rounded-full text-xs font-bold"
              >
                {tag}
              </span>
            ))}
          </div>
        </div>

        {/* Card */}
        <div className="w-full max-w-[420px] bg-surface-container-lowest rounded-[2rem] shadow-[0_24px_64px_rgba(48,41,80,0.10)] p-8 md:p-10">
          <Outlet />
        </div>
      </div>
    </div>
  )
}