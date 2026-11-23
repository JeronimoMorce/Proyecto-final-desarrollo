# ===== Etapa 1: compilar el proyecto con Maven =====
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -q clean package

# ===== Etapa 2: imagen liviana que solo ejecuta el jar =====
FROM eclipse-temurin:17-jre
WORKDIR /app

# El nombre sale de <finalName>cards4play</finalName> en el pom.xml
COPY --from=build /build/target/cards4play.jar app.jar

# Configuración que no es secreta. NO va PORT (lo asigna Railway),
# NO va EXCHANGE_API_KEY (es secreta) y NO va VOLUME (Railway lo rechaza).
ENV DATA_DIR=/app/data \
    TASA_DEFECTO=4000

EXPOSE 4567

CMD ["java", "-jar", "app.jar"]