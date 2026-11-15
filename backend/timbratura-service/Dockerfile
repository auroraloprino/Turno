# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY turno-common/pom.xml turno-common/
COPY auth-service/pom.xml auth-service/
COPY timbratura-service/pom.xml timbratura-service/
COPY permessi-service/pom.xml permessi-service/
COPY turni-service/pom.xml turni-service/
COPY chat-service/pom.xml chat-service/
COPY orchestratore-service/pom.xml orchestratore-service/
COPY turno-common/src turno-common/src
COPY timbratura-service/src timbratura-service/src
RUN --mount=type=cache,target=/root/.m2,sharing=locked mvn -q -B -pl timbratura-service -am clean package -DskipTests -Daether.connector.http.retryHandler.count=5

FROM eclipse-temurin:21-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/timbratura-service/target/timbratura-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
