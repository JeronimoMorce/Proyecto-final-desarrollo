# Cards4Play — API REST

API REST para la tienda **Cards4Play**: inventario de productos de juegos de cartas coleccionables (TCG), compras de clientes, boosters y torneos. Los datos se guardan en archivos JSON dentro de un volumen de Docker.

Proyecto final de Desarrollo de Software — Institución Universitaria EAM (Armenia, Colombia).

## Integrantes

- Nicole Dávila Quintero
- Jerónimo Morales Ceballos

## Estado del proyecto

| Entrega | Contenido | Estado |
|---|---|---|
| 1 | Diagrama de clases UML | Lista |
| 2 | Clases codificadas (esqueleto) | Lista |
| 3 | API REST, persistencia en volumen Docker y colección de Postman | Lista |
| 4 | Despliegue en PaaS | Pendiente |

## Tecnologías

- Java 17
- Spark Java 2.9.4 (microframework web)
- Gson 2.11.0 (JSON)
- Maven (dependencias y empaquetado en un solo `.jar`)
- Docker (imagen en dos etapas y volumen para los datos)
- Postman (colección de pruebas en `postman/`)

## Estructura de paquetes

```
src/main/java/co/edu/eam/cards4play
├── Main.java          Arma la aplicación y enciende el servidor
├── config             Configuracion (variables de entorno) y Servidor (Spark: filtros, errores)
├── security           FiltroAutenticacion (Basic Auth en cada petición)
├── controller         Rutas HTTP: Sistema, Cliente, Producto, Booster, Compra, Torneo
├── dto                Cuerpos JSON de las peticiones, con validación de campos
├── mapper             MapeadorRespuesta: objetos del modelo -> JSON de respuesta
├── service            Lógica de negocio, GeneradorBooster y ServicioTasaCambio
├── repository         AlmacenDatos y RepositorioJson (persistencia en archivos JSON)
├── model              Entidades del dominio y enums
├── exception          Cards4PlayException (error con código HTTP)
└── util               GeneradorId, Seguridad (SHA-256), Conversor
```

Recorrido de una petición: `FiltroAutenticacion` → `Controller` → `Service` → `model` → `AlmacenDatos` (guarda JSON) → `MapeadorRespuesta` → respuesta JSON.

## Variables de entorno

| Variable | Para qué | Valor por defecto |
|---|---|---|
| `PORT` | Puerto de la API | `4567` |
| `DATA_DIR` | Carpeta de los archivos JSON | `data` (en Docker: `/app/data`) |
| `TASA_DEFECTO` | Tasa USD→COP si la API externa falla | `4000` |
| `EXCHANGE_API_KEY` | Llave de ExchangeRate-API | vacía (se usa la tasa por defecto) |
| `ADMIN_USERNAME` | Usuario del administrador por defecto | `admin` |
| `ADMIN_PASSWORD` | Contraseña del administrador por defecto | `admin123` |

La llave de la API **no** se escribe en el código ni en el Dockerfile.

## Ejecutar en IntelliJ IDEA

1. **File > Open** y elegir la carpeta que contiene `pom.xml`. Esperar a que Maven descargue las dependencias.
2. **Run > Edit Configurations**, seleccionar `Main` y en **Environment variables** escribir:
   `EXCHANGE_API_KEY=su_llave;TASA_DEFECTO=4000`
3. Ejecutar `Main`. La consola muestra `Cards4Play API escuchando en http://localhost:4567`.
4. Probar en el navegador: `http://localhost:4567/api/salud`.

## Ejecutar con Docker

```bash
# 1. Construir la imagen
docker build -t cards4play:1.0 .

# 2. Ejecutar el contenedor con volumen y variables de entorno
docker run -d --name cards4play \
  -p 4567:4567 \
  -v cards4play-datos:/app/data \
  -e TASA_DEFECTO=4100 \
  -e EXCHANGE_API_KEY=su_llave \
  cards4play:1.0

# 3. Ver los mensajes de la aplicación
docker logs -f cards4play

# 4. Comprobar la persistencia: borrar el contenedor y crearlo de nuevo con el mismo volumen
docker rm -f cards4play
docker run -d --name cards4play -p 4567:4567 -v cards4play-datos:/app/data cards4play:1.0
```

Los datos quedan en el volumen `cards4play-datos`. Para empezar desde cero: `docker volume rm cards4play-datos`.

En Windows (PowerShell) se cambia `\` por `` ` `` al final de cada línea, o se escribe todo en una sola línea.

## Autenticación

Basic Auth en cada petición (en Postman: pestaña **Authorization > Basic Auth**).

- Administrador por defecto: `admin` / `admin123` (configurable con variables de entorno).
- Los clientes los registra el administrador con `POST /api/clientes`.

