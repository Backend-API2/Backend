# 🚀 Guía de Deployment - Backend API

Esta guía explica cómo configurar y desplegar la aplicación Spring Boot en EC2 usando GitHub Actions con un pipeline robusto y automatizado.

## 📋 Prerrequisitos

### En tu EC2:

1. **Java 21** instalado
2. **Curl** para health checks
3. **Usuario ec2-user** con permisos sudo
4. **Puerto 8080** abierto en Security Groups
5. **lsof** para gestión de procesos

### En GitHub:

1. **Secrets** configurados en el repositorio
2. **Rama master** como rama principal
3. **GitHub Actions** habilitado

## 🔧 Configuración de Secrets en GitHub

Ve a `Settings > Secrets and variables > Actions` y agrega:

```
EC2_HOST=tu-ip-ec2.amazonaws.com
EC2_USERNAME=ec2-user
EC2_SSH_KEY=tu-clave-privada-ssh
EC2_PORT=22
```

## 🏗️ Estructura del Deployment

```
/home/ec2-user/app/
├── Backend-0.0.1-SNAPSHOT.jar    # JAR de la aplicación
├── app.pid                       # PID del proceso
├── app.log                       # Logs de la aplicación
└── scripts/                      # Scripts de gestión avanzados
    ├── start.sh                  # Inicio con validaciones
    ├── stop.sh                   # Parada graceful
    ├── restart.sh                # Reinicio completo
    └── status.sh                 # Monitoreo detallado
```

## 🔄 Flujo de CI/CD Robusto

### 1. **Job de Testing** (siempre se ejecuta)

- ✅ **Checkout del código**
- ✅ **Setup JDK 21** con distribución Temurin
- ✅ **Cache de dependencias Maven** para optimización
- ✅ **Ejecución de tests** (`mvn clean test`)
- ✅ **Build de aplicación** (`mvn clean package -DskipTests`)
- ✅ **Upload de artefacto JAR**

### 2. **Job de Deployment** (solo en push a master)

- 🛑 **Múltiples métodos de cleanup** de procesos existentes
- 📦 **Descarga y copia** del nuevo JAR
- 🚀 **Inicio con validaciones** robustas
- 🏥 **Health checks múltiples** con retry logic
- ✅ **Verificación final** de funcionamiento

## 🛠️ Scripts de Gestión Avanzados

Los scripts se crean automáticamente durante el deployment y están ubicados en `/home/ec2-user/app/scripts/`.

### 🚀 **start.sh** - Inicio con Validaciones

```bash
cd /home/ec2-user/app/scripts
./start.sh
```

**Características:**

- ✅ Validación de existencia del JAR
- ✅ Verificación de procesos duplicados
- ✅ Gestión de PID files
- ✅ Logging estructurado
- ✅ Verificación de inicio exitoso

### 🛑 **stop.sh** - Parada Graceful

```bash
cd /home/ec2-user/app/scripts
./stop.sh
```

**Características:**

- ✅ Graceful shutdown con SIGTERM
- ✅ Timeout de 30 segundos
- ✅ Force kill como último recurso
- ✅ Cleanup de PID files
- ✅ Manejo de procesos zombie

### 🔄 **restart.sh** - Reinicio Completo

```bash
cd /home/ec2-user/app/scripts
./restart.sh
```

**Características:**

- ✅ Stop completo con cleanup adicional
- ✅ Eliminación de procesos Java Backend
- ✅ Cleanup de puerto 8080
- ✅ Limpieza de archivos temporales
- ✅ Reinicio con validaciones

### 🔍 **status.sh** - Monitoreo Detallado

```bash
cd /home/ec2-user/app/scripts
./status.sh
```

**Información mostrada:**

- 📊 Estado del proceso (PID, tiempo de ejecución, CPU, memoria)
- 🌐 Estado del puerto 8080
- 🏥 Health check de la aplicación
- 📋 Últimos 10 logs de la aplicación

## 🔍 Monitoreo y Health Checks

### 🏥 **Health Checks Múltiples**

El sistema implementa **dos endpoints de health check** con retry logic:

#### Actuator Health Check

```bash
curl http://tu-ip-ec2:8080/actuator/health
```

#### Custom Health Check

```bash
curl http://tu-ip-ec2:8080/health
```

**Características del Health Check:**

- ✅ **5 intentos** con intervalo de 10 segundos
- ✅ **Verificación de ambos endpoints**
- ✅ **Retry automático** en caso de fallo
- ✅ **Verificación final** de ambos endpoints

### 📊 **Monitoreo de Logs**

#### Ver logs en tiempo real

```bash
tail -f /home/ec2-user/app/app.log
```

#### Ver logs recientes

