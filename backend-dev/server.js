const express = require('express');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = 8000;
const JWT_SECRET = 'ev-roaming-hub-super-secret-key-minimum-256-bits-for-hs256-algorithm';

app.use(cors());
app.use(express.json());

// Request logger
app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
  next();
});

// Database file paths
const DB_DIR = path.join(__dirname, 'database');
const USERS_FILE = path.join(DB_DIR, 'users.json');
const SESSIONS_FILE = path.join(DB_DIR, 'sessions.json');
const WALLETS_FILE = path.join(DB_DIR, 'wallets.json');

// Ensure database files exist
if (!fs.existsSync(DB_DIR)) {
  fs.mkdirSync(DB_DIR);
}

const initJsonFile = (filePath, defaultData) => {
  if (!fs.existsSync(filePath)) {
    fs.writeFileSync(filePath, JSON.stringify(defaultData, null, 2));
  }
};

initJsonFile(USERS_FILE, {});
initJsonFile(SESSIONS_FILE, []);
initJsonFile(WALLETS_FILE, {});

// Helper function to read/write JSON
const readJson = (filePath) => JSON.parse(fs.readFileSync(filePath, 'utf8'));
const writeJson = (filePath, data) => fs.writeFileSync(filePath, JSON.stringify(data, null, 2));

// Auth Middleware
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) return res.status(401).json({ detail: 'Authentication token missing' });

  jwt.verify(token, JWT_SECRET, (err, user) => {
    if (err) return res.status(403).json({ detail: 'Invalid or expired token' });
    req.user = user;
    next();
  });
};

