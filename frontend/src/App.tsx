import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './store/authStore'
import Layout from './components/layout/Layout'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import MapPage from './pages/MapPage'
import StationDetailPage from './pages/StationDetailPage'
import DashboardPage from './pages/DashboardPage'
import PaymentsPage from './pages/PaymentsPage'
import ProfilePage from './pages/ProfilePage'
import AdminDashboard from './pages/admin/AdminDashboard'
import LandingPage from './pages/LandingPage'

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated } = useAuthStore()
  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />
}

function AdminRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, user } = useAuthStore()
  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (user?.role !== 'PLATFORM_ADMIN' && user?.role !== 'CPO_ADMIN') {
    return <Navigate to="/dashboard" replace />
  }
  return <>{children}</>
}

export default function App() {
  return (
    <Routes>
      {/* Public routes */}
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Protected routes inside main layout */}
      <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route path="/map" element={<MapPage />} />
        <Route path="/stations/:stationId" element={<StationDetailPage />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/payments" element={<PaymentsPage />} />
        <Route path="/profile" element={<ProfilePage />} />
      </Route>

      {/* Admin routes */}
      <Route element={<AdminRoute><Layout /></AdminRoute>}>
        <Route path="/admin/*" element={<AdminDashboard />} />
      </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
