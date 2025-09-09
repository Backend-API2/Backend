# 🚀 Configuración CI/CD - Backend API

Esta guía explica cómo configurar el sistema CI/CD mínimo para actualizar automáticamente el servidor backend cuando se hagan cambios en la rama master.

## 📋 Prerrequisitos

### En GitHub:

1. **Secrets configurados** en el repositorio
2. **Rama master** como rama principal
3. **Acceso SSH** a tu servidor EC2

### En tu servidor EC2:

1. **Java 21** instalado
2. **Curl** para health checks
3. **Usuario ubuntu** con permisos sudo
4. **Puerto 8080** abierto en Security Groups

## 🔧 Configuración de Secrets en GitHub

Ve a `Settings > Secrets and variables > Actions` en tu repositorio de GitHub y agrega:

```
EC2_HOST=tu-ip-ec2.amazonaws.com
EC2_USERNAME=ubuntu
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
   ssh-copy-id -i ~/.ssh/id_rsa.pub ubuntu@tu-ip-ec2
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

## 🔄 Flujo de CI/CD

### 1. **Push a master**

- ✅ Se ejecuta automáticamente el workflow
- ✅ Compila y ejecuta tests
- ✅ Crea el JAR ejecutable
- ✅ Despliega automáticamente a EC2

### 2. **Deployment automático**

- 🛑 Detiene la aplicación actual
- 📦 Copia el nuevo JAR a EC2
- 🚀 Inicia la nueva versión
- ✅ Verifica que esté funcionando

## 🛠️ Scripts de Gestión

Los scripts están ubicados en `/home/ubuntu/app/scripts/` en tu servidor EC2:

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

## 🔍 Monitoreo

### Health Check

```bash
curl http://tu-ip-ec2:8080/actuator/health
```

### Ver logs en tiempo real

```bash
# Con JAR directo
tail -f /home/ubuntu/app/app.log

# Con Docker
docker-compose logs -f
```

### Ver estado de la aplicación

```bash
# Con JAR directo
./status.sh

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

## 🚨 Troubleshooting

### La aplicación no inicia

1. **Verificar logs**:

   ```bash
   tail -f /home/ubuntu/app/app.log
   ```

2. **Verificar Java**:

   ```bash
   java -version
   ```

3. **Verificar puerto**:
   ```bash
   netstat -tlnp | grep 8080
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
```

### Problemas con GitHub Actions

1. **Verificar Secrets**: Asegúrate de que todos los secrets estén configurados correctamente
2. **Verificar conectividad SSH**: Prueba la conexión manualmente
3. **Verificar logs del workflow**: Ve a la pestaña "Actions" en GitHub

## 🔄 Rollback

Si necesitas volver a una versión anterior:

1. **Listar versiones disponibles**:

   ```bash
   ls -la /home/ubuntu/app/Backend-*.jar
   ```

2. **Detener aplicación actual**:

   ```bash
   ./stop.sh
   ```

3. **Ejecutar versión anterior**:
   ```bash
   java -jar Backend-version-anterior.jar
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
- [ ] Workflow de GitHub Actions configurado
- [ ] Scripts de gestión copiados a EC2
- [ ] Health check funcionando

## 🆘 Soporte

Si tienes problemas:

1. Revisa los logs: `/home/ubuntu/app/app.log`
2. Verifica el estado: `./status.sh`
3. Revisa el workflow en GitHub Actions
4. Verifica la conectividad de red y base de datos

## 📝 Notas Importantes

- ✅ La aplicación se ejecuta con el usuario `ubuntu`
- ✅ Los logs se rotan automáticamente
- ✅ Se mantienen las últimas 3 versiones del JAR
- ✅ Health checks verifican que la aplicación esté funcionando
- ✅ El deployment es atómico (todo o nada)
- ✅ Los tests se ejecutan antes del deployment
- ✅ El sistema es completamente automatizado