// Seed station data
let MOCK_STATIONS = [
  {
    id: 's001', name: 'Tata Power - Connaught Place', city: 'New Delhi', state: 'Delhi',
    latitude: 28.6315, longitude: 77.2167, status: 'AVAILABLE', cpoNetworkCode: 'TATA',
    cpoNetworkName: 'Tata Power EV', totalConnectors: 4, availableConnectors: 4,
    address: 'Block A, Connaught Place, New Delhi', tariffPerKwh: 14.0,
    connectors: [
      { id: 'c001a', evseId: 'TATA-CP-1-1', connectorNumber: 1, standard: 'CCS2', status: 'AVAILABLE', maxVoltage: 400, maxAmperage: 125, maxElectricPower: 50000, powerType: 'DC' },
      { id: 'c001b', evseId: 'TATA-CP-1-2', connectorNumber: 2, standard: 'CCS2', status: 'AVAILABLE', maxVoltage: 400, maxAmperage: 125, maxElectricPower: 50000, powerType: 'DC' },
      { id: 'c001c', evseId: 'TATA-CP-1-3', connectorNumber: 3, standard: 'CHAdeMO', status: 'AVAILABLE', maxVoltage: 400, maxAmperage: 125, maxElectricPower: 50000, powerType: 'DC' },
      { id: 'c001d', evseId: 'TATA-CP-1-4', connectorNumber: 4, standard: 'TYPE2', status: 'AVAILABLE', maxVoltage: 230, maxAmperage: 32, maxElectricPower: 22000, powerType: 'AC_3_PHASE' },
    ],
  },
  {
    id: 's002', name: 'Tata Power - Cyber Hub', city: 'Gurugram', state: 'Haryana',
    latitude: 28.4949, longitude: 77.0896, status: 'AVAILABLE', cpoNetworkCode: 'TATA',
    cpoNetworkName: 'Tata Power EV', totalConnectors: 2, availableConnectors: 2,
    address: 'DLF Cyber Hub, Sector 24, Gurugram', tariffPerKwh: 14.0,
    connectors: [
      { id: 'c002a', evseId: 'TATA-CH-1-1', connectorNumber: 1, standard: 'CCS2', status: 'AVAILABLE', maxVoltage: 400, maxAmperage: 125, maxElectricPower: 50000, powerType: 'DC' },
      { id: 'c002b', evseId: 'TATA-CH-1-2', connectorNumber: 2, standard: 'CCS2', status: 'AVAILABLE', maxVoltage: 400, maxAmperage: 125, maxElectricPower: 50000, powerType: 'DC' },
    ],
  },
  {
    id: 's003', name: 'Ather Grid - Koramangala', city: 'Bengaluru', state: 'Karnataka',
    latitude: 12.9352, longitude: 77.6245, status: 'AVAILABLE', cpoNetworkCode: 'ATHER',
    cpoNetworkName: 'Ather Grid', totalConnectors: 3, availableConnectors: 3,
    address: '80 Feet Rd, Koramangala, Bengaluru', tariffPerKwh: 12.5,
    connectors: [
      { id: 'c003a', evseId: 'ATHER-KM-1-1', connectorNumber: 1, standard: 'CCS2', status: 'AVAILABLE', maxVoltage: 350, maxAmperage: 80, maxElectricPower: 30000, powerType: 'DC' },
      { id: 'c003b', evseId: 'ATHER-KM-1-2', connectorNumber: 2, standard: 'CCS2', status: 'AVAILABLE', maxVoltage: 350, maxAmperage: 80, maxElectricPower: 30000, powerType: 'DC' },
      { id: 'c003c', evseId: 'ATHER-KM-1-3', connectorNumber: 3, standard: 'TYPE2', status: 'AVAILABLE', maxVoltage: 230, maxAmperage: 32, maxElectricPower: 7000, powerType: 'AC_1_PHASE' },
    ],
  },
  {
    id: 's004', name: 'Ather Grid - Indiranagar', city: 'Bengaluru', state: 'Karnataka',
    latitude: 12.9784, longitude: 77.6408, status: 'AVAILABLE', cpoNetworkCode: 'ATHER',
    cpoNetworkName: 'Ather Grid', totalConnectors: 2, availableConnectors: 2,
    address: '100 Feet Rd, Indiranagar, Bengaluru', tariffPerKwh: 12.5,
    connectors: [
      { id: 'c004a', evseId: 'ATHER-IN-1-1', connectorNumber: 1, standard: 'CCS2', status: 'AVAILABLE', maxVoltage: 350, maxAmperage: 80, maxElectricPower: 30000, powerType: 'DC' },
      { id: 'c004b', evseId: 'ATHER-IN-1-2', connectorNumber: 2, standard: 'TYPE2', status: 'AVAILABLE', maxVoltage: 230, maxAmperage: 32, maxElectricPower: 7000, powerType: 'AC_1_PHASE' },
    ],
  },
  {
    id: 's005', name: 'BPCL Pulse - Bandra', city: 'Mumbai', state: 'Maharashtra',
    latitude: 19.0544, longitude: 72.8405, status: 'AVAILABLE', cpoNetworkCode: 'BPCL',
    cpoNetworkName: 'BPCL Pulse', totalConnectors: 2, availableConnectors: 2,
    address: 'Turner Road, Bandra West, Mumbai', tariffPerKwh: 16.0,
    connectors: [
      { id: 'c005a', evseId: 'BPCL-BD-1-1', connectorNumber: 1, standard: 'CCS2', status: 'AVAILABLE', maxVoltage: 400, maxAmperage: 125, maxElectricPower: 50000, powerType: 'DC' },
      { id: 'c005b', evseId: 'BPCL-BD-1-2', connectorNumber: 2, standard: 'TYPE2', status: 'AVAILABLE', maxVoltage: 230, maxAmperage: 32, maxElectricPower: 22000, powerType: 'AC_3_PHASE' },
    ],
  },
  {
    id: 's006', name: 'BPCL Pulse - Powai', city: 'Mumbai', state: 'Maharashtra',
    latitude: 19.1176, longitude: 72.9060, status: 'AVAILABLE', cpoNetworkCode: 'BPCL',
    cpoNetworkName: 'BPCL Pulse', totalConnectors: 2, availableConnectors: 2,
    address: 'Hiranandani Gardens, Powai, Mumbai', tariffPerKwh: 16.0,
    connectors: [
      { id: 'c006a', evseId: 'BPCL-PW-1-1', connectorNumber: 1, standard: 'CCS2', status: 'AVAILABLE', maxVoltage: 400, maxAmperage: 125, maxElectricPower: 50000, powerType: 'DC' },
      { id: 'c006b', evseId: 'BPCL-PW-1-2', connectorNumber: 2, standard: 'TYPE2', status: 'AVAILABLE', maxVoltage: 230, maxAmperage: 32, maxElectricPower: 22000, powerType: 'AC_3_PHASE' },
    ],
  },
  {
    id: 's007', name: 'ChargeZone - Kalyani Nagar', city: 'Pune', state: 'Maharashtra',
    latitude: 18.5462, longitude: 73.9013, status: 'AVAILABLE', cpoNetworkCode: 'CHGZ',
    cpoNetworkName: 'ChargeZone', totalConnectors: 3, availableConnectors: 3,
    address: 'Kalyani Nagar, Pune', tariffPerKwh: 13.5,
    connectors: [
      { id: 'c007a', evseId: 'CHGZ-KN-1-1', connectorNumber: 1, standard: 'CCS2', status: 'AVAILABLE', maxVoltage: 450, maxAmperage: 150, maxElectricPower: 60000, powerType: 'DC' },
      { id: 'c007b', evseId: 'CHGZ-KN-1-2', connectorNumber: 2, standard: 'CCS2', status: 'AVAILABLE', maxVoltage: 450, maxAmperage: 150, maxElectricPower: 60000, powerType: 'DC' },
      { id: 'c007c', evseId: 'CHGZ-KN-1-3', connectorNumber: 3, standard: 'CHAdeMO', status: 'AVAILABLE', maxVoltage: 400, maxAmperage: 125, maxElectricPower: 50000, powerType: 'DC' },
    ],
  },
  {
    id: 's008', name: 'Fortum - Anna Salai', city: 'Chennai', state: 'Tamil Nadu',
    latitude: 13.0604, longitude: 80.2596, status: 'AVAILABLE', cpoNetworkCode: 'FORT',
    cpoNetworkName: 'Fortum India', totalConnectors: 2, availableConnectors: 2,
    address: 'Anna Salai, Teynampet, Chennai', tariffPerKwh: 15.0,
    connectors: [
      { id: 'c008a', evseId: 'FORT-AS-1-1', connectorNumber: 1, standard: 'CCS2', status: 'AVAILABLE', maxVoltage: 400, maxAmperage: 125, maxElectricPower: 50000, powerType: 'DC' },
      { id: 'c008b', evseId: 'FORT-AS-1-2', connectorNumber: 2, standard: 'TYPE2', status: 'AVAILABLE', maxVoltage: 230, maxAmperage: 32, maxElectricPower: 22000, powerType: 'AC_3_PHASE' },
    ],
  },
  {
    id: 's009', name: 'Tata Power - Salt Lake', city: 'Kolkata', state: 'West Bengal',
    latitude: 22.5726, longitude: 88.4312, status: 'MAINTENANCE', cpoNetworkCode: 'TATA',
    cpoNetworkName: 'Tata Power EV', totalConnectors: 2, availableConnectors: 0,
    address: 'Salt Lake Sector V, Kolkata', tariffPerKwh: 14.0,
    connectors: [
      { id: 'c009a', evseId: 'TATA-SL-1-1', connectorNumber: 1, standard: 'CCS2', status: 'MAINTENANCE', maxVoltage: 400, maxAmperage: 125, maxElectricPower: 50000, powerType: 'DC' },
      { id: 'c009b', evseId: 'TATA-SL-1-2', connectorNumber: 2, standard: 'TYPE2', status: 'MAINTENANCE', maxVoltage: 230, maxAmperage: 32, maxElectricPower: 22000, powerType: 'AC_3_PHASE' },
    ],
  },
  {
    id: 's010', name: 'ChargeZone - Gomti Nagar', city: 'Lucknow', state: 'Uttar Pradesh',
    latitude: 26.8617, longitude: 81.0229, status: 'AVAILABLE', cpoNetworkCode: 'CHGZ',
    cpoNetworkName: 'ChargeZone', totalConnectors: 2, availableConnectors: 2,
    address: 'Gomti Nagar, Lucknow', tariffPerKwh: 13.5,
    connectors: [
      { id: 'c010a', evseId: 'CHGZ-GN-1-1', connectorNumber: 1, standard: 'CCS2', status: 'AVAILABLE', maxVoltage: 450, maxAmperage: 150, maxElectricPower: 60000, powerType: 'DC' },
      { id: 'c010b', evseId: 'CHGZ-GN-1-2', connectorNumber: 2, standard: 'TYPE2', status: 'AVAILABLE', maxVoltage: 230, maxAmperage: 32, maxElectricPower: 22000, powerType: 'AC_3_PHASE' },
    ],
  },
];

