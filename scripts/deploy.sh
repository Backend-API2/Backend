#!/bin/bash
# =============================================
# Script de despliegue en producción
# =============================================

set -e

APP_DIR="/home/ec2-user/app"
COMPOSE_FILE="$APP_DIR/docker-compose.yml"
BACKUP_DIR="/home/ec2-user/backups"

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

# Crear directorio de backups si no existe
create_backup_dir() {
    if [ ! -d "$BACKUP_DIR" ]; then
        log "📁 Creando directorio de backups..."
        mkdir -p "$BACKUP_DIR"
    fi
}

# Crear backup de datos importantes
create_backup() {
    log "💾 Creando backup de datos..."
    
    create_backup_dir
    
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    BACKUP_FILE="$BACKUP_DIR/backup_$TIMESTAMP.tar.gz"
    
    # Crear backup de volúmenes Docker
    cd "$APP_DIR"
    
    # Backup de datos de Prometheus
    if docker volume ls | grep -q "prometheus_data"; then
        log "📦 Respaldando datos de Prometheus..."
        docker run --rm -v prometheus_data:/data -v "$BACKUP_DIR":/backup alpine tar czf "/backup/prometheus_data_$TIMESTAMP.tar.gz" -C /data .
    fi
    
    # Backup de datos de Grafana
    if docker volume ls | grep -q "grafana_data"; then
        log "📦 Respaldando datos de Grafana..."
        docker run --rm -v grafana_data:/data -v "$BACKUP_DIR":/backup alpine tar czf "/backup/grafana_data_$TIMESTAMP.tar.gz" -C /data .
    fi
    
    # Backup de configuración
    log "📦 Respaldando archivos de configuración..."
    tar czf "$BACKUP_FILE" \
        docker-compose.yml \
        Dockerfile \
        prometheus.yml \
        alertmanager.yml \
        alert_rules.yml \
        Backend-*.jar 2>/dev/null || warning "No se encontraron archivos JAR para respaldar"
    
    success "✅ Backup creado: $BACKUP_FILE"
}

# Verificar espacio en disco
check_disk_space() {
    log "💽 Verificando espacio en disco..."
    
    AVAILABLE_SPACE=$(df /home | awk 'NR==2 {print $4}')
    REQUIRED_SPACE=2097152  # 2GB en KB
    
    if [ "$AVAILABLE_SPACE" -lt "$REQUIRED_SPACE" ]; then
        error "Espacio insuficiente en disco. Disponible: $(($AVAILABLE_SPACE / 1024))MB, Requerido: $(($REQUIRED_SPACE / 1024))MB"
        exit 1
    fi
    
    success "✅ Espacio en disco suficiente"
}

# Verificar conectividad de red
check_network() {
    log "🌐 Verificando conectividad de red..."
    
    # Verificar conectividad a la base de datos
    DB_HOST=$(grep -o 'jdbc:mysql://[^:]*' "$APP_DIR/docker-compose.yml" | sed 's/jdbc:mysql:\/\///')
    if [ -n "$DB_HOST" ]; then
        if ping -c 1 "$DB_HOST" >/dev/null 2>&1; then
            success "✅ Conectividad a base de datos: OK"
        else
            warning "⚠️  No se puede hacer ping a la base de datos: $DB_HOST"
        fi
    fi
    
    # Verificar conectividad a internet
    if ping -c 1 8.8.8.8 >/dev/null 2>&1; then
        success "✅ Conectividad a internet: OK"
    else
        error "❌ Sin conectividad a internet"
        exit 1
    fi
}

# Verificar que Docker esté funcionando
check_docker() {
    log "🐳 Verificando Docker..."
    
    if ! command -v docker &> /dev/null; then
        error "Docker no está instalado"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose no está instalado"
        exit 1
    fi
    
    # Verificar que Docker esté ejecutándose
    if ! docker info >/dev/null 2>&1; then
        error "Docker no está ejecutándose"
        exit 1
    fi
    
    success "✅ Docker funcionando correctamente"
}

# Verificar archivos necesarios
check_files() {
    log "📋 Verificando archivos necesarios..."
    
    REQUIRED_FILES=(
        "docker-compose.yml"
        "Dockerfile"
        "prometheus.yml"
        "alertmanager.yml"
        "alert_rules.yml"
    )
    
    for file in "${REQUIRED_FILES[@]}"; do
        if [ ! -f "$APP_DIR/$file" ]; then
            error "Archivo requerido no encontrado: $file"
            exit 1
        fi
    done
    
    # Verificar que existe al menos un JAR
    if ! ls "$APP_DIR"/Backend-*.jar >/dev/null 2>&1; then
        error "No se encontró archivo JAR del backend"
        exit 1
    fi
    
    success "✅ Todos los archivos necesarios están presentes"
}

# Limpiar recursos del sistema
cleanup_system() {
    log "🧹 Limpiando recursos del sistema..."
    
    # Limpiar contenedores detenidos
    docker container prune -f
    
    # Limpiar imágenes no utilizadas
    docker image prune -f
    
    # Limpiar redes no utilizadas
    docker network prune -f
    
    # Limpiar volúmenes no utilizados
    docker volume prune -f
    
    success "✅ Limpieza del sistema completada"
}

