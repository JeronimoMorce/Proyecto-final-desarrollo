package co.edu.eam.cards4play;

import co.edu.eam.cards4play.config.Configuracion;
import co.edu.eam.cards4play.config.Servidor;
import co.edu.eam.cards4play.controller.BoosterController;
import co.edu.eam.cards4play.controller.ClienteController;
import co.edu.eam.cards4play.controller.CompraController;
import co.edu.eam.cards4play.controller.ControladorBase;
import co.edu.eam.cards4play.controller.ProductoController;
import co.edu.eam.cards4play.controller.SistemaController;
import co.edu.eam.cards4play.controller.TorneoController;
import co.edu.eam.cards4play.model.Administrador;
import co.edu.eam.cards4play.repository.AlmacenDatos;
import co.edu.eam.cards4play.security.FiltroAutenticacion;
import co.edu.eam.cards4play.service.AutenticacionService;
import co.edu.eam.cards4play.service.BoosterService;
import co.edu.eam.cards4play.service.ClienteService;
import co.edu.eam.cards4play.service.CompraService;
import co.edu.eam.cards4play.service.GeneradorBooster;
import co.edu.eam.cards4play.service.ProductoService;
import co.edu.eam.cards4play.service.ServicioTasaCambio;
import co.edu.eam.cards4play.service.TorneoService;

import java.util.List;

/**
 * Punto de entrada: ARMA la aplicación conectando todas las piezas y enciende el servidor.
 *
 * Ya no hay datos quemados. Lo único que existe al iniciar es el administrador
 * por defecto (lo pide el enunciado). Todo lo demás llega por la API.
 *
 * El orden importa, porque cada pieza necesita las anteriores:
 *
 *   Configuracion  (variables de entorno)
 *        |
 *   AlmacenDatos   (lee los JSON de la carpeta de datos)
 *        |
 *   Servicios      (reglas de negocio; reciben el almacén)
 *        |
 *   Controladores  (rutas HTTP; reciben los servicios)
 *        |
 *   Servidor       (Spark: filtros, rutas y errores)
 *
 * A esta forma de pasar a cada objeto lo que necesita por su constructor se le
 * llama "inyección de dependencias" (aquí hecha a mano, sin frameworks).
 */
public class Main {

    public static void main(String[] args) {
        // 1. Configuración desde variables de entorno
        Configuracion config = Configuracion.desdeEntorno();

        // 2. Datos: se cargan los archivos JSON (si existen)
        AlmacenDatos almacen = new AlmacenDatos(config.getCarpetaDatos());
        almacen.cargar();

        // 3. Administrador por defecto (se crea en cada arranque, no se guarda en archivo)
        Administrador admin = new Administrador(config.getAdminUsername(), config.getAdminPassword());

        // 4. Servicios
        ServicioTasaCambio servicioTasa = new ServicioTasaCambio(config.getLlaveApiTasa(), config.getTasaDefecto());
        AutenticacionService autenticacion = new AutenticacionService(admin, almacen);
        ClienteService clienteService = new ClienteService(almacen, autenticacion);
        ProductoService productoService = new ProductoService(almacen);
        BoosterService boosterService = new BoosterService(almacen, new GeneradorBooster());
        CompraService compraService = new CompraService(almacen, servicioTasa);
        TorneoService torneoService = new TorneoService(almacen);

        // 5. Controladores (cada uno registra sus rutas)
        List<ControladorBase> controladores = List.of(
                new SistemaController(servicioTasa),
                new ClienteController(clienteService, almacen),
                new ProductoController(productoService, servicioTasa),
                new BoosterController(boosterService, productoService, servicioTasa),
                new CompraController(compraService, almacen),
                new TorneoController(torneoService, almacen));

        // 6. Encender el servidor
        new Servidor().iniciar(config.getPuerto(), new FiltroAutenticacion(autenticacion), controladores);

        System.out.println("==============================================");
        System.out.println(" Cards4Play API escuchando en http://localhost:" + config.getPuerto());
        System.out.println(" Carpeta de datos: " + config.getCarpetaDatos());
        System.out.println(" Administrador por defecto: " + admin.getUsername());
        if (config.getLlaveApiTasa().isEmpty()) {
            System.out.println(" AVISO: EXCHANGE_API_KEY no está definida; se usará la tasa por defecto ("
                    + config.getTasaDefecto() + ")");
        }
        System.out.println(" Rutas disponibles: GET /api/ayuda");
        System.out.println("==============================================");
    }
}
