import { Link, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { MapPin, Shield, Globe, ChevronRight, Battery, Wifi, CreditCard, Network } from 'lucide-react'

const features = [
  {
    icon: Network,
    title: 'Multi-Network Access',
    description: 'Charge at Tata Power, Ather Grid, BPCL Pulse, ChargeZone, and 50+ networks with one account.',
    color: '#00D1FF',
  },
  {
    icon: MapPin,
    title: 'Live Station Map',
    description: 'Find charging stations near you with real-time availability and connector details.',
    color: '#39FF14',
  },
  {
    icon: Shield,
    title: 'RFID & PnC Auth',
    description: 'Authenticate via RFID card, mobile app, or ISO 15118 Plug & Charge for seamless access.',
    color: '#A855F7',
  },
  {
    icon: Globe,
    title: 'OCPI Roaming',
    description: 'International roaming via OCPI 2.2.1. Charge across networks like Europe\'s e-roaming.',
    color: '#FF6B6B',
  },
  {
    icon: CreditCard,
    title: 'UPI Payments',
    description: 'Pay via UPI, prepaid wallet, or Razorpay. Instant invoice with GST breakup.',
    color: '#FFA500',
  },
  {
    icon: Battery,
    title: 'Live Session Tracking',
    description: 'Monitor energy, cost, and SoC in real-time during your charging session.',
    color: '#00D1FF',
  },
]

const stats = [
  { value: '500+', label: 'Charging Stations' },
  { value: '5', label: 'CPO Networks' },
  { value: '13', label: 'Indian Cities' },
  { value: '50 kW', label: 'Max DC Power' },
]

export default function LandingPage() {
  const navigate = useNavigate()

  return (
    <div className="min-h-screen bg-dark-900 overflow-hidden">
      {/* Navigation */}
      <nav className="fixed top-0 left-0 right-0 z-50 px-6 py-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-2">
            <img src="/logo.png" alt="EV Roaming Hub Logo" className="w-8 h-8 object-contain rounded-lg flex-shrink-0" />
            <span className="font-display font-bold text-lg text-gradient">EV Roaming Hub</span>
          </div>
          <div className="flex items-center gap-3">
            <Link to="/login" className="btn-ghost text-sm">Sign In</Link>
            <button onClick={() => navigate('/register')} className="btn-primary text-sm">
              Get Started <ChevronRight size={16} />
            </button>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="relative pt-32 pb-20 px-6">
        {/* Background glow */}
        <div className="absolute inset-0 overflow-hidden pointer-events-none">
          <div className="absolute top-20 left-1/2 -translate-x-1/2 w-[800px] h-[400px] rounded-full opacity-10"
               style={{ background: 'radial-gradient(ellipse, #00D1FF 0%, transparent 70%)' }} />
          <div className="absolute top-40 left-1/4 w-[300px] h-[300px] rounded-full opacity-5"
               style={{ background: 'radial-gradient(ellipse, #39FF14 0%, transparent 70%)' }} />
        </div>

        <div className="max-w-5xl mx-auto text-center relative z-10">
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6 }}
          >
            {/* Badge */}
            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full border border-primary-400/30
                          bg-primary-400/10 text-primary-300 text-sm font-medium mb-8">
              <span className="live-dot" />
              India's First Unified EV Roaming Platform
            </div>

            {/* Headline */}
            <h1 className="font-display font-extrabold text-6xl md:text-7xl leading-tight mb-6">
              Charge Your EV
              <br />
              <span className="text-gradient">Anywhere in India</span>
            </h1>

            {/* Subheading */}
            <p className="text-white/60 text-xl max-w-2xl mx-auto mb-10 leading-relaxed">
              One account. Any network. Instant payment.
              Connect to Tata Power, Ather Grid, BPCL, ChargeZone and more, seamlessly.
            </p>

            {/* CTA Buttons */}
            <div className="flex items-center justify-center gap-4 flex-wrap">
              <button
                onClick={() => navigate('/register')}
                className="btn-primary py-3.5 px-8 text-base justify-center w-full sm:w-auto"
              >
                Start Charging Free
              </button>
              <button
                onClick={() => navigate('/map')}
                className="btn-ghost px-8 py-4 text-base"
              >
                <MapPin size={18} />
                View Station Map
              </button>
            </div>
          </motion.div>

          {/* Hero visual - animated charging card */}
          <motion.div
            initial={{ opacity: 0, y: 40 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8, delay: 0.3 }}
            className="mt-16 mx-auto max-w-lg"
          >
            <div className="glass-card p-6 neon-border">
              <div className="flex items-center justify-between mb-4">
                <div>
                  <p className="text-white/40 text-xs uppercase tracking-widest mb-1">Live Session</p>
                  <p className="font-display font-bold text-xl">Ather Grid - Koramangala</p>
                </div>
                <span className="badge-available"><span className="live-dot" />Charging</span>
              </div>
              <div className="grid grid-cols-3 gap-4">
                {[
                  { label: 'Energy', value: '12.4 kWh', icon: '🔌' },
                  { label: 'Cost', value: '₹173.60', icon: '💳' },
                  { label: 'SoC', value: '78%', icon: '🔋' },
                ].map((stat) => (
                  <div key={stat.label} className="bg-dark-700/60 rounded-xl p-3 text-center">
                    <div className="text-2xl mb-1">{stat.icon}</div>
                    <div className="font-bold text-primary-300">{stat.value}</div>
                    <div className="text-white/40 text-xs mt-0.5">{stat.label}</div>
                  </div>
                ))}
              </div>
              {/* Progress bar */}
              <div className="mt-4">
                <div className="flex justify-between text-xs text-white/40 mb-1.5">
                  <span>Charging progress</span><span>78%</span>
                </div>
                <div className="h-2 bg-dark-600 rounded-full overflow-hidden">
                  <motion.div
                    className="h-full rounded-full"
                    style={{ background: 'linear-gradient(90deg, #00D1FF, #39FF14)' }}
                    initial={{ width: '0%' }}
                    animate={{ width: '78%' }}
                    transition={{ duration: 2, delay: 0.8, ease: 'easeOut' }}
                  />
                </div>
              </div>
            </div>
          </motion.div>
        </div>
      </section>

      {/* Stats Bar */}
      <section className="py-12 border-y border-white/5">
        <div className="max-w-5xl mx-auto px-6">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
            {stats.map((stat, i) => (
              <motion.div
                key={stat.label}
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.1 * i }}
                className="text-center"
              >
                <div className="font-display font-extrabold text-4xl text-gradient">{stat.value}</div>
                <div className="text-white/50 text-sm mt-1">{stat.label}</div>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Features Grid */}
      <section className="py-20 px-6">
        <div className="max-w-6xl mx-auto">
          <div className="text-center mb-14">
            <h2 className="font-display font-bold text-4xl mb-4">
              Everything you need to <span className="text-gradient">charge seamlessly</span>
            </h2>
            <p className="text-white/50 text-lg">Built on OCPP 2.0.1, OCPI 2.2.1, and ISO 15118 standards</p>
          </div>
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {features.map((feature, i) => {
              const Icon = feature.icon
              return (
                <motion.div
                  key={feature.title}
                  initial={{ opacity: 0, y: 30 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.1 * i }}
                  className="glass-card p-6 hover:scale-[1.02] transition-transform duration-300 cursor-default"
                >
                  <div className="w-12 h-12 rounded-xl flex items-center justify-center mb-4"
                       style={{ background: `${feature.color}20`, border: `1px solid ${feature.color}30` }}>
                    <Icon size={22} style={{ color: feature.color }} />
                  </div>
                  <h3 className="font-display font-semibold text-lg mb-2">{feature.title}</h3>
                  <p className="text-white/50 text-sm leading-relaxed">{feature.description}</p>
                </motion.div>
              )
            })}
          </div>
        </div>
      </section>

      {/* Protocol Support */}
      <section className="py-16 px-6">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="font-display font-bold text-3xl mb-3">Industry Standard Protocols</h2>
          <p className="text-white/50 mb-10">Built for interoperability from day one</p>
          <div className="flex flex-wrap justify-center gap-4">
            {['OCPP 2.0.1', 'OCPI 2.2.1', 'ISO 15118', 'OAuth2 / OIDC', 'UPI / Razorpay', 'GST Compliant'].map((p) => (
              <div key={p} className="px-5 py-2.5 rounded-xl glass-card font-mono text-sm text-primary-300 font-medium">
                {p}
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 px-6">
        <div className="max-w-2xl mx-auto text-center glass-card p-12 neon-border">
          <h2 className="font-display font-extrabold text-4xl mb-4">
            Ready to <span className="text-gradient">go electric?</span>
          </h2>
          <p className="text-white/60 mb-8">Join thousands of EV drivers on India's unified charging network.</p>
          <button
            onClick={() => navigate('/register')}
            className="btn-charge py-3.5 px-8 text-base justify-center w-full sm:w-auto"
          >
            Create Free Account
          </button>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-white/5 py-10 px-6">
        <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <img src="/logo.png" alt="EV Roaming Hub Logo" className="w-6 h-6 object-contain rounded flex-shrink-0" />
            <span className="font-display font-bold text-sm text-gradient">EV Roaming Hub India</span>
          </div>
          <div className="flex gap-6 text-white/40 text-sm">
            <span>OCPP 2.0.1 Certified</span>
            <span>OCPI 2.2.1 Ready</span>
            <span>GST Compliant</span>
          </div>
          <p className="text-white/30 text-xs">© 2026 EV Roaming Hub India.</p>
        </div>
      </footer>
    </div>
  )
}
