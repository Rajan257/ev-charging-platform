import { Outlet, Link, useLocation, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { useAuthStore } from '../../store/authStore'
import {
  MapPin, LayoutDashboard, CreditCard, User, Settings,
  Zap, LogOut, Bell, Menu, X, Shield
} from 'lucide-react'
import { useState } from 'react'

const navItems = [
  { icon: MapPin,        label: 'Find Stations', href: '/map' },
  { icon: LayoutDashboard, label: 'Dashboard',   href: '/dashboard' },
  { icon: CreditCard,    label: 'Payments',       href: '/payments' },
  { icon: User,          label: 'Profile',        href: '/profile' },
]

export default function Layout() {
  const { user, logout } = useAuthStore()
  const location = useLocation()
  const navigate = useNavigate()
  const [sidebarOpen, setSidebarOpen] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const isAdmin = user?.role === 'PLATFORM_ADMIN' || user?.role === 'CPO_ADMIN'

  return (
    <div className="flex h-screen overflow-hidden bg-dark-900">
      {/* Mobile Overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 bg-black/60 z-40 lg:hidden"
             onClick={() => setSidebarOpen(false)} />
      )}

      {/* Sidebar */}
      <motion.aside
        initial={false}
        animate={{ x: sidebarOpen ? 0 : -300 }}
        className={`fixed lg:static inset-y-0 left-0 w-72 z-50 lg:translate-x-0 flex flex-col
                   border-r border-white/5 lg:flex`}
        style={{
          background: 'linear-gradient(180deg, #0d1729 0%, #0a0f1e 100%)',
          transform: window.innerWidth >= 1024 ? 'none' : undefined,
        }}
      >
        {/* Logo */}
        <div className="flex items-center justify-between px-6 py-5 border-b border-white/5">
          <Link to="/map" className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0"
                 style={{ background: 'linear-gradient(135deg, #00D1FF, #39FF14)' }}>
              <Zap size={16} className="text-dark-900" />
            </div>
            <div>
              <div className="font-display font-bold text-sm leading-tight">EV Roaming Hub</div>
              <div className="text-xs text-white/30">India Platform</div>
            </div>
          </Link>
          <button onClick={() => setSidebarOpen(false)} className="lg:hidden text-white/40 hover:text-white">
            <X size={20} />
          </button>
        </div>

        {/* User card */}
        <div className="px-4 py-4 border-b border-white/5">
          <div className="flex items-center gap-3 p-3 rounded-xl bg-white/5">
            <div className="w-9 h-9 rounded-full flex items-center justify-center flex-shrink-0 font-bold text-sm"
                 style={{ background: 'linear-gradient(135deg, #00D1FF, #39FF14)', color: '#0A0F1E' }}>
              {user?.fullName?.charAt(0) ?? 'U'}
            </div>
            <div className="flex-1 min-w-0">
              <p className="font-medium text-sm truncate">{user?.fullName}</p>
              <p className="text-xs text-white/40 truncate">{user?.email}</p>
            </div>
            <span className="text-xs px-1.5 py-0.5 rounded bg-primary-400/20 text-primary-300 font-medium">
              {user?.role === 'PLATFORM_ADMIN' ? 'Admin' : 'Driver'}
            </span>
          </div>
        </div>

        {/* Nav items */}
        <nav className="flex-1 px-4 py-4 space-y-1 overflow-y-auto">
          {navItems.map(({ icon: Icon, label, href }) => (
            <Link
              key={href}
              to={href}
              onClick={() => setSidebarOpen(false)}
              className={`nav-item ${location.pathname === href || location.pathname.startsWith(href + '/') ? 'active' : ''}`}
            >
              <Icon size={18} />
              {label}
            </Link>
          ))}

          {isAdmin && (
            <>
              <div className="pt-4 pb-2">
                <p className="text-xs text-white/20 uppercase tracking-widest px-4">Admin</p>
              </div>
              <Link
                to="/admin"
                onClick={() => setSidebarOpen(false)}
                className={`nav-item ${location.pathname.startsWith('/admin') ? 'active' : ''}`}
              >
                <Shield size={18} />
                Admin Panel
              </Link>
            </>
          )}
        </nav>

        {/* Bottom actions */}
        <div className="px-4 py-4 border-t border-white/5 space-y-1">
          <button className="nav-item w-full text-left">
            <Settings size={18} /> Settings
          </button>
          <button onClick={handleLogout} className="nav-item w-full text-left text-red-400/70 hover:text-red-400">
            <LogOut size={18} /> Sign Out
          </button>
        </div>
      </motion.aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Top Header */}
        <header className="flex items-center justify-between px-6 py-4 border-b border-white/5 bg-dark-800/50 backdrop-blur-xl flex-shrink-0">
          <button
            onClick={() => setSidebarOpen(true)}
            className="lg:hidden text-white/60 hover:text-white"
          >
            <Menu size={22} />
          </button>
          <div className="flex-1 lg:flex-none">
            <h2 className="font-display font-semibold text-lg capitalize hidden lg:block">
              {location.pathname.split('/')[1] || 'Dashboard'}
            </h2>
          </div>
          <div className="flex items-center gap-3 ml-auto">
            {/* Notification bell */}
            <button id="notification-bell" className="relative w-9 h-9 rounded-xl flex items-center justify-center
                         bg-white/5 hover:bg-white/10 border border-white/10 transition-colors">
              <Bell size={16} className="text-white/60" />
              <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-ev-charge" />
            </button>
            {/* Avatar */}
            <div className="w-9 h-9 rounded-xl flex items-center justify-center font-bold text-sm cursor-pointer"
                 style={{ background: 'linear-gradient(135deg, #00D1FF, #39FF14)', color: '#0A0F1E' }}>
              {user?.fullName?.charAt(0) ?? 'U'}
            </div>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