// ---- Auth endpoints ----
app.post('/api/v1/auth/register', (req, res) => {
  const { email, fullName, password, phone } = req.body;
  if (!email || !fullName || !password) {
    return res.status(400).json({ detail: 'Required fields missing' });
  }

  const users = readJson(USERS_FILE);
  const emailLower = email.toLowerCase().trim();

  if (users[emailLower]) {
    return res.status(400).json({ detail: 'An account with this email already exists.' });
  }

  const userId = 'u-' + Date.now();
  const newUser = {
    id: userId,
    email: emailLower,
    fullName: fullName.trim(),
    phone: phone || '',
    password,
    role: 'DRIVER'
  };

  users[emailLower] = newUser;
  writeJson(USERS_FILE, users);

  // Initialize wallet for new user
  const wallets = readJson(WALLETS_FILE);
  wallets[userId] = { balance: 500, currency: 'INR', history: [] };
  writeJson(WALLETS_FILE, wallets);

  const tokenUser = { id: userId, email: emailLower, fullName: newUser.fullName, role: newUser.role };
  const accessToken = jwt.sign(tokenUser, JWT_SECRET, { expiresIn: '7d' });
  const refreshToken = jwt.sign({ id: userId }, JWT_SECRET, { expiresIn: '30d' });

  res.status(201).json({
    accessToken,
    refreshToken,
    userId,
    fullName: newUser.fullName,
    role: newUser.role,
    user: tokenUser
  });
});

