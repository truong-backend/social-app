  import { Outlet } from 'react-router-dom'
  import { NotificationBell } from '@features/notification'
  import { useMyProfile } from '@features/user'
  import { useLogout } from '@features/auth/hooks/useAuthMutations'
  import { Link } from 'react-router-dom'

  export const MainLayout = () => {
    const { data: profile } = useMyProfile()
    const logout = useLogout()

    return (
      <div className="main-layout">
        <nav className="main-layout__navbar">
          <div className="main-layout__navbar-brand">
            <Link to="/feed" className="main-layout__logo">Social Z</Link>
          </div>

          <div className="main-layout__navbar-center">
            <Link to="/feed" className="main-layout__nav-link">Trang chủ</Link>
            <Link to="/friends" className="main-layout__nav-link">Bạn bè</Link>
            <Link to="/messages" className="main-layout__nav-link">Tin nhắn</Link>
          </div>

          <div className="main-layout__navbar-right">
            <NotificationBell />
            <Link to={`/profile/${profile?.id}`} className="main-layout__profile-link">
              <img
                src={profile?.profilePictureUrl ?? '/default-avatar.png'}
                alt={profile?.username ?? 'Profile'}
                className="main-layout__profile-avatar"
              />
            </Link>
            <button
              className="main-layout__logout-btn"
              onClick={() => logout.mutate()}
            >
              Đăng xuất
            </button>
          </div>
        </nav>

        <main className="main-layout__content">
          <Outlet />
        </main>
      </div>
    )
  }