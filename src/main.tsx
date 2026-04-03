import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'

import './index.css'
import { App } from './App.tsx'

const rootElement = document.getElementById('root')
 
if (!rootElement) {
  throw new Error('[main] Root element #root not found in DOM')
}

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
