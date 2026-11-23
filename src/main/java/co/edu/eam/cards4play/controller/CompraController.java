package co.edu.eam.cards4play.controller;

import co.edu.eam.cards4play.dto.CompraRequest;
import co.edu.eam.cards4play.mapper.MapeadorRespuesta;
import co.edu.eam.cards4play.model.Cliente;
import co.edu.eam.cards4play.model.Compra;
import co.edu.eam.cards4play.repository.AlmacenDatos;
import co.edu.eam.cards4play.service.CompraService;

import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Rutas de compras. Solo para CLIENTES.
 *
 *   POST /api/compras   comprar un producto      -> 201
 *   GET  /api/compras   mi historial de compras  -> 200
 *
 * En el historial, un booster abierto muestra sus cartas y uno cerrado no.
 * (El administrador ve el historial de un cliente en GET /api/clientes/:id/compras.)
 */
public class CompraController extends ControladorBase {

    private final CompraService servicio;
    private final AlmacenDatos almacen;

    public CompraController(CompraService servicio, AlmacenDatos almacen) {
        this.servicio = servicio;
        this.almacen = almacen;
    }

    @Override
    public void registrarRutas() {

        post("/api/compras", (request, response) -> {
            Cliente cliente = requerirCliente(request);
            CompraRequest datos = leerCuerpo(request, CompraRequest.class);
            Compra compra = servicio.comprar(cliente, datos);
            response.status(201);
            return json(MapeadorRespuesta.compra(compra, almacen.getInventario()));
        });

        get("/api/compras", (request, response) -> {
            Cliente cliente = requerirCliente(request);
            return json(MapeadorRespuesta.compras(servicio.historial(cliente), almacen.getInventario()));
        });
    }
}
