#!/bin/bash

# Script de configuración inicial para EC2
# Ejecutar en tu instancia EC2 antes del primer deployment

echo "🔧 Configurando EC2 para deployment de Spring Boot"
echo "=================================================="

# Actualizar sistema
echo "1️⃣ Actualizando sistema..."
sudo apt update && sudo apt upgrade -y

# Instalar Java 21
echo "2️⃣ Instalando Java 21..."
sudo apt install -y openjdk-21-jdk

# Verificar instalación
echo "3️⃣ Verificando Java..."
java -version

# Instalar curl para health checks
echo "4️⃣ Instalando curl..."
sudo apt install -y curl

# Crear directorio de aplicación
echo "5️⃣ Creando directorio de aplicación..."
mkdir -p /home/ubuntu/app
chown ubuntu:ubuntu /home/ubuntu/app

# Configurar firewall (opcional)
echo "6️⃣ Configurando firewall..."
sudo ufw allow 8080/tcp
sudo ufw --force enable

# Crear usuario de sistema para la aplicación (opcional)
echo "7️⃣ Creando usuario de sistema..."
sudo useradd -r -s /bin/false -d /home/ubuntu/app appuser || echo "Usuario ya existe"

# Configurar límites del sistema
echo "8️⃣ Configurando límites del sistema..."
echo "ubuntu soft nofile 65536" | sudo tee -a /etc/security/limits.conf
echo "ubuntu hard nofile 65536" | sudo tee -a /etc/security/limits.conf

# Configurar swap (opcional, para instancias pequeñas)
echo "9️⃣ Configurando swap..."
if [ ! -f /swapfile ]; then
    sudo fallocate -l 1G /swapfile
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
fi

# Instalar herramientas de monitoreo
echo "🔟 Instalando herramientas de monitoreo..."
sudo apt install -y htop iotop nethogs

echo ""
echo "✅ Configuración completada!"
echo ""
echo "📋 Próximos pasos:"
echo "1. Configura los secrets en GitHub:"
echo "   - EC2_HOST: $(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)"
echo "   - EC2_USERNAME: ubuntu"
echo "   - EC2_SSH_KEY: [tu clave privada SSH]"
echo "   - EC2_PORT: 22"
echo ""
echo "2. Haz push a la rama master para activar el deployment"
echo ""
echo "3. Verifica el deployment:"
echo "   curl http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8080/actuator/health"
echo ""
echo "🎉 ¡Tu EC2 está listo para recibir deployments!"
