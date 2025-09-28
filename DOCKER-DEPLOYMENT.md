# 🐳 Docker Deployment Guide

Este documento describe cómo usar el nuevo flujo de despliegue con Docker para el backend Spring Boot y los servicios de monitoreo.

## 📋 Archivos Principales

- `Dockerfile` - Imagen Docker optimizada para el backend Spring Boot
- `docker-compose.yml` - Orquestación de todos los servicios (backend + monitoreo)
- `prometheus.yml` - Configuración de Prometheus actualizada para Docker
- `scripts/docker-manager.sh` - Script de gestión de servicios Docker
- `scripts/deploy.sh` - Script de despliegue en producción

## 🚀 Comandos Principales

### Desarrollo Local

```bash
# Construir y levantar todos los servicios
docker-compose up -d

# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f backend

# Detener todos los servicios
docker-compose down

# Reconstruir imagen del backend
docker-compose build --no-cache backend
```

### Producción en EC2

```bash
# Despliegue completo (recomendado)
./scripts/deploy.sh deploy

# Gestión de servicios
./scripts/docker-manager.sh start    # Iniciar servicios
./scripts/docker-manager.sh stop     # Detener servicios
./scripts/docker-manager.sh restart  # Reiniciar servicios
./scripts/docker-manager.sh status   # Ver estado
./scripts/docker-manager.sh logs     # Ver logs

# Backup y mantenimiento
./scripts/deploy.sh backup   # Crear backup
./scripts/deploy.sh cleanup  # Limpiar recursos
./scripts/deploy.sh info     # Información del sistema
```

## 🌐 Puertos y Servicios

| Servicio     | Puerto | URL                   | Descripción              |
| ------------ | ------ | --------------------- | ------------------------ |
| Backend      | 8080   | http://localhost:8080 | API Spring Boot          |
| Prometheus   | 9090   | http://localhost:9090 | Métricas y alertas       |
| Alertmanager | 9093   | http://localhost:9093 | Gestión de alertas       |
| Grafana      | 3000   | http://localhost:3000 | Dashboards (admin/admin) |

## 🔧 Configuración

### Variables de Entorno

Las siguientes variables pueden ser configuradas en el archivo `.env` o como variables de entorno del sistema:

```bash
# Base de datos
DB_URL=jdbc:mysql://your-db-host:3306/your-database
DB_USERNAME=your-username
DB_PASSWORD=your-password

# JWT
JWT_SECRET=your-jwt-secret-key

# Grafana
GRAFANA_PASSWORD=your-grafana-password
```

### Red Docker

Todos los servicios están conectados a la red `backend-network`, lo que permite:

- Comunicación interna entre servicios
- Prometheus puede hacer scraping del backend usando `backend:8080`
- Alertmanager puede comunicarse con Prometheus usando `prometheus:9090`

## 📊 Monitoreo

### Métricas Disponibles

El backend expone métricas en `/actuator/prometheus`:

- Métricas JVM (memoria, CPU, GC)
- Métricas HTTP (requests, response time)
- Métricas de base de datos (HikariCP)
- Métricas personalizadas

### Alertas Configuradas

- **HighHikariCPConnectionUsage**: Uso alto de conexiones de BD (>80%)
- **HighSystemCPUUsage**: Uso alto de CPU (>90%)
- **HighHTTPErrorRate**: Alto porcentaje de errores 5xx (>5%)
- **HighJVMMemoryUsage**: Uso alto de memoria JVM (>85%)
- **HighHTTPResponseTime**: Tiempo de respuesta alto (>2s)
- **ApplicationDown**: Aplicación caída

## 🔄 CI/CD

El flujo de CI/CD ha sido actualizado para usar Docker:

1. **Build**: Construye el JAR con Maven
2. **Test**: Ejecuta tests con JaCoCo
3. **Sonar**: Análisis de código con SonarCloud
4. **Deploy**:
   - Copia archivos de configuración Docker
   - Construye imagen Docker del backend
   - Levanta todos los servicios con `docker-compose up -d`
   - Verifica health checks

### Ventajas del Nuevo Flujo

- ✅ **Sin scripts de PID**: Todo manejado por Docker
- ✅ **Aislamiento**: Cada servicio en su propio contenedor
- ✅ **Escalabilidad**: Fácil escalado horizontal
- ✅ **Rollback**: Fácil rollback con `docker-compose down && docker-compose up -d`
- ✅ **Logs centralizados**: `docker-compose logs`
- ✅ **Health checks**: Automáticos con Docker
- ✅ **Red compartida**: Comunicación segura entre servicios

## 🛠️ Troubleshooting

### Problemas Comunes

1. **Puerto en uso**:

   ```bash
   # Verificar qué proceso usa el puerto
   lsof -i:8080

   # Detener servicios Docker
   docker-compose down
   ```

2. **Servicio no responde**:

   ```bash
   # Ver logs del servicio
   docker-compose logs backend

   # Verificar estado
   docker-compose ps
   ```

3. **Prometheus no puede hacer scraping**:

   ```bash
   # Verificar conectividad en la red Docker
   docker-compose exec prometheus ping backend

   # Verificar configuración
   curl http://localhost:9090/api/v1/targets
   ```

4. **Espacio en disco**:

   ```bash
   # Limpiar recursos Docker
   docker system prune -a

   # Verificar espacio
   df -h
   ```

### Comandos de Diagnóstico

```bash
# Estado general
./scripts/docker-manager.sh status

# Logs detallados
./scripts/docker-manager.sh logs backend

# Información del sistema
./scripts/deploy.sh info

# Verificar health checks
./scripts/deploy.sh verify
```

## 📈 Optimizaciones

### Dockerfile Optimizado

- **Multi-stage build**: Reduce tamaño de imagen final
- **Usuario no-root**: Mejora seguridad
- **JVM optimizado**: Configurado para contenedores
- **Health checks**: Automáticos con curl

### Docker Compose Optimizado

- **Restart policies**: `unless-stopped` para alta disponibilidad
- **Logging**: Rotación automática de logs
- **Volúmenes**: Persistencia de datos
- **Networks**: Comunicación segura entre servicios

## 🔒 Seguridad

- Usuario no-root en contenedores
- Redes Docker aisladas
- Volúmenes de solo lectura para configuración
- Variables de entorno para secretos
- Health checks para detección de problemas

## 📚 Referencias

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Prometheus Configuration](https://prometheus.io/docs/prometheus/latest/configuration/configuration/)
- [Grafana Docker Setup](https://grafana.com/docs/grafana/latest/installation/docker/)
