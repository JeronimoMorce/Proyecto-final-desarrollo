package co.edu.eam.cards4play.service;

import co.edu.eam.cards4play.dto.CompraRequest;
import co.edu.eam.cards4play.model.Cliente;
import co.edu.eam.cards4play.model.Compra;
import co.edu.eam.cards4play.model.Producto;
import co.edu.eam.cards4play.model.TasaCambio;
import co.edu.eam.cards4play.repository.AlmacenDatos;

import java.util.List;

/**
 * Lógica de las compras.
 *
 * Una compra toca DOS archivos:
 *  - productos.json: baja el stock (o el booster queda vendido y con dueño).
 *  - clientes.json:  la compra se agrega al historial del cliente.
 */
public class CompraService {

    private final AlmacenDatos almacen;
    private final ServicioTasaCambio servicioTasa;

    public CompraService(AlmacenDatos almacen, ServicioTasaCambio servicioTasa) {
        this.almacen = almacen;
        this.servicioTasa = servicioTasa;
    }

    public Compra comprar(Cliente cliente, CompraRequest datos) {
        datos.validar();

        // La tasa se pide ANTES de bloquear el almacén: la API puede tardar hasta
        // 3 segundos y no queremos que las demás peticiones esperen por eso.
        TasaCambio tasa = servicioTasa.obtenerTasa();

        synchronized (almacen) {
            Producto producto = almacen.getInventario().buscarPorId(datos.getProductoId()); // 404 si no existe
            Compra compra = cliente.comprar(producto, tasa); // 409 si ya está vendido o sin stock
            almacen.guardarProductos();
            almacen.guardarClientes();
            return compra;
        }
    }

    public List<Compra> historial(Cliente cliente) {
        return cliente.getHistorial();
    }
}
