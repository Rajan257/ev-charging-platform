-- ============================================================
-- V2: Charging Stations, Connectors, CPO Networks
-- ============================================================

-- Charge Point Operator Networks
CREATE TABLE cpo_networks (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            VARCHAR(255) NOT NULL,
    code            VARCHAR(20) UNIQUE NOT NULL,  -- e.g. TATA, ATHER, BPCL
    website         TEXT,
    support_email   VARCHAR(255),
    support_phone   VARCHAR(20),
    party_id        VARCHAR(3),                   -- OCPI party_id
    country_code    VARCHAR(2) DEFAULT 'IN',
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    logo_url        TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trigger_cpo_networks_updated_at
    BEFORE UPDATE ON cpo_networks
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Charging Stations / Locations
CREATE TABLE charging_stations (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cpo_network_id      UUID NOT NULL REFERENCES cpo_networks(id),
    name                VARCHAR(255) NOT NULL,
    address             TEXT NOT NULL,
    city                VARCHAR(100) NOT NULL,
    state               VARCHAR(100) NOT NULL,
    pincode             VARCHAR(10) NOT NULL,
    country             VARCHAR(2) NOT NULL DEFAULT 'IN',
    latitude            DECIMAL(10, 8) NOT NULL,
    longitude           DECIMAL(11, 8) NOT NULL,
    location_type       VARCHAR(50) DEFAULT 'PUBLIC'
                        CHECK (location_type IN ('PUBLIC', 'PRIVATE', 'SEMI_PUBLIC', 'RESTRICTED')),
    amenities           TEXT[],              -- e.g. {wifi, restroom, cafe}
    operating_hours     JSONB,               -- {"mon_fri": "08:00-22:00", "sat_sun": "10:00-20:00"}
    parking_type        VARCHAR(50),
    phone               VARCHAR(20),
    images              TEXT[],
    ocpp_endpoint       TEXT,               -- ws://host:port/ocpp/
    status              VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE'
                        CHECK (status IN ('AVAILABLE', 'BUSY', 'OFFLINE', 'MAINTENANCE', 'UNKNOWN')),
    last_heartbeat      TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_stations_cpo_id  ON charging_stations(cpo_network_id);
CREATE INDEX idx_stations_city    ON charging_stations(city);
CREATE INDEX idx_stations_status  ON charging_stations(status);
-- PostGIS-like bbox search using lat/lng
CREATE INDEX idx_stations_location ON charging_stations(latitude, longitude);

CREATE TRIGGER trigger_stations_updated_at
    BEFORE UPDATE ON charging_stations
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Connectors (EVSE)
CREATE TABLE connectors (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    station_id          UUID NOT NULL REFERENCES charging_stations(id) ON DELETE CASCADE,
    evse_id             VARCHAR(100) UNIQUE NOT NULL,  -- e.g. IN*TATA*E001*1
    connector_number    INT NOT NULL,
    standard            VARCHAR(50) NOT NULL
                        CHECK (standard IN ('CCS2', 'CHAdeMO', 'TYPE2', 'BHARAT_AC', 'BHARAT_DC', 'GB_T', 'TYPE1')),
    power_type          VARCHAR(20) NOT NULL
                        CHECK (power_type IN ('AC_1_PHASE', 'AC_3_PHASE', 'DC')),
    max_voltage         INT NOT NULL,        -- Volts
    max_amperage        INT NOT NULL,        -- Amperes
    max_electric_power  INT NOT NULL,        -- Watts (computed: voltage * amperage)
    status              VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE'
                        CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'RESERVED', 'FAULTED', 'UNAVAILABLE', 'PREPARING', 'CHARGING', 'FINISHING', 'SUSPENDED_EV', 'SUSPENDED_EVSE')),
    tariff_id           UUID,               -- FK to tariffs (added in V4)
    floor_level         VARCHAR(10),
    physical_reference  VARCHAR(50),
    last_status_update  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_connectors_station_id ON connectors(station_id);
CREATE INDEX idx_connectors_evse_id    ON connectors(evse_id);
CREATE INDEX idx_connectors_status     ON connectors(status);
CREATE INDEX idx_connectors_standard   ON connectors(standard);

CREATE TRIGGER trigger_connectors_updated_at
    BEFORE UPDATE ON connectors
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- OCPP Charge Point Registry (physical charger boxes)
CREATE TABLE charge_points (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    station_id          UUID NOT NULL REFERENCES charging_stations(id),
    charge_point_id     VARCHAR(100) UNIQUE NOT NULL,  -- OCPP identity
    model               VARCHAR(100),
    vendor              VARCHAR(100),
    serial_number       VARCHAR(100),
    firmware_version    VARCHAR(50),
    ocpp_version        VARCHAR(20) DEFAULT '2.0.1',
    connection_status   VARCHAR(20) DEFAULT 'DISCONNECTED'
                        CHECK (connection_status IN ('CONNECTED', 'DISCONNECTED', 'BOOTING')),
    last_boot_at        TIMESTAMPTZ,
    last_heartbeat_at   TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_charge_points_station_id ON charge_points(station_id);

-- Reservations
CREATE TABLE reservations (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES users(id),
    connector_id    UUID NOT NULL REFERENCES connectors(id),
    reservation_id  INT NOT NULL,              -- OCPP reservation ID
    start_time      TIMESTAMPTZ NOT NULL,
    expiry_time     TIMESTAMPTZ NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'EXPIRED', 'USED', 'CANCELLED')),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