# Desplegar servicios
deploy() {
    log "🚀 Iniciando despliegue en producción..."
    
    cd "$APP_DIR"
    
    # Crear backup antes del despliegue
    create_backup
    
    # Detener servicios existentes
    log "🛑 Deteniendo servicios existentes..."
    docker-compose down --remove-orphans || warning "No había servicios ejecutándose"
    
    # Limpiar sistema
    cleanup_system
    
    # Construir imagen del backend
    log "🔨 Construyendo imagen del backend..."
    docker-compose build --no-cache backend
    
    # Iniciar servicios
    log "🚀 Iniciando servicios..."
    docker-compose up -d
    
    # Esperar a que los servicios estén listos
    log "⏳ Esperando a que los servicios estén listos..."
    sleep 45
    
    # Verificar estado de los servicios
    log "🔍 Verificando estado de los servicios..."
    docker-compose ps
    
    # Verificar health checks
    verify_health_checks
    
    success "🎉 Despliegue completado exitosamente"
}

# Verificar health checks de todos los servicios
verify_health_checks() {
    log "🏥 Verificando health checks..."
    
    # Backend health check
    log "Verificando backend..."
    for i in {1..15}; do
        if curl -f -s http://localhost:8080/actuator/health >/dev/null 2>&1; then
            success "Backend: ✅ Saludable"
            break
        fi
        if [ $i -eq 15 ]; then
            error "Backend: ❌ No responde después de 15 intentos"
            show_logs "backend"
            exit 1
        fi
        log "Backend: ⏳ Intento $i/15..."
        sleep 10
    done
    
    # Prometheus health check
    log "Verificando Prometheus..."
    for i in {1..10}; do
        if curl -f -s http://localhost:9090/-/healthy >/dev/null 2>&1; then
            success "Prometheus: ✅ Saludable"
            break
        fi
        if [ $i -eq 10 ]; then
            error "Prometheus: ❌ No responde después de 10 intentos"
            show_logs "prometheus"
            exit 1
        fi
        log "Prometheus: ⏳ Intento $i/10..."
        sleep 10
    done
    
    # Grafana health check
    log "Verificando Grafana..."
    for i in {1..10}; do
        if curl -f -s http://localhost:3000/api/health >/dev/null 2>&1; then
            success "Grafana: ✅ Saludable"
            break
        fi
        if [ $i -eq 10 ]; then
            error "Grafana: ❌ No responde después de 10 intentos"
            show_logs "grafana"
            exit 1
        fi
        log "Grafana: ⏳ Intento $i/10..."
        sleep 10
    done
    
    # Verificar que Prometheus puede hacer scraping del backend
    log "Verificando scraping de Prometheus..."
    sleep 30  # Dar tiempo a Prometheus para hacer scraping
    
    TARGETS_RESPONSE=$(curl -s http://localhost:9090/api/v1/targets)
    if echo "$TARGETS_RESPONSE" | grep -q '"health":"up"'; then
        success "Prometheus scraping: ✅ Funcionando"
    else
        warning "Prometheus scraping: ⚠️  Puede no estar funcionando correctamente"
        log "Respuesta de targets: $TARGETS_RESPONSE"
    fi
}

# Mostrar logs de un servicio
show_logs() {
    local service="$1"
    log "📋 Mostrando logs de $service..."
    docker-compose logs --tail=50 "$service"
}

# Función para mostrar información del sistema
system_info() {
    log "📊 Información del sistema"
    echo "=========================="
    
    echo "Sistema operativo:"
    cat /etc/os-release | grep PRETTY_NAME
    
    echo ""
    echo "Uso de memoria:"
    free -h
    
    echo ""
    echo "Uso de disco:"
    df -h /home
    
    echo ""
    echo "Versión de Docker:"
    docker --version
    docker-compose --version
    
    echo ""
    echo "Servicios Docker ejecutándose:"
    docker-compose ps
}

# Función para mostrar ayuda
help() {
    echo "Script de despliegue en producción"
    echo "=================================="
    echo ""
    echo "Uso: $0 [COMANDO]"
    echo ""
    echo "Comandos disponibles:"
    echo "  deploy       - Realizar despliegue completo"
    echo "  backup       - Crear backup de datos"
    echo "  verify       - Verificar health checks"
    echo "  cleanup      - Limpiar recursos del sistema"
    echo "  info         - Mostrar información del sistema"
    echo "  help         - Mostrar esta ayuda"
    echo ""
    echo "Ejemplos:"
    echo "  $0 deploy"
    echo "  $0 backup"
    echo "  $0 verify"
}

# Función principal
main() {
    case "${1:-help}" in
        deploy)
            check_disk_space
            check_network
            check_docker
            check_files
            deploy
            ;;
        backup)
            create_backup
            ;;
        verify)
            verify_health_checks
            ;;
        cleanup)
            cleanup_system
            ;;
        info)
            system_info
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
