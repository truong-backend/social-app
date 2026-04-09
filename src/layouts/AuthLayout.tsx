import { Outlet, Navigate } from 'react-router-dom'
import { useSessionStore } from '@stores/session.store'

export const AuthLayout = () => {
  const isAuthenticated = useSessionStore((state) => state.isAuthenticated)

  if (isAuthenticated) {
    return <Navigate to="/feed" replace />
  }

  return (
    <div className="min-h-screen bg-surface font-body text-on-surface overflow-x-hidden">
      <main className="min-h-screen flex items-center justify-center relative px-4 py-12 md:p-0">
        {/* Abstract Background Orbs */}
        <div className="absolute top-[-10%] left-[-10%] w-[500px] h-[500px] rounded-full bg-primary/10 blur-[120px] pointer-events-none" />
        <div className="absolute bottom-[-5%] right-[-5%] w-[400px] h-[400px] rounded-full bg-secondary/10 blur-[100px] pointer-events-none" />

        <div className="w-full max-w-6xl grid grid-cols-1 lg:grid-cols-2 gap-0 overflow-hidden rounded-[2.5rem] bg-surface-container-lowest shadow-[0_20px_40px_rgba(35,44,81,0.06)]">
          {/* Branding Column */}
          <div className="hidden lg:flex flex-col justify-between p-16 relative overflow-hidden bg-surface-container-low">
            <div className="z-10">
              <h1
                className="text-3xl font-extrabold tracking-tight text-primary mb-2"
                style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
              >
                The Kinetic Pulse
              </h1>
              <p className="text-on-surface-variant font-medium">Join the digital nervous system.</p>
            </div>
            <div className="z-10 mt-auto">
              <h2
                className="text-5xl font-extrabold leading-[1.1] mb-6 tracking-tighter text-on-surface"
                style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
              >
                Connect with the <br />
                <span className="text-primary">flow of insight.</span>
              </h2>
              <div className="flex items-center gap-4">
                <div className="flex -space-x-3">
                  {[1, 2, 3].map((i) => (
                    <div
                      key={i}
                      className="w-10 h-10 rounded-full border-2 border-surface-container-lowest bg-gradient-to-br from-primary to-primary-container"
                    />
                  ))}
                </div>
                <p className="text-sm font-semibold text-on-surface-variant">Join 2,400+ creators active now</p>
              </div>
            </div>
            {/* Abstract Vector */}
            <div className="absolute right-[-20%] top-[20%] w-[600px] h-[600px] opacity-20 pointer-events-none">
              <svg viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
                <path
                  d="M44.7,-76.4C58.1,-69.2,69.5,-57.4,77.3,-43.8C85.1,-30.3,89.2,-15.1,88.4,-0.5C87.5,14.1,81.7,28.2,73.4,40.8C65.1,53.4,54.4,64.5,41.4,72.1C28.4,79.8,14.2,84,-0.6,85.1C-15.5,86.2,-31,84.1,-44.7,77C-58.4,70,-70.3,58,-78.2,44C-86.2,30,-90.1,15,-88.9,0.7C-87.7,-13.6,-81.4,-27.2,-72.5,-39.3C-63.6,-51.4,-52.1,-62,-39.2,-69.5C-26.3,-77,-13.1,-81.5,1.1,-83.4C15.3,-85.3,31.2,-83.6,44.7,-76.4Z"
                  fill="#0058BB"
                  transform="translate(100 100)"
                />
              </svg>
            </div>
          </div>

          {/* Form Column */}
          <div className="p-8 md:p-16 flex flex-col justify-center bg-surface-container-lowest">
            <div className="max-w-md w-full mx-auto">
              <div className="lg:hidden mb-8">
                <h1
                  className="text-2xl font-extrabold tracking-tight text-primary"
                  style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
                >
                  The Kinetic Pulse
                </h1>
              </div>
              <Outlet />
            </div>
          </div>
        </div>

        {/* Floating Live Status */}
        <div className="fixed bottom-8 right-8 hidden md:flex items-center gap-3 bg-white/70 backdrop-blur-xl px-5 py-3 rounded-full border border-outline-variant/10 shadow-xl">
          <span className="relative flex h-3 w-3">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-tertiary opacity-75" />
            <span className="relative inline-flex rounded-full h-3 w-3 bg-tertiary" />
          </span>
          <span className="text-sm font-bold text-on-surface">Pulse Live</span>
          <div className="h-4 w-[1px] bg-outline-variant/30" />
          <span className="text-xs text-on-surface-variant font-medium">v4.0.2</span>
        </div>
      </main>
    </div>
  )
}
