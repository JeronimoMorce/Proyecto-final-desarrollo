package co.edu.eam.cards4play.controller;

import co.edu.eam.cards4play.dto.ClienteRequest;
import co.edu.eam.cards4play.mapper.MapeadorRespuesta;
import co.edu.eam.cards4play.model.Cliente;
import co.edu.eam.cards4play.repository.AlmacenDatos;
import co.edu.eam.cards4play.service.ClienteService;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Rutas de clientes. Todas son solo para el ADMINISTRADOR.
 *
 *   POST   /api/clientes              registrar un cliente          -> 201
 *   GET    /api/clientes              listar clientes               -> 200
 *   GET    /api/clientes/:id          ver un cliente                -> 200
 *   GET    /api/clientes/:id/compras  historial de un cliente       -> 200
 *   DELETE /api/clientes/:id          eliminar (si no tiene compras) -> 204
 */
public class ClienteController extends ControladorBase {

    private final ClienteService servicio;
    private final AlmacenDatos almacen;

    public ClienteController(ClienteService servicio, AlmacenDatos almacen) {
        this.servicio = servicio;
        this.almacen = almacen;
    }

    @Override
    public void registrarRutas() {

        post("/api/clientes", (request, response) -> {
            // 1. ¿Quién pide? Debe ser admin (si no, 403)
            // 2. Leer el cuerpo JSON como ClienteRequest
            // 3. El servicio valida, registra y guarda
            // 4. Responder 201 con el cliente creado (sin contraseña)
            ClienteRequest datos = leerCuerpo(request, ClienteRequest.class);
            Cliente cliente = servicio.registrar(requerirAdmin(request), datos);
            response.status(201);
            return json(MapeadorRespuesta.cliente(cliente));
        });

        get("/api/clientes", (request, response) -> {
            requerirAdmin(request);
            return json(MapeadorRespuesta.clientes(servicio.listar()));
        });

        get("/api/clientes/:id", (request, response) -> {
            requerirAdmin(request);
            // request.params(":id") toma el valor de la ruta: /api/clientes/USR-123 -> "USR-123"
            return json(MapeadorRespuesta.cliente(servicio.buscar(request.params(":id"))));
        });

        get("/api/clientes/:id/compras", (request, response) -> {
            requerirAdmin(request);
            Cliente cliente = servicio.buscar(request.params(":id"));
            return json(MapeadorRespuesta.compras(cliente.getHistorial(), almacen.getInventario()));
        });

        delete("/api/clientes/:id", (request, response) -> {
            requerirAdmin(request);
            servicio.eliminar(request.params(":id"));
            response.status(204); // 204 = "hecho, no hay nada que mostrar"
            return "";
        });
    }
}
