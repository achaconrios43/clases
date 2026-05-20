# ========================================
# ETAPA 1: BUILD - Java 25 con mvnw wrapper
# ========================================
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copiar todo el proyecto
COPY pom.xml .
COPY mvnw mvnw
COPY .mvn .mvn
COPY src ./src

RUN sed -i 's/\r//' mvnw && chmod +x mvnw && ./mvnw clean package -DskipTests -B

# ========================================
# ETAPA 2: RUNTIME - JRE 25 ligero
# ========================================
FROM eclipse-temurin:25-jre AS runtime

LABEL maintainer="achaconrios@gmail.com"
LABEL description="DCIM - Data Center Infrastructure Management"
LABEL version="1.0"

RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8082

ENV JAVA_OPTS="-Xmx450m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8" \
    SPRING_PROFILES_ACTIVE=production \
    PORT=8082

HEALTHCHECK --interval=60s --timeout=10s --start-period=240s --retries=5 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

CMD java $JAVA_OPTS -Dserver.port=$PORT -jar app.jar
