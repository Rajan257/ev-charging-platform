-- Station Service Schema
CREATE TABLE IF NOT EXISTS cpo_networks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    support_email VARCHAR(200),
    support_phone VARCHAR(20),
    website VARCHAR(300),
    party_id VARCHAR(50),
    country_code VARCHAR(10) DEFAULT 'IN',
    active BOOLEAN DEFAULT TRUE,
    logo_url TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS charging_stations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cpo_network_id UUID NOT NULL REFERENCES cpo_networks(id),
    name VARCHAR(200) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    location_type VARCHAR(50),
    operating_hours JSONB,
    phone VARCHAR(20),
    ocpp_endpoint VARCHAR(300),
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    last_heartbeat TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS station_images (
    station_id UUID REFERENCES charging_stations(id) ON DELETE CASCADE,
    image_url VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS connectors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id UUID NOT NULL REFERENCES charging_stations(id) ON DELETE CASCADE,
    evse_id VARCHAR(100) UNIQUE NOT NULL,
    connector_number INTEGER NOT NULL,
    standard VARCHAR(30) NOT NULL,
    power_type VARCHAR(20) NOT NULL,
    max_voltage INTEGER,
    max_amperage INTEGER,
    max_electric_power INTEGER NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    ocpp_connector_id INTEGER,
    tariff_id UUID,
    last_status_update TIMESTAMP DEFAULT NOW(),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS charge_points (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id UUID REFERENCES charging_stations(id) ON DELETE CASCADE,
    charge_point_id VARCHAR(100) UNIQUE NOT NULL,
    model VARCHAR(100),
    vendor VARCHAR(100),
    serial_number VARCHAR(100),
    firmware_version VARCHAR(50),
    connection_status VARCHAR(20) DEFAULT 'DISCONNECTED',
    ocpp_version VARCHAR(10) DEFAULT '2.0.1',
    last_boot_at TIMESTAMP,
    last_heartbeat_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_stations_city ON charging_stations(city);
CREATE INDEX IF NOT EXISTS idx_stations_status ON charging_stations(status);
CREATE INDEX IF NOT EXISTS idx_connectors_station ON connectors(station_id);

-- Default CPO network
INSERT INTO cpo_networks (id, name, code, support_email, active)
VALUES (
    '00000000-0000-0001-0001-000000000001',
    'EV Roaming Hub India',
    'EVH',
    'ops@evroaminghub.in',
    true
) ON CONFLICT DO NOTHING;

-- Sample station
INSERT INTO charging_stations (id, cpo_network_id, name, address, city, state, pincode, latitude, longitude, status)
VALUES (
    '00000000-0000-0002-0001-000000000001',
    '00000000-0000-0001-0001-000000000001',
    'Connaught Place EV Hub',
    'Block A, Connaught Place',
    'New Delhi',
    'Delhi',
    '110001',
    28.6315,
    77.2167,
    'AVAILABLE'
) ON CONFLICT DO NOTHING;

-- Sample connector
INSERT INTO connectors (id, station_id, evse_id, connector_number, standard, power_type, max_electric_power, status)
VALUES (
    '00000000-0000-0003-0001-000000000001',
    '00000000-0000-0002-0001-000000000001',
    'IN*EVH*E00001',
    1,
    'CCS2',
    'DC',
    150,
    'AVAILABLE'
) ON CONFLICT DO NOTHING;