app.post('/api/v1/auth/login', (req, res) => {
  const { email, password } = req.body;
  const users = readJson(USERS_FILE);
  const emailLower = email ? email.toLowerCase().trim() : '';
  const user = users[emailLower];

  if (!user || user.password !== password) {
    return res.status(401).json({ detail: 'Invalid credentials. Please try again.' });
  }

  const userId = user.id;

  // Initialize wallet if it somehow doesn't exist
  const wallets = readJson(WALLETS_FILE);
  if (!wallets[userId]) {
    wallets[userId] = { balance: 500, currency: 'INR', history: [] };
    writeJson(WALLETS_FILE, wallets);
  }

  const tokenUser = { id: userId, email: emailLower, fullName: user.fullName, role: user.role };
  const accessToken = jwt.sign(tokenUser, JWT_SECRET, { expiresIn: '7d' });
  const refreshToken = jwt.sign({ id: userId }, JWT_SECRET, { expiresIn: '30d' });

  res.json({
    accessToken,
    refreshToken,
    userId,
    fullName: user.fullName,
    role: user.role,
    user: tokenUser
  });
});

app.post('/api/v1/auth/logout', (req, res) => {
  res.json({});
});

app.get('/api/v1/auth/users/me', authenticateToken, (req, res) => {
  const users = readJson(USERS_FILE);
  const user = Object.values(users).find(u => u.id === req.user.id);
  if (!user) return res.status(404).json({ detail: 'User not found' });
  res.json({ id: user.id, email: user.email, fullName: user.fullName, role: user.role, phone: user.phone });
});

app.put('/api/v1/auth/users/me', authenticateToken, (req, res) => {
  const { fullName, phone } = req.body;
  const users = readJson(USERS_FILE);
  const userKey = Object.keys(users).find(k => users[k].id === req.user.id);
  if (!userKey) return res.status(404).json({ detail: 'User not found' });

  if (fullName) users[userKey].fullName = fullName;
  if (phone) users[userKey].phone = phone;

  writeJson(USERS_FILE, users);
  res.json({ id: users[userKey].id, email: users[userKey].email, fullName: users[userKey].fullName, role: users[userKey].role, phone: users[userKey].phone });
});

