package co.edu.eam.cards4play.model;

import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.model.enums.Rol;
import co.edu.eam.cards4play.util.GeneradorId;
import co.edu.eam.cards4play.util.Seguridad;

/**
 * Clase base (abstracta) de las personas que usan el sistema.
 *
 * Es abstracta porque un usuario siempre es Administrador o Cliente.
 *
 * Encapsulamiento y seguridad:
 *  - La contraseña NO se guarda tal cual: se guarda cifrada (passwordHash).
 *  - No hay getter de la contraseña. Solo se puede preguntar si una es correcta con autenticar().
 *
 * Relaciones UML:
 *  - Administrador y Cliente heredan de Usuario.
 *  - Usuario tiene un Rol.
 */
public abstract class Usuario {

    protected String id;
    protected String username;
    protected String passwordHash;
    protected Rol rol;

    protected Usuario(String username, String password, Rol rol) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new Cards4PlayException(400, "El usuario y la contraseña son obligatorios");
        }
        if (username.contains(":")) {
            // La autenticación Basic separa usuario y contraseña con ":"
            throw new Cards4PlayException(400, "El nombre de usuario no puede contener ':'");
        }
        this.id = GeneradorId.generar("USR");
        this.username = username.trim();
        this.passwordHash = Seguridad.cifrar(password);
        this.rol = rol;
    }

    /** Devuelve true si la contraseña recibida es la de este usuario. */
    public boolean autenticar(String password) {
        return password != null && this.passwordHash.equals(Seguridad.cifrar(password));
    }

    /**
     * Revisa que el objeto leído del archivo JSON tenga sus datos básicos.
     * Un archivo de una versión anterior (por ejemplo con "password" en vez de
     * "passwordHash") no los tiene, y AlmacenDatos lo descarta con un aviso.
     */
    public boolean tieneDatosCompletos() {
        return id != null && username != null && passwordHash != null && rol != null;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Rol getRol() {
        return rol;
    }

    @Override
    public String toString() {
        return rol + " [" + id + "] " + username;
    }
}
