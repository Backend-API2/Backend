#!/bin/bash
# =============================================
# Script de gestión de servicios Docker
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

# Verificar que Docker esté instalado
check_docker() {
    if ! command -v docker &> /dev/null; then
        error "Docker no está instalado"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose no está instalado"
        exit 1
    fi
}

# Verificar que el archivo docker-compose.yml existe
check_compose_file() {
    if [ ! -f "$COMPOSE_FILE" ]; then
        error "Archivo docker-compose.yml no encontrado en $APP_DIR"
        exit 1
    fi
}

# Función para mostrar el estado de los servicios
status() {
    log "🔍 Estado de los servicios Docker"
    echo "=================================="
    
    cd "$APP_DIR"
    
    # Mostrar estado de Docker Compose
    docker-compose ps
    
    echo ""
    log "📊 Información adicional:"
    
    # Mostrar uso de recursos
    echo "Uso de memoria:"
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}" 2>/dev/null || echo "No hay contenedores ejecutándose"
    
    echo ""
    echo "Puertos en uso:"
    for port in 8080 9090 9093 3000; do
        if lsof -ti:$port >/dev/null 2>&1; then
            echo "✅ Puerto $port: En uso"
        else
            echo "❌ Puerto $port: Libre"
        fi
    done
}

# Función para iniciar servicios
start() {
    log "🚀 Iniciando servicios Docker"
    
    check_docker
    check_compose_file
    
    cd "$APP_DIR"
    
    # Verificar si ya hay servicios ejecutándose
    if docker-compose ps --services --filter 'status=running' | grep -q .; then
        warning "Algunos servicios ya están ejecutándose"
        docker-compose ps
        read -p "¿Desea reiniciar todos los servicios? (y/N): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            log "🔄 Reiniciando servicios..."
            docker-compose down
        else
            log "Continuando con servicios existentes..."
        fi
    fi
    
    # Construir imagen del backend si es necesario
    log "🔨 Construyendo imagen del backend..."
    docker-compose build backend
    
    # Iniciar todos los servicios
    log "🚀 Iniciando todos los servicios..."
    docker-compose up -d
    
    # Esperar a que los servicios estén listos
    log "⏳ Esperando a que los servicios estén listos..."
    sleep 30
    
    # Verificar estado
    status
    
    # Verificar health checks
    log "🏥 Verificando health checks..."
    
    # Backend health check
    for i in {1..10}; do
        if curl -f -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
            success "Backend: ✅ Saludable"
            break
        fi
        if [ $i -eq 10 ]; then
            error "Backend: ❌ No responde después de 10 intentos"
        else
            log "Backend: ⏳ Intento $i/10..."
            sleep 10
        fi
    done
    
    # Prometheus health check
    for i in {1..5}; do
        if curl -f -s http://localhost:9090/-/healthy >/dev/null 2>&1; then
            success "Prometheus: ✅ Saludable"
            break
        fi
        if [ $i -eq 5 ]; then
            error "Prometheus: ❌ No responde después de 5 intentos"
        else
            log "Prometheus: ⏳ Intento $i/5..."
            sleep 10
        fi
    done
    
    # Grafana health check
    for i in {1..5}; do
        if curl -f -s http://localhost:3000/api/health >/dev/null 2>&1; then
            success "Grafana: ✅ Saludable"
            break
        fi
        if [ $i -eq 5 ]; then
            error "Grafana: ❌ No responde después de 5 intentos"
        else
            log "Grafana: ⏳ Intento $i/5..."
            sleep 10
        fi
    done
    
    success "🎉 Todos los servicios iniciados correctamente"
}

# Función para detener servicios
stop() {
    log "🛑 Deteniendo servicios Docker"
    
    check_docker
    check_compose_file
    
    cd "$APP_DIR"
    
    # Detener servicios
    docker-compose down
    
    # Limpiar contenedores huérfanos
    log "🧹 Limpiando contenedores huérfanos..."
    docker container prune -f
    
    # Limpiar redes huérfanas
    log "🧹 Limpiando redes huérfanas..."
    docker network prune -f
    
    success "✅ Servicios detenidos correctamente"
}

# Función para reiniciar servicios
restart() {
    log "🔄 Reiniciando servicios Docker"
    
    stop
    sleep 5
    start
}

# Función para mostrar logs
logs() {
    log "📋 Mostrando logs de servicios"
    
    check_docker
    check_compose_file
    
    cd "$APP_DIR"
    
    if [ -n "$1" ]; then
        # Mostrar logs de un servicio específico
        log "Mostrando logs de: $1"
        docker-compose logs -f --tail=100 "$1"
    else
        # Mostrar logs de todos los servicios
        log "Mostrando logs de todos los servicios"
        docker-compose logs -f --tail=50
    fi
}

# Función para actualizar servicios
update() {
    log "🔄 Actualizando servicios Docker"
    
    check_docker
    check_compose_file
    
    cd "$APP_DIR"
    
    # Detener servicios
    log "🛑 Deteniendo servicios..."
    docker-compose down
    
    # Limpiar imágenes antiguas
    log "🧹 Limpiando imágenes antiguas..."
    docker image prune -f
    
    # Construir nuevas imágenes
    log "🔨 Construyendo nuevas imágenes..."
    docker-compose build --no-cache
    
    # Iniciar servicios
    log "🚀 Iniciando servicios actualizados..."
    docker-compose up -d
    
    success "✅ Servicios actualizados correctamente"
}

# Función para mostrar ayuda
help() {
    echo "Script de gestión de servicios Docker"
    echo "====================================="
    echo ""
    echo "Uso: $0 [COMANDO] [OPCIONES]"
    echo ""
    echo "Comandos disponibles:"
    echo "  start     - Iniciar todos los servicios"
    echo "  stop      - Detener todos los servicios"
    echo "  restart   - Reiniciar todos los servicios"
    echo "  status    - Mostrar estado de los servicios"
    echo "  logs      - Mostrar logs (opcional: nombre del servicio)"
    echo "  update    - Actualizar servicios"
    echo "  help      - Mostrar esta ayuda"
    echo ""
    echo "Ejemplos:"
    echo "  $0 start"
    echo "  $0 logs backend"
    echo "  $0 status"
}

# Función principal
main() {
    case "${1:-help}" in
        start)
            start
            ;;
        stop)
            stop
            ;;
        restart)
            restart
            ;;
        status)
            status
            ;;
        logs)
            logs "$2"
            ;;
        update)
            update
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
