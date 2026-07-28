
# Dockerfile pour API Gateway
# Point d'entrée principal de l'API

# Stage 1: Build avec Maven
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copier le pom.xml et télécharger les dépendances
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le code source et builder
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime avec Java 21
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copier le jar depuis le stage de build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port du Gateway
EXPOSE 8080

# Variables d'environnement par défaut
ENV JAVA_OPTS="-Xms512m -Xmx1024m"
ENV SPRING_PROFILES_ACTIVE=prod

# Point d'entrée avec options JVM
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

# Healthcheck pour Docker Compose
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1