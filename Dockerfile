# ---------- Stage 1: Build ----------
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy project files needed for build
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY src ./src

# Build the JAR inside Docker (skip tests if needed)
RUN ./mvnw clean package -DskipTests


# ---------- Stage 2: Run ----------
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy only the built JAR from the build stage
COPY --from=build /app/target/expensetracker-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
