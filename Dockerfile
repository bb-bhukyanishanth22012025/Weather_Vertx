# Use Maven to build the application
FROM maven:3.8-openjdk-11-slim AS build

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and source files
COPY pom.xml /app/
COPY src /app/src/

# Build the project with Maven
RUN mvn clean package -DskipTests

# Create a new image based on OpenJDK 11
FROM openjdk:11-jre-slim

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from the previous build stage
COPY --from=build /app/target/weather_vertx-1.0-SNAPSHOT.jar /app/weather_vertx.jar

# Expose the application port (same as in your MainVerticle)
EXPOSE 8888

# Run the application
CMD ["java", "-jar", "weather_vertx.jar"]
