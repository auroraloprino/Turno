FROM eclipse-temurin:21-jdk-alpine
RUN apk add --no-cache openssl
COPY genera-certificati.sh /genera-certificati.sh
ENTRYPOINT ["sh", "/genera-certificati.sh"]
