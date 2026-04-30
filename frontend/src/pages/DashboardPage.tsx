import { useEffect, useState } from 'react'
import { motion } from 'framer-motion'
import { useAuthStore } from '../store/authStore'
import { sessionApi, billingApi, paymentApi } from '../services/api'
import {
  Zap, Clock, Battery, IndianRupee, TrendingUp, MapPin,
  CheckCircle, AlertCircle, Play, Square
} from 'lucide-react'
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, BarChart, Bar
} from 'recharts'
import toast from 'react-hot-toast'

interface Session {
  id: string; transactionId: string; status: string
  startedAt: string; stoppedAt?: string; energyKwh?: number
  totalAmount?: number; stationId: string; connectorId: string
}

// Mock usage data for the charts
const weeklyData = [
  { day: 'Mon', kwh: 15.2, cost: 182 },
  { day: 'Tue', kwh: 0,    cost: 0 },
  { day: 'Wed', kwh: 22.1, cost: 265 },
  { day: 'Thu', kwh: 18.5, cost: 222 },
  { day: 'Fri', kwh: 0,    cost: 0 },
  { day: 'Sat', kwh: 45.0, cost: 540 },
  { day: 'Sun', kwh: 30.2, cost: 362 },
]

const tooltipStyle = {
  background: 'rgba(10,15,30,0.95)',
  border: '1px solid rgba(255,255,255,0.1)',
  borderRadius: '12px',
  color: '#fff',
  fontFamily: 'Inter, sans-serif',
  fontSize: '12px',
}

