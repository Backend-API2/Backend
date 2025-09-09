#!/bin/bash

# Script para iniciar la aplicación Spring Boot
# Uso: ./start.sh

APP_DIR="$HOME/app"
# Tomar siempre el JAR más reciente
JAR_FILE=$(ls -t $APP_DIR/Backend-*.jar 2>/dev/null | head -1)
PID_FILE="$APP_DIR/app.pid"
LOG_FILE="$APP_DIR/app.log"

# Verificar que el JAR existe
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ Error: No se encontró el archivo JAR en $APP_DIR"
    exit 1
fi

# Verificar si ya está corriendo
if [ -f "$PID_FILE" ] && kill -0 $(cat "$PID_FILE") 2>/dev/null; then
    echo "⚠️  La aplicación ya está corriendo (PID: $(cat $PID_FILE))"
    exit 1
fi

# Configurar variables de entorno
export SPRING_PROFILES_ACTIVE=prod
export JAVA_OPTS="-Xmx512m -Xms256m -Dserver.port=8080"

echo "🚀 Iniciando aplicación..."
echo "📁 JAR: $JAR_FILE"
echo "📋 Logs: $LOG_FILE"

# Ejecutar aplicación en background
nohup java $JAVA_OPTS -jar "$JAR_FILE" > "$LOG_FILE" 2>&1 &

# Guardar PID
echo $! > "$PID_FILE"

echo "✅ Aplicación iniciada (PID: $!)"
echo "📋 Para ver logs: tail -f $LOG_FILE"
echo "🛑 Para detener: ./stop.sh"
