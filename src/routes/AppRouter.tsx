import { lazy, Suspense } from 'react'
import { createBrowserRouter, RouterProvider, Navigate } from 'react-router-dom'
import { MainLayout } from '@layouts/MainLayout'
import { AuthLayout } from '@layouts/AuthLayout'
import { ProtectedRoute } from './ProtectedRoute'
import { AdminRoute } from './AdminRoute'
import { Spinner } from '@components/feedback/Spinner'

// ── Lazy pages ─────────────────────────────────────────────────────────────
const FeedPage          = lazy(() => import('@pages/FeedPage').then((m) => ({ default: m.FeedPage })))
const LoginPage         = lazy(() => import('@pages/LoginPage').then((m) => ({ default: m.LoginPage })))
const RegisterPage      = lazy(() => import('@pages/RegisterPage').then((m) => ({ default: m.RegisterPage })))
const ConfirmEmailPage  = lazy(() => import('@pages/ConfirmEmailPage').then((m) => ({ default: m.ConfirmEmailPage })))
const ResetPasswordPage = lazy(() => import('@pages/ResetPasswordPage').then((m) => ({ default: m.ResetPasswordPage })))
const ProfilePage       = lazy(() => import('@pages/ProfilePage').then((m) => ({ default: m.ProfilePage })))
const EditProfilePage   = lazy(() => import('@pages/EditProfilePage').then((m) => ({ default: m.EditProfilePage })))
const PostDetailPage    = lazy(() => import('@pages/PostDetailPage').then((m) => ({ default: m.PostDetailPage })))
const MessagesPage      = lazy(() => import('@pages/MessagesPage').then((m) => ({ default: m.MessagesPage })))
const FriendsPage       = lazy(() => import('@pages/FriendsPage').then((m) => ({ default: m.FriendsPage })))
const SearchPage        = lazy(() => import('@pages/SearchPage').then((m) => ({ default: m.SearchPage })))
const NotFoundPage      = lazy(() => import('@pages/NotFoundPage').then((m) => ({ default: m.NotFoundPage })))
const AdminPage         = lazy(() => import('@pages/AdminPage').then((m) => ({ default: m.AdminPage })))

const SuspenseFallback = () => (
  <div style={{ display: 'flex', justifyContent: 'center', padding: '4rem' }}>
    <Spinner size="lg" />
  </div>
)

const wrap = (element: React.ReactNode) => (
  <Suspense fallback={<SuspenseFallback />}>{element}</Suspense>
)

const router = createBrowserRouter([
  // ── Auth routes ────────────────────────────────────────────────────────
  {
    element: <AuthLayout />,
    children: [
      { path: '/login',          element: wrap(<LoginPage />) },
      { path: '/register',       element: wrap(<RegisterPage />) },
      { path: '/confirm-email',  element: wrap(<ConfirmEmailPage />) },
      { path: '/reset-password', element: wrap(<ResetPasswordPage />) },
    ],
  },

  // ── Protected routes ───────────────────────────────────────────────────
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <MainLayout />,
        children: [
          { index: true,               element: <Navigate to="/feed" replace /> },
          { path: '/feed',             element: wrap(<FeedPage />) },
          { path: '/profile/edit',     element: wrap(<EditProfilePage />) },
          { path: '/profile/:userId',  element: wrap(<ProfilePage />) },
          { path: '/posts/:postId',    element: wrap(<PostDetailPage />) },
          { path: '/messages',         element: wrap(<MessagesPage />) },
          { path: '/messages/:chatId', element: wrap(<MessagesPage />) },
          { path: '/friends',          element: wrap(<FriendsPage />) },
          { path: '/search',           element: wrap(<SearchPage />) },
        ],
      },

      // ── Admin only ───────────────────────────────────────────────────
      {
        element: <AdminRoute />,
        children: [
          {
            element: <MainLayout />,
            children: [
              { path: '/admin', element: wrap(<AdminPage />) },
            ],
          },
        ],
      },
    ],
  },

  // ── 404 ───────────────────────────────────────────────────────────────
  { path: '*', element: wrap(<NotFoundPage />) },
])

export const AppRouter = () => <RouterProvider router={router} />