import axios, { type InternalAxiosRequestConfig, type AxiosResponse, type AxiosError } from 'axios'
import { useAuthStore } from '../store/authStore'

const api = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor - attach JWT
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = useAuthStore.getState().accessToken
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => Promise.reject(error)
)

// Response interceptor - handle 401 and token refresh
api.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined

    if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
      originalRequest._retry = true
      const { refreshToken, login, logout } = useAuthStore.getState()

      if (refreshToken) {
        try {
          const response = await axios.post('/api/v1/auth/refresh', { refreshToken })
          const { accessToken, refreshToken: newRefresh, ...rest } = response.data
          login({ ...useAuthStore.getState().user!, ...rest }, accessToken, newRefresh)
          originalRequest.headers.Authorization = `Bearer ${accessToken}`
          return api(originalRequest)
        } catch {
          logout()
          window.location.href = '/login'
        }
      } else {
        logout()
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

// ===== AUTH APIs =====
export const authApi = {
  login: (email: string, password: string) =>
    api.post('/auth/login', { email, password }),
  register: (data: { email: string; fullName: string; password: string; phone?: string }) =>
    api.post('/auth/register', data),
  logout: () => api.post('/auth/logout'),
  refreshToken: (refreshToken: string) =>
    api.post('/auth/refresh', { refreshToken }),
}

// ===== STATION APIs =====
export const stationApi = {
  getStations: (params?: {
    city?: string; lat?: number; lng?: number;
    radiusKm?: number; page?: number; size?: number
  }) => api.get('/stations', { params }),
  getStation: (id: string) => api.get(`/stations/${id}`),
  getConnectors: (stationId: string) => api.get(`/stations/${stationId}/connectors`),
  getStats: () => api.get('/stations/stats'),
  getNetworkStations: (networkId: string) => api.get(`/stations/network/${networkId}`),
}

// ===== SESSION APIs =====
export const sessionApi = {
  startSession: (data: {
    connectorId: string; stationId: string;
    rfidCardId?: string; vehicleId?: string; authMethod?: string
  }) => api.post('/sessions/start', data),
  stopSession: (sessionId: string, reason?: string) =>
    api.post(`/sessions/${sessionId}/stop`, { reason }),
  getSession: (id: string) => api.get(`/sessions/${id}`),
  getActiveSession: () => api.get('/sessions/active'),
  getHistory: (params?: { page?: number; size?: number }) =>
    api.get('/sessions/history', { params }),
}

// ===== BILLING APIs =====
export const billingApi = {
  getInvoice: (id: string) => api.get(`/billing/invoices/${id}`),
  getMyInvoices: (params?: { page?: number; size?: number }) =>
    api.get('/billing/invoices', { params }),
  getTariffs: () => api.get('/billing/tariffs'),
}

// ===== PAYMENT APIs =====
export const paymentApi = {
  initiatePayment: (data: {
    invoiceId: string; amount: number; paymentMethod: string; upiVpa?: string
  }) => api.post('/payments/initiate', data),
  confirmPayment: (paymentId: string, data: {
    paymentId: string; signature: string
  }) => api.post(`/payments/${paymentId}/confirm`, data),
  getWallet: () => api.get('/payments/wallet'),
  topUpWallet: (data: { amount: number; paymentMethod: string }) =>
    api.post('/payments/wallet/topup', data),
  getPaymentHistory: (params?: { page?: number; size?: number }) =>
    api.get('/payments/history', { params }),
}

// ===== RFID APIs =====
export const rfidApi = {
  getMyCards: () => api.get('/auth/rfid'),
  registerCard: (data: { uid: string; label?: string; cardType?: string }) =>
    api.post('/auth/rfid', data),
  deactivateCard: (cardId: string) => api.delete(`/auth/rfid/${cardId}`),
}

// ===== USER APIs =====
export const userApi = {
  getProfile: () => api.get('/auth/users/me'),
  updateProfile: (data: {
    fullName?: string; phone?: string; profilePhoto?: string
  }) => api.put('/auth/users/me', data),
  getVehicles: () => api.get('/auth/vehicles'),
  addVehicle: (data: {
    make: string; model: string; year?: number;
    registrationNumber?: string; vin?: string
  }) => api.post('/auth/vehicles', data),
}

// ===== ADMIN APIs =====
export const adminApi = {
  // Networks
  getNetworks: () => api.get('/stations/networks'),
  // Settlements
  getSettlements: () => api.get('/settlements'),
  triggerSettlement: (cpoNetworkId: string, periodStart: string, periodEnd: string) =>
    api.post('/settlements/trigger', { cpoNetworkId, periodStart, periodEnd }),
  approveSettlement: (id: string) => api.post(`/settlements/${id}/approve`),
  // Roaming Partners
  getRoamingPartners: () => api.get('/roaming/partners'),
  // System stats
  getSystemStats: () => api.get('/stations/stats'),
}

export default api
