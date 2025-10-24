#!/bin/bash

# Script para probar la integración con matching/solicitudes
# Este script prueba la suscripción y el procesamiento de solicitudes de pago

echo "🧪 Probando integración con Matching/Solicitudes"
echo "=============================================="

# Configuración
BASE_URL="http://localhost:8080"
API_ENDPOINT="$BASE_URL/api/data/subscriptions"

echo ""
echo "1. Verificando estado de conexión al CORE Hub..."
curl -s "$API_ENDPOINT/connection" | jq '.'

echo ""
echo "2. Verificando estado de suscripciones actuales..."
curl -s "$API_ENDPOINT/status" | jq '.'

echo ""
echo "3. Suscribiéndose a solicitudes de pago de matching..."
curl -s -X POST "$API_ENDPOINT/subscribe-matching-payments" | jq '.'

echo ""
echo "4. Verificando estado de suscripciones actualizado..."
curl -s "$API_ENDPOINT/status" | jq '.'

echo ""
echo "5. Probando webhook de matching con datos de ejemplo..."
curl -s -X POST "$BASE_URL/api/core/webhook/matching-payment-requests" \
  -H "Content-Type: application/json" \
  -d '{
    "messageId": "test-matching-123",
    "timestamp": "2025-01-27T20:30:00.000Z",
    "source": "matching",
    "destination": {
      "channel": "matching.pago.emitida",
      "eventName": "emitida"
    },
    "payload": {
      "squad": "Matching y Agenda",
      "topico": "Pago",
      "evento": "Solicitud Pago Emitida",
      "cuerpo": {
        "idCorrelacion": "PED-TEST-123",
        "idUsuario": 999,
        "idPrestador": 1,
        "idSolicitud": 555,
        "montoSubtotal": 1000.00,
        "impuestos": 0.00,
        "comisiones": 0.00,
        "moneda": "ARS",
        "metodoPreferido": "MERCADO_PAGO"
      }
    }
  }' | jq '.'

echo ""
echo "6. Verificando salud del webhook..."
curl -s "$BASE_URL/api/core/webhook/health" | jq '.'

echo ""
echo "✅ Pruebas completadas!"
echo ""
echo "📋 Integración implementada:"
echo "   - ✅ DTO para PaymentRequestMessage"
echo "   - ✅ Servicio PaymentRequestProcessorService"
echo "   - ✅ Webhook endpoint /api/core/webhook/matching-payment-requests"
echo "   - ✅ Suscripción al tópico matching.pago.emitida"
echo "   - ✅ Búsqueda en user_data y prestador_data"
echo "   - ✅ Procesamiento de solicitudes de pago"
echo ""
echo "🔍 Para ver logs en tiempo real:"
echo "   journalctl --since '5 minutes ago' | grep -E '(matching|PaymentRequest|🔄|✅|❌)'"
