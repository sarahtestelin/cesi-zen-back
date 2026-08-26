# Étape 1 : build du jar
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY . .
RUN mvn -DskipTests package


# Étape 2 : exécution
FROM eclipse-temurin:21-jre

WORKDIR /app

# Installation de curl pour le healthcheck
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system cesizen \
    && useradd --system --gid cesizen --no-create-home cesizen

COPY --from=build --chown=cesizen:cesizen /app/target/*.jar app.jar

# L'application ne tourne pas avec les droits root
USER cesizen

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]