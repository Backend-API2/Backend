#!/bin/bash

# Script para detener la aplicación Spring Boot
# Uso: ./stop.sh

APP_DIR="$HOME/app"
PID_FILE="$APP_DIR/app.pid"

# Verificar si el archivo PID existe
if [ ! -f "$PID_FILE" ]; then
    echo "⚠️  No se encontró archivo PID. Buscando proceso por nombre..."
    
    # Buscar proceso por nombre
    PID=$(pgrep -f "Backend-.*.jar")
    if [ -z "$PID" ]; then
        echo "ℹ️  No se encontró la aplicación corriendo"
        exit 0
    fi
else
    PID=$(cat "$PID_FILE")
fi

# Verificar si el proceso existe
if ! kill -0 "$PID" 2>/dev/null; then
    echo "ℹ️  La aplicación no está corriendo (PID: $PID)"
    rm -f "$PID_FILE"
    exit 0
fi

echo "🛑 Deteniendo aplicación (PID: $PID)..."

# Intentar terminación graceful
kill "$PID"

# Esperar hasta 30 segundos
for i in {1..30}; do
    if ! kill -0 "$PID" 2>/dev/null; then
        echo "✅ Aplicación detenida exitosamente"
        rm -f "$PID_FILE"
        exit 0
    fi
    sleep 1
done

# Si no se detiene, forzar terminación
echo "⚠️  Forzando terminación..."
kill -9 "$PID" 2>/dev/null

if ! kill -0 "$PID" 2>/dev/null; then
    echo "✅ Aplicación detenida forzadamente"
    rm -f "$PID_FILE"
else
    echo "❌ Error: No se pudo detener la aplicación"
    exit 1
fi
