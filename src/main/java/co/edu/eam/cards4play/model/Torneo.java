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
 * CAMBIO EN LA ENTREGA 3: los participantes se guardan por id (participantesIds)
 * y no como objetos Cliente completos. Así cada cliente existe una sola vez
 * (en clientes.json) y el torneo solo apunta a él.
 *
 * Relaciones UML:
 *  - Torneo tiene muchos Cliente como participantes (agregación, por id).
 *  - Administrador crea los torneos.
 */
public class Torneo {

    private String id;
    private String nombre;
    private LocalDate fecha;
    private LocalDate fechaLimiteInscripcion;
    private int maxParticipantes;
    private List<String> participantesIds;

    /**
     * @param fechaLimiteInscripcion si es null, se usa el día anterior al torneo
     */
    public Torneo(String nombre, LocalDate fecha, LocalDate fechaLimiteInscripcion, int maxParticipantes) {
        this.id = GeneradorId.generar("TOR");
        this.participantesIds = new ArrayList<>();
        cambiarNombre(nombre);
        cambiarFechas(fecha, fechaLimiteInscripcion);
        cambiarMaxParticipantes(maxParticipantes);
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
        return participantesIds.size() < maxParticipantes;
    }

    /** ¿El cliente ya está inscrito? */
    public boolean estaInscrito(Cliente cliente) {
        return participantesIds.contains(cliente.getId());
    }

    /** Inscribe al cliente si se cumplen las tres reglas; si no, lanza un error 409. */
    public void inscribir(Cliente cliente, LocalDate hoy) {
        if (!inscripcionAbierta(hoy)) {
            throw new Cards4PlayException(409, "Las inscripciones al torneo " + nombre
                    + " cerraron el " + fechaLimiteInscripcion);
        }
        if (estaInscrito(cliente)) {
            throw new Cards4PlayException(409, "El cliente " + cliente.getUsername()
                    + " ya tiene una inscripción en el torneo " + nombre);
        }
        if (!hayCupo()) {
            throw new Cards4PlayException(409, "El torneo " + nombre + " no tiene cupos disponibles");
        }
        participantesIds.add(cliente.getId());
    }

    // ---------- Métodos para modificar (los usa PUT /api/torneos/:id) ----------

    public void cambiarNombre(String nuevoNombre) {
        if (nuevoNombre == null || nuevoNombre.isBlank()) {
            throw new Cards4PlayException(400, "El nombre del torneo es obligatorio");
        }
        this.nombre = nuevoNombre.trim();
    }

    /** Cambia la fecha del torneo y la fecha límite juntas, porque una depende de la otra. */
    public void cambiarFechas(LocalDate nuevaFecha, LocalDate nuevaFechaLimite) {
        if (nuevaFecha == null || !nuevaFecha.isAfter(LocalDate.now())) {
            throw new Cards4PlayException(400, "La fecha del torneo debe ser posterior a hoy");
        }
        LocalDate limite = (nuevaFechaLimite == null) ? nuevaFecha.minusDays(1) : nuevaFechaLimite;
        if (!limite.isBefore(nuevaFecha)) {
            throw new Cards4PlayException(400, "La fecha límite de inscripción debe ser anterior al día del torneo");
        }
        this.fecha = nuevaFecha;
        this.fechaLimiteInscripcion = limite;
    }

    public void cambiarMaxParticipantes(int nuevoMax) {
        if (nuevoMax <= 0) {
            throw new Cards4PlayException(400, "El máximo de participantes debe ser mayor que cero");
        }
        if (nuevoMax < participantesIds.size()) {
            throw new Cards4PlayException(409, "Ya hay " + participantesIds.size()
                    + " inscritos; el máximo no puede ser menor");
        }
        this.maxParticipantes = nuevoMax;
    }

    /** Igual que en Usuario: detecta torneos guardados con el formato de la entrega anterior. */
    public boolean tieneDatosCompletos() {
        return id != null && fecha != null && fechaLimiteInscripcion != null && participantesIds != null;
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

    public List<String> getParticipantesIds() {
        return new ArrayList<>(participantesIds); // copia: desde afuera no se puede modificar la lista real
    }
}
