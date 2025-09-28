# ====================
# Build Stage
# ====================
FROM maven:3.9.2-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml mvnw .mvn/ ./
RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ====================
# Runtime Stage
# ====================
FROM openjdk:17-jdk
WORKDIR /app

COPY --from=builder /app/target/Backend-*.jar app.jar

RUN groupadd -r appuser && useradd -r -g appuser appuser
RUN chown -R appuser:appuser /app
USER appuser

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar -Dspring.profiles.active=prod app.jar"]
