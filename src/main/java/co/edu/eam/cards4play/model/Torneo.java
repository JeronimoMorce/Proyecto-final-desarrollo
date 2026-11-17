package co.edu.eam.cards4play.model;

import co.edu.eam.cards4play.exception.Cards4PlayException;
import co.edu.eam.cards4play.util.GeneradorId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Torneo organizado por la tienda.
 *
 * Reglas de inscripción (incluye la corrección del profesor):
 *  1. Solo se puede inscribir hasta la fecha límite, que siempre es ANTES del día del torneo.
 *     El día del torneo o después ya no se aceptan inscripciones, aunque queden cupos.
 *  2. No se puede superar el número máximo de participantes.
 *  3. Un cliente no puede inscribirse dos veces.
 *
 * Relaciones UML:
 *  - Torneo tiene muchos Cliente como participantes (agregación).
 *  - Administrador crea los torneos.
 */
public class Torneo {

    private String id;
    private String nombre;
    private LocalDate fecha;
    private LocalDate fechaLimiteInscripcion;
    private int maxParticipantes;
    private List<Cliente> participantes;

    /** Constructor corto: la fecha límite de inscripción es el día anterior al torneo. */
    public Torneo(String nombre, LocalDate fecha, int maxParticipantes) {
        this(nombre, fecha, fecha == null ? null : fecha.minusDays(1), maxParticipantes);
    }

    /** Constructor completo: permite elegir la fecha límite de inscripción. */
    public Torneo(String nombre, LocalDate fecha, LocalDate fechaLimiteInscripcion, int maxParticipantes) {
        if (nombre == null || nombre.isBlank()) {
            throw new Cards4PlayException(400, "El nombre del torneo es obligatorio");
        }
        if (fecha == null || !fecha.isAfter(LocalDate.now())) {
            throw new Cards4PlayException(400, "La fecha del torneo debe ser posterior a hoy");
        }
        if (fechaLimiteInscripcion == null || !fechaLimiteInscripcion.isBefore(fecha)) {
            throw new Cards4PlayException(400, "La fecha límite de inscripción debe ser anterior al día del torneo");
        }
        if (maxParticipantes <= 0) {
            throw new Cards4PlayException(400, "El máximo de participantes debe ser mayor que cero");
        }
        this.id = GeneradorId.generar("TOR");
        this.nombre = nombre;
        this.fecha = fecha;
        this.fechaLimiteInscripcion = fechaLimiteInscripcion;
        this.maxParticipantes = maxParticipantes;
        this.participantes = new ArrayList<>();
    }

    /**
     * ¿Todavía se aceptan inscripciones en la fecha indicada?
     * Recibe "hoy" como parámetro para poder probar el método con cualquier fecha.
     */
    public boolean inscripcionAbierta(LocalDate hoy) {
        return !hoy.isAfter(fechaLimiteInscripcion); // hoy <= fecha límite
    }

    /** ¿Quedan cupos libres? */
    public boolean hayCupo() {
        return participantes.size() < maxParticipantes;
    }

    /** ¿El cliente ya está inscrito? Se compara por id. */
    public boolean estaInscrito(Cliente cliente) {
        for (Cliente participante : participantes) {
            if (participante.getId().equals(cliente.getId())) {
                return true;
            }
        }
        return false;
    }

    /** Inscribe al cliente si se cumplen las tres reglas; si no, lanza un error 409. */
    public void inscribir(Cliente cliente, LocalDate hoy) {
        if (!inscripcionAbierta(hoy)) {
            throw new Cards4PlayException(409, "Las inscripciones al torneo " + nombre
                    + " cerraron el " + fechaLimiteInscripcion);
        }
        if (estaInscrito(cliente)) {
            throw new Cards4PlayException(409, "El cliente " + cliente.getUsername() + " ya tiene una inscripción en el torneo " + nombre);
        }
        if (!hayCupo()) {
            throw new Cards4PlayException(409, "El torneo " + nombre + " no tiene cupos disponibles");
        }
        participantes.add(cliente);
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalDate getFechaLimiteInscripcion() {
        return fechaLimiteInscripcion;
    }

    public int getMaxParticipantes() {
        return maxParticipantes;
    }

    public List<Cliente> getParticipantes() {
        return new ArrayList<>(participantes); // copia: desde afuera no se puede modificar la lista real
    }

    @Override
    public String toString() {
        return "Torneo [" + id + "] " + nombre + " | fecha: " + fecha
                + " | inscripciones hasta: " + fechaLimiteInscripcion
                + " | inscritos: " + participantes.size() + "/" + maxParticipantes;
    }
}
