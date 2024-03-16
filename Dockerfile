# Use the official Maven image as the build image
FROM maven:3.8.2-openjdk-21 AS builder

# Define a working directory in the container
WORKDIR /app

# Copy only the POM file to leverage Docker cache
COPY pom.xml .

# Download the dependencies and package the application
RUN mvn clean package

# Use the official OpenJDK 21 image as the base image for the final image
FROM openjdk:21-jdk-slim

# Define a working directory in the container
WORKDIR /app

# Copy the JAR file from the builder stage to the final image
COPY --from=builder /app/target/sua-aplicacao.jar /app/sua-aplicacao.jar

# Expose the port that your Spring Boot application will run on
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "/app/sua-aplicacao.jar"]