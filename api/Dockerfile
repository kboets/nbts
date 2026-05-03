# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app

# Copy the maven wrapper and pom first to leverage Docker cache
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline


# Copy source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy the built jar from the builder stage
COPY --from=builder /app/target/nbts.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

