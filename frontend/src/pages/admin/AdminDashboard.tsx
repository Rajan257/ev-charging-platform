import { Routes, Route } from 'react-router-dom'
import { motion } from 'framer-motion'
import { LayoutDashboard, Zap, Globe, Users, TrendingUp } from 'lucide-react'

function AdminHome() {
  return (
    <div className="p-6 animate-fade-in">
      <h1 className="font-display font-bold text-2xl mb-6">Admin Panel</h1>
      <div className="grid md:grid-cols-3 gap-4">
        {[
          { icon: Zap, label: 'Total Stations', value: '127', color: '#00D1FF' },
          { icon: Users, label: 'Registered Users', value: '4,832', color: '#39FF14' },
          { icon: Globe, label: 'CPO Networks', value: '5', color: '#A855F7' },
          { icon: TrendingUp, label: 'Revenue (MTD)', value: '₹2.4L', color: '#FFA500' },
          { icon: LayoutDashboard, label: 'Active Sessions', value: '23', color: '#FF6B6B' },
          { icon: Zap, label: 'Energy (MTD)', value: '12,450 kWh', color: '#00D1FF' },
        ].map(({ icon: Icon, label, value, color }) => (
          <motion.div key={label} initial={{ opacity:0, y:20 }} animate={{ opacity:1, y:0 }}
                      className="glass-card p-5">
            <div className="w-9 h-9 rounded-xl flex items-center justify-center mb-3"
                 style={{ background: `${color}20`, border: `1px solid ${color}30` }}>
              <Icon size={18} style={{ color }} />
            </div>
            <div className="font-display font-bold text-2xl" style={{ color }}>{value}</div>
            <div className="text-white/50 text-sm mt-0.5">{label}</div>
          </motion.div>
        ))}
      </div>
    </div>
  )
}

export default function AdminDashboard() {
  return (
    <Routes>
      <Route index element={<AdminHome />} />
    </Routes>
  )
}