app.get('/api/v1/auth/vehicles', authenticateToken, (req, res) => {
  res.json([]);
});

app.get('/api/v1/auth/rfid', authenticateToken, (req, res) => {
  res.json([]);
});

// ---- Station endpoints ----
app.get('/api/v1/stations', (req, res) => {
  res.json({ content: MOCK_STATIONS, totalElements: MOCK_STATIONS.length });
});

app.get('/api/v1/stations/stats', (req, res) => {
  const available = MOCK_STATIONS.filter(s => s.status === 'AVAILABLE').length;
  res.json({ totalStations: MOCK_STATIONS.length, availableStations: available });
});

app.get('/api/v1/stations/:id', (req, res) => {
  const station = MOCK_STATIONS.find(s => s.id === req.params.id);
  if (!station) return res.status(404).json({ detail: 'Station not found' });
  res.json(station);
});

app.get('/api/v1/stations/:id/connectors', (req, res) => {
  const station = MOCK_STATIONS.find(s => s.id === req.params.id);
  if (!station) return res.status(404).json({ detail: 'Station not found' });
  res.json(station.connectors || []);
});

// ---- Sessions endpoints ----
app.get('/api/v1/sessions/active', authenticateToken, (req, res) => {
  const sessions = readJson(SESSIONS_FILE);
  const active = sessions.find(s => s.userId === req.user.id && s.status === 'ACTIVE');
  if (!active) return res.status(404).json({ detail: 'No active session' });
  res.json(active);
});

app.post('/api/v1/sessions/start', authenticateToken, (req, res) => {
  const { connectorId, stationId } = req.body;
  const sessions = readJson(SESSIONS_FILE);

  // Check if already has active session
  const active = sessions.find(s => s.userId === req.user.id && s.status === 'ACTIVE');
  if (active) {
    return res.status(400).json({ detail: 'You already have an active session!' });
  }

  const station = MOCK_STATIONS.find(s => s.id === stationId);
  if (!station) return res.status(404).json({ detail: 'Station not found' });

  const connector = station.connectors.find(c => c.id === connectorId);
  if (!connector) return res.status(404).json({ detail: 'Connector not found' });
  if (connector.status !== 'AVAILABLE') return res.status(400).json({ detail: 'Connector not available' });

  // Update connector status
  connector.status = 'BUSY';
  station.availableConnectors = station.connectors.filter(c => c.status === 'AVAILABLE').length;

  const session = {
    id: 'sess-' + Date.now(),
    transactionId: 'TXN' + Math.random().toString(36).slice(2, 10).toUpperCase(),
    status: 'ACTIVE',
    startedAt: new Date().toISOString(),
    stationId,
    stationName: station.name,
    connectorId,
    energyKwh: 0,
    totalAmount: 0,
    userId: req.user.id
  };

  sessions.push(session);
  writeJson(SESSIONS_FILE, sessions);

  res.status(201).json(session);
});

app.post('/api/v1/sessions/:id/stop', authenticateToken, (req, res) => {
  const { id } = req.params;
  const sessions = readJson(SESSIONS_FILE);
  const sessionIndex = sessions.findIndex(s => s.id === id && s.userId === req.user.id && s.status === 'ACTIVE');

  if (sessionIndex === -1) return res.status(404).json({ detail: 'Active session not found' });

  const session = sessions[sessionIndex];
  const durationSec = (Date.now() - new Date(session.startedAt).getTime()) / 1000;
  const energyKwh = parseFloat(((durationSec / 3600) * 11).toFixed(2));
  const totalAmount = parseFloat((energyKwh * 14).toFixed(2));

  session.status = 'COMPLETED';
  session.stoppedAt = new Date().toISOString();
  session.energyKwh = energyKwh;
  session.totalAmount = totalAmount;

  // Revert connector status to AVAILABLE
  const station = MOCK_STATIONS.find(s => s.id === session.stationId);
  if (station) {
    const connector = station.connectors.find(c => c.id === session.connectorId);
    if (connector) connector.status = 'AVAILABLE';
    station.availableConnectors = station.connectors.filter(c => c.status === 'AVAILABLE').length;
  }

  writeJson(SESSIONS_FILE, sessions);

  // Deduct from wallet
  const wallets = readJson(WALLETS_FILE);
  if (wallets[req.user.id]) {
    wallets[req.user.id].balance = parseFloat((wallets[req.user.id].balance - totalAmount).toFixed(2));
    wallets[req.user.id].history.unshift({
      id: 'txn-' + Date.now(),
      type: 'DEBIT',
      amount: totalAmount,
      description: `Charging payment for Transaction ${session.transactionId}`,
      createdAt: new Date().toISOString()
    });
    writeJson(WALLETS_FILE, wallets);
  }

  res.json(session);
});

