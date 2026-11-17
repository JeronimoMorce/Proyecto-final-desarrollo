package co.edu.eam.cards4play.model.enums;

/**
 * Roles que puede tener un usuario del sistema.
 *
 * Se usa un enum porque son valores fijos: así evitamos escribir "admin" o
 * "Admin" como texto y equivocarnos.
 *
 * Relación UML: Usuario tiene un Rol.
 */
public enum Rol {
    ADMIN,
    CLIENTE
}
