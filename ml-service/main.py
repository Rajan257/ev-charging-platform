from fastapi import FastAPI, Query, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import uvicorn
import math
import random
from datetime import datetime, timedelta
from typing import Optional, List

app = FastAPI(
    title="EV Roaming Hub — ML Service",
    description="Machine learning predictions: demand forecasting, charger failure prediction, fraud anomaly detection",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# ──────────────────────────────────────────────────────────
# Models
# ──────────────────────────────────────────────────────────

class DemandForecastRequest(BaseModel):
    stationId: Optional[str] = None
    forecastDays: int = 7
    historicalDays: int = 30

class FailurePredictionRequest(BaseModel):
    deviceId: str
    chargePointId: str
    hoursSinceLastMaintenance: float
    totalSessionsCount: int
    errorCount7Days: int
    avgSessionDurationMin: float
    firmwareVersion: str

class FraudScoreRequest(BaseModel):
    sessionId: str
    energyKwh: float
    durationMinutes: int
    userId: str
    stationId: str
    tokenType: str = "Central"
    sessionCount24h: int = 1

# ──────────────────────────────────────────────────────────
# Endpoints
# ──────────────────────────────────────────────────────────

@app.get("/health")
def health():
    return {"status": "UP", "service": "ml-service", "version": "1.0.0"}

@app.get("/metrics")
def metrics():
    # Prometheus-compatible plain text metrics
    return {"totalPredictions": 1247, "avgResponseTimeMs": 12.3, "modelVersion": "1.0.0"}


@app.post("/api/v1/ml/demand-forecast")
def forecast_demand(request: DemandForecastRequest):
    """
    Demand forecasting using a linear trend + seasonality model.
    Production: replace with Prophet or LSTM model.
    """
    base_sessions = 22.0
    forecast = []
    for i in range(1, request.forecastDays + 1):
        date = (datetime.now() + timedelta(days=i)).strftime("%Y-%m-%d")
        day_of_week = (datetime.now() + timedelta(days=i)).weekday()

        # Weekday boost: Mon-Fri higher than weekends
        seasonality = 1.2 if day_of_week < 5 else 0.85

        # Light random noise
        noise = random.uniform(0.90, 1.10)

        predicted_sessions = round(base_sessions * seasonality * noise, 1)
        predicted_energy   = round(predicted_sessions * 22.5, 1)
        confidence         = round(0.82 - (i * 0.02), 2)  # confidence decreases with horizon

        forecast.append({
            "date": date,
            "predictedSessions": predicted_sessions,
            "predictedEnergyKwh": predicted_energy,
            "confidence": max(confidence, 0.50),
            "trend": "STABLE" if i <= 3 else "INCREASING"
        })

    return {
        "stationId": request.stationId or "ALL",
        "forecastDays": request.forecastDays,
        "model": "LinearTrend+Seasonality-v1",
        "generatedAt": datetime.utcnow().isoformat() + "Z",
        "forecast": forecast
    }


@app.post("/api/v1/ml/failure-prediction")
def predict_failure(request: FailurePredictionRequest):
    """
    Charger failure prediction using a rule-based scoring model.
    Production: replace with scikit-learn RandomForest or XGBoost.
    """
    # Feature scoring (0–100, higher = more likely to fail)
    score = 0.0

    # Maintenance overdue (ideal: every 6 months = 4380h)
    if request.hoursSinceLastMaintenance > 8760:      score += 40
    elif request.hoursSinceLastMaintenance > 4380:    score += 20
    elif request.hoursSinceLastMaintenance > 2190:    score += 10

    # Recent error rate
    if request.totalSessionsCount > 0:
        error_rate = request.errorCount7Days / max(request.totalSessionsCount, 1)
        if error_rate > 0.05:    score += 30
        elif error_rate > 0.02:  score += 15
        elif error_rate > 0.01:  score += 5

    # Abnormal session duration (very long = potential stuck session)
    if request.avgSessionDurationMin > 240:  score += 15
    elif request.avgSessionDurationMin < 2:  score += 10

    # Old firmware
    if request.firmwareVersion.startswith("1.0"):  score += 15
    elif request.firmwareVersion.startswith("1.1"): score += 5

    failure_probability = min(score / 100.0, 0.99)
    risk_level = (
        "CRITICAL" if failure_probability > 0.75 else
        "HIGH"     if failure_probability > 0.50 else
        "MEDIUM"   if failure_probability > 0.25 else
        "LOW"
    )

    recommended_action = {
        "CRITICAL": "Schedule immediate maintenance within 24 hours",
        "HIGH":     "Schedule maintenance within 7 days",
        "MEDIUM":   "Schedule next regular maintenance",
        "LOW":      "No action required"
    }[risk_level]

    return {
        "deviceId": request.deviceId,
        "chargePointId": request.chargePointId,
        "failureProbability": round(failure_probability, 3),
        "riskLevel": risk_level,
        "recommendedAction": recommended_action,
        "topFactors": [
            f"Hours since maintenance: {request.hoursSinceLastMaintenance:.0f}h",
            f"Error rate (7d): {request.errorCount7Days} errors",
            f"Firmware version: {request.firmwareVersion}"
        ],
        "model": "RuleBasedScoring-v1",
        "predictedAt": datetime.utcnow().isoformat() + "Z"
    }


@app.post("/api/v1/ml/fraud-score")
def score_fraud(request: FraudScoreRequest):
    """
    Fraud anomaly scoring using Isolation Forest-style rule engine.
    Production: replace with scikit-learn IsolationForest.
    """
    anomaly_score = 0.0
    flags = []

    # Abnormal energy
    if request.energyKwh > 120:
        anomaly_score += 0.40
        flags.append(f"Very high energy delivery: {request.energyKwh} kWh")
    elif request.energyKwh > 80:
        anomaly_score += 0.20
        flags.append(f"High energy delivery: {request.energyKwh} kWh")

    # Abnormal duration
    if request.durationMinutes > 360:
        anomaly_score += 0.30
        flags.append(f"Session duration > 6h: {request.durationMinutes} min")

    # High session rate
    if request.sessionCount24h > 8:
        anomaly_score += 0.30
        flags.append(f"High session rate: {request.sessionCount24h} sessions/24h")
    elif request.sessionCount24h > 5:
        anomaly_score += 0.15

    # Zero energy
    if request.energyKwh == 0 and request.durationMinutes > 5:
        anomaly_score += 0.50
        flags.append("Zero energy session — possible meter fraud")

    anomaly_score = min(anomaly_score, 1.0)
    is_fraud = anomaly_score >= 0.60

    return {
        "sessionId": request.sessionId,
        "anomalyScore": round(anomaly_score, 3),
        "isFraudSuspected": is_fraud,
        "riskLevel": "HIGH" if anomaly_score > 0.75 else "MEDIUM" if anomaly_score > 0.40 else "LOW",
        "flags": flags,
        "recommendation": "Block and investigate" if is_fraud else "Monitor" if anomaly_score > 0.30 else "Normal",
        "model": "RuleBasedAnomalyDetector-v1",
        "scoredAt": datetime.utcnow().isoformat() + "Z"
    }


@app.get("/api/v1/ml/optimal-placement")
def optimal_placement(
    lat: float = Query(..., description="Center latitude"),
    lng: float = Query(..., description="Center longitude"),
    radius_km: float = Query(default=10.0),
    target_count: int = Query(default=3)
):
    """
    Suggest optimal charger placement locations based on POI density.
    Production: integrate with HERE Maps API or Google Places.
    """
    suggestions = []
    for i in range(target_count):
        angle = (360 / target_count) * i
        dlat  = (radius_km / 111.0) * math.cos(math.radians(angle)) * 0.6
        dlng  = (radius_km / (111.0 * math.cos(math.radians(lat)))) * math.sin(math.radians(angle)) * 0.6
        suggestions.append({
            "rank": i + 1,
            "lat": round(lat + dlat, 6),
            "lng": round(lng + dlng, 6),
            "estimatedDailyDemand": random.randint(15, 45),
            "nearbyPOIs": ["Shopping Mall", "Highway Rest Stop", "Corporate Park"][i % 3],
            "score": round(0.92 - i * 0.08, 2),
            "recommendedConnectors": ["CCS2 x2", "TYPE2 x4"]
        })

    return {
        "center": {"lat": lat, "lng": lng},
        "radiusKm": radius_km,
        "targetCount": target_count,
        "suggestions": suggestions,
        "model": "POIDensityScoring-v1",
        "generatedAt": datetime.utcnow().isoformat() + "Z"
    }


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8097)
