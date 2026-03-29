import { Navigate, Outlet } from 'react-router-dom'
import { useSessionStore } from '@stores/session.store'

export const AdminRoute = () => {
  const role = useSessionStore((state) => state.role)

  if (role !== 'ADMIN') {
    return <Navigate to="/feed" replace />
  }

  return <Outlet />
}