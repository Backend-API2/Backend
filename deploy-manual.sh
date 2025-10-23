#!/bin/bash

# Script para despliegue manual
set -e

echo "🚀 Iniciando despliegue manual..."

# Verificar que el JAR existe
if [ ! -f "Backend-0.0.1-SNAPSHOT.jar" ]; then
    echo "❌ JAR no encontrado. Compilando..."
    cd Backend
    ./mvnw clean package -DskipTests
    cp target/Backend-0.0.1-SNAPSHOT.jar ../
    cd ..
fi

echo "✅ JAR encontrado: Backend-0.0.1-SNAPSHOT.jar"

# Construir imagen Docker localmente
echo "🔨 Construyendo imagen Docker..."
docker build -t backend-api:latest ./Backend

echo "✅ Imagen construida exitosamente"

# Mostrar información de la imagen
echo "📊 Información de la imagen:"
docker images | grep backend-api

echo "🎉 Despliegue manual completado!"
echo "Para desplegar en producción, ejecuta:"
echo "docker run -d -p 8080:8080 --name backend-prod backend-api:latest"
