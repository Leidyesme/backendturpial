package Modelo.Entidades;

/**
 * Entidad que representa la solicitud de devolución de un pedido.
 */
public class Devolucion {

    private String idDevolucion;
    private String idPedido;
    private String motivo;
    private String fechaSolicitud;
    private String estadoDevolucion;

    /**
     * Constructor vacío.
     */
    public Devolucion() {
    }

    /**
     * Constructor completo.
     *
     * @param idDevolucion Identificador único de la solicitud de devolución.
     * @param idPedido Identificador del pedido asociado a la devolución.
     * @param motivo Motivo o justificación de la devolución.
     * @param fechaSolicitud Fecha y hora en que se solicitó la devolución.
     * @param estadoDevolucion Estado de la devolución (Pendiente, Aprobada, Rechazada).
     */
    public Devolucion(String idDevolucion, String idPedido, String motivo, String fechaSolicitud, String estadoDevolucion) {
        this.idDevolucion = idDevolucion;
        this.idPedido = idPedido;
        this.motivo = motivo;
        this.fechaSolicitud = fechaSolicitud;
        this.estadoDevolucion = estadoDevolucion;
    }

    public String getIdDevolucion() {
        return idDevolucion;
    }

    public void setIdDevolucion(String idDevolucion) {
        this.idDevolucion = idDevolucion;
    }

    public String getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(String idPedido) {
        this.idPedido = idPedido;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getEstadoDevolucion() {
        return estadoDevolucion;
    }

    public void setEstadoDevolucion(String estadoDevolucion) {
        this.estadoDevolucion = estadoDevolucion;
    }
}
