-- Fix discrepancies between V1 schema and JPA entities

-- RfidCard fixes
ALTER TABLE rfid_cards RENAME COLUMN active TO is_active;
ALTER TABLE rfid_cards ADD COLUMN card_type VARCHAR(20) DEFAULT 'RFID';
ALTER TABLE rfid_cards ADD COLUMN created_at TIMESTAMP DEFAULT NOW();

-- Vehicle fixes
ALTER TABLE vehicles RENAME COLUMN is_primary TO is_default;
ALTER TABLE vehicles DROP COLUMN battery_capacity_kwh;
ALTER TABLE vehicles DROP COLUMN connector_types;
ALTER TABLE vehicles ADD COLUMN vin VARCHAR(17) UNIQUE;
ALTER TABLE vehicles ADD COLUMN contract_id VARCHAR(255) UNIQUE;
ALTER TABLE vehicles ADD COLUMN pnc_certificate TEXT;
