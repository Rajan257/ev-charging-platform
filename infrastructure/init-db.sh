#!/bin/bash
set -e

# ==============================================================
# EV Roaming Hub India — PostgreSQL Database Initialization
# Creates all service databases on the single PostgreSQL instance
# ==============================================================

echo "Initializing EV Roaming Hub databases..."

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL

    -- ── Original service databases ─────────────────────────
    CREATE DATABASE evroaming_auth;
    CREATE DATABASE evroaming_station;
    CREATE DATABASE evroaming_session;
    CREATE DATABASE evroaming_billing;
    CREATE DATABASE evroaming_payment;
    CREATE DATABASE evroaming_roaming;
    CREATE DATABASE evroaming_settlement;
    CREATE DATABASE evroaming_notification;

    -- ── Extended platform service databases ────────────────
    CREATE DATABASE evroaming_device;
    CREATE DATABASE evroaming_smartcharging;
    CREATE DATABASE evroaming_fleet;
    CREATE DATABASE evroaming_analytics;
    CREATE DATABASE evroaming_fraud;
    CREATE DATABASE evroaming_energy;

    -- Keycloak database
    CREATE DATABASE keycloak;

    -- Grant all privileges to the main user
    GRANT ALL PRIVILEGES ON DATABASE evroaming_auth         TO evroaming;
    GRANT ALL PRIVILEGES ON DATABASE evroaming_station      TO evroaming;
    GRANT ALL PRIVILEGES ON DATABASE evroaming_session      TO evroaming;
    GRANT ALL PRIVILEGES ON DATABASE evroaming_billing      TO evroaming;
    GRANT ALL PRIVILEGES ON DATABASE evroaming_payment      TO evroaming;
    GRANT ALL PRIVILEGES ON DATABASE evroaming_roaming      TO evroaming;
    GRANT ALL PRIVILEGES ON DATABASE evroaming_settlement   TO evroaming;
    GRANT ALL PRIVILEGES ON DATABASE evroaming_notification TO evroaming;
    GRANT ALL PRIVILEGES ON DATABASE evroaming_device       TO evroaming;
    GRANT ALL PRIVILEGES ON DATABASE evroaming_smartcharging TO evroaming;
    GRANT ALL PRIVILEGES ON DATABASE evroaming_fleet        TO evroaming;
    GRANT ALL PRIVILEGES ON DATABASE evroaming_analytics    TO evroaming;
    GRANT ALL PRIVILEGES ON DATABASE evroaming_fraud        TO evroaming;
    GRANT ALL PRIVILEGES ON DATABASE evroaming_energy       TO evroaming;
    GRANT ALL PRIVILEGES ON DATABASE keycloak               TO evroaming;

EOSQL

echo "All databases initialized successfully."
