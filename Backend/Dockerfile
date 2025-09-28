# ====================
# Build Stage
# ====================
FROM maven:3.9.2-eclipse-temurin-17 AS builder

WORKDIR /app

# Copiar archivos necesarios para Maven
COPY pom.xml ./

# Descargar dependencias para cachear
RUN mvn -q dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar JAR
RUN mvn -q clean package -DskipTests -B

# ====================
# Runtime Stage
# ====================
FROM eclipse-temurin:17-jdk-focal

WORKDIR /app

# Instalar curl como root antes de cambiar de usuario
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copiar JAR desde build stage
COPY --from=builder /app/target/Backend-*.jar app.jar

# Crear usuario no-root
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app

# Cambiar a usuario no-root
USER appuser

# Exponer puerto
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar -Dspring.profiles.active=prod app.jar"]
