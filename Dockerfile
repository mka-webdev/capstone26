FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY src ./src
RUN mvn -q -DskipTests package

FROM mcr.microsoft.com/playwright:v1.58.2-jammy
USER root

RUN apt-get update \
    && apt-get install -y --no-install-recommends openjdk-17-jre-headless \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /workspace/target/OAGP-0.0.1-SNAPSHOT.jar ./app.jar
COPY scanner/package*.json ./scanner/
COPY scanner/scan-page.js ./scanner/

WORKDIR /app/scanner
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
RUN npm install --omit=dev

WORKDIR /app
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
