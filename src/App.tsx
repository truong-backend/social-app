import { Toaster } from 'react-hot-toast'
import { QueryProvider } from '@providers/QueryProvider'
import { WebSocketProvider } from '@providers/WebSocketProvider'
import { AppRouter } from '@routes/AppRouter'
import { ErrorBoundary } from '@components/feedback/ErrorBoundary'

export const App = () => (
  <ErrorBoundary>
    <QueryProvider>
      <WebSocketProvider>
        <AppRouter />
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 3000,
            style: {
              borderRadius: '12px',
              fontSize: '14px',
              fontFamily: "'Inter', sans-serif",
              background: '#ffffff',
              color: '#232c51',
              boxShadow: '0 8px 30px rgba(35, 44, 81, 0.12)',
              border: '1px solid rgba(162, 171, 215, 0.2)',
            },
            success: {
              iconTheme: { primary: '#006a26', secondary: '#fff' },
            },
            error: {
              iconTheme: { primary: '#b31b25', secondary: '#fff' },
            },
          }}
        />
      </WebSocketProvider>
    </QueryProvider>
  </ErrorBoundary>
)
