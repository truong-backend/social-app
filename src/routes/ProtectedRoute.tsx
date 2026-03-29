import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useSessionStore } from '@stores/session.store'

export const ProtectedRoute = () => {
  const isAuthenticated = useSessionStore((state) => state.isAuthenticated)
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return <Outlet />
}