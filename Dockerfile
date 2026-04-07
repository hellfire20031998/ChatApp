# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src

RUN chmod +x mvnw && ./mvnw package -DskipTests -B

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/Real-Time-Notifications-System-*.jar app.jar

# Render sets PORT; default 8080 for local runs
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar --server.port=${PORT}"]
