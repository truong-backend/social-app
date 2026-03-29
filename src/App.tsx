// import { useState } from 'react'
// import reactLogo from './assets/react.svg'
// import viteLogo from './assets/vite.svg'
// import heroImg from './assets/hero.png'
// import './App.css'

import { Toaster } from 'react-hot-toast'
import { QueryProvider } from '@providers/QueryProvider'
import { WebSocketProvider } from '@providers/WebSocketProvider'
import { AppRouter } from '@routes/AppRouter'
import { ErrorBoundary } from '@components/feedback/ErrorBoundary'
import { CallOverlay } from '@features/call/components/CallOverlay'

export const App = () => (
  <ErrorBoundary>
    <QueryProvider>
      <WebSocketProvider>
        <AppRouter />
        {/* CallOverlay nằm ngoài Router để luôn hiển thị khi call đang diễn ra */}
        <CallOverlay />
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 3000,
            style: { borderRadius: '8px', fontSize: '14px' },
            success: { iconTheme: { primary: '#22c55e', secondary: '#fff' } },
            error:   { iconTheme: { primary: '#ef4444', secondary: '#fff' } },
          }}
        />
      </WebSocketProvider>
    </QueryProvider>
  </ErrorBoundary>
)
