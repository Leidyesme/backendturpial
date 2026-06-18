package Modelo.Entidades;

/**
 * Entidad que representa un registro de auditoría o historial de actividad de un usuario.
 */
public class Auditoria {

    private String idHistorial;
    private String idUsuario;
    private String accion;
    private String tipoAccion;
    private String fecha;

    /**
     * Constructor vacío.
     */
    public Auditoria() {
    }

    /**
     * Constructor completo.
     *
     * @param idHistorial Identificador único del registro de auditoría.
     * @param idUsuario Identificador del usuario que realiza la acción.
     * @param accion Descripción de la acción realizada.
     * @param tipoAccion Categoría o tipo de acción realizada (LOGIN, COMPRA, etc).
     * @param fecha Fecha y hora del evento.
     */
    public Auditoria(String idHistorial, String idUsuario, String accion, String tipoAccion, String fecha) {
        this.idHistorial = idHistorial;
        this.idUsuario = idUsuario;
        this.accion = accion;
        this.tipoAccion = tipoAccion;
        this.fecha = fecha;
    }

    public String getIdHistorial() {
        return idHistorial;
    }

    public void setIdHistorial(String idHistorial) {
        this.idHistorial = idHistorial;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getTipoAccion() {
        return tipoAccion;
    }

    public void setTipoAccion(String tipoAccion) {
        this.tipoAccion = tipoAccion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
