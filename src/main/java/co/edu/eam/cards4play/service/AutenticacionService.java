package co.edu.eam.cards4play.service;

import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.model.Administrador;
import co.edu.eam.cards4play.model.Cliente;
import co.edu.eam.cards4play.model.Usuario;
import co.edu.eam.cards4play.repository.AlmacenDatos;

/**
 * Revisa si un usuario y una contraseña son correctos.
 *
 * Hay dos tipos de usuario:
 *  - El administrador por defecto: se crea en Main con ADMIN_USERNAME / ADMIN_PASSWORD.
 *  - Los clientes: están en AlmacenDatos (clientes.json).
 *
 * Lo usa security/FiltroAutenticacion en CADA petición.
 */
public class AutenticacionService {

    private final Administrador administrador;
    private final AlmacenDatos almacen;

    public AutenticacionService(Administrador administrador, AlmacenDatos almacen) {
        this.administrador = administrador;
        this.almacen = almacen;
    }

    /** Devuelve el usuario si las credenciales son correctas; si no, lanza 401. */
    public Usuario autenticar(String username, String password) {
        Usuario usuario;
        if (administrador.getUsername().equalsIgnoreCase(username)) {
            usuario = administrador;
        } else {
            usuario = almacen.buscarClientePorUsername(username); // null si no existe
        }

        // Mismo mensaje si el usuario no existe o la contraseña está mal:
        // así nadie puede averiguar qué usuarios existen.
        if (usuario == null || !usuario.autenticar(password)) {
            throw new Cards4PlayException(401, "Usuario o contraseña incorrectos");
        }
        return usuario;
    }

    /** ¿Ese username ya lo usa alguien (admin o cliente)? Lo usa ClienteService al registrar. */
    public boolean usernameOcupado(String username) {
        if (administrador.getUsername().equalsIgnoreCase(username)) {
            return true;
        }
        Cliente existente = almacen.buscarClientePorUsername(username);
        return existente != null;
    }
}
