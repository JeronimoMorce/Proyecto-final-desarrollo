# Cards4Play — Backend

Sistema backend para la tienda **Cards4Play**: gestiona el inventario de productos de juegos de cartas coleccionables (TCG), las compras de los clientes, los boosters y los torneos.

Proyecto final de Desarrollo de Software — Institución Universitaria EAM (Armenia, Colombia).

## Integrantes

- Nicole Dávila Quintero
- Jerónimo Morales Ceballos

## Estado del proyecto

| Entrega | Contenido | Estado |
|---|---|---|
| 1 | Diagrama de clases UML | Lista |
| 2 | Clases codificadas (esqueleto de la aplicación) | Lista |
| 3 | API REST, Docker, despliegue en PaaS y colección de Postman | Pendiente |

En esta entrega la aplicación todavía no expone endpoints. La clase `Main` ejecuta una demostración por consola que recorre todo el sistema para mostrar que las clases están conectadas como en el diagrama.

## Tecnologías

- Java 17
- Maven (manejo de dependencias)
- Gson 2.11.0 (lectura y escritura de JSON)
- IntelliJ IDEA

## Cómo ejecutar el proyecto en IntelliJ IDEA

1. Clonar el repositorio: `git clone <url-del-repositorio>`
2. En IntelliJ: **File > Open** y seleccionar la carpeta que contiene el archivo `pom.xml`.
3. Esperar a que IntelliJ importe el proyecto Maven y descargue Gson (barra de progreso abajo a la derecha).
4. Verificar el JDK en **File > Project Structure > Project > SDK**: debe ser 17 o superior.
5. Abrir `src/main/java/co/edu/eam/cards4play/Main.java` y dar clic en el botón verde de ejecutar.

La demostración crea la carpeta `data/` con los archivos JSON. Esa carpeta está en el `.gitignore` porque son datos generados, no código.

Si en la consola las tildes salen como `?`, revisar **File > Settings > Editor > File Encodings** y dejar todo en UTF-8.

## Estructura de paquetes

```
src/main/java/co/edu/eam/cards4play
├── Main.java                  Punto de entrada (demostración por consola)
├── model                      Clases del dominio: lo que existe en la tienda
│   ├── Usuario.java           (abstracta) datos comunes y autenticación
│   ├── Administrador.java     gestiona clientes, inventario, boosters y torneos
│   ├── Cliente.java           consulta, compra, abre boosters y se inscribe en torneos
│   ├── Producto.java          (abstracta) id, nombre, precio en USD y stock
│   ├── Carta.java             producto con rareza
│   ├── Booster.java           sobre con 10 cartas, con dueño y estado abierto/cerrado
│   ├── ProductoSellado.java
│   ├── Accesorio.java
│   ├── Inventario.java        contiene todos los productos
│   ├── Compra.java            registro de una compra (historial del cliente)
│   ├── Torneo.java            cupos, fechas y participantes
│   ├── TasaCambio.java        valor USD -> COP y su fuente
│   └── enums
│       ├── Rol.java           ADMIN, CLIENTE
│       ├── Rareza.java        N, UC, R, SR, UR con sus probabilidades
│       └── FuenteTasa.java    API, DEFECTO
├── service                    Lógica que no pertenece a una sola entidad
│   ├── GeneradorBooster.java  sortea las 10 cartas de cada booster
│   └── ServicioTasaCambio.java consulta la API externa de tasa de cambio
├── repository                 Persistencia en archivos JSON
│   ├── RepositorioJson.java   guarda y lee listas de cualquier tipo
│   ├── ConfiguracionGson.java configuración de Gson
│   └── adapter                adaptadores para Producto y fechas
├── exception
│   └── Cards4PlayException.java  error de regla de negocio con su código HTTP
└── util
    └── GeneradorId.java       crea identificadores únicos
```

## Cómo se conectan las clases

- **Herencia:** `Administrador` y `Cliente` heredan de `Usuario`. `Carta`, `Booster`, `ProductoSellado` y `Accesorio` heredan de `Producto`.
- **Inventario:** contiene todos los productos. El cliente ve los disponibles (`consultarDisponibles`) y el administrador ve todo el stock (`consultarStock`).
- **Tasa de cambio:** `ServicioTasaCambio` crea una `TasaCambio` y `Producto.calcularPrecioCop(tasa)` hace la conversión al consultar el producto. `Compra` solo guarda una copia del precio pagado.
- **Boosters:** `Administrador.crearBooster` le pide el booster a `GeneradorBooster`, que sortea las cartas con `Rareza.sortear()` y las toma del `Inventario`.
- **Compras:** `Cliente.comprar` crea una `Compra` y la agrega a su historial. Si el producto es un booster, queda asignado al cliente.
- **Torneos:** `Cliente.inscribirse` delega en `Torneo.inscribir`, que valida fecha límite, inscripción duplicada y cupo.

## Reglas de negocio implementadas

| Regla | Dónde está | Código HTTP previsto |
|---|---|---|
| Datos inválidos o incompletos | constructores de `Producto`, `Usuario`, `Torneo` | 400 |
| Abrir un booster ajeno | `Booster.abrir` | 403 |
| Producto inexistente | `Inventario.buscarPorId` | 404 |
| Booster ya vendido o producto sin stock | `Cliente.comprar`, `Booster.vender` | 409 |
| Booster ya abierto | `Booster.abrir` | 409 |
| Torneo sin cupo o inscripción duplicada | `Torneo.inscribir` | 409 |
| Inscripción el día del torneo o después | `Torneo.inscribir` | 409 |
| Crear un booster sin cartas registradas | `GeneradorBooster.generar` | 409 |

## API externa y tasa por defecto

- **API utilizada:** `https://open.er-api.com/v6/latest/USD` (ExchangeRate-API, acceso abierto sin llave).
- **Tiempo máximo de espera:** 3 segundos.
- **Tasa por defecto:** se lee de la variable de entorno `TASA_DEFECTO`. Si la variable no existe se usa `4000.0`.
- Cuando la API no responde, la consola muestra: `La API no respondió, por lo que se toma un valor por defecto de X`.

Para probar otra tasa por defecto en IntelliJ: **Run > Edit Configurations > Environment variables** y agregar `TASA_DEFECTO=4200`.

## Flujo de trabajo con Git

- La rama `main` siempre contiene una versión que compila y se puede ejecutar.
- Cada funcionalidad se desarrolla en su propia rama `feature/nombre-corto` y se integra a `main` con un merge.
- Antes de crear una rama nueva: `git checkout main` y `git pull origin main`.
- Mensajes de commit descriptivos con un prefijo:
  - `feat:` funcionalidad nueva
  - `fix:` corrección de un error
  - `docs:` documentación
  - `chore:` configuración del proyecto
  - `refactor:` mejora del código sin cambiar lo que hace

## Resolución de un conflicto de fusión

> Pendiente: documentar aquí el conflicto que se presente (qué archivo, por qué ocurrió y cómo se resolvió).

## Pendiente para la siguiente entrega

- Exponer la funcionalidad como API REST con Spark Java.
- Autenticación por petición y restricción de acciones según el rol.
- Cargar los datos desde los archivos JSON al iniciar y guardarlos en cada cambio.
- Dockerfile con la variable `TASA_DEFECTO` y volumen para la carpeta `data/`.
- Despliegue en una plataforma PaaS.
- Colección de Postman con casos exitosos y de error.
