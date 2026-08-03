package Modelo.Entidades; // Qué hace: Declara el paquete Modelo.Entidades. | Para qué sirve / Destino: Ubica la clase lógicamente en la Capa de Entidades del modelo de datos de la arquitectura del proyecto.

/**
 * Clase que representa
 * el historial de pedidos.
 */
public class Historial { // Qué hace: Declara la clase pública Historial. | Para qué sirve / Destino: Actúa como la entidad de persistencia en memoria para mapear los registros consolidados del historial de pedidos provenientes de o hacia la base de datos MySQL.

    /**
     * ATRIBUTOS
     */

    private String idPedido; // Qué hace: Declara el atributo privado idPedido. | Para qué sirve / Destino: Almacena el identificador único del pedido mapeado desde la clave primaria en MySQL.

    private String idUsuario; // Qué hace: Declara el atributo privado idUsuario. | Para qué sirve / Destino: Almacena la clave foránea que relaciona el historial con el usuario que efectuó el pedido en MySQL.

    private String fecha; // Qué hace: Declara el atributo privado fecha. | Para qué sirve / Destino: Almacena la marca de tiempo de cuándo se registró la orden en la base de datos.

    private double total; // Qué hace: Declara el atributo privado total de tipo double. | Para qué sirve / Destino: Almacena el costo monetario acumulado del pedido para su presentación en reportes o vistas.

    private String estado; // Qué hace: Declara el atributo privado estado. | Para qué sirve / Destino: Almacena el estatus operativo actual del pedido (ej: en proceso, entregado, cancelado).

    private String tipoEntrega; // Qué hace: Declara el atributo privado tipoEntrega. | Para qué sirve / Destino: Almacena la modalidad de entrega seleccionada (ej: domicilio, pasar a recoger).

    private String customerName; // Qué hace: Declara el atributo privado customerName. | Para qué sirve / Destino: Almacena el nombre del cliente desnormalizado para optimizar la consulta y visualización en el historial.

    /**
     * GETTERS Y SETTERS
     */

    public String getIdPedido() { // Qué hace: Declara el método getter getIdPedido que retorna un String. | Para qué sirve / Destino: Permite a los controladores o DAOs consultar el identificador único del pedido en el historial.

        return idPedido; // Qué hace: Retorna el valor del atributo idPedido. | Para qué sirve / Destino: Suministra el ID del pedido hacia la capa que lo solicite.
    }

    public void setIdPedido( // Qué hace: Declara el método setter setIdPedido que recibe un String. | Para qué sirve / Destino: Permite modificar o asignar el identificador del pedido en la entidad.
        String idPedido
    ) {

        this.idPedido = // Qué hace: Asigna el valor recibido al atributo de la instancia. | Para qué sirve / Destino: Actualiza internamente la propiedad idPedido del objeto Historial.
            idPedido;
    }

    public String getIdUsuario() { // Qué hace: Declara el método getter getIdUsuario que retorna un String. | Para qué sirve / Destino: Permite consultar el ID del usuario asociado desde los DAOs o lógica de negocio.

        return idUsuario; // Qué hace: Retorna el valor del atributo idUsuario. | Para qué sirve / Destino: Proporciona la referencia del usuario hacia los componentes de consulta.
    }

    public void setIdUsuario( // Qué hace: Declara el método setter setIdUsuario que recibe un String. | Para qué sirve / Destino: Permite asociar o cambiar el usuario responsable del pedido en la entidad.
        String idUsuario
    ) {

        this.idUsuario = // Qué hace: Asigna el valor recibido al atributo de la instancia. | Para qué sirve / Destino: Actualiza internamente la propiedad idUsuario del objeto Historial.
            idUsuario;
    }

    public String getFecha() { // Qué hace: Declara el método getter getFecha que retorna un String. | Para qué sirve / Destino: Permite consultar la marca temporal del pedido para su ordenamiento cronológico.

        return fecha; // Qué hace: Retorna el valor del atributo fecha. | Para qué sirve / Destino: Suministra el dato de tiempo hacia la interfaz o controladores.
    }

