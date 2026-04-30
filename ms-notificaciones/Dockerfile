FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["sh", "-c", "echo INICIANDO_APP && ls -la && java -version && java -jar app.jar"]