-- Migration: Add descripcion and descripcion_solicitud columns to payments table
-- Date: 2025-11-14
-- Description: Add dedicated columns for payment and solicitud descriptions instead of storing in metadata

ALTER TABLE payments 
ADD COLUMN IF NOT EXISTS descripcion TEXT,
ADD COLUMN IF NOT EXISTS descripcion_solicitud TEXT;

-- Add comments for documentation
COMMENT ON COLUMN payments.descripcion IS 'Descripción del pago';
COMMENT ON COLUMN payments.descripcion_solicitud IS 'Descripción de la solicitud asociada al pago';

