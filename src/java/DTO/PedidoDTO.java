package DTO; // Qué hace: Declara el paquete DTO. | Para qué sirve / Destino: Ubica la clase lógicamente en la Capa de Objetos de Transferencia de Datos (Data Transfer Object) de la arquitectura del proyecto.

// Importar la interfaz List de Java para manejar colecciones tipadas de productos asociados al pedido
import java.util.List; // Qué hace: Importa la interfaz List. | Para qué sirve / Destino: Permite tipar la lista de objetos ProductoPedidoDTO que conforman el detalle del pedido.

/**
 * Objeto de Transferencia de Datos (DTO) para la entidad Pedido.
 * Encargado de transportar la información consolidada del pedido entre las vistas,
 * controladores y servicios de la aplicación web de la cafetería.
 */
public class PedidoDTO { // Qué hace: Declara la clase pública PedidoDTO. | Para qué sirve / Destino: Actúa como contenedor liviano para empaquetar y transferir los datos del pedido a través de las capas del sistema.

    private String cliente; // Qué hace: Declara el atributo privado cliente. | Para qué sirve / Destino: Almacena el nombre o identificador del cliente que realiza el pedido transportado hacia los controladores o servicios.
    private String tipoEntrega; // Qué hace: Declara el atributo privado tipoEntrega. | Para qué sirve / Destino: Define la modalidad de entrega seleccionada (ej: domicilio, en local) para procesar la lógica de negocio.
    private String direccion; // Qué hace: Declara el atributo privado direccion. | Para qué sirve / Destino: Almacena la dirección física de envío en caso de que aplique para la gestión del pedido.
    private List<ProductoPedidoDTO> productos; // Qué hace: Declara el atributo privado productos como una lista de ProductoPedidoDTO. | Para qué sirve / Destino: Contiene el detalle de los productos y cantidades solicitados, facilitando el transporte conjunto hacia los DAOs o la lógica de procesamiento.
    private String estadoPago ="sin_pagar"; // Qué hace: Declara el atributo privado estadoPago inicializado por defecto en "sin_pagar". | Para qué sirve / Destino: Establece el estado financiero inicial del pedido al ser transferido hacia los controladores de pago.

    // GETTERS Y SETTERS

    /**
     * Obtiene el nombre del cliente asociado al pedido.
     *
     * @return El nombre del cliente.
     */
    public String getCliente() { // Qué hace: Declara el método getter getCliente que retorna un String. | Para qué sirve / Destino: Permite a los controladores o capas superiores leer el nombre del cliente almacenado en el DTO.
        return cliente; // Qué hace: Retorna el valor del atributo cliente. | Para qué sirve / Destino: Suministra el dato del cliente hacia el componente que lo solicite.
    }

    /**
     * Establece el nombre del cliente asociado al pedido.
     *
     * @param cliente Nombre del cliente.
     */
    public void setCliente(String cliente) { // Qué hace: Declara el método setter setCliente que recibe un String. | Para qué sirve / Destino: Permite a los controladores asignar o modificar el nombre del cliente en el DTO.
        this.cliente = cliente; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad cliente del objeto PedidoDTO.
    }

    /**
     * Obtiene el tipo de entrega seleccionado para el pedido.
     *
     * @return El tipo de entrega.
     */
    public String getTipoEntrega() { // Qué hace: Declara el método getter getTipoEntrega que retorna un String. | Para qué sirve / Destino: Permite consultar la modalidad de entrega elegida desde los controladores o servicios.
        return tipoEntrega; // Qué hace: Retorna el valor del atributo tipoEntrega. | Para qué sirve / Destino: Proporciona la información del tipo de entrega al flujo de negocio.
    }

    /**
     * Establece el tipo de entrega para el pedido.
     *
     * @param tipoEntrega Modalidad de entrega.
     */
    public void setTipoEntrega(String tipoEntrega) { // Qué hace: Declara el método setter setTipoEntrega que recibe un String. | Para qué sirve / Destino: Permite asignar la modalidad de entrega recibida desde la vista o controladores.
        this.tipoEntrega = tipoEntrega; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad tipoEntrega del objeto PedidoDTO.
    }

    /**
     * Obtiene la dirección de entrega del pedido.
     *
     * @return La dirección física.
     */
    public String getDireccion() { // Qué hace: Declara el método getter getDireccion que retorna un String. | Para qué sirve / Destino: Permite leer la dirección de envío desde los servicios o lógica de entrega.
        return direccion; // Qué hace: Retorna el valor del atributo direccion. | Para qué sirve / Destino: Suministra la ubicación física hacia los componentes encargados de despachos.
    }

    /**
     * Establece la dirección de entrega del pedido.
     *
     * @param direccion Dirección física.
     */
    public void setDireccion(String direccion) { // Qué hace: Declara el método setter setDireccion que recibe un String. | Para qué sirve / Destino: Permite asignar la dirección capturada desde las interfaces de usuario o controladores.
        this.direccion = direccion; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad direccion del objeto PedidoDTO.
    }

    /**
     * Obtiene la lista de productos que componen el pedido.
     *
     * @return Lista de objetos ProductoPedidoDTO.
     */
    public List<ProductoPedidoDTO> getProductos() { // Qué hace: Declara el método getter getProductos que retorna una List de ProductoPedidoDTO. | Para qué sirve / Destino: Permite a los DAOs o servicios acceder al detalle completo de los ítems del pedido.
        return productos; // Qué hace: Retorna la lista de productos. | Para qué sirve / Destino: Suministra la colección de ítems solicitados hacia la capa de procesamiento de datos.
    }

    /**
     * Establece la lista de productos que componen el pedido.
     *
     * @param productos Lista de objetos ProductoPedidoDTO.
     */
    public void setProductos(List<ProductoPedidoDTO> productos) { // Qué hace: Declara el método setter setProductos que recibe una List de ProductoPedidoDTO. | Para qué sirve / Destino: Permite asociar el detalle de productos recopilado desde la vista o controladores al DTO.
        this.productos = productos; // Qué hace: Asigna la lista recibida al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la colección de productos del objeto PedidoDTO.
    }
    
    /**
     * Obtiene el estado actual del pago del pedido.
     *
     * @return El estado de pago en formato texto.
     */
    public String getEstadoPago() { // Qué hace: Declara el método getter getEstadoPago que retorna un String. | Para qué sirve / Destino: Permite consultar el estatus financiero actual desde los controladores o pasarelas de pago.
        return estadoPago; // Qué hace: Retorna el valor del atributo estadoPago. | Para qué sirve / Destino: Proporciona el estado de pago ("sin_pagar" u otro) hacia la lógica de validación.
    }
    
    /**
     * Establece el estado de pago del pedido.
     *
     * @param estadoPago Nuevo estado de pago.
     */
    public void setEstadoPago(String estadoPago) { // Qué hace: Declara el método setter setEstadoPago que recibe un String. | Para qué sirve / Destino: Permite modificar el estatus financiero del pedido tras procesar una transacción.
        this.estadoPago = estadoPago; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad estadoPago del objeto PedidoDTO.
    }
}