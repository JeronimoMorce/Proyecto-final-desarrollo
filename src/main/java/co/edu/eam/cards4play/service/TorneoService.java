package co.edu.eam.cards4play.service;

import co.edu.eam.cards4play.dto.ActualizarTorneoRequest;
import co.edu.eam.cards4play.dto.TorneoRequest;
import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.model.Administrador;
import co.edu.eam.cards4play.model.Cliente;
import co.edu.eam.cards4play.model.Torneo;
import co.edu.eam.cards4play.repository.AlmacenDatos;
import co.edu.eam.cards4play.util.Conversor;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de los torneos: crear, consultar, modificar, eliminar e inscribir.
 * Las reglas de inscripción (fecha límite, cupo, duplicado) las aplica la clase Torneo.
 */
public class TorneoService {

    private final AlmacenDatos almacen;

    public TorneoService(AlmacenDatos almacen) {
        this.almacen = almacen;
    }

    public Torneo crear(Administrador admin, TorneoRequest datos) {
        datos.validar();
        LocalDate fecha = Conversor.fecha(datos.getFecha(), "fecha");
        LocalDate limite = Conversor.fechaOpcional(datos.getFechaLimiteInscripcion(), "fechaLimiteInscripcion");
        synchronized (almacen) {
            Torneo torneo = admin.crearTorneo(datos.getNombre(), fecha, limite, datos.getMaxParticipantes());
            almacen.agregarTorneo(torneo);
            almacen.guardarTorneos();
            return torneo;
        }
    }

    public List<Torneo> listar() {
        return almacen.getTorneos();
    }

    public Torneo buscar(String id) {
        return almacen.buscarTorneo(id);
    }

    /** Cambia solo los campos que llegaron. */
    public Torneo actualizar(String id, ActualizarTorneoRequest datos) {
        datos.validar();
        synchronized (almacen) {
            Torneo torneo = almacen.buscarTorneo(id);
            if (datos.getNombre() != null) {
                torneo.cambiarNombre(datos.getNombre());
            }
            if (datos.getFecha() != null || datos.getFechaLimiteInscripcion() != null) {
                // Si solo llega una de las dos fechas, la otra se conserva
                LocalDate fecha = (datos.getFecha() != null)
                        ? Conversor.fecha(datos.getFecha(), "fecha")
                        : torneo.getFecha();
                LocalDate limite = Conversor.fechaOpcional(datos.getFechaLimiteInscripcion(), "fechaLimiteInscripcion");
                torneo.cambiarFechas(fecha, limite);
            }
            if (datos.getMaxParticipantes() != null) {
                torneo.cambiarMaxParticipantes(datos.getMaxParticipantes());
            }
            almacen.guardarTorneos();
            return torneo;
        }
    }

    /** Solo se elimina un torneo sin inscritos (409 si ya hay alguno). */
    public void eliminar(String id) {
        synchronized (almacen) {
            Torneo torneo = almacen.buscarTorneo(id);
            if (!torneo.getParticipantesIds().isEmpty()) {
                throw new Cards4PlayException(409, "El torneo ya tiene inscritos y no se puede eliminar");
            }
            almacen.eliminarTorneo(torneo);
            almacen.guardarTorneos();
        }
    }

    public Torneo inscribir(Cliente cliente, String torneoId) {
        synchronized (almacen) {
            Torneo torneo = almacen.buscarTorneo(torneoId); // 404 si no existe
            cliente.inscribirse(torneo);                    // 409 si cerró, está lleno o ya inscrito
            almacen.guardarTorneos();
            return torneo;
        }
    }
}
