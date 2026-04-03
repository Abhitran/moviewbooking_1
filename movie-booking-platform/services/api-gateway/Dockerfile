# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy parent pom and common modules first for better caching
COPY pom.xml .
COPY common/shared-utils common/shared-utils
COPY common/shared-dtos common/shared-dtos
COPY common/domain-events common/domain-events

# Copy api-gateway source
COPY services/api-gateway services/api-gateway

# Build the application
RUN mvn clean package -pl services/api-gateway -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar
COPY --from=build /app/services/api-gateway/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
