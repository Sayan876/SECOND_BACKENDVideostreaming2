# ===============================
# Stage 1: Build the JAR
# ===============================
FROM maven:3.9.12-eclipse-temurin-21-noble AS builder
WORKDIR /app

# Copy project files
COPY . .

# Build the Spring Boot JAR (skip tests for speed)
RUN mvn clean package -DskipTests

# ===============================
# Stage 2: Run the app
# ===============================
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy the JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port 8080 for Render
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
