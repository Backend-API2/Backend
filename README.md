# Backend Spring Boot - API de Pagos y Facturación

## 📊 Monitoreo y Métricas

Este proyecto incluye una infraestructura mínima de monitoreo usando Spring Boot Actuator y Prometheus para métricas locales.

### 🚀 Endpoints de Métricas Disponibles

Una vez que la aplicación esté ejecutándose, los siguientes endpoints estarán disponibles:

- **Health Check**: `http://localhost:8080/actuator/health`
- **Métricas**: `http://localhost:8080/actuator/metrics`
- **Prometheus**: `http://localhost:8080/actuator/prometheus`
- **Info**: `http://localhost:8080/actuator/info`

### 🔧 Configuración de Prometheus

#### Opción 1: Usando el script automatizado (Recomendado)

```bash
# Asegúrate de que el backend esté corriendo
mvn spring-boot:run

# En otra terminal, ejecuta el script
./scripts/start-prometheus.sh
```

#### Opción 2: Usando Docker Compose

```bash
# Levantar Prometheus y Grafana
docker-compose -f docker-compose-monitoring.yml up -d
```

#### Opción 3: Instalación manual de Prometheus

1. **Descargar Prometheus**:

   ```bash
   # macOS con Homebrew
   brew install prometheus

   # O descargar desde https://prometheus.io/download/
   ```

2. **Configurar Prometheus**:

   ```bash
   # Usar el archivo prometheus.yml incluido
   prometheus --config.file=prometheus.yml
   ```

3. **Acceder a la UI**:
   - Prometheus: http://localhost:9090
   - Grafana (si usas Docker Compose): http://localhost:3000

### 📈 Métricas Disponibles

El backend expone las siguientes categorías de métricas:

#### Métricas de Aplicación

- `application.ready.time` - Tiempo de inicio de la aplicación
- `application.started.time` - Tiempo total de arranque

#### Métricas de JVM

- `jvm.memory.used` - Memoria utilizada
- `jvm.memory.max` - Memoria máxima disponible
- `jvm.gc.*` - Métricas del Garbage Collector
- `jvm.threads.*` - Información de threads

#### Métricas de Base de Datos

- `hikaricp.connections.*` - Pool de conexiones HikariCP
- `jdbc.connections.*` - Conexiones JDBC

#### Métricas HTTP

- `http.server.requests` - Requests HTTP procesados
- `http.server.requests.active` - Requests activos

#### Métricas de Sistema

- `system.cpu.usage` - Uso de CPU
- `system.load.average.1m` - Carga promedio del sistema
- `disk.free` - Espacio libre en disco

### 🔍 Verificación de Funcionamiento

1. **Verificar endpoints**:

   ```bash
   # Health check
   curl http://localhost:8080/actuator/health

   # Lista de métricas
   curl http://localhost:8080/actuator/metrics

   # Métricas específicas
   curl http://localhost:8080/actuator/metrics/jvm.memory.used

   # Formato Prometheus
   curl http://localhost:8080/actuator/prometheus
   ```

2. **Verificar en Prometheus**:
   - Ve a http://localhost:9090
   - Status → Targets
   - Verifica que `spring-boot-backend` esté UP

### 🛠️ Agregar Nuevas Métricas

Para agregar métricas personalizadas a tu aplicación:

```java
@Component
public class CustomMetrics {

    private final MeterRegistry meterRegistry;
    private final Counter customCounter;
    private final Timer customTimer;

    public CustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.customCounter = Counter.builder("custom.operations")
            .description("Número de operaciones personalizadas")
            .register(meterRegistry);

        this.customTimer = Timer.builder("custom.operation.duration")
            .description("Duración de operaciones personalizadas")
            .register(meterRegistry);
    }

    public void incrementCounter() {
        customCounter.increment();
    }

    public void recordTimer(Duration duration) {
        customTimer.record(duration);
    }
}
```

### 🔒 Seguridad

Los endpoints de Actuator están configurados para ser accesibles públicamente en el entorno local. En producción, considera:

- Restringir acceso usando autenticación
- Usar HTTPS
- Configurar firewalls apropiados
- Usar variables de entorno para credenciales sensibles

### 📝 Variables de Entorno

El proyecto está configurado para usar variables de entorno para datos sensibles:

```bash
# Base de datos
export DB_URL="jdbc:mysql://your-db-host:3306/your-db"
export DB_USERNAME="your-username"
export DB_PASSWORD="your-password"

# JWT
export JWT_SECRET="your-jwt-secret"
export JWT_EXPIRATION="86400000"
export JWT_ISSUER="your-issuer"

# Servidor
export SERVER_PORT="8080"
```

### 🚨 Troubleshooting

**Problema**: Endpoint `/actuator/prometheus` devuelve 403
**Solución**: Verifica que la aplicación esté usando el perfil correcto y que Spring Security esté configurado para permitir acceso a Actuator.

**Problema**: Prometheus no puede scrapear métricas
**Solución**:

1. Verifica que el backend esté corriendo en el puerto correcto
2. Verifica que el endpoint `/actuator/prometheus` sea accesible
3. Revisa la configuración en `prometheus.yml`

**Problema**: Métricas no aparecen en Prometheus
**Solución**:

1. Espera unos minutos para que se recopilen métricas
2. Verifica que el target esté UP en Status → Targets
3. Revisa los logs de Prometheus para errores

### 📚 Recursos Adicionales

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
