import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { type AxiosResponse } from 'axios'
import { MapPin, Phone, Clock, Zap, Wifi, ArrowLeft, Battery } from 'lucide-react'
import { stationApi, sessionApi } from '../services/api'
import { useAuthStore } from '../store/authStore'
import toast from 'react-hot-toast'

// Fallback station data
const FALLBACK: Record<string, any> = {
  's003': {
    id: 's003', name: 'Ather Grid — Koramangala', address: '7th Block, Koramangala, Bengaluru',
    city: 'Bengaluru', state: 'Karnataka', pincode: '560095',
    latitude: 12.9352, longitude: 77.6245, status: 'AVAILABLE',
    cpoNetworkName: 'Ather Grid', cpoNetworkCode: 'ATHER', phone: '+91-80-4567-8901',
    connectors: [
      { id: 'c001', evseId: 'ATHER-KRM-1-1', connectorNumber: 1, standard: 'CCS2',
        powerType: 'DC', maxVoltage: 400, maxAmperage: 125, maxElectricPower: 50000,
        status: 'AVAILABLE', lastStatusUpdate: new Date().toISOString() },
      { id: 'c002', evseId: 'ATHER-KRM-1-2', connectorNumber: 2, standard: 'TYPE2',
        powerType: 'AC_3_PHASE', maxVoltage: 230, maxAmperage: 32, maxElectricPower: 22000,
        status: 'AVAILABLE', lastStatusUpdate: new Date().toISOString() },
    ]
  }
}

const connectorColors: Record<string, string> = {
  CCS2: '#00D1FF', TYPE2: '#A855F7', CHADEMO: '#FFA500', BHARAT_AC: '#39FF14'
}