```bash
tail -50 /home/ec2-user/app/app.log
```

#### Ver logs con filtros

```bash
# Solo errores
grep "ERROR" /home/ec2-user/app/app.log

# Solo warnings
grep "WARN" /home/ec2-user/app/app.log
```

## ⚙️ Configuración de Producción

La aplicación usa el perfil `prod` que incluye:

- **Logging optimizado** (solo WARN/ERROR)
- **Pool de conexiones** configurado para producción
- **Health checks** habilitados
- **Logs rotativos** (máximo 10MB, 30 archivos)

## 🔒 Variables de Entorno

Puedes sobrescribir la configuración usando variables de entorno:

```bash
export DB_URL="jdbc:mysql://tu-db:3306/tu_base"
export DB_USERNAME="tu_usuario"
export DB_PASSWORD="tu_password"
export JWT_SECRET="tu_secret_jwt"
export SERVER_PORT="8080"
```

## 🛡️ Proceso de Cleanup Robusto

El pipeline implementa un **sistema de cleanup multicapa** para garantizar deployments limpios:

### **Método 1: Script de Stop**

- Utiliza el script `stop.sh` si existe
- Graceful shutdown con timeout de 30 segundos
- Force kill si es necesario

### **Método 2: Cleanup por Puerto**

- Identifica procesos usando puerto 8080 con `lsof`
- Envía SIGTERM a todos los procesos
- Espera 5 segundos y luego SIGKILL

### **Método 3: Cleanup por Proceso Java**

- Elimina procesos Java con "Backend" en el comando
- Usa `pkill -f 'java.*Backend'`
- Cleanup adicional de procesos Java en puerto 8080

### **Método 4: Cleanup de PID Files**

- Verifica y limpia archivos PID obsoletos
- Elimina procesos asociados a PID files
- Limpia archivos de log temporales

### **Método 5: Cleanup Final**

- Eliminación forzada de cualquier proceso restante en puerto 8080
- Verificación final de que el puerto esté libre
- Falla el deployment si el puerto sigue ocupado

## 🚨 Troubleshooting

### La aplicación no inicia

1. **Verificar logs**: `tail -f /home/ec2-user/app/app.log`
2. **Verificar Java**: `java -version`
3. **Verificar puerto**: `netstat -tlnp | grep 8080`
4. **Verificar estado**: `./status.sh`

### Error de conexión a base de datos

1. Verificar Security Groups de RDS
2. Verificar credenciales en `application-prod.properties`
3. Verificar conectividad: `telnet tu-db.amazonaws.com 3306`

### Puerto ocupado

```bash
# Encontrar proceso usando puerto 8080
sudo lsof -i :8080

# Matar proceso
sudo kill -9 PID

# O usar el script de cleanup
cd /home/ec2-user/app/scripts
./restart.sh
```

## 📊 Métricas y Monitoreo

### Endpoints disponibles:

- `GET /actuator/health` - Estado de la aplicación
- `GET /actuator/info` - Información de la aplicación
- `GET /actuator/metrics` - Métricas de la aplicación

### Logs importantes:

- **ERROR**: Errores críticos
- **WARN**: Advertencias
- **INFO**: Información general de la aplicación

## 🔄 Rollback

Si necesitas volver a una versión anterior:

1. **Listar versiones disponibles:**

```bash
ls -la /home/ec2-user/app/Backend-*.jar
```

2. **Detener aplicación actual:**

```bash
cd /home/ec2-user/app/scripts
./stop.sh
```

3. **Ejecutar versión anterior:**

```bash
cd /home/ec2-user/app
java -jar -Dspring.profiles.active=prod Backend-version-anterior.jar
```

4. **Verificar funcionamiento:**

```bash
cd /home/ec2-user/app/scripts
./status.sh
```

## 📝 Notas Importantes

- ✅ La aplicación se ejecuta con el usuario `ec2-user`
- ✅ Los logs se rotan automáticamente
- ✅ Se mantienen las últimas 3 versiones del JAR
- ✅ Health checks múltiples verifican que la aplicación esté funcionando
- ✅ El deployment es atómico (todo o nada)
- ✅ Sistema de cleanup multicapa garantiza deployments limpios
- ✅ Scripts de gestión se crean automáticamente durante el deployment
- ✅ Retry logic en health checks para mayor robustez

## 🆘 Soporte

Si tienes problemas:

1. **Revisa los logs**: `/home/ec2-user/app/app.log`
2. **Verifica el estado**: `cd /home/ec2-user/app/scripts && ./status.sh`
3. **Revisa el workflow** en GitHub Actions
4. **Verifica la conectividad** de red y base de datos
5. **Usa el script de restart**: `cd /home/ec2-user/app/scripts && ./restart.sh`
