import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import './index.css'
import { Toaster } from 'react-hot-toast'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
      <Toaster
        position="top-right"
        toastOptions={{
          style: {
            background: 'rgba(17, 24, 39, 0.95)',
            color: '#fff',
            border: '1px solid rgba(255,255,255,0.1)',
            backdropFilter: 'blur(20px)',
            fontFamily: 'Inter, sans-serif',
          },
          success: {
            iconTheme: { primary: '#39FF14', secondary: '#0A0F1E' },
          },
          error: {
            iconTheme: { primary: '#FF3B3B', secondary: '#0A0F1E' },
          },
        }}
      />
    </BrowserRouter>
  </React.StrictMode>
)