export default function DashboardPage() {
  const { user } = useAuthStore()
  const [activeSession, setActiveSession] = useState<Session | null>(null)
  const [history, setHistory] = useState<Session[]>([])
  const [wallet, setWallet] = useState<{ balance: number; currency: string } | null>(null)
  const [loading, setLoading] = useState(true)
  const [sessionTimer, setSessionTimer] = useState(0)
  const [stoppingSession, setStoppingSession] = useState(false)

  // Live session timer
  useEffect(() => {
    if (!activeSession) return
    const start = new Date(activeSession.startedAt).getTime()
    const interval = setInterval(() => {
      setSessionTimer(Math.floor((Date.now() - start) / 1000))
    }, 1000)
    return () => clearInterval(interval)
  }, [activeSession])

  useEffect(() => {
    const load = async () => {
      setLoading(true)
      try {
        const [sessionRes, historyRes, walletRes] = await Promise.allSettled([
          sessionApi.getActiveSession(),
          sessionApi.getHistory({ size: 10 }),
          paymentApi.getWallet(),
        ])
        if (sessionRes.status === 'fulfilled') setActiveSession(sessionRes.value.data)
        if (historyRes.status === 'fulfilled') setHistory(historyRes.value.data.content || [])
        if (walletRes.status === 'fulfilled') setWallet(walletRes.value.data)
      } catch {}
      setLoading(false)
    }
    load()
  }, [])

  const stopSession = async () => {
    if (!activeSession) return
    setStoppingSession(true)
    try {
      await sessionApi.stopSession(activeSession.id, 'USER_REQUEST')
      toast.success('Session stopped. Generating invoice...')
      setActiveSession(null)
    } catch {
      toast.error('Failed to stop session. Please try again.')
    }
    setStoppingSession(false)
  }

  const formatTime = (s: number) => {
    const h = Math.floor(s / 3600), m = Math.floor((s % 3600) / 60), sec = s % 60
    return h > 0
      ? `${h}h ${m}m ${sec}s`
      : `${m}m ${sec}s`
  }

  const totalKwh = history.reduce((sum, s) => sum + (s.energyKwh || 0), 0)
  const totalSpend = history.reduce((sum, s) => sum + (s.totalAmount || 0), 0)
  const completedSessions = history.filter(s => s.status !== 'ACTIVE').length

  return (
    <div className="p-6 space-y-6 max-w-7xl mx-auto animate-fade-in">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display font-bold text-2xl">
            Good {new Date().getHours() < 12 ? 'Morning' : new Date().getHours() < 18 ? 'Afternoon' : 'Evening'},
            {' '}{user?.fullName?.split(' ')[0]} ⚡
          </h1>
          <p className="text-white/50 mt-0.5">Your EV charging dashboard</p>
        </div>
      </div>

      {/* ACTIVE SESSION BANNER */}
      {activeSession && (
        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          className="glass-card p-6 neon-border"
        >
          <div className="flex items-center justify-between mb-5">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl flex items-center justify-center"
                   style={{ background: 'rgba(57,255,20,0.15)', border: '1px solid rgba(57,255,20,0.3)' }}>
                <Zap size={20} style={{ color: '#39FF14' }} />
              </div>
              <div>
                <p className="font-display font-bold text-lg">⚡ Charging in Progress</p>
                <p className="text-sm text-white/50">TX: {activeSession.transactionId}</p>
              </div>
            </div>
            <span className="badge-available animate-pulse-slow">
              <span className="live-dot" /> LIVE
            </span>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
            {[
              { icon: Clock, label: 'Duration', value: formatTime(sessionTimer), color: '#00D1FF' },
              { icon: Battery, label: 'Energy', value: `${((sessionTimer / 3600) * 11).toFixed(1)} kWh`, color: '#39FF14' },
              { icon: IndianRupee, label: 'Est. Cost', value: `₹${((sessionTimer / 3600) * 11 * 14).toFixed(0)}`, color: '#FFA500' },
              { icon: Zap, label: 'Power', value: '50 kW DC', color: '#A855F7' },
            ].map(({ icon: Icon, label, value, color }) => (
              <div key={label} className="bg-dark-700/50 rounded-xl p-4 text-center">
                <Icon size={20} className="mx-auto mb-2" style={{ color }} />
                <div className="font-bold text-lg" style={{ color }}>{value}</div>
                <div className="text-xs text-white/40 mt-0.5">{label}</div>
              </div>
            ))}
          </div>

          {/* Battery progress */}
          <div className="mb-5">
            <div className="flex justify-between text-sm text-white/50 mb-2">
              <span>Charging progress (estimated)</span>
              <span>{Math.min(95, Math.floor((sessionTimer / 3600) * 25 + 10))}%</span>
            </div>
            <div className="h-3 bg-dark-600 rounded-full overflow-hidden">
              <motion.div
                className="h-full rounded-full"
                style={{ background: 'linear-gradient(90deg, #00D1FF, #39FF14)' }}
                animate={{ width: `${Math.min(95, Math.floor((sessionTimer / 3600) * 25 + 10))}%` }}
                transition={{ duration: 0.5 }}
              />
            </div>
          </div>

          <button
            id="stop-session-btn"
            onClick={stopSession}
            disabled={stoppingSession}
            className="btn-danger w-full justify-center py-3"
          >
            {stoppingSession ? (
              <span className="flex items-center gap-2">
                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                Stopping...
              </span>
            ) : (
              <><Square size={16} /> Stop Charging</>
            )}
          </button>
        </motion.div>
      )}

      {/* Stats Grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          {
            icon: Zap, label: 'Total Energy', value: `${totalKwh.toFixed(1)} kWh`,
            sub: `${completedSessions} sessions`, color: '#00D1FF',
            bg: 'rgba(0,209,255,0.08)'
          },
          {
            icon: IndianRupee, label: 'Total Spent', value: `₹${totalSpend.toFixed(0)}`,
            sub: 'All time', color: '#39FF14',
            bg: 'rgba(57,255,20,0.08)'
          },
          {
            icon: CheckCircle, label: 'Sessions', value: completedSessions.toString(),
            sub: 'Completed', color: '#A855F7',
            bg: 'rgba(168,85,247,0.08)'
          },
          {
            icon: Battery, label: 'Wallet Balance', value: `₹${wallet?.balance?.toFixed(0) ?? '0'}`,
            sub: 'Prepaid wallet', color: '#FFA500',
            bg: 'rgba(255,165,0,0.08)'
          },
        ].map(stat => {
          const Icon = stat.icon
          return (
            <div key={stat.label} className="stat-card">
              <div className="flex items-center justify-between mb-3">
                <div className="w-9 h-9 rounded-xl flex items-center justify-center"
                     style={{ background: stat.bg, border: `1px solid ${stat.color}30` }}>
                  <Icon size={18} style={{ color: stat.color }} />
                </div>
              </div>
              <div className="font-display font-bold text-2xl" style={{ color: stat.color }}>{stat.value}</div>
              <div className="text-sm font-medium text-white/80 mt-0.5">{stat.label}</div>
              <div className="text-xs text-white/40 mt-0.5">{stat.sub}</div>
            </div>
          )
        })}
      </div>

      {/* Charts Row */}
      <div className="grid lg:grid-cols-2 gap-6">
        {/* Energy Chart */}
        <div className="glass-card p-6">
          <h3 className="font-display font-semibold mb-4">Weekly Energy Usage (kWh)</h3>
          <ResponsiveContainer width="100%" height={200}>
            <AreaChart data={weeklyData}>
              <defs>
                <linearGradient id="kwhGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#00D1FF" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="#00D1FF" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
              <XAxis dataKey="day" stroke="rgba(255,255,255,0.3)" tick={{ fontSize: 12 }} />
              <YAxis stroke="rgba(255,255,255,0.3)" tick={{ fontSize: 12 }} />
              <Tooltip contentStyle={tooltipStyle} />
              <Area type="monotone" dataKey="kwh" stroke="#00D1FF" fill="url(#kwhGrad)"
                    strokeWidth={2} dot={{ fill: '#00D1FF', r: 3 }} />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        {/* Cost Chart */}
        <div className="glass-card p-6">
          <h3 className="font-display font-semibold mb-4">Weekly Spend (₹)</h3>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={weeklyData}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
              <XAxis dataKey="day" stroke="rgba(255,255,255,0.3)" tick={{ fontSize: 12 }} />
              <YAxis stroke="rgba(255,255,255,0.3)" tick={{ fontSize: 12 }} />
              <Tooltip contentStyle={tooltipStyle} />
              <Bar dataKey="cost" fill="#39FF14" opacity={0.8} radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Recent Sessions */}
      <div className="glass-card p-6">
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-display font-semibold">Recent Sessions</h3>
          <a href="/payments" className="text-primary-400 hover:text-primary-300 text-sm">View all →</a>
        </div>
        {history.length === 0 ? (
          <div className="text-center py-12 text-white/30">
            <Zap size={40} className="mx-auto mb-3 opacity-30" />
            <p>No charging sessions yet.</p>
            <a href="/map" className="btn-primary inline-flex mt-4">Find a Station</a>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Transaction</th>
                  <th>Status</th>
                  <th>Started</th>
                  <th>Energy</th>
                  <th>Amount</th>
                </tr>
              </thead>
              <tbody>
                {history.map(s => (
                  <tr key={s.id}>
                    <td className="font-mono text-xs text-primary-300">{s.transactionId}</td>
                    <td>
                      <span className={
                        s.status === 'ACTIVE' ? 'badge-available'
                        : s.status.includes('STOPPED') || s.status === 'COMPLETED' ? 'text-green-400 text-xs'
                        : 'badge-offline text-xs'
                      }>
                        {s.status}
                      </span>
                    </td>
                    <td>{new Date(s.startedAt).toLocaleDateString('en-IN', {
                      day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
                    })}</td>
                    <td className="text-primary-300">{s.energyKwh ? `${s.energyKwh.toFixed(2)} kWh` : '—'}</td>
                    <td className="text-ev-charge font-medium">
                      {s.totalAmount ? `₹${s.totalAmount.toFixed(2)}` : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
