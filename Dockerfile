# =====================================================================
#  Cards4Play - Dockerfile en dos etapas
#
#  Etapa 1 (build):   usa una imagen con Maven + JDK para compilar y
#                     generar target/cards4play.jar
#  Etapa 2 (runtime): usa una imagen solo con el JRE (más liviana) y
#                     copia únicamente el .jar. Maven y el código fuente
#                     no quedan en la imagen final.
# =====================================================================

# ---------- Etapa 1: compilar ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Primero solo el pom.xml: Docker guarda en caché las dependencias descargadas.
# Si después solo cambia el código, no se vuelven a descargar.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Ahora el código fuente y se empaqueta el jar
COPY src ./src
RUN mvn -B -q package

# ---------- Etapa 2: ejecutar ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/cards4play.jar app.jar

# Variables de entorno con valores por defecto.
# Se pueden cambiar al ejecutar con:  docker run -e TASA_DEFECTO=4200 ...
#   PORT:         puerto donde escucha la API dentro del contenedor
#   DATA_DIR:     carpeta de los archivos JSON (es el volumen)
#   TASA_DEFECTO: tasa USD -> COP si la API externa falla
# La llave EXCHANGE_API_KEY NO se escribe aquí (este archivo se sube a GitHub):
# se pasa con -e al ejecutar el contenedor.
ENV PORT=4567 \
    DATA_DIR=/app/data \
    TASA_DEFECTO=4000

# La carpeta de datos se declara como volumen: lo que se guarde aquí
# sobrevive aunque el contenedor se elimine.
VOLUME ["/app/data"]

# Documenta el puerto que usa la aplicación (el mapeo real se hace con -p)
EXPOSE 4567

# Comando que se ejecuta al iniciar el contenedor
CMD ["java", "-jar", "app.jar"]
