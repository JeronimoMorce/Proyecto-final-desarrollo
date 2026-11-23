package co.edu.eam.cards4play.service;

import co.edu.eam.cards4play.dto.ClienteRequest;
import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.model.Administrador;
import co.edu.eam.cards4play.model.Cliente;
import co.edu.eam.cards4play.model.Torneo;
import co.edu.eam.cards4play.repository.AlmacenDatos;

import java.util.List;

/**
 * Lógica de los clientes: registrar, consultar y eliminar.
 *
 * Patrón de todos los servicios:
 *  1. Validar los datos de entrada (el DTO).
 *  2. Pedirle el trabajo al modelo (aquí: Administrador.registrarCliente).
 *  3. Guardar en el AlmacenDatos (que escribe el JSON).
 */
public class ClienteService {

    private final AlmacenDatos almacen;
    private final AutenticacionService autenticacion;

    public ClienteService(AlmacenDatos almacen, AutenticacionService autenticacion) {
        this.almacen = almacen;
        this.autenticacion = autenticacion;
    }

    /** Solo el administrador registra clientes (lo exige el enunciado). */
    public Cliente registrar(Administrador admin, ClienteRequest datos) {
        datos.validar();
        synchronized (almacen) {
            if (autenticacion.usernameOcupado(datos.getUsername())) {
                throw new Cards4PlayException(409, "Ya existe un usuario llamado " + datos.getUsername());
            }
            Cliente cliente = admin.registrarCliente(datos.getUsername(), datos.getPassword());
            almacen.agregarCliente(cliente);
            almacen.guardarClientes();
            return cliente;
        }
    }

    public List<Cliente> listar() {
        return almacen.getClientes();
    }

    public Cliente buscar(String id) {
        return almacen.buscarCliente(id);
    }

    /**
     * Elimina un cliente. No se permite si ya compró algo o está inscrito en un
     * torneo: se perdería el rastro de esas operaciones (409).
     */
    public void eliminar(String id) {
        synchronized (almacen) {
            Cliente cliente = almacen.buscarCliente(id);
            if (!cliente.getHistorial().isEmpty()) {
                throw new Cards4PlayException(409, "El cliente tiene compras registradas y no se puede eliminar");
            }
            for (Torneo torneo : almacen.getTorneos()) {
                if (torneo.estaInscrito(cliente)) {
                    throw new Cards4PlayException(409, "El cliente está inscrito en el torneo " + torneo.getNombre());
                }
            }
            almacen.eliminarCliente(cliente);
            almacen.guardarClientes();
        }
    }
}
