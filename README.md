# EV Roaming Hub India

EV Roaming Hub India is an enterprise-grade, microservice-based EV charging ecosystem. It serves as a unified platform connecting Charge Point Operators (CPOs), Mobility Service Providers (MSPs), and EV drivers through standard protocols like OCPP and OCPI.

## Architecture & Features

This platform is built with a highly scalable architecture to support a robust EV ecosystem:

- **17+ Microservices**: Built with Spring Boot (Java 17) encompassing Core Auth, Station Management, Billing, Payments, Fleet Management, Fraud Detection, Energy Reporting, Smart Charging, and more.
- **Frontend**: A modern, responsive dashboard built with React, TypeScript, and Vite.
- **Machine Learning (Python/FastAPI)**: Predictive analytics for charger failure, demand forecasting, optimal charger placement, and fraud anomaly detection.
- **Event-Driven Communications**: Apache Kafka handles asynchronous messaging across all microservices with Confluent Schema Registry.
- **Database & Observability**: Powered by PostgreSQL for relational data, ClickHouse for high-performance analytics, Redis for caching, and a complete observability stack (Prometheus, Grafana, Jaeger, ELK).

For full details on the system design and microservice boundaries, please refer to the [Architecture Documentation](docs/ARCHITECTURE.md).

## Project Structure

```
├── backend/                  # Java/Spring Boot Microservices
│   ├── api-gateway/          # Edge gateway and routing
│   ├── auth-service/         # Keycloak integration and user management
│   ├── station-service/      # OCPP 2.0.1 charger communications
│   ├── billing-service/      # Invoicing and CDR generation
│   ├── ...                   # (Other microservices)
├── frontend/                 # React + Vite web dashboard
├── ml-service/               # Python ML predictive engine
├── infrastructure/           # Docker Compose, K8s manifests, and monitoring configs
├── database/                 # Flyway SQL migration scripts
└── docs/                     # Architecture and Kafka event schema documentation
```

## Getting Started

### Prerequisites
- Docker and Docker Compose
- Java 17 (for local development)
- Maven
- Node.js 20+

### Running the Platform Locally

The platform is designed to be fully deployable via Docker Compose.

1. **Build the Java Microservices:**
   ```bash
   cd backend
   mvn clean package -DskipTests
   ```

2. **Start the Infrastructure and Base Services:**
   ```bash
   cd ../infrastructure
   docker-compose up -d
   ```

3. **Start the Extended Services (Observability, ML, Fleet, Fraud, etc.):**
   ```bash
   docker-compose -f docker-compose.yml -f docker-compose-extended.yml up -d
   ```

### Accessing the Applications
- **Web Dashboard**: `http://localhost:3000`
- **Grafana Dashboards**: `http://localhost:3001`
- **API Gateway**: `http://localhost:8000`

## License
Proprietary / All Rights Reserved.
