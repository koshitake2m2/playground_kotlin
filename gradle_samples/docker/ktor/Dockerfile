# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy entire project from parent directory
COPY . .

# Build the application using Ktor plugin's buildFatJar task
RUN gradle :ktor:buildFatJar --no-daemon

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/ktor/build/libs/*-all.jar app.jar

# Expose the port that Ktor runs on
EXPOSE 10000

# Set environment variables for database connection
ENV DB_TYPE=flyway
ENV DB_HOST=mysql8
ENV DB_PORT=3306

# Run the application
CMD ["java", "-jar", "app.jar"]
