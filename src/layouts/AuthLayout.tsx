import { Outlet, Navigate } from 'react-router-dom'
import { useSessionStore } from '@stores/session.store'

export const AuthLayout = () => {
  const isAuthenticated = useSessionStore((state) => state.isAuthenticated)

  if (isAuthenticated) {
    return <Navigate to="/feed" replace />
  }

  return (
    <div className="auth-layout">
      <div className="auth-layout__container">
        <div className="auth-layout__brand">
          <h1 className="auth-layout__logo">Social Z</h1>
          <p className="auth-layout__tagline">Kết nối với bạn bè của bạn</p>
        </div>
        <div className="auth-layout__card">
          <Outlet />
        </div>
      </div>
    </div>
  )
}