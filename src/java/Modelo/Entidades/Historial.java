package Modelo.Entidades;

/**
 * Clase que representa
 * el historial de pedidos.
 */
public class Historial {

    /**
     * ATRIBUTOS
     */

    private String idPedido;

    private String idUsuario;

    private String fecha;

    private double total;

    private String estado;

    /**
     * GETTERS Y SETTERS
     */

    public String getIdPedido() {

        return idPedido;
    }

    public void setIdPedido(
        String idPedido
    ) {

        this.idPedido =
            idPedido;
    }

    public String getIdUsuario() {

        return idUsuario;
    }

    public void setIdUsuario(
        String idUsuario
    ) {

        this.idUsuario =
            idUsuario;
    }

    public String getFecha() {

        return fecha;
    }

    public void setFecha(
        String fecha
    ) {

        this.fecha =
            fecha;
    }

    public double getTotal() {

        return total;
    }

    public void setTotal(
        double total
    ) {

        this.total =
            total;
    }

    public String getEstado() {

        return estado;
    }

    public void setEstado(
        String estado
    ) {

        this.estado =
            estado;
    }
}