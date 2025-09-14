# 🚀 Configuración CI/CD - Backend API

Esta guía explica cómo configurar el sistema CI/CD robusto y automatizado para actualizar automáticamente el servidor backend cuando se hagan cambios en la rama master.

## 📋 Prerrequisitos

### En GitHub:

1. **Secrets configurados** en el repositorio
2. **Rama master** como rama principal
3. **Acceso SSH** a tu servidor EC2
4. **GitHub Actions** habilitado

### En tu servidor EC2:

1. **Java 21** instalado
2. **Curl** para health checks
3. **Usuario ec2-user** con permisos sudo
4. **Puerto 8080** abierto en Security Groups
5. **lsof** para gestión de procesos

## 🔧 Configuración de Secrets en GitHub

Ve a `Settings > Secrets and variables > Actions` en tu repositorio de GitHub y agrega:

```
EC2_HOST=tu-ip-ec2.amazonaws.com
EC2_USERNAME=ec2-user
EC2_SSH_KEY=tu-clave-privada-ssh
EC2_PORT=22
```

### Cómo obtener la clave SSH:

1. **Generar clave SSH** (si no tienes una):

   ```bash
   ssh-keygen -t rsa -b 4096 -C "tu-email@example.com"
   ```

2. **Copiar clave pública a EC2**:

   ```bash
   ssh-copy-id -i ~/.ssh/id_rsa.pub ec2-user@tu-ip-ec2
   ```

3. **Copiar clave privada** para GitHub Secrets:
   ```bash
   cat ~/.ssh/id_rsa
   ```

## 🏗️ Estructura del Sistema CI/CD

```
Backend/
├── .github/
│   └── workflows/
│       └── deploy.yml          # Workflow de GitHub Actions
├── scripts/                    # Scripts de gestión del servidor
│   ├── start.sh               # Iniciar aplicación
│   ├── stop.sh                # Detener aplicación
│   ├── restart.sh             # Reiniciar aplicación
│   └── status.sh              # Ver estado
├── Dockerfile                 # Configuración Docker
├── docker-compose.yml         # Orquestación Docker
└── .dockerignore             # Archivos ignorados por Docker
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

## 🐳 Opción Docker (Alternativa)

Si prefieres usar Docker en lugar de JAR directo:

### Construir imagen

```bash
docker build -t backend-api .
```

### Ejecutar con Docker Compose

```bash
docker-compose up -d
```

### Ver logs

```bash
docker-compose logs -f
```

### Detener

```bash
docker-compose down
```

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
# Con JAR directo
tail -f /home/ec2-user/app/app.log

# Con Docker
docker-compose logs -f
```

#### Ver logs con filtros

```bash
# Solo errores
grep "ERROR" /home/ec2-user/app/app.log

# Solo warnings
grep "WARN" /home/ec2-user/app/app.log
```

### 🔍 **Ver estado de la aplicación**

```bash
# Con JAR directo
cd /home/ec2-user/app/scripts && ./status.sh

# Con Docker
docker-compose ps
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

1. **Verificar logs**:

   ```bash
   tail -f /home/ec2-user/app/app.log
   ```

2. **Verificar Java**:

   ```bash
   java -version
   ```

3. **Verificar puerto**:

   ```bash
   netstat -tlnp | grep 8080
   ```

4. **Verificar estado**:
   ```bash
   cd /home/ec2-user/app/scripts && ./status.sh
   ```

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
cd /home/ec2-user/app/scripts && ./restart.sh
```

### Problemas con GitHub Actions

1. **Verificar Secrets**: Asegúrate de que todos los secrets estén configurados correctamente
2. **Verificar conectividad SSH**: Prueba la conexión manualmente
3. **Verificar logs del workflow**: Ve a la pestaña "Actions" en GitHub

## 🔄 Rollback

Si necesitas volver a una versión anterior:

1. **Listar versiones disponibles**:

   ```bash
   ls -la /home/ec2-user/app/Backend-*.jar
   ```

2. **Detener aplicación actual**:

   ```bash
   cd /home/ec2-user/app/scripts && ./stop.sh
   ```

3. **Ejecutar versión anterior**:

   ```bash
   cd /home/ec2-user/app
   java -jar -Dspring.profiles.active=prod Backend-version-anterior.jar
   ```

4. **Verificar funcionamiento**:
   ```bash
   cd /home/ec2-user/app/scripts && ./status.sh
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

## ✅ Checklist de Configuración

- [ ] Secrets configurados en GitHub
- [ ] Clave SSH configurada en EC2
- [ ] Puerto 8080 abierto en Security Groups
- [ ] Java 21 instalado en EC2
- [ ] Curl instalado en EC2
- [ ] lsof instalado en EC2
- [ ] Workflow de GitHub Actions configurado
- [ ] Scripts de gestión se crean automáticamente
- [ ] Health checks múltiples funcionando
- [ ] Sistema de cleanup multicapa operativo

## 🆘 Soporte

Si tienes problemas:

1. **Revisa los logs**: `/home/ec2-user/app/app.log`
2. **Verifica el estado**: `cd /home/ec2-user/app/scripts && ./status.sh`
3. **Revisa el workflow** en GitHub Actions
4. **Verifica la conectividad** de red y base de datos
5. **Usa el script de restart**: `cd /home/ec2-user/app/scripts && ./restart.sh`

## 📝 Notas Importantes

- ✅ La aplicación se ejecuta con el usuario `ec2-user`
- ✅ Los logs se rotan automáticamente
- ✅ Se mantienen las últimas 3 versiones del JAR
- ✅ Health checks múltiples verifican que la aplicación esté funcionando
- ✅ El deployment es atómico (todo o nada)
- ✅ Los tests se ejecutan antes del deployment
- ✅ El sistema es completamente automatizado
- ✅ Sistema de cleanup multicapa garantiza deployments limpios
- ✅ Scripts de gestión se crean automáticamente durante el deployment
- ✅ Retry logic en health checks para mayor robustez
