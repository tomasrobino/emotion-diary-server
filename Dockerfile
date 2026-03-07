# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Download dependencies first (layer cached unless pom.xml changes)
RUN apt-get update && apt-get install -y maven
RUN mvn dependency:go-offline -B
RUN mvn package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]