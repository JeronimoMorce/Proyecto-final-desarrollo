package co.edu.eam.cards4play.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cuerpo de POST /api/clientes (el administrador registra un cliente).
 *
 *   { "username": "nicole", "password": "clave123" }
 */
public class ClienteRequest extends Peticion {

    private String username;
    private String password;

    @Override
    protected void revisarCampos(List<String> faltantes) {
        if (vacio(username)) {
            faltantes.add("username");
        }
        if (vacio(password)) {
            faltantes.add("password");
        }
    }

    @Override
    public Map<String, Object> ejemplo() {
        Map<String, Object> ejemplo = new LinkedHashMap<>();
        ejemplo.put("username", "nicole");
        ejemplo.put("password", "clave123");
        return ejemplo;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
