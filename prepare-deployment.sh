#!/bin/bash

# Script para desplegar la imagen Docker en el servidor
set -e

echo "🚀 Preparando despliegue en servidor..."

# Verificar que la imagen existe
if [ ! -f "backend-api.tar" ]; then
    echo "❌ Archivo de imagen no encontrado. Creando..."
    docker save backend-api:latest -o backend-api.tar
fi

echo "✅ Imagen Docker preparada: backend-api.tar"
echo "📊 Tamaño del archivo: $(du -h backend-api.tar | cut -f1)"

# Crear script de despliegue para el servidor
cat > deploy-server.sh << 'EOF'
#!/bin/bash
set -e

echo "🚀 Iniciando despliegue en servidor..."

# Detener contenedores existentes
echo "🛑 Deteniendo contenedores existentes..."
docker-compose -p app-prod -f docker-compose.prod.yml down --remove-orphans || true
docker stop backend-prod 2>/dev/null || true
docker rm backend-prod 2>/dev/null || true

# Limpiar imágenes antiguas
echo "🧹 Limpiando imágenes antiguas..."
docker rmi backend-api:latest 2>/dev/null || true
docker image prune -f || true

# Cargar nueva imagen
echo "📦 Cargando nueva imagen..."
docker load -i backend-api.tar

# Verificar que la imagen se cargó
echo "✅ Verificando imagen cargada..."
docker images | grep backend-api

# Crear directorio de logs si no existe
mkdir -p /home/appuser/app/logs

# Ejecutar nuevo contenedor
echo "🚀 Iniciando nuevo contenedor..."
docker run -d \
  --name backend-prod \
  --restart unless-stopped \
  -p 8081:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -v /home/appuser/app/logs:/home/appuser/app/logs \
  backend-api:latest

# Esperar a que el contenedor esté listo
echo "⏳ Esperando a que el contenedor esté listo..."
sleep 30

# Verificar estado del contenedor
echo "🔍 Verificando estado del contenedor..."
docker ps | grep backend-prod

# Verificar health check
echo "🏥 Verificando health check..."
for i in {1..10}; do
    if curl -f -s http://localhost:8081/actuator/health >/dev/null 2>&1; then
        echo "✅ Backend funcionando correctamente!"
        break
    fi
    if [ $i -eq 10 ]; then
        echo "❌ Backend no responde después de 10 intentos"
        docker logs backend-prod --tail 20
        exit 1
    fi
    echo "⏳ Intento $i/10..."
    sleep 10
done

echo "🎉 Despliegue completado exitosamente!"
echo "Backend disponible en: http://localhost:8081"
EOF

chmod +x deploy-server.sh

echo "✅ Script de despliegue creado: deploy-server.sh"
echo ""
echo "📋 Para desplegar en el servidor:"
echo "1. Sube el archivo backend-api.tar al servidor"
echo "2. Sube el archivo deploy-server.sh al servidor"
echo "3. Ejecuta: ./deploy-server.sh"
echo ""
echo "🔧 Comandos para subir archivos:"
echo "scp -i ~/Downloads/llave3.pem backend-api.tar ec2-user@ec2-18-189-28-131.us-east-2.compute.amazonaws.com:/home/ec2-user/"
echo "scp -i ~/Downloads/llave3.pem deploy-server.sh ec2-user@ec2-18-189-28-131.us-east-2.compute.amazonaws.com:/home/ec2-user/"
echo "ssh -i ~/Downloads/llave3.pem ec2-user@ec2-18-189-28-131.us-east-2.compute.amazonaws.com './deploy-server.sh'"
