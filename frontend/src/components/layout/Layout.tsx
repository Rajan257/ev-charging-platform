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
  const [notificationsOpen, setNotificationsOpen] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)
  const [unreadCount, setUnreadCount] = useState(2)

  const [notifications, setNotifications] = useState([
    {
      id: 1,
      title: 'Charging Completed',
      message: 'Your session at Ather Grid - Koramangala has ended.',
      time: '10 mins ago',
      unread: true,
      type: 'success'
    },
    {
      id: 2,
      title: 'Wallet Balance Alert',
      message: 'Your wallet balance is below Rs.100. Top up to ensure seamless charging.',
      time: '2 hours ago',
      unread: true,
      type: 'warning'
    },
    {
      id: 3,
      title: 'New Station Added',
      message: 'Tata Power has opened a new DC Fast charger in Indiranagar, Bengaluru.',
      time: '1 day ago',
      unread: false,
      type: 'info'
    }
  ])

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const markAllRead = () => {
    setNotifications(notifications.map(n => ({ ...n, unread: false })))
    setUnreadCount(0)
  }

  const handleNotificationClick = (id: number) => {
    setNotifications(notifications.map(n => n.id === id ? { ...n, unread: false } : n))
    setUnreadCount(prev => Math.max(0, prev - 1))
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
            <img src="/logo.png" alt="EV Roaming Hub" className="w-8 h-8 object-contain rounded-lg flex-shrink-0" />
            <div>
              <div className="font-display font-bold text-sm leading-tight">EV Roaming Hub</div>
              <div className="text-xs text-white/30">India Platform</div>
            </div>
          </Link>
          <button onClick={() => setSidebarOpen(false)} className="lg:hidden text-white/40 hover:text-white">
            <X size={20} />
          </button>
        </div>

        {/* User card (clickable, navigates to dashboard/profile) */}
        <div className="px-4 py-4 border-b border-white/5">
          <div
            onClick={() => navigate('/dashboard')}
            className="flex items-center gap-3 p-3 rounded-xl bg-white/5 cursor-pointer hover:bg-white/10 transition-colors"
          >
            <div className="w-9 h-9 rounded-full flex items-center justify-center flex-shrink-0 font-bold text-sm"
                 style={{ background: 'linear-gradient(135deg, #00D1FF, #39FF14)', color: '#0A0F1E' }}>
              {user?.fullName?.charAt(0) ?? 'U'}
            </div>
            <div className="flex-1 min-w-0">
              <p className="font-medium text-sm truncate">{user?.fullName}</p>
              <p className="text-xs text-white/40 truncate">{user?.email}</p>
            </div>
            <span className="text-xs px-1.5 py-0.5 rounded bg-primary-400/20 text-primary-300 font-medium flex-shrink-0">
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
          <button className="nav-item w-full text-left" onClick={() => navigate('/profile')}>
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
        <header className="relative z-30 flex items-center justify-between px-6 py-4 border-b border-white/5 bg-dark-800/50 backdrop-blur-xl flex-shrink-0">
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
            {/* Notification bell dropdown */}
            <div className="relative">
              <button
                id="notification-bell"
                onClick={() => {
                  setNotificationsOpen(!notificationsOpen)
                  setProfileOpen(false)
                }}
                className={`relative w-9 h-9 rounded-xl flex items-center justify-center border transition-colors ${
                  notificationsOpen ? 'bg-primary-400/10 border-primary-400/30' : 'bg-white/5 hover:bg-white/10 border-white/10'
                }`}
              >
                <Bell size={16} className={notificationsOpen ? 'text-primary-300' : 'text-white/60'} />
                {unreadCount > 0 && (
                  <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-ev-charge animate-pulse" />
                )}
              </button>

              {notificationsOpen && (
                <>
                  <div className="fixed inset-0 z-40" onClick={() => setNotificationsOpen(false)} />
                  <div
                    className="absolute right-0 mt-2 w-80 rounded-2xl border border-white/5 p-4 shadow-2xl z-50 animate-fade-in"
                    style={{ background: 'rgba(13, 23, 41, 0.95)', backdropFilter: 'blur(16px)' }}
                  >
                    <div className="flex items-center justify-between mb-3 pb-2 border-b border-white/5">
                      <span className="font-display font-semibold text-sm">Notifications</span>
                      {unreadCount > 0 && (
                        <button onClick={markAllRead} className="text-xs text-primary-400 hover:text-primary-300">
                          Mark all read
                        </button>
                      )}
                    </div>
                    <div className="space-y-2 max-h-64 overflow-y-auto">
                      {notifications.map(n => (
                        <div
                          key={n.id}
                          onClick={() => handleNotificationClick(n.id)}
                          className={`p-2.5 rounded-xl border text-left cursor-pointer transition-colors ${
                            n.unread ? 'bg-white/5 border-primary-400/20' : 'bg-transparent border-transparent hover:bg-white/5'
                          }`}
                        >
                          <div className="flex items-start justify-between gap-1">
                            <span className={`font-semibold text-xs ${n.unread ? 'text-white' : 'text-white/70'}`}>
                              {n.title}
                            </span>
                            <span className="text-[10px] text-white/30 whitespace-nowrap">{n.time}</span>
                          </div>
                          <p className="text-xs text-white/50 mt-1 leading-relaxed">{n.message}</p>
                        </div>
                      ))}
                    </div>
                  </div>
                </>
              )}
            </div>

            {/* Profile Avatar Dropdown */}
            <div className="relative">
              <div
                onClick={() => {
                  setProfileOpen(!profileOpen)
                  setNotificationsOpen(false)
                }}
                className="w-9 h-9 rounded-xl flex items-center justify-center font-bold text-sm cursor-pointer hover:scale-105 transition-transform"
                style={{ background: 'linear-gradient(135deg, #00D1FF, #39FF14)', color: '#0A0F1E' }}
              >
                {user?.fullName?.charAt(0) ?? 'U'}
              </div>

              {profileOpen && (
                <>
                  <div className="fixed inset-0 z-40" onClick={() => setProfileOpen(false)} />
                  <div
                    className="absolute right-0 mt-2 w-48 rounded-2xl border border-white/5 py-2 shadow-2xl z-50 animate-fade-in"
                    style={{ background: 'rgba(13, 23, 41, 0.95)', backdropFilter: 'blur(16px)' }}
                  >
                    <div className="px-4 py-2 border-b border-white/5 mb-1">
                      <p className="font-semibold text-sm truncate">{user?.fullName}</p>
                      <p className="text-xs text-white/40 truncate">{user?.email}</p>
                    </div>
                    <button
                      onClick={() => {
                        setProfileOpen(false)
                        navigate('/dashboard')
                      }}
                      className="w-full text-left px-4 py-2 text-sm text-white/75 hover:bg-white/5 hover:text-white transition-colors"
                    >
                      Dashboard
                    </button>
                    <button
                      onClick={() => {
                        setProfileOpen(false)
                        navigate('/profile')
                      }}
                      className="w-full text-left px-4 py-2 text-sm text-white/75 hover:bg-white/5 hover:text-white transition-colors"
                    >
                      My Profile
                    </button>
                    <button
                      onClick={() => {
                        setProfileOpen(false)
                        navigate('/payments')
                      }}
                      className="w-full text-left px-4 py-2 text-sm text-white/75 hover:bg-white/5 hover:text-white transition-colors"
                    >
                      Payments
                    </button>
                    <div className="border-t border-white/5 my-1" />
                    <button
                      onClick={() => {
                        setProfileOpen(false)
                        handleLogout()
                      }}
                      className="w-full text-left px-4 py-2 text-sm text-red-400/80 hover:bg-red-500/10 hover:text-red-400 transition-colors font-medium"
                    >
                      Sign Out
                    </button>
                  </div>
                </>
              )}
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
