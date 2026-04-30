# EV Roaming Hub India — Kafka Event Schemas
# All platform events, their topics, and payload structure
# Format: Apache Avro-compatible JSON schema definitions

---
# ─────────────────────────── CHARGER EVENTS ──────────────────────────

topic: charger.status.updated
description: "Fired when a charger status changes (BootNotification, StatusNotification)"
schema:
  chargePointId: string
  stationId: string
  status: enum[AVAILABLE, OCCUPIED, FAULTED, OFFLINE, BOOTING]
  connectorId: integer
  timestamp: string (ISO8601)
  source: enum[OCPP, DEVICE_MANAGEMENT, SYSTEM]

---
topic: charger.health.alert
description: "Fired when a charger misses heartbeat or has critical fault"
schema:
  chargePointId: string
  alert: enum[HEARTBEAT_MISSED, CONNECTOR_FAULT, FIRMWARE_ERROR, CERTIFICATE_EXPIRED]
  lastHeartbeat: string (ISO8601)
  severity: enum[LOW, MEDIUM, HIGH, CRITICAL]
  timestamp: string (ISO8601)

---
topic: charger.command
description: "OCPP remote command dispatched to a charger via station-service"
schema:
  chargePointId: string
  command: enum[Reset, UnlockConnector, ChangeConfiguration, UpdateFirmware, GetConfiguration]
  payload: object (command-specific)
  timestamp: string (ISO8601)

---
topic: charger.firmware.update
description: "Firmware OTA update triggered"
schema:
  chargePointId: string
  targetVersion: string
  firmwareUrl: string
  retrieveDate: string (ISO8601)
  triggeredBy: string

---
# ─────────────────────────── SESSION EVENTS ──────────────────────────

topic: session.started
description: "Fired when a charging session begins (TransactionEvent:Started)"
schema:
  sessionId: string
  userId: string
  tokenId: string
  tokenType: enum[RFID, APP, PLUG_AND_CHARGE, OCPI]
  stationId: string
  chargePointId: string
  connectorId: integer
  startedAt: string (ISO8601)

---
topic: session.meter.value
description: "Real-time meter value update during active session"
schema:
  sessionId: string
  chargePointId: string
  powerKw: number
  energyKwh: number
  stateOfCharge: integer (optional, 0-100)
  timestamp: string (ISO8601)

---
topic: session.ended
description: "Fired when a session ends (TransactionEvent:Ended)"
schema:
  sessionId: string
  userId: string
  driverId: string (optional, fleet)
  tokenId: string
  stationId: string
  chargePointId: string
  energyKwh: number
  durationMinutes: integer
  totalCost: number
  currency: string (default: INR)
  stopReason: enum[Local, Remote, EVDisconnected, PowerLoss, Timeout]
  endedAt: string (ISO8601)

---
# ─────────────────────────── BILLING EVENTS ──────────────────────────

topic: invoice.generated
description: "Fired when a CDR/invoice is created post-session"
schema:
  invoiceId: string
  sessionId: string
  userId: string
  stationId: string
  energyKwh: number
  subtotalInr: number
  cgstInr: number
  sgstInr: number
  totalInr: number
  generatedAt: string (ISO8601)

---
# ─────────────────────────── PAYMENT EVENTS ──────────────────────────

topic: payment.completed
description: "Fired when payment is successfully captured"
schema:
  paymentId: string
  sessionId: string
  userId: string
  amount: number
  currency: string
  paymentMethod: enum[UPI, CARD, WALLET, FLEET_ACCOUNT]
  gatewayTransactionId: string
  completedAt: string (ISO8601)

---
topic: payment.failed
description: "Fired when payment fails or is declined"
schema:
  paymentId: string
  sessionId: string
  userId: string
  amount: number
  reason: string
  errorCode: string
  failedAt: string (ISO8601)

---
# ─────────────────────────── FRAUD EVENTS ──────────────────────────

topic: fraud.alert.detected
description: "Fired when fraud detection engine flags a session/payment"
schema:
  alertId: string
  type: enum[ABNORMAL_ENERGY, IMPOSSIBLE_DURATION, RAPID_SESSION_RATE, ZERO_ENERGY_SESSION, HIGH_VALUE_PAYMENT, ROAMING_GEO_ANOMALY]
  severity: enum[LOW, MEDIUM, HIGH, CRITICAL]
  sessionId: string
  userId: string
  description: string
  detectedAt: string (ISO8601)

---
# ─────────────────────────── SMART CHARGING EVENTS ──────────────────

topic: smartcharging.profile.set
description: "OCPP SetChargingProfile dispatched to charger"
schema:
  stationId: string
  chargePointId: string
  profileId: string
  purpose: enum[TxDefaultProfile, ChargePointMaxProfile, TxProfile]
  maxChargingRateKw: number
  timestamp: string (ISO8601)

---
topic: smartcharging.power.limit
description: "Dynamic power limit applied to a station"
schema:
  stationId: string
  limitKw: number
  reason: string
  setAt: string (ISO8601)

---
topic: smartcharging.demand.response
description: "Grid demand response event — reduce load across stations"
schema:
  type: DEMAND_RESPONSE
  reductionPercent: number
  durationMinutes: integer
  triggeredAt: string (ISO8601)
  expiresAt: string (ISO8601)

---
# ─────────────────────────── ROAMING EVENTS ──────────────────────────

topic: roaming.token.validated
description: "OCPI token validation result for a roaming driver"
schema:
  tokenId: string
  tokenType: enum[RFID, APP_USER, AD_HOC_USER]
  cpoId: string
  mspId: string
  valid: boolean
  reason: string (if invalid)
  validatedAt: string (ISO8601)

---
topic: settlement.cdr.sent
description: "CDR forwarded to MSP for settlement"
schema:
  cdrId: string
  sessionId: string
  cpoId: string
  mspId: string
  energyKwh: number
  totalCostInr: number
  sentAt: string (ISO8601)
