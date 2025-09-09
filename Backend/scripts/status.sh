#!/bin/bash

# Script para verificar el estado de la aplicación Spring Boot
# Uso: ./status.sh

APP_DIR="$HOME/app"
PID_FILE="$APP_DIR/app.pid"
LOG_FILE="$APP_DIR/app.log"

echo "🔍 Estado de la aplicación Spring Boot"
echo "======================================"

# Verificar archivo PID
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    echo "📄 PID file: $PID_FILE (PID: $PID)"
    
    # Verificar si el proceso existe
    if kill -0 "$PID" 2>/dev/null; then
        echo "✅ Estado: CORRIENDO"
        echo "🆔 PID: $PID"
        
        # Mostrar información del proceso
        echo "📊 Información del proceso:"
        ps -p "$PID" -o pid,ppid,cmd,etime,pcpu,pmem
        
        # Verificar puerto 8080
        if netstat -tlnp 2>/dev/null | grep -q ":8080 "; then
            echo "🌐 Puerto 8080: ACTIVO"
        else
            echo "⚠️  Puerto 8080: NO DETECTADO"
        fi
        
        # Verificar health endpoint real
        echo "🏥 Health check:"
        if curl -s -f http://localhost:8080/api/auth/login >/dev/null 2>&1; then
            echo "✅ Aplicación responde correctamente"
        else
            echo "❌ Aplicación no responde en health endpoint"
        fi
        
    else
        echo "❌ Estado: DETENIDO (PID file existe pero proceso no)"
        rm -f "$PID_FILE"
    fi
else
    echo "❌ Estado: DETENIDO (No hay PID file)"
fi

# Mostrar logs recientes
if [ -f "$LOG_FILE" ]; then
    echo ""
    echo "📋 Logs recientes (últimas 10 líneas):"
    echo "--------------------------------------"
    tail -10 "$LOG_FILE"
else
    echo "⚠️  No se encontró archivo de logs"
fi

# Mostrar uso de memoria
echo ""
echo "💾 Uso de memoria del sistema:"
free -h

# Mostrar espacio en disco
echo ""
echo "💿 Espacio en disco:"
df -h "$APP_DIR"
