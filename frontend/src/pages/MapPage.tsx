import { useEffect, useState } from 'react'
import { type AxiosResponse } from 'axios'
import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet'
import L from 'leaflet'
import { motion, AnimatePresence } from 'framer-motion'
import { Search, MapPin, Filter, Zap, Navigation, X, Wifi } from 'lucide-react'
import { stationApi } from '../services/api'
import { useNavigate } from 'react-router-dom'
import 'leaflet/dist/leaflet.css'

// Sample stations fallback for when API is not running
const SAMPLE_STATIONS = [
  { id: 's001', name: 'Tata Power - Connaught Place', city: 'New Delhi', state: 'Delhi',
    latitude: 28.6315, longitude: 77.2167, status: 'AVAILABLE', cpoNetworkCode: 'TATA',
    cpoNetworkName: 'Tata Power EV', totalConnectors: 2, availableConnectors: 2 },
  { id: 's002', name: 'Tata Power - Cyber Hub', city: 'Gurugram', state: 'Haryana',
    latitude: 28.4949, longitude: 77.0896, status: 'AVAILABLE', cpoNetworkCode: 'TATA',
    cpoNetworkName: 'Tata Power EV', totalConnectors: 2, availableConnectors: 1 },
  { id: 's003', name: 'Ather Grid - Koramangala', city: 'Bengaluru', state: 'Karnataka',
    latitude: 12.9352, longitude: 77.6245, status: 'AVAILABLE', cpoNetworkCode: 'ATHER',
    cpoNetworkName: 'Ather Grid', totalConnectors: 2, availableConnectors: 2 },
  { id: 's004', name: 'Ather Grid - Indiranagar', city: 'Bengaluru', state: 'Karnataka',
    latitude: 12.9784, longitude: 77.6408, status: 'BUSY', cpoNetworkCode: 'ATHER',
    cpoNetworkName: 'Ather Grid', totalConnectors: 2, availableConnectors: 0 },
  { id: 's005', name: 'BPCL Pulse - Bandra', city: 'Mumbai', state: 'Maharashtra',
    latitude: 19.0544, longitude: 72.8405, status: 'AVAILABLE', cpoNetworkCode: 'BPCL',
    cpoNetworkName: 'BPCL Pulse', totalConnectors: 1, availableConnectors: 1 },
  { id: 's006', name: 'BPCL Pulse - Powai', city: 'Mumbai', state: 'Maharashtra',
    latitude: 19.1176, longitude: 72.9060, status: 'AVAILABLE', cpoNetworkCode: 'BPCL',
    cpoNetworkName: 'BPCL Pulse', totalConnectors: 1, availableConnectors: 1 },
  { id: 's007', name: 'ChargeZone - Kalyani Nagar', city: 'Pune', state: 'Maharashtra',
    latitude: 18.5462, longitude: 73.9013, status: 'AVAILABLE', cpoNetworkCode: 'CHGZ',
    cpoNetworkName: 'ChargeZone', totalConnectors: 1, availableConnectors: 1 },
  { id: 's008', name: 'Fortum - Anna Salai', city: 'Chennai', state: 'Tamil Nadu',
    latitude: 13.0604, longitude: 80.2596, status: 'AVAILABLE', cpoNetworkCode: 'FORT',
    cpoNetworkName: 'Fortum India', totalConnectors: 1, availableConnectors: 1 },
  { id: 's009', name: 'Tata Power - Salt Lake', city: 'Kolkata', state: 'West Bengal',
    latitude: 22.5726, longitude: 88.4312, status: 'MAINTENANCE', cpoNetworkCode: 'TATA',
    cpoNetworkName: 'Tata Power EV', totalConnectors: 1, availableConnectors: 0 },
  { id: 's010', name: 'ChargeZone - Gomti Nagar', city: 'Lucknow', state: 'Uttar Pradesh',
    latitude: 26.8617, longitude: 81.0229, status: 'AVAILABLE', cpoNetworkCode: 'CHGZ',
    cpoNetworkName: 'ChargeZone', totalConnectors: 1, availableConnectors: 1 },
]

const networkColors: Record<string, string> = {
  TATA: '#00D1FF', ATHER: '#39FF14', BPCL: '#FFA500',
  CHGZ: '#A855F7', FORT: '#FF6B6B',
}

