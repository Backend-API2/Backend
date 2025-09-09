# 🚀 Guía de Deployment - Backend API

Esta guía explica cómo configurar y desplegar la aplicación Spring Boot en EC2 usando GitHub Actions.

## 📋 Prerrequisitos

### En tu EC2:
1. **Java 21** instalado
2. **Curl** para health checks
3. **Usuario ubuntu** con permisos sudo
4. **Puerto 8080** abierto en Security Groups

### En GitHub:
1. **Secrets** configurados en el repositorio
2. **Rama master/main** como rama principal

## 🔧 Configuración de Secrets en GitHub

Ve a `Settings > Secrets and variables > Actions` y agrega:

```
EC2_HOST=tu-ip-ec2.amazonaws.com
EC2_USERNAME=ubuntu
EC2_SSH_KEY=tu-clave-privada-ssh
EC2_PORT=22
```

## 🏗️ Estructura del Deployment

```
/home/ubuntu/app/
├── Backend-0.0.1-SNAPSHOT.jar    # JAR de la aplicación
├── app.pid                       # PID del proceso
├── app.log                       # Logs de la aplicación
└── scripts/                      # Scripts de gestión
    ├── start.sh
    ├── stop.sh
    ├── restart.sh
    └── status.sh
```

## 🔄 Flujo de CI/CD

### 1. **Push a master/main**
- Se ejecuta automáticamente el workflow
- Compila y ejecuta tests
- Crea el JAR ejecutable

### 2. **Deployment automático**
- Detiene la aplicación actual
- Copia el nuevo JAR a EC2
- Inicia la nueva versión
- Verifica que esté funcionando

## 🛠️ Scripts de Gestión

### Iniciar aplicación
```bash
cd /home/ubuntu/app/scripts
./start.sh
```

### Detener aplicación
```bash
cd /home/ubuntu/app/scripts
./stop.sh
```

### Reiniciar aplicación
```bash
cd /home/ubuntu/app/scripts
./restart.sh
```

### Ver estado
```bash
cd /home/ubuntu/app/scripts
./status.sh
```

## 🔍 Monitoreo

### Health Check
```bash
curl http://tu-ip-ec2:8080/actuator/health
```

### Ver logs en tiempo real
```bash
tail -f /home/ubuntu/app/app.log
```

### Ver logs recientes
```bash
tail -50 /home/ubuntu/app/app.log
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

## 🚨 Troubleshooting

### La aplicación no inicia
1. Verificar logs: `tail -f /home/ubuntu/app/app.log`
2. Verificar Java: `java -version`
3. Verificar puerto: `netstat -tlnp | grep 8080`

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
ls -la /home/ubuntu/app/Backend-*.jar
```

2. **Detener aplicación actual:**
```bash
./stop.sh
```

3. **Ejecutar versión anterior:**
```bash
java -jar Backend-version-anterior.jar
```

## 📝 Notas Importantes

- ✅ La aplicación se ejecuta con el usuario `ubuntu`
- ✅ Los logs se rotan automáticamente
- ✅ Se mantienen las últimas 3 versiones del JAR
- ✅ Health checks verifican que la aplicación esté funcionando
- ✅ El deployment es atómico (todo o nada)

## 🆘 Soporte

Si tienes problemas:

1. Revisa los logs: `/home/ubuntu/app/app.log`
2. Verifica el estado: `./status.sh`
3. Revisa el workflow en GitHub Actions
4. Verifica la conectividad de red y base de datos