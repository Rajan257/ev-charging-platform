import { useState } from 'react'
import { useAuthStore } from '../store/authStore'
import { User, Phone, Mail, Shield, Car, CreditCard } from 'lucide-react'
import { motion } from 'framer-motion'

export default function ProfilePage() {
  const { user } = useAuthStore()
  const [editing, setEditing] = useState(false)
  const [form, setForm] = useState({ fullName: user?.fullName || '', phone: user?.phone || '' })

  return (
    <div className="p-6 max-w-3xl mx-auto animate-fade-in">
      <h1 className="font-display font-bold text-2xl mb-6">Profile</h1>

      <motion.div initial={{ opacity:0, y:20 }} animate={{ opacity:1, y:0 }} className="glass-card p-6 mb-6">
        <div className="flex items-center gap-4 mb-6">
          <div className="w-16 h-16 rounded-2xl flex items-center justify-center font-display font-bold text-2xl flex-shrink-0"
               style={{ background: 'linear-gradient(135deg, #00D1FF, #39FF14)', color: '#0A0F1E' }}>
            {user?.fullName?.charAt(0) ?? 'U'}
          </div>
          <div>
            <h2 className="font-display font-bold text-xl">{user?.fullName}</h2>
            <p className="text-white/50 text-sm">{user?.email}</p>
            <span className="text-xs px-2 py-0.5 rounded-full mt-1 inline-block"
                  style={{ background: 'rgba(0,209,255,0.1)', color: '#00D1FF', border: '1px solid rgba(0,209,255,0.3)' }}>
              {user?.role?.replace('_', ' ')}
            </span>
          </div>
        </div>

        <div className="grid sm:grid-cols-2 gap-4">
          {[
            { icon: Mail, label: 'Email', value: user?.email },
            { icon: Phone, label: 'Phone', value: user?.phone || '+91 — Not set' },
            { icon: Shield, label: 'Role', value: user?.role?.replace('_', ' ') },
            { icon: User, label: 'Account ID', value: user?.id?.substring(0, 16) + '...' },
          ].map(({ icon: Icon, label, value }) => (
            <div key={label} className="flex items-center gap-3 p-3 rounded-xl bg-dark-700/50">
              <Icon size={16} className="text-white/40 flex-shrink-0" />
              <div className="min-w-0">
                <div className="text-xs text-white/40">{label}</div>
                <div className="text-sm font-medium truncate">{value}</div>
              </div>
            </div>
          ))}
        </div>
      </motion.div>

      {/* RFID Cards placeholder */}
      <div className="glass-card p-5 mb-4">
        <h3 className="font-display font-semibold mb-3 flex items-center gap-2">
          <CreditCard size={18} className="text-primary-400"/> RFID Cards
        </h3>
        <p className="text-white/40 text-sm">No RFID cards registered. Add one to charge without the app.</p>
        <button className="btn-primary mt-3 text-sm py-2">+ Add RFID Card</button>
      </div>

      {/* Vehicles placeholder */}
      <div className="glass-card p-5">
        <h3 className="font-display font-semibold mb-3 flex items-center gap-2">
          <Car size={18} className="text-primary-400"/> My Vehicles
        </h3>
        <p className="text-white/40 text-sm">No vehicles added. Register your EV for Plug & Charge (ISO 15118).</p>
        <button className="btn-primary mt-3 text-sm py-2">+ Add Vehicle</button>
      </div>
    </div>
  )
}
