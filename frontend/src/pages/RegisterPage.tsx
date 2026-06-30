import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Mail, Lock, User, Phone, Eye, EyeOff, AlertCircle, CheckCircle } from 'lucide-react'
import { authApi } from '../services/api'
import { useAuthStore } from '../store/authStore'
import toast from 'react-hot-toast'

export default function RegisterPage() {
  const [form, setForm] = useState({ fullName: '', email: '', phone: '', password: '', confirm: '' })
  const [showPass, setShowPass] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()
  const { login } = useAuthStore()

  const update = (field: string) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm(f => ({ ...f, [field]: e.target.value }))

  const passwordStrength = () => {
    const p = form.password
    if (!p) return null
    if (p.length < 6)  return { label: 'Weak',   color: '#FF3B3B', width: '25%' }
    if (p.length < 10) return { label: 'Fair',   color: '#FFA500', width: '60%' }
    return               { label: 'Strong', color: '#39FF14', width: '100%' }
  }
  const strength = passwordStrength()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')

    // Client-side validation
    if (!form.fullName.trim()) { setError('Full name is required'); return }
    if (!form.email.trim())    { setError('Email address is required'); return }
    if (!form.password)        { setError('Password is required'); return }
    if (form.password.length < 8) { setError('Password must be at least 8 characters'); return }
    if (form.password !== form.confirm) { setError('Passwords do not match'); return }

    setLoading(true)
    try {
      const res = await authApi.register({
        email: form.email.trim(),
        fullName: form.fullName.trim(),
        password: form.password,
        phone: form.phone.trim() || undefined,
      })

      const { accessToken, refreshToken, id, email, fullName, role, userId, user } = res.data
      if (accessToken) {
        login(
          {
            id: id || userId || user?.id,
            email: email || user?.email || form.email,
            fullName: fullName || user?.fullName || form.fullName,
            role: role || user?.role || 'DRIVER',
            phone: form.phone || user?.phone
          },
          accessToken,
          refreshToken
        )
      }
      toast.success('Account created! Welcome to EV Roaming Hub')
      navigate('/dashboard')
    } catch (err: any) {
      console.error('Registration error:', err)
      setError(err.response?.data?.detail || 'Registration failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center px-4 py-10 bg-dark-900">
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 right-1/4 w-[500px] h-[400px] opacity-8 rounded-full"
             style={{ background: 'radial-gradient(ellipse, #39FF14 0%, transparent 70%)' }} />
      </div>

      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-md relative z-10"
      >
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl mb-4">
            <img src="/logo.png" alt="EV Roaming Hub Logo" className="w-16 h-16 object-contain rounded-2xl" />
          </div>
          <h1 className="font-display font-bold text-3xl">Create Account</h1>
          <p className="text-white/50 mt-1">Join India's unified EV charging network</p>
        </div>

        <div className="glass-card p-8">
          {error && (
            <div className="flex items-center gap-2 mb-5 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-400 text-sm">
              <AlertCircle size={16} className="flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Full Name */}
            <div>
              <label className="block text-sm font-medium text-white/70 mb-2">Full Name *</label>
              <div className="relative">
                <User size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-white/30" />
                <input
                  id="reg-name"
                  type="text"
                  value={form.fullName}
                  onChange={update('fullName')}
                  placeholder="Priya Sharma"
                  className="input-field pl-10"
                  autoComplete="name"
                />
              </div>
            </div>

            {/* Email */}
            <div>
              <label className="block text-sm font-medium text-white/70 mb-2">Email Address *</label>
              <div className="relative">
                <Mail size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-white/30" />
                <input
                  id="reg-email"
                  type="email"
                  value={form.email}
                  onChange={update('email')}
                  placeholder="priya@example.com"
                  className="input-field pl-10"
                  autoComplete="email"
                />
              </div>
            </div>

            {/* Phone */}
            <div>
              <label className="block text-sm font-medium text-white/70 mb-2">Phone Number <span className="text-white/30">(optional)</span></label>
              <div className="relative">
                <Phone size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-white/30" />
                <input
                  id="reg-phone"
                  type="tel"
                  value={form.phone}
                  onChange={update('phone')}
                  placeholder="+91 98765 43210"
                  className="input-field pl-10"
                  autoComplete="tel"
                />
              </div>
            </div>

            {/* Password */}
            <div>
              <label className="block text-sm font-medium text-white/70 mb-2">Password *</label>
              <div className="relative">
                <Lock size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-white/30" />
                <input
                  id="reg-password"
                  type={showPass ? 'text' : 'password'}
                  value={form.password}
                  onChange={update('password')}
                  placeholder="Min 8 characters"
                  className="input-field pl-10 pr-10"
                  autoComplete="new-password"
                />
                <button
                  type="button"
                  onClick={() => setShowPass(!showPass)}
                  className="absolute right-3.5 top-1/2 -translate-y-1/2 text-white/30 hover:text-white/60"
                  tabIndex={-1}
                >
                  {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              {/* Fixed-height strength bar */}
              <div className="mt-2 h-6">
                {strength ? (
                  <>
                    <div className="flex justify-between text-xs mb-1">
                      <span className="text-white/40">Password strength</span>
                      <span style={{ color: strength.color }}>{strength.label}</span>
                    </div>
                    <div className="h-1.5 bg-dark-600 rounded-full overflow-hidden">
                      <div
                        className="h-full rounded-full transition-all duration-300"
                        style={{ width: strength.width, background: strength.color }}
                      />
                    </div>
                  </>
                ) : (
                  <div className="h-1.5 bg-dark-600/30 rounded-full" />
                )}
              </div>
            </div>

            {/* Confirm Password */}
            <div>
              <label className="block text-sm font-medium text-white/70 mb-2">Confirm Password *</label>
              <div className="relative">
                <Lock size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-white/30" />
                <input
                  id="reg-confirm"
                  type="password"
                  value={form.confirm}
                  onChange={update('confirm')}
                  placeholder="Repeat your password"
                  className="input-field pl-10 pr-10"
                  autoComplete="new-password"
                />
                {form.confirm && form.confirm.length > 0 && (
                  form.password === form.confirm ? (
                    <CheckCircle size={16} className="absolute right-3.5 top-1/2 -translate-y-1/2 text-green-400" />
                  ) : (
                    <AlertCircle size={16} className="absolute right-3.5 top-1/2 -translate-y-1/2 text-red-400" />
                  )
                )}
              </div>
            </div>

            {/* Submit */}
            <button
              id="reg-submit"
              type="submit"
              disabled={loading}
              className="btn-primary w-full justify-center py-3.5 text-base mt-2"
            >
              {loading ? (
                <span className="flex items-center gap-2">
                  <span className="w-4 h-4 border-2 border-dark-900/30 border-t-dark-900 rounded-full animate-spin" />
                  Creating account...
                </span>
              ) : (
                <>Create Account</>
              )}
            </button>
          </form>

          <p className="text-center text-white/40 text-sm mt-5">
            Already have an account?{' '}
            <Link to="/login" className="text-primary-400 hover:text-primary-300 font-medium">Sign in</Link>
          </p>
        </div>
      </motion.div>
    </div>
  )
}
