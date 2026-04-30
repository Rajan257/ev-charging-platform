-- ============================================================
-- V9: Sample Data for Testing
-- ============================================================

-- CPO Networks
INSERT INTO cpo_networks (id, name, code, website, support_email, party_id) VALUES
('a1b2c3d4-0001-0001-0001-000000000001', 'Tata Power EV Charging', 'TATA', 'https://tatapower.com', 'ev@tatapower.com', 'TAT'),
('a1b2c3d4-0002-0002-0002-000000000002', 'Ather Grid', 'ATHER', 'https://atherenergy.com', 'grid@atherenergy.com', 'ATH'),
('a1b2c3d4-0003-0003-0003-000000000003', 'BPCL Pulse', 'BPCL', 'https://bpcl.in', 'pulse@bpcl.in', 'BPC'),
('a1b2c3d4-0004-0004-0004-000000000004', 'ChargeZone', 'CHGZ', 'https://chargezone.in', 'ops@chargezone.in', 'CHZ'),
('a1b2c3d4-0005-0005-0005-000000000005', 'Fortum India', 'FORT', 'https://fortum.in', 'ev@fortum.in', 'FOR');

-- Charging Stations
INSERT INTO charging_stations (id, cpo_network_id, name, address, city, state, pincode, latitude, longitude, status) VALUES
('s001', 'a1b2c3d4-0001-0001-0001-000000000001', 'Tata Power - Connaught Place', 'Block A, Connaught Place', 'New Delhi', 'Delhi', '110001', 28.6315, 77.2167, 'AVAILABLE'),
('s002', 'a1b2c3d4-0001-0001-0001-000000000001', 'Tata Power - Cyber Hub', 'DLF Cyber Hub, Phase II', 'Gurugram', 'Haryana', '122002', 28.4949, 77.0896, 'AVAILABLE'),
('s003', 'a1b2c3d4-0002-0002-0002-000000000002', 'Ather Grid - Koramangala', '80 Feet Rd, Koramangala', 'Bengaluru', 'Karnataka', '560034', 12.9352, 77.6245, 'AVAILABLE'),
('s004', 'a1b2c3d4-0002-0002-0002-000000000002', 'Ather Grid - Indiranagar', '100 Feet Rd, Indiranagar', 'Bengaluru', 'Karnataka', '560038', 12.9784, 77.6408, 'BUSY'),
('s005', 'a1b2c3d4-0003-0003-0003-000000000003', 'BPCL Pulse - Bandra', 'Turner Road, Bandra West', 'Mumbai', 'Maharashtra', '400050', 19.0544, 72.8405, 'AVAILABLE'),
('s006', 'a1b2c3d4-0003-0003-0003-000000000003', 'BPCL Pulse - Powai', 'Hiranandani, Powai', 'Mumbai', 'Maharashtra', '400076', 19.1176, 72.9060, 'AVAILABLE'),
('s007', 'a1b2c3d4-0004-0004-0004-000000000004', 'ChargeZone - Kalyani Nagar', 'Kalyani Nagar, Pune', 'Pune', 'Maharashtra', '411006', 18.5462, 73.9013, 'AVAILABLE'),
('s008', 'a1b2c3d4-0005-0005-0005-000000000005', 'Fortum - Anna Salai', 'Anna Salai, Nungambakkam', 'Chennai', 'Tamil Nadu', '600006', 13.0604, 80.2596, 'AVAILABLE'),
('s009', 'a1b2c3d4-0001-0001-0001-000000000001', 'Tata Power - Salt Lake', 'Sector V, Salt Lake', 'Kolkata', 'West Bengal', '700091', 22.5726, 88.4312, 'MAINTENANCE'),
('s010', 'a1b2c3d4-0004-0004-0004-000000000004', 'ChargeZone - Gomti Nagar', 'Vibhuti Khand, Gomti Nagar', 'Lucknow', 'Uttar Pradesh', '226010', 26.8617, 81.0229, 'AVAILABLE');

-- Connectors
INSERT INTO connectors (id, station_id, evse_id, connector_number, standard, power_type, max_voltage, max_amperage, max_electric_power, status) VALUES
('c001', 's001', 'IN*TAT*E001*1', 1, 'CCS2',      'DC',        500, 100, 50000,  'AVAILABLE'),
('c002', 's001', 'IN*TAT*E001*2', 2, 'TYPE2',     'AC_3_PHASE', 415, 32, 22000,  'AVAILABLE'),
('c003', 's002', 'IN*TAT*E002*1', 1, 'CCS2',      'DC',        500, 100, 50000,  'AVAILABLE'),
('c004', 's002', 'IN*TAT*E002*2', 2, 'CHAdeMO',   'DC',        500, 100, 50000,  'AVAILABLE'),
('c005', 's003', 'IN*ATH*E003*1', 1, 'CCS2',      'DC',        500, 200, 100000, 'AVAILABLE'),
('c006', 's003', 'IN*ATH*E003*2', 2, 'BHARAT_DC', 'DC',        48,  100, 15000,  'AVAILABLE'),
('c007', 's004', 'IN*ATH*E004*1', 1, 'CCS2',      'DC',        500, 200, 100000, 'OCCUPIED'),
('c008', 's004', 'IN*ATH*E004*2', 2, 'TYPE2',     'AC_3_PHASE', 415, 32, 22000,  'AVAILABLE'),
('c009', 's005', 'IN*BPC*E005*1', 1, 'CCS2',      'DC',        500, 125, 60000,  'AVAILABLE'),
('c010', 's006', 'IN*BPC*E006*1', 1, 'CCS2',      'DC',        500, 125, 60000,  'AVAILABLE'),
('c011', 's007', 'IN*CHZ*E007*1', 1, 'CCS2',      'DC',        500, 250, 120000, 'AVAILABLE'),
('c012', 's008', 'IN*FOR*E008*1', 1, 'CCS2',      'DC',        500, 100, 50000,  'AVAILABLE'),
('c013', 's010', 'IN*CHZ*E010*1', 1, 'TYPE2',     'AC_3_PHASE', 415, 32, 22000,  'AVAILABLE');

