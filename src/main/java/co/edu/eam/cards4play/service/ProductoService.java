package co.edu.eam.cards4play.service;

import co.edu.eam.cards4play.dto.ActualizarProductoRequest;
import co.edu.eam.cards4play.dto.CartaRequest;
import co.edu.eam.cards4play.dto.ProductoRequest;
import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.model.Accesorio;
import co.edu.eam.cards4play.model.Administrador;
import co.edu.eam.cards4play.model.Carta;
import co.edu.eam.cards4play.model.Inventario;
import co.edu.eam.cards4play.model.Producto;
import co.edu.eam.cards4play.model.ProductoSellado;
import co.edu.eam.cards4play.model.Usuario;
import co.edu.eam.cards4play.model.enums.Rol;
import co.edu.eam.cards4play.repository.AlmacenDatos;
import co.edu.eam.cards4play.util.Conversor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lógica del inventario: consultar, registrar, modificar y eliminar productos.
 *
 * Usa las dos vistas del Inventario (corrección del profesor):
 *  - Cliente       -> consultarDisponibles()
 *  - Administrador -> consultarStock()
 */
public class ProductoService {

    /** Tipos que se pueden pedir con ?tipo=... */
    private static final List<String> TIPOS = Arrays.asList("CARTA", "BOOSTER", "PRODUCTOSELLADO", "ACCESORIO");

    private final AlmacenDatos almacen;

    public ProductoService(AlmacenDatos almacen) {
        this.almacen = almacen;
    }

    /**
     * Lista productos según quién pregunta.
     *
     * @param tipo null para todos, o CARTA / BOOSTER / PRODUCTOSELLADO (o SELLADO) / ACCESORIO
     */
    public List<Producto> listar(Usuario usuario, String tipo) {
        List<Producto> productos;
        synchronized (almacen) { // nadie modifica el inventario mientras se copia la lista
            Inventario inventario = almacen.getInventario();
            productos = (usuario.getRol() == Rol.ADMIN)
                    ? inventario.consultarStock()
                    : inventario.consultarDisponibles();
        }

        if (tipo == null || tipo.isBlank()) {
            return productos;
        }

        String tipoBuscado = tipo.trim().toUpperCase();
        if (tipoBuscado.equals("SELLADO")) {
            tipoBuscado = "PRODUCTOSELLADO"; // atajo para no escribir el nombre largo
        }
        if (!TIPOS.contains(tipoBuscado)) {
            throw new Cards4PlayException(400, "Tipo de producto inválido: " + tipo)
                    .conDetalle("valoresPermitidos", TIPOS);
        }

        List<Producto> filtrados = new ArrayList<>();
        for (Producto producto : productos) {
            if (producto.getTipo().equals(tipoBuscado)) {
                filtrados.add(producto);
            }
        }
        return filtrados;
    }

    public Producto buscar(String id) {
        synchronized (almacen) {
            return almacen.getInventario().buscarPorId(id);
        }
    }

    public Carta registrarCarta(Administrador admin, CartaRequest datos) {
        datos.validar();
        synchronized (almacen) {
            Carta carta = admin.registrarCarta(almacen.getInventario(), datos.getNombre(),
                    Conversor.rareza(datos.getRareza()), datos.getPrecioUsd(), datos.getStock());
            almacen.guardarProductos();
            return carta;
        }
    }

    public ProductoSellado registrarSellado(Administrador admin, ProductoRequest datos) {
        datos.validar();
        synchronized (almacen) {
            ProductoSellado sellado = admin.registrarProductoSellado(almacen.getInventario(),
                    datos.getNombre(), datos.getPrecioUsd(), datos.getStock());
            almacen.guardarProductos();
            return sellado;
        }
    }

    public Accesorio registrarAccesorio(Administrador admin, ProductoRequest datos) {
        datos.validar();
        synchronized (almacen) {
            Accesorio accesorio = admin.registrarAccesorio(almacen.getInventario(),
                    datos.getNombre(), datos.getPrecioUsd(), datos.getStock());
            almacen.guardarProductos();
            return accesorio;
        }
    }

    /** Cambia solo los campos que llegaron. Las validaciones las hace el Producto. */
    public Producto actualizar(String id, ActualizarProductoRequest datos) {
        datos.validar();
        synchronized (almacen) {
            Producto producto = buscar(id);
            if (datos.getNombre() != null) {
                producto.cambiarNombre(datos.getNombre());
            }
            if (datos.getPrecioUsd() != null) {
                producto.cambiarPrecioUsd(datos.getPrecioUsd());
            }
            if (datos.getStock() != null) {
                producto.cambiarStock(datos.getStock()); // un Booster responde 409
            }
            almacen.guardarProductos();
            return producto;
        }
    }

    /** Elimina un producto. Un booster vendido no se puede eliminar (409, lo revisa Inventario). */
    public void eliminar(String id) {
        synchronized (almacen) {
            almacen.getInventario().eliminar(id);
            almacen.guardarProductos();
        }
    }
}