export default function StationDetailPage() {
  const { stationId } = useParams<{ stationId: string }>()
  const navigate = useNavigate()
  const { isAuthenticated } = useAuthStore()
  const [station, setStation] = useState<any>(null)
  const [loading, setLoading] = useState(true)
  const [starting, setStarting] = useState<string | null>(null)

  useEffect(() => {
    stationApi.getStation(stationId!)
      .then((r: AxiosResponse) => setStation(r.data))
      .catch(() => setStation(FALLBACK[stationId!] || FALLBACK['s003']))
      .finally(() => setLoading(false))
  }, [stationId])

  const startCharging = async (connector: any) => {
    if (!isAuthenticated) { navigate('/login'); return }
    if (connector.status !== 'AVAILABLE') { toast.error('Connector not available'); return }
    setStarting(connector.id)
    try {
      await sessionApi.startSession({
        connectorId: connector.id,
        stationId: station.id,
        authMethod: 'APP',
      })
      toast.success('⚡ Charging session started!')
      navigate('/dashboard')
    } catch (err: any) {
      const msg = err.response?.data?.detail || 'Failed to start session'
      if (msg.includes('active session')) toast.error('You already have an active session!')
      else toast.error(msg)
    }
    setStarting(null)
  }

  if (loading) {
    return (
      <div className="p-6 space-y-4 max-w-4xl mx-auto">
        {[...Array(5)].map((_, i) => (
          <div key={i} className="skeleton h-20 rounded-2xl" />
        ))}
      </div>
    )
  }

  if (!station) return null

  return (
    <div className="p-6 max-w-4xl mx-auto animate-fade-in">
      {/* Back */}
      <button onClick={() => navigate(-1)} className="btn-ghost mb-6 gap-2">
        <ArrowLeft size={16} /> Back to Map
      </button>

      {/* Header Card */}
      <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}
                  className="glass-card p-6 mb-6">
        <div className="flex items-start justify-between gap-4 flex-wrap">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <div className="w-3 h-3 rounded-full"
                   style={{ background: '#00D1FF', boxShadow: '0 0 8px #00D1FF' }} />
              <span className="text-sm text-white/50">{station.cpoNetworkName}</span>
            </div>
            <h1 className="font-display font-bold text-2xl mb-1">{station.name}</h1>
            <p className="text-white/50 flex items-center gap-1.5">
              <MapPin size={14} />
              {station.address}, {station.city} — {station.pincode}
            </p>
          </div>
          <span className={
            station.status === 'AVAILABLE' ? 'badge-available text-base px-4 py-2'
            : station.status === 'BUSY' ? 'badge-busy text-base px-4 py-2'
            : 'badge-offline text-base px-4 py-2'
          }>
            {station.status}
          </span>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-3 gap-4 mt-6">
          {station.phone && (
            <div className="flex items-center gap-2 text-sm text-white/60">
              <Phone size={14} /> {station.phone}
            </div>
          )}
          <div className="flex items-center gap-2 text-sm text-white/60">
            <Clock size={14} /> 24/7 Available
          </div>
          <div className="flex items-center gap-2 text-sm text-white/60">
            <Wifi size={14} /> OCPP 2.0.1
          </div>
        </div>
      </motion.div>

      {/* Connectors */}
      <h2 className="font-display font-semibold text-xl mb-4">Charging Connectors</h2>
      <div className="grid sm:grid-cols-2 gap-4">
        {(station.connectors || []).map((c: any, i: number) => {
          const color = connectorColors[c.standard] || '#00D1FF'
          const isAvail = c.status === 'AVAILABLE'
          return (
            <motion.div
              key={c.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: i * 0.1 }}
              className="glass-card p-5"
            >
              <div className="flex items-start justify-between gap-2 mb-4">
                <div>
                  <div className={`text-lg font-bold font-mono mb-1
                                  ${c.standard === 'CCS2' ? 'connector-ccs2'
                                    : c.standard === 'TYPE2' ? 'connector-type2'
                                    : c.standard === 'CHADEMO' ? 'connector-chademo'
                                    : 'connector-bharat'}`}>
                    {c.standard}
                  </div>
                  <p className="text-xs text-white/40">EVSE: {c.evseId}</p>
                </div>
                <span className={isAvail ? 'badge-available' : c.status === 'OCCUPIED' ? 'badge-busy' : 'badge-offline'}>
                  {c.status}
                </span>
              </div>

              <div className="grid grid-cols-3 gap-3 mb-4">
                <div className="text-center">
                  <div className="font-bold text-lg" style={{ color }}>{c.maxVoltage}V</div>
                  <div className="text-xs text-white/40">Voltage</div>
                </div>
                <div className="text-center">
                  <div className="font-bold text-lg" style={{ color }}>{c.maxAmperage}A</div>
                  <div className="text-xs text-white/40">Amperage</div>
                </div>
                <div className="text-center">
                  <div className="font-bold text-lg" style={{ color }}>
                    {c.maxElectricPower >= 1000
                      ? `${(c.maxElectricPower / 1000).toFixed(0)} kW`
                      : `${c.maxElectricPower} W`}
                  </div>
                  <div className="text-xs text-white/40">Max Power</div>
                </div>
              </div>

              <div className="flex items-center justify-between mb-3 text-xs text-white/30">
                <span>{c.powerType?.replace('_', ' ')}</span>
                <span>Connector #{c.connectorNumber}</span>
              </div>

              {isAvail ? (
                <button
                  id={`start-charge-${c.id}`}
                  onClick={() => startCharging(c)}
                  disabled={starting === c.id}
                  className="btn-charge w-full justify-center py-3 text-sm"
                >
                  {starting === c.id ? (
                    <span className="flex items-center gap-2">
                      <span className="w-4 h-4 border-2 border-dark-900/30 border-t-dark-900 rounded-full animate-spin" />
                      Starting...
                    </span>
                  ) : (
                    <><Zap size={16} /> Start Charging</>
                  )}
                </button>
              ) : (
                <button disabled className="w-full py-3 rounded-xl bg-dark-600 text-white/30 text-sm cursor-not-allowed">
                  {c.status === 'OCCUPIED' ? '⚡ In Use' : '× Not Available'}
                </button>
              )}
            </motion.div>
          )
        })}
      </div>

      {/* Tariff info placeholder */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.4 }}
        className="glass-card p-5 mt-6"
      >
        <h3 className="font-display font-semibold mb-3 flex items-center gap-2">
          <Battery size={18} className="text-primary-400" /> Pricing
        </h3>
        <div className="grid sm:grid-cols-3 gap-4 text-center">
          {[
            { label: 'Energy', value: '₹14/kWh (DC)' },
            { label: 'Idle Fee',  value: '₹2/min (>15 min)' },
            { label: 'GST',  value: '18% (included)' },
          ].map(p => (
            <div key={p.label} className="bg-dark-700/50 rounded-xl p-3">
              <div className="text-ev-charge font-bold">{p.value}</div>
              <div className="text-xs text-white/40 mt-0.5">{p.label}</div>
            </div>
          ))}
        </div>
      </motion.div>
    </div>
  )
}
