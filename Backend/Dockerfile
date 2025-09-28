# ====================
# Build Stage
# ====================
FROM maven:3.9.2-openjdk-17 AS builder
WORKDIR /app

# Copiar pom y dependencias offline
COPY pom.xml mvnw .mvn/ ./
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente y compilar
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ====================
# Runtime Stage
# ====================
FROM openjdk:17-jdk
WORKDIR /app

# Copiar JAR compilado desde el build stage
COPY --from=builder /app/target/Backend-*.jar app.jar

# Crear usuario no root
RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

# Exponer puerto y healthcheck
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Ejecutar la aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar -Dspring.profiles.active=prod app.jar"]