app.get('/api/v1/sessions/history', authenticateToken, (req, res) => {
  const sessions = readJson(SESSIONS_FILE);
  const userHistory = sessions.filter(s => s.userId === req.user.id).reverse();
  res.json({ content: userHistory, totalElements: userHistory.length });
});

// ---- Payments endpoints ----
app.get('/api/v1/payments/wallet', authenticateToken, (req, res) => {
  const wallets = readJson(WALLETS_FILE);
  const userWallet = wallets[req.user.id] || { balance: 0, currency: 'INR', history: [] };
  res.json(userWallet);
});

app.post('/api/v1/payments/wallet/topup', authenticateToken, (req, res) => {
  const { amount } = req.body;
  if (!amount || amount <= 0) return res.status(400).json({ detail: 'Invalid amount' });

  const wallets = readJson(WALLETS_FILE);
  if (!wallets[req.user.id]) {
    wallets[req.user.id] = { balance: 0, currency: 'INR', history: [] };
  }

  wallets[req.user.id].balance = parseFloat((wallets[req.user.id].balance + amount).toFixed(2));
  wallets[req.user.id].history.unshift({
    id: 'txn-' + Date.now(),
    type: 'TOPUP',
    amount: amount,
    description: 'Prepaid Wallet Top-up via UPI',
    createdAt: new Date().toISOString()
  });

  writeJson(WALLETS_FILE, wallets);
  res.json(wallets[req.user.id]);
});

app.get('/api/v1/payments/history', authenticateToken, (req, res) => {
  const wallets = readJson(WALLETS_FILE);
  const userWallet = wallets[req.user.id] || { balance: 0, currency: 'INR', history: [] };
  res.json({ content: userWallet.history || [], totalElements: (userWallet.history || []).length });
});

// ---- Billing endpoints ----
app.get('/api/v1/billing/invoices', authenticateToken, (req, res) => {
  const sessions = readJson(SESSIONS_FILE);
  const completed = sessions.filter(s => s.userId === req.user.id && s.status === 'COMPLETED');

  const invoices = completed.map((s, idx) => {
    const subtotal = parseFloat((s.totalAmount / 1.18).toFixed(2));
    const cgst = parseFloat((subtotal * 0.09).toFixed(2));
    const sgst = parseFloat((subtotal * 0.09).toFixed(2));

    return {
      id: 'inv-' + s.id,
      invoiceNumber: `INV-EV-${s.transactionId}`,
      energyKwh: s.energyKwh,
      subtotal,
      cgstAmount: cgst,
      sgstAmount: sgst,
      totalAmount: s.totalAmount,
      status: 'PAID',
      billingStart: s.startedAt,
      billingEnd: s.stoppedAt
    };
  });

  res.json({ content: invoices, totalElements: invoices.length });
});

app.get('/api/v1/billing/tariffs', (req, res) => {
  res.json([
    { network: 'Tata Power EV', tariffPerKwh: 14.0 },
    { network: 'Ather Grid', tariffPerKwh: 12.5 },
    { network: 'BPCL Pulse', tariffPerKwh: 16.0 },
    { network: 'ChargeZone', tariffPerKwh: 13.5 },
    { network: 'Fortum India', tariffPerKwh: 15.0 },
    { network: 'Statiq', tariffPerKwh: 13.0 },
  ]);
});

app.listen(PORT, () => {
  console.log(`EV Roaming Hub Dev Backend listening on http://localhost:${PORT}`);
});
