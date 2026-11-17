package co.edu.eam.cards4play.model.enums;

/**
 * Indica de dónde salió la tasa de cambio USD -> COP.
 *
 * API:     la tasa vino del servicio externo.
 * DEFECTO: la API falló y se usó la tasa por defecto (variable TASA_DEFECTO).
 *
 * Relación UML: TasaCambio tiene una FuenteTasa.
 */
public enum FuenteTasa {
    API,
    DEFECTO
}
