import { Outlet, Link, useLocation } from 'react-router-dom'
import { NotificationBell } from '@features/notification'
import { useMyProfile } from '@features/user'
import { useLogout } from '@features/auth/hooks/useAuthMutations'

const NAV_ITEMS = [
  { icon: 'home',          label: 'Home',          path: '/feed' },
  { icon: 'explore',       label: 'Explore',        path: '/search' },
  { icon: 'notifications', label: 'Notifications',  path: '/notifications' },
  { icon: 'mail',          label: 'Messages',       path: '/messages' },
  { icon: 'person',        label: 'Profile',        path: '/profile' },
]

export const MainLayout = () => {
  const { data: profile } = useMyProfile()
  const logout   = useLogout()
  const location = useLocation()

  return (
    <div className="max-w-[1600px] mx-auto flex min-h-screen bg-background">

      {/* ── Left Sidebar (Desktop) ── */}
      <aside className="hidden md:flex flex-col gap-4 p-6 h-screen sticky top-0 w-72 bg-surface">
        <div className="mb-8 px-4">
          <h1
            className="text-2xl font-extrabold tracking-tight text-on-surface"
            style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
          >
            Social Z
          </h1>
          <p className="text-on-surface-variant text-sm font-medium">Digital Gallery</p>
        </div>

        <nav className="space-y-2">
          {NAV_ITEMS.map(({ icon, label, path }) => {
            const isActive = location.pathname.startsWith(path)
            return (
              <Link
                key={path}
                to={path}
                className={`flex items-center gap-4 px-6 py-3 rounded-full transition-all duration-200 font-medium text-lg ${
                  isActive
                    ? 'text-primary bg-surface-variant'
                    : 'text-on-surface-variant hover:bg-surface-container-low'
                }`}
                style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
              >
                <span
                  className="material-symbols-outlined"
                  style={isActive ? { fontVariationSettings: "'FILL' 1" } : undefined}
                >
                  {icon}
                </span>
                {label}
              </Link>
            )
          })}
        </nav>

        <Link
          to="/create-post"
          className="mt-8 w-full py-4 rounded-full bg-gradient-to-br from-primary to-primary-container text-on-primary font-bold shadow-lg text-center active:scale-95 transition-all"
          style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
        >
          Create Post
        </Link>

        {/* Profile + Logout at bottom */}
        <div className="mt-auto flex items-center justify-between px-2 py-3">
          <Link to="/profile" className="flex items-center gap-3 min-w-0">
            <img
              src={profile?.profilePictureUrl ?? '/default-avatar.png'}
              alt={profile?.username}
              className="w-10 h-10 rounded-full object-cover border-2 border-primary ring-2 ring-primary-container/20 flex-shrink-0"
            />
            <div className="min-w-0">
              <p className="text-sm font-bold text-on-surface truncate">
                {profile?.familyName} {profile?.givenName}
              </p>
              <p className="text-xs text-on-surface-variant truncate">@{profile?.username}</p>
            </div>
          </Link>
          <button
            onClick={() => logout.mutate()}
            className="p-2 text-on-surface-variant hover:bg-surface-container-high rounded-full transition-colors active:scale-95"
            title="Đăng xuất"
          >
            <span className="material-symbols-outlined text-sm">logout</span>
          </button>
        </div>
      </aside>

      {/* ── Main Content ── */}
      <main className="flex-1 min-w-0">
        {/* Mobile Header */}
        <header className="sticky top-0 z-50 w-full bg-surface/70 backdrop-blur-xl shadow-[0_12px_40px_0_rgba(48,41,80,0.04)] md:hidden">
          <div className="flex items-center justify-between px-6 py-3">
            <span
              className="text-2xl font-extrabold tracking-tight text-on-surface"
              style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}
            >
              Social Z
            </span>
            <div className="flex gap-3 items-center">
              <NotificationBell />
              <Link to="/messages">
                <span className="material-symbols-outlined text-on-surface-variant">mail</span>
              </Link>
            </div>
          </div>
        </header>

        <Outlet />
      </main>

      {/* ── Bottom Nav (Mobile) ── */}
      <nav className="md:hidden fixed bottom-0 left-0 w-full bg-surface/70 backdrop-blur-xl flex justify-around items-center py-4 z-50 shadow-[0_-8px_20px_0_rgba(48,41,80,0.08)]">
        {NAV_ITEMS.filter((i) => i.icon !== 'notifications').map(({ icon, path }) => {
          const isActive = location.pathname.startsWith(path)
          return (
            <Link key={path} to={path} className={isActive ? 'text-primary' : 'text-on-surface-variant'}>
              <span
                className="material-symbols-outlined"
                style={isActive ? { fontVariationSettings: "'FILL' 1" } : undefined}
              >
                {icon}
              </span>
            </Link>
          )
        })}
        {/* FAB create post */}
        <Link
          to="/create-post"
          className="bg-gradient-to-br from-primary to-primary-container p-3 rounded-full -mt-10 shadow-lg active:scale-95 transition-all"
        >
          <span className="material-symbols-outlined text-white">add</span>
        </Link>
      </nav>
    </div>
  )
}