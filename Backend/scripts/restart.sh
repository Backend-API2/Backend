#!/bin/bash

# Script para reiniciar la aplicación Spring Boot
# Uso: ./restart.sh

APP_DIR="/home/ubuntu/app"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "🔄 Reiniciando aplicación Spring Boot..."
echo "======================================="

# Detener aplicación
echo "1️⃣ Deteniendo aplicación..."
"$SCRIPT_DIR/stop.sh"

# Esperar un momento
sleep 3

# Iniciar aplicación
echo ""
echo "2️⃣ Iniciando aplicación..."
"$SCRIPT_DIR/start.sh"

# Verificar estado
echo ""
echo "3️⃣ Verificando estado..."
sleep 5
"$SCRIPT_DIR/status.sh"
