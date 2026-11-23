package co.edu.eam.cards4play.model;

import co.edu.eam.cards4play.model.enums.FuenteTasa;
import co.edu.eam.cards4play.util.GeneradorId;

import java.time.LocalDateTime;

/**
 * Registro de una compra hecha por un cliente.
 *
 * Guarda una "foto" del producto y del precio en el momento de comprar:
 * si mañana cambia la tasa o el precio, la compra de hoy conserva lo que se pagó.
 *
 * CAMBIO EN LA ENTREGA 3: la compra ya no guarda el objeto Producto completo,
 * sino su id (productoId). ¿Por qué? Al guardar en JSON, el booster quedaba
 * copiado dos veces (en productos.json y dentro de clientes.json). Al volver a
 * cargar eran dos objetos distintos: si el cliente abría uno, el otro seguía
 * "cerrado". Con el id hay una sola copia (la del inventario) y se busca cuando se necesita.
 *
 * Importante: Compra NO consulta la API de tasa. El precio en pesos lo calcula el
 * Producto y aquí solo se guarda el resultado.
 *
 * Relaciones UML:
 *  - Cliente tiene muchas Compra (su historial).
 *  - Cada Compra apunta a un Producto (por su id).
 */
public class Compra {

    private String id;
    private LocalDateTime fecha;
    private String productoId;
    private String nombreProducto;
    private String tipoProducto;
    private double precioUsd;
    private double precioCop;
    private FuenteTasa fuenteTasa;

    public Compra(Producto producto, TasaCambio tasa) {
        this.id = GeneradorId.generar("COM");
        this.fecha = LocalDateTime.now();
        this.productoId = producto.getId();
        this.nombreProducto = producto.getNombre();
        this.tipoProducto = producto.getTipo();
        this.precioUsd = producto.getPrecioUsd();
        this.precioCop = producto.calcularPrecioCop(tasa); // el cálculo lo hace Producto
        this.fuenteTasa = tasa.getFuente();
    }

    /** ¿Esta compra fue de un booster? */
    public boolean esDeBooster() {
        return "BOOSTER".equals(tipoProducto);
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getProductoId() {
        return productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public String getTipoProducto() {
        return tipoProducto;
    }

    public double getPrecioUsd() {
        return precioUsd;
    }

    public double getPrecioCop() {
        return precioCop;
    }

    public FuenteTasa getFuenteTasa() {
        return fuenteTasa;
    }
}
