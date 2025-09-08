INSERT INTO card_bins (bin, bank_name, card_type, is_active) VALUES 
('454', 'Banco Galicia', 'VISA', true),
('455', 'Banco Francés', 'VISA', true),
('515', 'Banco Santander', 'MASTERCARD', true),
('520', 'Banco Macro', 'MASTERCARD', true),
('422', 'Banco Nación', 'VISA', true),
('527', 'Banco HSBC', 'MASTERCARD', true),
('456', 'Banco Ciudad', 'VISA', true),
('377', 'American Express', 'AMERICAN_EXPRESS', true),
('345', 'American Express', 'AMERICAN_EXPRESS', true);


-- Tarjetas de PRUEBA (DEV). NO USAR EN PRODUCCIÓN.
-- Estructura: brand, pan_masked, pan_sha256, cvv_sha256, bin3, last4, is_active

INSERT INTO test_cards (brand, pan_masked, pan_sha256, cvv_sha256, bin3, last4, is_active) VALUES
-- VISA (BIN 454 / 455 / 422 / 456)
('VISA', '454123******0123', SHA2('4541234567890123', 256), SHA2('123', 256), '454', '0123', true),
('VISA', '455987******0123', SHA2('4559876543210123', 256), SHA2('737', 256), '455', '0123', true),
('VISA', '422987******1223', SHA2('4229870001111223', 256), SHA2('321', 256), '422', '1223', true),

-- MASTERCARD (BIN 515 / 520 / 527)
('MASTERCARD', '515000******3334', SHA2('5150001112223334', 256), SHA2('123', 256), '515', '3334', true),
('MASTERCARD', '520123******3123', SHA2('5201231231233123', 256), SHA2('456', 256), '520', '3123', true),
('MASTERCARD', '527555******8882', SHA2('5275556667778882', 256), SHA2('753', 256), '527', '8882', true),

-- VISA (BIN 456) extra para llegar a 10
('VISA', '456111******4445', SHA2('4561112223334445', 256), SHA2('999', 256), '456', '4445', true),

-- AMERICAN EXPRESS (BIN 377 / 345) - 15 dígitos y CVV de 4
('AMERICAN_EXPRESS', '377123******9012', SHA2('377123456789012', 256), SHA2('1234', 256), '377', '9012', true),
('AMERICAN_EXPRESS', '345987******1098', SHA2('345987654321098', 256), SHA2('4321', 256), '345', '1098', true),

-- Otra VISA (BIN 454) para completar 10
('VISA', '454999******6661', SHA2('4549998887776661', 256), SHA2('555', 256), '454', '6661', true);