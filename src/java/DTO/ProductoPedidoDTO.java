package DTO; // Qué hace: Declara el paquete DTO. | Para qué sirve / Destino: Ubica la clase lógicamente en la Capa de Objetos de Transferencia de Datos (Data Transfer Objects) de la arquitectura del proyecto.

public class ProductoPedidoDTO { // Qué hace: Declara la clase pública ProductoPedidoDTO. | Para qué sirve / Destino: Actúa como un contenedor ligero de datos para transportar información específica sobre los productos y sus cantidades asociadas a un pedido entre el controlador, la vista y los DAOs, sin exponer directamente las entidades de la base de datos MySQL.

    private String idProducto; // Qué hace: Declara el atributo privado idProducto de tipo String. | Para qué sirve / Destino: Almacena temporalmente el identificador único del producto que se desea incluir en un pedido.
    private int cantidad; // Qué hace: Declara el atributo privado cantidad de tipo int. | Para qué sirve / Destino: Almacena temporalmente el número de unidades solicitadas de ese producto específico dentro del pedido.

    public String getIdProducto() { // Qué hace: Declara el método getter getIdProducto que retorna un String. | Para qué sirve / Destino: Permite consultar el identificador del producto desde los controladores, servicios o DAOs.
        return idProducto; // Qué hace: Retorna el valor del atributo idProducto. | Para qué sirve / Destino: Suministra el ID del producto hacia la capa que procesa la lógica de la orden.
    }

    public void setIdProducto(String idProducto) { // Qué hace: Declara el método setter setIdProducto que recibe un String. | Para qué sirve / Destino: Permite asignar o modificar el identificador del producto dentro del DTO.
        this.idProducto = idProducto; // Qué hace: Asigna el valor recibido al atributo de la instancia. | Para qué sirve / Destino: Actualiza internamente la propiedad idProducto del objeto ProductoPedidoDTO.
    }

    public int getCantidad() { // Qué hace: Declara el método getter getCantidad que retorna un int. | Para qué sirve / Destino: Permite consultar la cantidad de unidades solicitadas desde los DAOs o lógica de negocio.
        return cantidad; // Qué hace: Retorna el valor del atributo cantidad. | Para qué sirve / Destino: Proporciona el número de unidades hacia los cálculos de stock o inserción en MySQL.
    }

    public void setCantidad(int cantidad) { // Qué hace: Declara el método setter setCantidad que recibe un int. | Para qué sirve / Destino: Permite asignar o modificar la cantidad de unidades del producto en el DTO.
        this.cantidad = cantidad; // Qué hace: Asigna el valor recibido al atributo de la instancia. | Para qué sirve / Destino: Actualiza internamente la propiedad cantidad del objeto ProductoPedidoDTO.
    }
}