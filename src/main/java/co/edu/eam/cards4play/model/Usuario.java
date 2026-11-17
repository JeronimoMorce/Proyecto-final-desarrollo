package co.edu.eam.cards4play.model;

import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.model.enums.Rol;
import co.edu.eam.cards4play.util.GeneradorId;

/**
 * Clase base (abstracta) de las personas que usan el sistema.
 *
 * Es abstracta porque un usuario siempre es Administrador o Cliente.
 *
 * Encapsulamiento: la contraseña NO tiene getter. Nadie de afuera puede leerla;
 * solo se puede preguntar si una contraseña es correcta con autenticar().
 *
 * Relaciones UML:
 *  - Administrador y Cliente heredan de Usuario.
 *  - Usuario tiene un Rol.
 */
public abstract class Usuario {

    protected String id;
    protected String username;
    protected String password;
    protected Rol rol;

    protected Usuario(String username, String password, Rol rol) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new Cards4PlayException(400, "El usuario y la contraseña son obligatorios");
        }
        this.id = GeneradorId.generar("USR");
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    /** Devuelve true si la contraseña recibida es la de este usuario. */
    public boolean autenticar(String password) {
        return this.password.equals(password);
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
