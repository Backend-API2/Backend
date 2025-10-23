#!/bin/bash
# =============================================
# Script de limpieza completa para servidor
# Usar cuando hay problemas de compilación o archivos residuales
# =============================================

set -e

APP_DIR="/home/ec2-user/app"

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
    docker-compose -p app-staging down --remove-orphans --volumes || warning "No había servicios staging ejecutándose"
    docker-compose -p app-prod -f docker-compose.prod.yml down --remove-orphans --volumes || warning "No había servicios prod ejecutándose"
    
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
    
    # 4. Limpiar logs antiguos
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

# Función para mostrar ayuda
help() {
    echo "Script de limpieza completa para servidor"
    echo "========================================="
    echo ""
    echo "Uso: $0 [COMANDO]"
    echo ""
    echo "Comandos disponibles:"
    echo "  cleanup      - Limpieza completa del sistema"
    echo "  check-space  - Verificar espacio en disco"
    echo "  help         - Mostrar esta ayuda"
    echo ""
    echo "Ejemplos:"
    echo "  $0 cleanup"
    echo "  $0 check-space"
}

# Función principal
main() {
    case "${1:-help}" in
        cleanup)
            full_cleanup
            check_space_after_cleanup
            ;;
        check-space)
            check_space_after_cleanup
            ;;
        help|--help|-h)
            help
            ;;
        *)
            error "Comando desconocido: $1"
            help
            exit 1
            ;;
    esac
}

# Ejecutar función principal
main "$@"
