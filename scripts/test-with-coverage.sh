#!/bin/bash

# 🧪 Script de Testing con Coverage - Backend API
# Este script ejecuta los tests unitarios y genera reportes de cobertura

set -e  # Salir si cualquier comando falla

echo "🧪 INICIANDO TESTS CON COBERTURA"
echo "================================"

# Verificar que estamos en el directorio correcto
if [ ! -f "Backend/pom.xml" ]; then
    echo "❌ Error: No se encontró Backend/pom.xml"
    echo "💡 Asegúrate de ejecutar este script desde la raíz del proyecto"
    exit 1
fi

# Cambiar al directorio Backend
cd Backend

echo "📁 Directorio de trabajo: $(pwd)"
echo "☕ Verificando Java..."
java -version

echo ""
echo "🧹 Limpiando builds anteriores..."
mvn clean

echo ""
echo "🧪 Ejecutando tests con cobertura..."
mvn test jacoco:report

echo ""
echo "📊 Generando reporte de cobertura..."
mvn jacoco:report

echo ""
echo "🔍 Verificando umbrales de cobertura..."
if mvn jacoco:check; then
    echo "✅ Cobertura dentro de los umbrales establecidos"
else
    echo "⚠️ Cobertura por debajo de los umbrales mínimos"
    echo "📋 Umbrales actuales:"
    echo "   - Instrucciones: ≥60%"
    echo "   - Ramas: ≥50%"
fi

echo ""
echo "📁 Reportes generados:"
echo "   - HTML: target/site/jacoco/index.html"
echo "   - XML: target/site/jacoco/jacoco.xml"
echo "   - CSV: target/site/jacoco/jacoco.csv"

echo ""
echo "🌐 Para ver el reporte HTML:"
echo "   open target/site/jacoco/index.html"

echo ""
echo "✅ TESTS CON COBERTURA COMPLETADOS"
echo "=================================="
