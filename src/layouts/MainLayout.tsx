import { useState, useRef, useEffect } from 'react'
import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom'
import { NotificationBell } from '@features/notification'
import { useMyProfile } from '@features/user'
import { useLogout } from '@features/auth/hooks/useAuthMutations'

export const MainLayout = () => {
  const { data: profile } = useMyProfile()
  const logout   = useLogout()
  const location = useLocation()
  const navigate = useNavigate()
  const [searchText, setSearchText] = useState('')

  const navLinks = [
    { to: '/feed',                             icon: 'dynamic_feed',  label: 'Feed' },
    { to: '/messages',                         icon: 'forum',         label: 'Messages' },
    { to: '/notifications',                    icon: 'notifications', label: 'Notifications' },
    { to: '/profile/' + (profile?.id ?? ''),   icon: 'person',        label: 'Profile' },
    { to: '/friends',                          icon: 'group',         label: 'Friends' },
    // { to: '/search',                           icon: 'search',        label: 'Search' },
  ]

  const isActive = (to: string) =>
    to.startsWith('/profile')
      ? location.pathname.startsWith('/profile')
      : location.pathname.startsWith(to)

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    if (searchText.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchText.trim())}`)
      setSearchText('')
    }
  }

  return (
    <div className="min-h-screen bg-surface text-on-surface">
      {/* Top NavBar */}
      <header className="fixed top-0 w-full z-50 bg-white/70 backdrop-blur-xl shadow-sm flex items-center justify-between px-8 py-3 border-b border-slate-100">
        <div className="flex items-center gap-8">
          <Link to="/feed" className="text-2xl font-bold tracking-tight text-primary" style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}>
            The Kinetic Pulse
          </Link>
          <form onSubmit={handleSearch} className="hidden md:flex bg-slate-100/50 rounded-full px-4 py-1.5 items-center gap-2">
            <span className="material-symbols-outlined text-slate-400 text-lg">search</span>
            <input
              className="bg-transparent border-none focus:ring-0 text-sm w-64 text-on-surface outline-none placeholder:text-slate-400"
              placeholder="Search the Pulse..."
              type="text"
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
            />
          </form>
        </div>

        <div className="flex items-center gap-4">
          {/* <Link
            to="/posts/create"
            className="bg-gradient-to-br from-primary to-primary-container text-on-primary px-6 py-2 rounded-xl text-sm font-semibold shadow-sm active:scale-95 transition-transform"
          >
            Create Post
          </Link> */}

          <NotificationBell />

          <Link
            to={`/profile/${profile?.id ?? ''}`}
            className="p-1 hover:bg-slate-100/50 rounded-full transition-colors cursor-pointer"
          >
            {profile?.profilePictureUrl ? (
              <img
                src={profile.profilePictureUrl}
                alt={profile.username ?? 'Profile'}
                className="w-8 h-8 rounded-full object-cover"
              />
            ) : (
              <span className="material-symbols-outlined text-on-surface-variant">account_circle</span>
            )}
          </Link>
        </div>
      </header>

      <div className="flex pt-16">
        {/* Side NavBar */}
        <aside className="hidden md:flex flex-col gap-2 p-4 h-screen w-64 fixed left-0 top-0 pt-20 bg-slate-50 border-r border-slate-100">
          <div className="mb-6 px-4">
            <h3 className="text-xs font-bold uppercase tracking-widest text-slate-400">Navigation</h3>
            <p className="text-sm font-semibold text-primary" style={{ fontFamily: "'Plus Jakarta Sans', sans-serif" }}>Pulse Network</p>
          </div>
          <nav className="flex flex-col gap-1">
            {navLinks.map(({ to, icon, label }) => {
              const active = isActive(to)
              return (
                <Link
                  key={to}
                  to={to}
                  className={`flex items-center gap-3 px-4 py-3 rounded-xl transition-all ${
                    active
                      ? 'text-primary font-bold bg-blue-50 translate-x-1'
                      : 'text-slate-500 hover:text-primary hover:bg-slate-100/50'
                  }`}
                >
                  <span
                    className="material-symbols-outlined"
                    style={active ? { fontVariationSettings: "'FILL' 1" } : undefined}
                  >
                    {icon}
                  </span>
                  <span className="font-semibold">{label}</span>
                </Link>
              )
            })}
          </nav>

          <div className="mt-auto px-4 pb-8">
            <button
              onClick={() => logout.mutate()}
              className="flex items-center gap-3 text-slate-400 hover:text-error transition-colors text-sm font-semibold w-full px-4 py-3"
            >
              <span className="material-symbols-outlined">logout</span>
              Đăng xuất
            </button>
          </div>
        </aside>

        {/* Main Content */}
        <main className="flex-1 md:ml-64">
          <Outlet />
        </main>
      </div>

      {/* Mobile Bottom Nav */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 bg-white/90 backdrop-blur-lg border-t border-slate-100 flex items-center justify-around py-3 z-50">
        {navLinks.slice(0, 5).map(({ to, icon, label }) => {
          const active = isActive(to)
          return (
            <Link
              key={to}
              to={to}
              className={`flex flex-col items-center gap-1 ${active ? 'text-primary' : 'text-slate-500'}`}
            >
              <span className="material-symbols-outlined" style={active ? { fontVariationSettings: "'FILL' 1" } : undefined}>
                {icon}
              </span>
              <span className="text-[10px] font-medium">{label}</span>
            </Link>
          )
        })}
      </nav>
    </div>
  )
}