## Endpoints

| Método | Ruta | Rol | Éxito |
|---|---|---|---|
| GET | `/api/salud` | Pública | 200 |
| GET | `/api/ayuda` | Pública | 200 |
| GET | `/api/yo` | Cualquiera | 200 |
| GET | `/api/tasa` | Cualquiera | 200 |
| POST | `/api/clientes` | Admin | 201 |
| GET | `/api/clientes` | Admin | 200 |
| GET | `/api/clientes/:id` | Admin | 200 |
| GET | `/api/clientes/:id/compras` | Admin | 200 |
| DELETE | `/api/clientes/:id` | Admin | 204 |
| GET | `/api/productos?tipo=CARTA\|BOOSTER\|SELLADO\|ACCESORIO` | Cualquiera | 200 |
| GET | `/api/productos/:id` | Cualquiera | 200 |
| GET | `/api/cartas`, `/api/sellados`, `/api/accesorios` | Cualquiera | 200 |
| POST | `/api/cartas` | Admin | 201 |
| POST | `/api/sellados` | Admin | 201 |
| POST | `/api/accesorios` | Admin | 201 |
| PUT | `/api/productos/:id` | Admin | 200 |
| DELETE | `/api/productos/:id` | Admin | 204 |
| GET | `/api/boosters` | Cualquiera | 200 |
| POST | `/api/boosters` | Admin | 201 |
| GET | `/api/mis-boosters` | Cliente | 200 |
| POST | `/api/boosters/:id/abrir` | Cliente | 200 |
| POST | `/api/compras` | Cliente | 201 |
| GET | `/api/compras` | Cliente | 200 |
| GET | `/api/torneos` | Cualquiera | 200 |
| GET | `/api/torneos/:id` | Cualquiera | 200 |
| POST | `/api/torneos` | Admin | 201 |
| PUT | `/api/torneos/:id` | Admin | 200 |
| DELETE | `/api/torneos/:id` | Admin | 204 |
| POST | `/api/torneos/:id/inscripciones` | Cliente | 201 |

`GET /api/ayuda` devuelve esta misma lista con un ejemplo del cuerpo JSON de cada petición.

## Códigos de respuesta HTTP

| Código | Cuándo |
|---|---|
| 200 OK | Consulta o modificación exitosa |
| 201 Created | Se creó un cliente, producto, booster, compra, torneo o inscripción |
| 204 No Content | Se eliminó un cliente, producto o torneo |
| 400 Bad Request | Faltan campos, JSON mal escrito, rareza o fecha inválida |
| 401 Unauthorized | Sin usuario y contraseña, o incorrectos |
| 403 Forbidden | Cliente en acción de admin (o al revés), abrir booster ajeno |
| 404 Not Found | Cliente, producto, booster, torneo o ruta inexistente |
| 409 Conflict | Booster vendido o abierto, sin stock, torneo sin cupo o cerrado, inscripción o usuario duplicado |
| 500 Internal Server Error | Error inesperado del servidor |

Todos los errores responden con el mismo formato:

```json
{ "error": "Faltan datos obligatorios: rareza", "codigo": 400, "camposFaltantes": ["rareza"], "ejemplo": { "...": "..." } }
```

## API externa y tasa por defecto

- **API:** ExchangeRate-API v6 — `https://v6.exchangerate-api.com/v6/{EXCHANGE_API_KEY}/latest/USD`
- **Tiempo máximo de espera:** 3 segundos.
- **Caché:** una tasa obtenida de la API se reutiliza 10 minutos para no agotar la cuota gratuita.
- **Tasa por defecto:** variable `TASA_DEFECTO` (4000 si no existe). Si la API falla, la consola muestra `La API no respondió, por lo que se toma un valor por defecto de X`.
- Toda respuesta con precios en COP incluye `"fuenteTasa": "API"` o `"fuenteTasa": "DEFECTO"`.

## Pruebas con Postman

Importar `postman/Cards4Play.postman_collection.json` y ejecutar las carpetas en orden (0 a 6) o con **Run collection**. Cada petición guarda los ids que usan las siguientes. La carpeta 6 contiene un caso de error por cada código HTTP.

Para repetir la colección desde cero hay que borrar los datos (carpeta `data/` o el volumen de Docker), porque los clientes ya existirán.

## Flujo de trabajo con Git

- `main` siempre contiene una versión que compila.
- Cada funcionalidad en su rama `feature/...`, integrada a `main` con Pull Request y merge.
- Commits con prefijo: `feat:`, `fix:`, `docs:`, `chore:`, `refactor:`.

## Resolución de un conflicto de fusión

> Pendiente: documentar aquí el conflicto (qué archivo, por qué ocurrió y cómo se resolvió).
