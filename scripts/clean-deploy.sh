#!/bin/bash
# =============================================
# Script de despliegue con limpieza completa
# Para usar en CI/CD o cuando hay problemas de compilación
# =============================================

set -e

APP_DIR="/home/ec2-user/app"
COMPOSE_FILE="$APP_DIR/docker-compose.yml"

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para logging
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Limpieza completa del sistema
full_cleanup() {
    log "🧹 Iniciando limpieza completa del sistema..."
    
    cd "$APP_DIR"
    
    # 1. Detener todos los servicios
    log "🛑 Deteniendo todos los servicios..."
    docker-compose down --remove-orphans --volumes || warning "No había servicios ejecutándose"
    
    # 2. Limpiar archivos Maven residuales
    log "🗑️  Limpiando archivos Maven residuales..."
    if [ -d "Backend/target" ]; then
        rm -rf Backend/target
        success "Directorio target eliminado"
    fi
    
    # Limpiar archivos .class residuales
    find . -name "*.class" -type f -delete 2>/dev/null || true
    find . -name "*.jar.original" -type f -delete 2>/dev/null || true
    find . -name "*.war" -type f -delete 2>/dev/null || true
    
    # 3. Limpiar Docker completamente
    log "🐳 Limpiando Docker completamente..."
    
    # Detener todos los contenedores
    docker stop $(docker ps -aq) 2>/dev/null || true
    
    # Eliminar todos los contenedores
    docker rm $(docker ps -aq) 2>/dev/null || true
    
    # Eliminar todas las imágenes
    docker rmi $(docker images -q) 2>/dev/null || true
    
    # Limpiar sistema Docker
    docker system prune -af --volumes
    
    # 4. Limpiar caché de Maven en el host (si existe)
    log "📦 Limpiando caché de Maven..."
    if [ -d "$HOME/.m2/repository" ]; then
        # Solo limpiar caché de dependencias, no todo
        find "$HOME/.m2/repository" -name "*.lastUpdated" -delete 2>/dev/null || true
        find "$HOME/.m2/repository" -name "*.repositories" -delete 2>/dev/null || true
    fi
    
    # 5. Limpiar logs antiguos
    log "📋 Limpiando logs antiguos..."
    find /var/log -name "*.log" -mtime +7 -delete 2>/dev/null || true
    
    success "✅ Limpieza completa finalizada"
}

# Verificar espacio en disco después de limpieza
check_space_after_cleanup() {
    log "💽 Verificando espacio en disco después de limpieza..."
    
    AVAILABLE_SPACE=$(df /home | awk 'NR==2 {print $4}')
    REQUIRED_SPACE=1048576  # 1GB en KB
    
    if [ "$AVAILABLE_SPACE" -lt "$REQUIRED_SPACE" ]; then
        error "Espacio insuficiente después de limpieza. Disponible: $(($AVAILABLE_SPACE / 1024))MB"
        exit 1
    fi
    
    success "✅ Espacio en disco suficiente: $(($AVAILABLE_SPACE / 1024))MB disponibles"
}

# Construir con limpieza completa
build_with_cleanup() {
    log "🔨 Construyendo con limpieza completa..."
    
    cd "$APP_DIR"
    
    # Construir imagen con parámetros de limpieza máxima
    docker-compose build --no-cache --pull --force-rm backend
    
    success "✅ Construcción completada"
}

# Desplegar con verificación
deploy_with_verification() {
    log "🚀 Desplegando con verificación..."
    
    cd "$APP_DIR"
    
    # Iniciar servicios
    docker-compose up -d
    
    # Esperar más tiempo para inicialización completa
    log "⏳ Esperando inicialización completa..."
    sleep 60
    
    # Verificar health checks
    verify_health_checks
    
    success "🎉 Despliegue con limpieza completado exitosamente"
}

# Verificar health checks
verify_health_checks() {
    log "🏥 Verificando health checks..."
    
    # Backend health check con más intentos
    log "Verificando backend..."
    for i in {1..20}; do
        if curl -f -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
            success "Backend: ✅ Saludable"
            break
        fi
        if [ $i -eq 20 ]; then
            error "Backend: ❌ No responde después de 20 intentos"
            docker-compose logs --tail=100 backend
            exit 1
        fi
        log "Backend: ⏳ Intento $i/20..."
        sleep 15
    done
}

# Función principal
main() {
    log "🚀 Iniciando despliegue con limpieza completa..."
    
    # Verificar que estamos en el directorio correcto
    if [ ! -f "$COMPOSE_FILE" ]; then
        error "No se encontró docker-compose.yml en $APP_DIR"
        exit 1
    fi
    
    # Ejecutar limpieza completa
    full_cleanup
    
    # Verificar espacio
    check_space_after_cleanup
    
    # Construir con limpieza
    build_with_cleanup
    
    # Desplegar con verificación
    deploy_with_verification
    
    success "🎉 Despliegue con limpieza completa finalizado exitosamente"
}

# Ejecutar función principal
main "$@"
