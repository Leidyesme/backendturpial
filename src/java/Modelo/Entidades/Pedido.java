package Modelo.Entidades;

// Clase que representa un pedido dentro del sistema.
 
public class Pedido {

    // ATRIBUTOS
     

    // Identificador único del pedido
    private String idPedido;

    // Usuario registrado que realizó el pedido
    private String idUsuario;

    // Nombre del cliente cuando no está registrado
    private String nombreClienteOpcional;

    // Tipo de entrega:
    // domicilio, recoger o consumir aquí
    private String tipoEntrega;

    // Número de mesa para consumo local
    private Integer numeroMesa;

    // Dirección para pedidos a domicilio
    private String direccionEntrega;

    // Observaciones adicionales del cliente
    private String observaciones;

    // Total del pedido
    private double total;

    // Estado actual del pedido
    private String estado;

    // Fecha y hora del pedido
    private String fechaPedido;

    /**
     * CONSTRUCTOR VACÍO
     *
     * Se usa para crear objetos
     * sin enviar datos inicialmente.
     */
    public Pedido() {
    }

    //CONSTRUCTOR COMPLETO Se usa cuando recuperamos datos desde la base de datos.
     
    public Pedido(
            String idPedido,
            String idUsuario,
            String nombreClienteOpcional,
            String tipoEntrega,
            Integer numeroMesa,
            String direccionEntrega,
            String observaciones,
            double total,
            String estado,
            String fechaPedido
    ) {

        this.idPedido = idPedido;

        this.idUsuario = idUsuario;

        this.nombreClienteOpcional =
                nombreClienteOpcional;

        this.tipoEntrega = tipoEntrega;

        this.numeroMesa = numeroMesa;

        this.direccionEntrega =
                direccionEntrega;

        this.observaciones =
                observaciones;

        this.total = total;

        this.estado = estado;

        this.fechaPedido = fechaPedido;
    }

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

    public String getNombreClienteOpcional() {

        return nombreClienteOpcional;
    }

    public void setNombreClienteOpcional(
            String nombreClienteOpcional
    ) {

        this.nombreClienteOpcional =
                nombreClienteOpcional;
    }

    public String getTipoEntrega() {

        return tipoEntrega;
    }

    public void setTipoEntrega(
            String tipoEntrega
    ) {

        this.tipoEntrega =
                tipoEntrega;
    }

    public Integer getNumeroMesa() {

        return numeroMesa;
    }

    public void setNumeroMesa(
            Integer numeroMesa
    ) {

        this.numeroMesa =
                numeroMesa;
    }

    public String getDireccionEntrega() {

        return direccionEntrega;
    }

    public void setDireccionEntrega(
            String direccionEntrega
    ) {

        this.direccionEntrega =
                direccionEntrega;
    }

    public String getObservaciones() {

        return observaciones;
    }

    public void setObservaciones(
            String observaciones
    ) {

        this.observaciones =
                observaciones;
    }

    public double getTotal() {

        return total;
    }

    public void setTotal(
            double total
    ) {

        this.total = total;
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

    public String getFechaPedido() {

        return fechaPedido;
    }

    public void setFechaPedido(
            String fechaPedido
    ) {

        this.fechaPedido =
                fechaPedido;
    }
}