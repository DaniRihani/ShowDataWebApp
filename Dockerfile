FROM maven:3.8.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Explicitly copy the assembly JAR
COPY --from=build /app/target/auth-mongodb-app-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]