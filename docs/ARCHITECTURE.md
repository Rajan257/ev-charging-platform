# EV Roaming Hub India — System Architecture

## Overview

EV Roaming Hub India is an enterprise-grade unified EV charging access platform connecting **17 microservices** across Charge Point Operators (CPOs) and Mobility Service Providers (MSPs).

---

## Microservice Architecture

```mermaid
graph TD
    subgraph "Client Layer"
        A[React Web App<br/>Port 3000]
        B[Mobile App<br/>iOS/Android]
        C[OCPP Chargers<br/>WebSocket]
    end

    subgraph "Edge Layer"
        D[API Gateway<br/>Port 8000<br/>Spring Cloud Gateway]
    end

    subgraph "Identity"
        E[Keycloak<br/>Port 8080<br/>OIDC/OAuth2]
    end

    subgraph "Core Services"
        F[Auth Service<br/>8081]
        G[Station Service<br/>8082<br/>OCPP 2.0.1]
        H[Session Service<br/>8083]
        I[Billing Service<br/>8084]
        J[Payment Service<br/>8085<br/>Razorpay/UPI]
        K[Roaming Service<br/>8086<br/>OCPI 2.2.1]
        L[Settlement Service<br/>8087]
        M[Notification Service<br/>8088]
    end

    subgraph "Extended Services"
        N[Device Mgmt<br/>8090]
        O[Smart Charging<br/>8091]
        P[Fleet Service<br/>8092]
        Q[Analytics<br/>8093]
        R[Route Planning<br/>8094]
        S[Fraud Detection<br/>8095]
        T[Energy Reporting<br/>8096]
        U[ML Service<br/>8097<br/>Python/FastAPI]
        V[OCPP Simulator<br/>8098]
    end

    subgraph "Event Bus"
        W[Apache Kafka<br/>Port 9092]
        X[Schema Registry<br/>8089]
    end

    subgraph "Data Layer"
        Y[(PostgreSQL<br/>14 databases)]
        Z[(Redis<br/>Cache/Sessions)]
        AA[(ClickHouse<br/>Analytics)]
    end

    subgraph "Observability"
        BB[Prometheus<br/>9090]
        CC[Grafana<br/>3001]
        DD[Jaeger<br/>16686]
        EE[ELK Stack<br/>9200/5601]
    end

    subgraph "Security"
        FF[HashiCorp Vault<br/>8200]
    end

    A --> D
    B --> D
    C --> G
    D --> E
    D --> F & G & H & I & J & K & L & M
    D --> N & O & P & Q & R & S & T & U
    F & G & H & I & J & K & L & M --> W
    N & O & P & S --> W
    W --> X
    F & G & H --> Y
    I & J & K & L & M --> Y
    N & O & P & S & T --> Y
    Q --> AA
    D & G & H --> Z
    R --> Z
    BB --> CC
    DD --> CC
    EE --> CC
```

---

## Kafka Event Flow

```mermaid
sequenceDiagram
    participant Charger as OCPP Charger
    participant Station as station-service
    participant Session as session-service
    participant Billing as billing-service
    participant Payment as payment-service
    participant Fraud as fraud-detection
    participant Analytics as analytics-service

    Charger->>Station: BootNotification (WebSocket)
    Station->>Kafka: charger.status.updated
    Kafka->>Analytics: consume (session metrics)

    Charger->>Station: TransactionEvent:Started
    Station->>Kafka: session.started
    Kafka->>Session: consume → create session record

    Charger->>Station: MeterValues (periodic)
    Station->>Kafka: session.meter.value
    Kafka->>Analytics: consume (real-time power)

    Charger->>Station: TransactionEvent:Ended
    Station->>Kafka: session.ended
    Kafka->>Session: consume → close session
    Kafka->>Billing: consume → generate CDR/invoice
    Kafka->>Fraud: consume → run fraud checks

    Billing->>Kafka: invoice.generated
    Kafka->>Payment: consume → initiate charge

    Payment->>Kafka: payment.completed
    Kafka->>Analytics: consume (revenue tracking)
```

---

## Database Schema (Entity Relationships)