-- Tariffs
INSERT INTO tariffs (id, cpo_network_id, name, price_per_kwh, price_per_min, flat_fee, gst_rate, valid_from) VALUES
('t001', 'a1b2c3d4-0001-0001-0001-000000000001', 'Tata Standard DC',  12.00, 0.50, 0, 18, NOW()),
('t002', 'a1b2c3d4-0001-0001-0001-000000000001', 'Tata AC Type 2',     8.00, 0.20, 0, 18, NOW()),
('t003', 'a1b2c3d4-0002-0002-0002-000000000002', 'Ather Grid Fast DC', 14.00, 0,   0, 18, NOW()),
('t004', 'a1b2c3d4-0003-0003-0003-000000000003', 'BPCL Pulse DC',      11.50, 0.30, 10, 18, NOW()),
('t005', 'a1b2c3d4-0004-0004-0004-000000000004', 'ChargeZone Ultra',   15.00, 0,   0, 18, NOW()),
('t006', 'a1b2c3d4-0005-0005-0005-000000000005', 'Fortum Standard',    10.00, 0.25, 0, 18, NOW());

-- Update connectors with tariff IDs
UPDATE connectors SET tariff_id = 't001' WHERE id IN ('c001', 'c003', 'c004');
UPDATE connectors SET tariff_id = 't002' WHERE id IN ('c002', 'c008', 'c013');
UPDATE connectors SET tariff_id = 't003' WHERE id IN ('c005', 'c006', 'c007');
UPDATE connectors SET tariff_id = 't004' WHERE id IN ('c009', 'c010');
UPDATE connectors SET tariff_id = 't005' WHERE id IN ('c011');
UPDATE connectors SET tariff_id = 't006' WHERE id IN ('c012');

-- Demo Users
INSERT INTO users (id, email, phone, full_name, role, email_verified) VALUES
('u001', 'driver1@evroaminghub.in', '+919876543210', 'Priya Sharma',     'DRIVER',         TRUE),
('u002', 'driver2@evroaminghub.in', '+919876543211', 'Rahul Gupta',      'DRIVER',         TRUE),
('u003', 'admin@evroaminghub.in',   '+919876543212', 'Admin User',       'PLATFORM_ADMIN', TRUE),
('u004', 'cpo@tatapower.com',       '+919876543213', 'Tata CPO Admin',   'CPO_ADMIN',      TRUE),
('u005', 'cpo@atherenergy.com',     '+919876543214', 'Ather CPO Admin',  'CPO_ADMIN',      TRUE);

-- RFID Cards
INSERT INTO rfid_cards (user_id, uid, label) VALUES
('u001', 'AA:BB:CC:DD:01', 'My Primary RFID'),
('u002', 'AA:BB:CC:DD:02', 'Tesla Key Card');

-- Wallets
INSERT INTO wallets (user_id, balance) VALUES
('u001', 500.00),
('u002', 1200.00),
('u003', 0.00),
('u004', 0.00),
('u005', 0.00);

-- Roaming Partner (self-reference for demo)
INSERT INTO roaming_partners (id, name, party_id, country_code, role, ocpi_base_url, auth_status) VALUES
('rp001', 'ChargePoint Global', 'CPG', 'US', 'MSP', 'https://api.chargepoint.com/ocpi/2.2', 'ACTIVE'),
('rp002', 'Electrify Asia', 'ELA', 'SG', 'CPO', 'https://api.electrifyasia.com/ocpi/2.2', 'ACTIVE');

-- CPO Contracts
INSERT INTO cpo_contracts (cpo_network_id, revenue_share_pct, platform_fee_pct, roaming_fee_pct, settlement_cycle, valid_from) VALUES
('a1b2c3d4-0001-0001-0001-000000000001', 85.00, 10.00, 5.00, 'WEEKLY', '2024-01-01'),
('a1b2c3d4-0002-0002-0002-000000000002', 82.00, 12.00, 6.00, 'WEEKLY', '2024-01-01'),
('a1b2c3d4-0003-0003-0003-000000000003', 84.00, 11.00, 5.00, 'MONTHLY', '2024-01-01'),
('a1b2c3d4-0004-0004-0004-000000000004', 80.00, 13.00, 7.00, 'WEEKLY', '2024-01-01'),
('a1b2c3d4-0005-0005-0005-000000000005', 83.00, 11.00, 6.00, 'MONTHLY', '2024-01-01');