    public void setFecha( // Qué hace: Declara el método setter setFecha que recibe un String. | Para qué sirve / Destino: Permite asignar la marca temporal del pedido en la entidad.
        String fecha
    ) {

        this.fecha = // Qué hace: Asigna el valor recibido al atributo de la instancia. | Para qué sirve / Destino: Actualiza internamente la propiedad fecha del objeto Historial.
            fecha;
    }

    public double getTotal() { // Qué hace: Declara el método getter getTotal que retorna un double. | Para qué sirve / Destino: Permite leer el costo total del pedido para cálculos financieros o visualización.

        return total; // Qué hace: Retorna el valor del atributo total. | Para qué sirve / Destino: Suministra el monto monetario hacia las capas superiores.
    }

    public void setTotal( // Qué hace: Declara el método setter setTotal que recibe un double. | Para qué sirve / Destino: Permite asignar el valor financiero calculado del pedido en la entidad.
        double total
    ) {

        this.total = // Qué hace: Asigna el valor recibido al atributo de la instancia. | Para qué sirve / Destino: Actualiza internamente la propiedad total del objeto Historial.
            total;
    }

    public String getEstado() { // Qué hace: Declara el método getter getEstado que retorna un String. | Para qué sirve / Destino: Permite consultar el estatus operativo actual del pedido desde los controladores.

        return estado; // Qué hace: Retorna el valor del atributo estado. | Para qué sirve / Destino: Proporciona el estado del pedido hacia los componentes de interfaz.
    }

    public void setEstado( // Qué hace: Declara el método setter setEstado que recibe un String. | Para qué sirve / Destino: Permite modificar el estatus operativo del pedido en la entidad.
        String estado
    ) {

        this.estado = // Qué hace: Asigna el valor recibido al atributo de la instancia. | Para qué sirve / Destino: Actualiza internamente la propiedad estado del objeto Historial.
            estado;
    }

    public String getTipoEntrega() { // Qué hace: Declara el método getter getTipoEntrega que retorna un String. | Para qué sirve / Destino: Permite consultar la modalidad de entrega seleccionada desde la lógica de negocio.
        return tipoEntrega; // Qué hace: Retorna el valor del atributo tipoEntrega. | Para qué sirve / Destino: Suministra la información del tipo de entrega hacia los controladores.
    }

    public void setTipoEntrega(String tipoEntrega) { // Qué hace: Declara el método setter setTipoEntrega que recibe un String. | Para qué sirve / Destino: Permite asignar la modalidad de entrega en la entidad.
        this.tipoEntrega = tipoEntrega; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad tipoEntrega del objeto Historial.
    }

    public String getCustomerName() { // Qué hace: Declara el método getter getCustomerName que retorna un String. | Para qué sirve / Destino: Permite consultar el nombre del cliente para mostrarlo en las vistas de historial.
        return customerName; // Qué hace: Retorna el valor del atributo customerName. | Para qué sirve / Destino: Proporciona el nombre del cliente hacia las interfaces gráficas.
    }

    public void setCustomerName(String customerName) { // Qué hace: Declara el método setter setCustomerName que recibe un String. | Para qué sirve / Destino: Permite asignar el nombre del cliente en la entidad.
        this.customerName = customerName; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad customerName del objeto Historial.
    }

    private String estadoPago = "Sin pagar"; // Qué hace: Declara el atributo privado estadoPago inicializado por defecto en "Sin pagar". | Para qué sirve / Destino: Almacena el estatus financiero del pedido registrado en la base de datos MySQL.

    public String getEstadoPago() { // Qué hace: Declara el método getter getEstadoPago que retorna un String. | Para qué sirve / Destino: Permite consultar el estatus financiero actual del pedido desde los controladores o DAOs.
        return estadoPago; // Qué hace: Retorna el valor del atributo estadoPago. | Para qué sirve / Destino: Suministra el estado de pago hacia las capas superiores.
    }

    public void setEstadoPago(String estadoPago) { // Qué hace: Declara el método setter setEstadoPago que recibe un String. | Para qué sirve / Destino: Permite modificar el estatus financiero del pedido en la entidad.
        this.estadoPago = estadoPago; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad estadoPago del objeto Historial.
    }
}