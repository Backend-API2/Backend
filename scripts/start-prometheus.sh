# Script para levantar Prometheus localmente
#!/bin/bash

echo "🚀 Iniciando Prometheus para monitoreo del Backend Spring Boot..."

# Verificar si Docker está instalado
if ! command -v docker &> /dev/null; then
    echo "❌ Docker no está instalado. Por favor instala Docker primero."
    echo "📖 Instrucciones: https://docs.docker.com/get-docker/"
    exit 1
fi

# Verificar si el archivo de configuración existe
if [ ! -f "prometheus.yml" ]; then
    echo "❌ Archivo prometheus.yml no encontrado en el directorio actual."
    exit 1
fi

# Verificar si el backend está corriendo
echo "🔍 Verificando que el backend esté corriendo..."
if ! curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "❌ El backend Spring Boot no está corriendo en localhost:8080"
    echo "💡 Por favor inicia el backend primero con: mvn spring-boot:run"
    exit 1
fi

echo "✅ Backend detectado correctamente"

# Iniciar Prometheus
echo "🐳 Iniciando Prometheus..."
docker run -d \
    --name prometheus-backend \
    -p 9090:9090 \
    -v $(pwd)/prometheus.yml:/etc/prometheus/prometheus.yml \
    prom/prometheus:latest \
    --config.file=/etc/prometheus/prometheus.yml \
    --storage.tsdb.path=/prometheus \
    --web.console.libraries=/etc/prometheus/console_libraries \
    --web.console.templates=/etc/prometheus/consoles \
    --storage.tsdb.retention.time=200h \
    --web.enable-lifecycle

if [ $? -eq 0 ]; then
    echo "✅ Prometheus iniciado correctamente!"
    echo ""
    echo "📊 Accesos disponibles:"
    echo "   • Prometheus UI: http://localhost:9090"
    echo "   • Backend Metrics: http://localhost:8080/actuator/prometheus"
    echo "   • Backend Health: http://localhost:8080/actuator/health"
    echo ""
    echo "🔍 Para verificar que está funcionando:"
    echo "   1. Ve a http://localhost:9090"
    echo "   2. En el menú Status > Targets"
    echo "   3. Verifica que 'spring-boot-backend' esté UP"
    echo ""
    echo "🛑 Para detener Prometheus:"
    echo "   docker stop prometheus-backend && docker rm prometheus-backend"
else
    echo "❌ Error al iniciar Prometheus"
    exit 1
fi
