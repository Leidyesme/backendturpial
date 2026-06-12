package DTO;

import java.util.List;

public class PedidoDTO {

    private String cliente;
    private String tipoEntrega;
    private String direccion;
    private List<ProductoPedidoDTO> productos;

    // GETTERS Y SETTERS

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getTipoEntrega() {
        return tipoEntrega;
    }

    public void setTipoEntrega(String tipoEntrega) {
        this.tipoEntrega = tipoEntrega;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public List<ProductoPedidoDTO>
    getProductos() {
        return productos;
    }

    public void setProductos(List<ProductoPedidoDTO>productos) {
        this.productos = productos;
    }
}