function createStationIcon(status: string, code: string) {
  const color = status === 'AVAILABLE' ? (networkColors[code] || '#00D1FF')
              : status === 'BUSY'        ? '#FFA500'
              : status === 'MAINTENANCE' ? '#A855F7'
              : '#6B7280'

  return L.divIcon({
    className: 'ev-marker',
    html: `<div style="
      width: 36px; height: 36px; border-radius: 50%;
      background: ${color}25; border: 2px solid ${color};
      display: flex; align-items: center; justify-content: center;
      font-size: 16px; cursor: pointer;
      box-shadow: 0 0 12px ${color}60;
      transition: transform 0.2s;
    " onmouseenter="this.style.transform='scale(1.2)'" onmouseleave="this.style.transform='scale(1)'">
      ⚡
    </div>`,
    iconSize: [36, 36],
    iconAnchor: [18, 18],
  })
}

function MapController({ center }: { center: [number, number] | null }) {
  const map = useMap()
  useEffect(() => {
    if (center) map.flyTo(center, 13, { duration: 1.5 })
  }, [center, map])
  return null
}

export default function MapPage() {
  const [stations, setStations] = useState(SAMPLE_STATIONS)
  const [selected, setSelected] = useState<typeof SAMPLE_STATIONS[0] | null>(null)
  const [search, setSearch] = useState('')
  const [filterCity, setFilterCity] = useState('')
  const [filterStatus, setFilterStatus] = useState('')
  const [mapCenter, setMapCenter] = useState<[number, number] | null>(null)
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  // Try to load from API
  useEffect(() => {
    setLoading(true)
    stationApi.getStations({ size: 100 })
      .then((res: AxiosResponse) => {
        if (res.data?.content?.length > 0) setStations(res.data.content)
      })
      .catch(() => {}) // fallback to sample data
      .finally(() => setLoading(false))
  }, [])

  const cities = [...new Set(stations.map(s => s.city))].sort()

  const filtered = stations.filter(s => {
    const matchSearch = !search || s.name.toLowerCase().includes(search.toLowerCase())
                                || s.city.toLowerCase().includes(search.toLowerCase())
    const matchCity   = !filterCity   || s.city === filterCity
    const matchStatus = !filterStatus || s.status === filterStatus
    return matchSearch && matchCity && matchStatus
  })

  const handleLocate = () => {
    navigator.geolocation?.getCurrentPosition(
      pos => setMapCenter([pos.coords.latitude, pos.coords.longitude]),
      () => {}
    )
  }

  return (
    <div className="flex h-full">
      {/* Left Panel */}
      <div className="w-96 flex flex-col border-r border-white/5 bg-dark-800/30 flex-shrink-0">
        {/* Search & Filters */}
        <div className="p-4 border-b border-white/5 space-y-3">
          <div className="relative">
            <Search size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-white/30" />
            <input
              id="station-search"
              value={search}
              onChange={e => setSearch(e.target.value)}
              placeholder="Search stations or cities..."
              className="input-field pl-10 py-2.5 text-sm"
            />
            {search && (
              <button onClick={() => setSearch('')}
                      className="absolute right-3.5 top-1/2 -translate-y-1/2 text-white/30 hover:text-white">
                <X size={14} />
              </button>
            )}
          </div>
          <div className="flex gap-2">
            <select id="filter-city" value={filterCity} onChange={e => setFilterCity(e.target.value)}
                    className="input-field py-2 text-sm flex-1 cursor-pointer">
              <option value="">All Cities</option>
              {cities.map(c => <option key={c} value={c}>{c}</option>)}
            </select>
            <select id="filter-status" value={filterStatus} onChange={e => setFilterStatus(e.target.value)}
                    className="input-field py-2 text-sm flex-1 cursor-pointer">
              <option value="">All Status</option>
              <option value="AVAILABLE">Available</option>
              <option value="BUSY">Busy</option>
              <option value="MAINTENANCE">Maintenance</option>
              <option value="OFFLINE">Offline</option>
            </select>
          </div>

          <div className="flex items-center justify-between text-xs text-white/40">
            <span>{filtered.length} stations found</span>
            <button onClick={handleLocate}
                    className="flex items-center gap-1 text-primary-400 hover:text-primary-300">
              <Navigation size={12} /> Near me
            </button>
          </div>
        </div>

        {/* Station list */}
        <div className="flex-1 overflow-y-auto p-3 space-y-2">
          <AnimatePresence>
            {filtered.map(station => (
              <motion.div
                key={station.id}
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -20 }}
                onClick={() => {
                  setSelected(station)
                  setMapCenter([station.latitude, station.longitude])
                }}
                className={`p-4 rounded-xl border cursor-pointer transition-all duration-200
                          hover:border-primary-400/30 hover:bg-white/5
                          ${selected?.id === station.id
                            ? 'border-primary-400/50 bg-primary-400/10'
                            : 'border-white/5'}`}
              >
                <div className="flex items-start justify-between gap-2 mb-2">
                  <div className="flex items-center gap-2 min-w-0">
                    <div className="w-2 h-2 rounded-full flex-shrink-0"
                         style={{ background: networkColors[station.cpoNetworkCode] || '#00D1FF',
                                  boxShadow: `0 0 6px ${networkColors[station.cpoNetworkCode] || '#00D1FF'}` }} />
                    <p className="font-medium text-sm leading-tight truncate">{station.name}</p>
                  </div>
                  <span className={
                    station.status === 'AVAILABLE' ? 'badge-available flex-shrink-0'
                    : station.status === 'BUSY'    ? 'badge-busy flex-shrink-0'
                    : station.status === 'MAINTENANCE' ? 'badge-maintenance flex-shrink-0'
                    : 'badge-offline flex-shrink-0'
                  }>
                    {station.status === 'AVAILABLE' ? '●' : station.status === 'BUSY' ? '●' : '●'}
                    {' '}{station.status}
                  </span>
                </div>
                <div className="flex items-center gap-1 text-xs text-white/40 mb-2">
                  <MapPin size={11} /> {station.city}, {station.state}
                </div>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-1 text-xs">
                    <Wifi size={11} className="text-white/40" />
                    <span style={{ color: networkColors[station.cpoNetworkCode] || '#00D1FF' }}
                          className="font-medium">{station.cpoNetworkName}</span>
                  </div>
                  <span className="text-xs text-white/40">
                    {station.availableConnectors}/{station.totalConnectors} avail.
                  </span>
                </div>
              </motion.div>
            ))}
          </AnimatePresence>
        </div>
      </div>

      {/* Map */}
      <div className="flex-1 relative">
        <MapContainer
          center={[20.5937, 78.9629]}
          zoom={5}
          style={{ width: '100%', height: '100%' }}
          className="map-container"
        >
          <TileLayer
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='© OpenStreetMap contributors'
          />
          <MapController center={mapCenter} />
          {filtered.map(station => (
            <Marker
              key={station.id}
              position={[station.latitude, station.longitude]}
              icon={createStationIcon(station.status, station.cpoNetworkCode)}
              eventHandlers={{
                click: () => {
                  setSelected(station)
                  setMapCenter([station.latitude, station.longitude])
                }
              }}
            >
              <Popup className="ev-popup">
                <div className="p-1">
                  <p className="font-bold text-dark-900">{station.name}</p>
                  <p className="text-sm text-dark-700">{station.city}</p>
                  <p className="text-sm font-medium" style={{ color: networkColors[station.cpoNetworkCode] }}>
                    {station.cpoNetworkName}
                  </p>
                </div>
              </Popup>
            </Marker>
          ))}
        </MapContainer>

        {/* Selected station overlay */}
        <AnimatePresence>
          {selected && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 20 }}
              className="absolute bottom-6 left-1/2 -translate-x-1/2 w-96 glass-card p-5 z-[1000]"
            >
              <div className="flex items-start justify-between gap-3 mb-3">
                <div>
                  <h3 className="font-display font-bold text-base">{selected.name}</h3>
                  <p className="text-sm text-white/50 flex items-center gap-1 mt-0.5">
                    <MapPin size={12} /> {selected.city}, {selected.state}
                  </p>
                </div>
                <button onClick={() => setSelected(null)} className="text-white/40 hover:text-white flex-shrink-0">
                  <X size={18} />
                </button>
              </div>
              <div className="flex items-center justify-between mb-4">
                <span className={
                  selected.status === 'AVAILABLE' ? 'badge-available'
                  : selected.status === 'BUSY' ? 'badge-busy' : 'badge-offline'
                }>
                  {selected.status}
                </span>
                <span className="text-sm text-white/50">
                  {selected.availableConnectors}/{selected.totalConnectors} connectors available
                </span>
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => navigate(`/stations/${selected.id}`)}
                  className="btn-primary flex-1 justify-center py-2.5 text-sm"
                >
                  View Details
                </button>
                {selected.status === 'AVAILABLE' && (
                  <button
                    onClick={() => navigate(`/stations/${selected.id}`)}
                    className="btn-charge px-6 py-2.5 text-sm"
                  >
                    <Zap size={16} /> Charge
                  </button>
                )}
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  )
}