| Service | Database | Key Tables |
|---------|----------|------------|
| auth-service | evroaming_auth | users, rfid_cards, vehicles, audit_logs |
| station-service | evroaming_station | charging_stations, charge_points, connectors, cpo_networks |
| session-service | evroaming_session | charging_sessions, meter_values |
| billing-service | evroaming_billing | charge_detail_records, tariffs, invoices |
| payment-service | evroaming_payment | payments, wallet_transactions, refunds |
| roaming-service | evroaming_roaming | ocpi_tokens, location_cache, cpo_connections |
| settlement-service | evroaming_settlement | settlements, settlement_items |
| notification-service | evroaming_notification | notifications, notification_templates |
| device-management | evroaming_device | devices, device_configs, firmware_versions, health_logs |
| smart-charging | evroaming_smartcharging | charging_profiles, grid_load_metrics, power_limits |
| fleet-service | evroaming_fleet | fleets, fleet_vehicles, fleet_drivers, fleet_invoices |
| fraud-detection | evroaming_fraud | fraud_alerts, fraud_rules, fraud_rule_violations |
| energy-reporting | evroaming_energy | energy_reports, carbon_credits, tax_summaries |
| analytics-service | ClickHouse | session_events, meter_events, revenue_events |

---

## Deployment Architecture

```mermaid
graph LR
    subgraph "Docker / Kubernetes Cluster"
        subgraph "Ingress"
            ING[Nginx Ingress<br/>TLS Termination]
        end
        subgraph "Application Pods"
            GW[api-gateway x2]
            SVC[17 microservices<br/>1-3 replicas each]
            FE[frontend x2]
        end
        subgraph "Infrastructure"
            PG[PostgreSQL<br/>Primary + Replica]
            KFK[Kafka<br/>3 brokers]
            RD[Redis<br/>Cluster mode]
            KC[Keycloak<br/>HA pair]
            CH[ClickHouse<br/>Single node]
        end
        subgraph "Observability"
            PROM[Prometheus]
            GRAF[Grafana]
            JAE[Jaeger]
            ELK[ELK Stack]
        end
    end
    Internet --> ING --> GW --> SVC
```

---

## Security Architecture

| Layer | Implementation |
|-------|----------------|
| Authentication | Keycloak OIDC/OAuth2, JWT tokens |
| Service-to-service | JWT validation at API Gateway, mTLS (planned) |
| Secrets | HashiCorp Vault (dev mode), K8s Secrets (prod) |
| Rate limiting | Redis token bucket at API Gateway |
| Audit logging | Every auth event → Kafka → Elasticsearch |
| Charger identity | ISO 15118 certificate management (device-management-service) |

---

## API Endpoints Summary

| Service | Base Path | Swagger |
|---------|-----------|---------|
| api-gateway | `http://localhost:8000` | N/A (proxy) |
| auth-service | `/api/v1/auth` | `:8081/swagger-ui.html` |
| station-service | `/api/v1/stations` | `:8082/swagger-ui.html` |
| session-service | `/api/v1/sessions` | `:8083/swagger-ui.html` |
| billing-service | `/api/v1/billing` | `:8084/swagger-ui.html` |
| payment-service | `/api/v1/payments` | `:8085/swagger-ui.html` |
| roaming-service | `/ocpi/2.2.1` | `:8086/swagger-ui.html` |
| device-management | `/api/v1/devices` | `:8090/swagger-ui.html` |
| smart-charging | `/api/v1/smart-charging` | `:8091/swagger-ui.html` |
| fleet-service | `/api/v1/fleets` | `:8092/swagger-ui.html` |
| analytics-service | `/api/v1/analytics` | `:8093/swagger-ui.html` |
| route-planning | `/api/v1/route` | `:8094/swagger-ui.html` |
| fraud-detection | `/api/v1/fraud` | `:8095/swagger-ui.html` |
| energy-reporting | `/api/v1/reports` | `:8096/swagger-ui.html` |
| ml-service | `/api/v1/ml` | `:8097/docs` |
| ocpp-simulator | `/api/v1/simulator` | `:8098/swagger-ui.html` |
