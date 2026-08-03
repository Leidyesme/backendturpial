package Modelo.Entidades; // Qué hace: Declara el paquete Modelo.Entidades. | Para qué sirve / Destino: Ubica la clase lógicamente en la Capa de Entidades del modelo de datos de la arquitectura del proyecto.

// Clase que representa un pedido dentro del sistema.
 
public class Pedido { // Qué hace: Declara la clase pública Pedido. | Para qué sirve / Destino: Actúa como la entidad de persistencia en memoria para mapear los registros de pedidos provenientes de o hacia la base de datos MySQL.

    // ATRIBUTOS
     

    // Identificador único del pedido
    private String idPedido; // Qué hace: Declara el atributo privado idPedido. | Para qué sirve / Destino: Almacena el identificador único del pedido mapeado desde la clave primaria en la base de datos MySQL.

    // Usuario registrado que realizó el pedido
    private String idUsuario; // Qué hace: Declara el atributo privado idUsuario. | Para qué sirve / Destino: Almacena la clave foránea que relaciona el pedido con el cliente registrado en la base de datos MySQL.

    // Nombre del cliente cuando no está registrado
    private String nombreClienteOpcional; // Qué hace: Declara el atributo privado nombreClienteOpcional. | Para qué sirve / Destino: Almacena el nombre del cliente de forma temporal cuando realiza un pedido como invitado, comunicándose con las vistas o controladores.

    // Tipo de entrega:
    // domicilio, recoger o consumir aquí
    private String tipoEntrega; // Qué hace: Declara el atributo privado tipoEntrega. | Para qué sirve / Destino: Almacena la modalidad elegida para procesar la lógica de despachos o consumo en el sistema.

    // Número de mesa para consumo local
    private Integer numeroMesa; // Qué hace: Declara el atributo privado numeroMesa de tipo Integer. | Para qué sirve / Destino: Almacena el número de mesa asignado cuando el pedido es para consumir en el establecimiento.

    // Dirección para pedidos a domicilio
    private String direccionEntrega; // Qué hace: Declara el atributo privado direccionEntrega. | Para qué sirve / Destino: Almacena la ubicación física de envío para los pedidos solicitados a domicilio.

    // Observaciones adicionales del cliente
    private String observaciones; // Qué hace: Declara el atributo privado observaciones. | Para qué sirve / Destino: Almacena notas especiales o restricciones del pedido enviadas desde la interfaz de usuario.

    // Total del pedido
    private double total; // Qué hace: Declara el atributo privado total de tipo double. | Para qué sirve / Destino: Almacena el costo monetario acumulado del pedido para su gestión financiera y persistencia en MySQL.

    // Estado actual del pedido
    private String estado; // Qué hace: Declara el atributo privado estado. | Para qué sirve / Destino: Almacena el estatus operativo actual (ej: pendiente, en preparación, entregado) utilizado por los controladores.

    // Fecha y hora del pedido
    private String fechaPedido; // Qué hace: Declara el atributo privado fechaPedido. | Para qué sirve / Destino: Almacena la marca de tiempo de creación del registro para su consulta y ordenamiento en la base de datos MySQL.

    /**
     * CONSTRUCTOR VACÍO
     *
     * Se usa para crear objetos
     * sin enviar datos inicialmente.
     */
    public Pedido() { // Qué hace: Define el constructor sin parámetros de la clase Pedido. | Para qué sirve / Destino: Permite instanciar objetos vacíos en los controladores o formularios web para ser poblados posteriormente mediante setters.
    }

    //CONSTRUCTOR COMPLETO Se usa cuando recuperamos datos desde la base de datos.
     
    public Pedido( // Qué hace: Define el constructor sobrecargado que recibe todos los atributos de la clase. | Para qué sirve / Destino: Facilita la instanciación rápida de un objeto Pedido completo al ser recuperado mediante los DAOs desde la base de datos MySQL.
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

        this.idPedido = idPedido; // Qué hace: Asigna el parámetro idPedido al atributo de la instancia. | Para qué sirve / Destino: Inicializa el ID del pedido en memoria.

        this.idUsuario = idUsuario; // Qué hace: Asigna el parámetro idUsuario al atributo de la instancia. | Para qué sirve / Destino: Inicializa la relación con el usuario en el objeto.

        this.nombreClienteOpcional = // Qué hace: Asigna el parámetro nombreClienteOpcional al atributo de la instancia. | Para qué sirve / Destino: Inicializa el nombre opcional del cliente en memoria.
                nombreClienteOpcional;

        this.tipoEntrega = tipoEntrega; // Qué hace: Asigna el parámetro tipoEntrega al atributo de la instancia. | Para qué sirve / Destino: Inicializa la modalidad de entrega en el objeto.

        this.numeroMesa = numeroMesa; // Qué hace: Asigna el parámetro numeroMesa al atributo de la instancia. | Para qué sirve / Destino: Inicializa el número de mesa en memoria para consumo local.

        this.direccionEntrega = // Qué hace: Asigna el parámetro direccionEntrega al atributo de la instancia. | Para qué sirve / Destino: Inicializa la ubicación de envío en el objeto.
                direccionEntrega;

        this.observaciones = // Qué hace: Asigna el parámetro observaciones al atributo de la instancia. | Para qué sirve / Destino: Inicializa las notas adicionales del pedido en memoria.
                observaciones;

        this.total = total; // Qué hace: Asigna el parámetro total al atributo de la instancia. | Para qué sirve / Destino: Inicializa el costo total del pedido en el objeto.

        this.estado = estado; // Qué hace: Asigna el parámetro estado al atributo de la instancia. | Para qué sirve / Destino: Inicializa el estatus operativo del pedido en memoria.

        this.fechaPedido = fechaPedido; // Qué hace: Asigna el parámetro fechaPedido al atributo de la instancia. | Para qué sirve / Destino: Inicializa la marca temporal del pedido en el objeto.
    }

    /**
     * GETTERS Y SETTERS
     */

    public String getIdPedido() { // Qué hace: Declara el método getter getIdPedido que retorna un String. | Para qué sirve / Destino: Permite a los controladores o DAOs consultar el identificador único del pedido.

        return idPedido; // Qué hace: Retorna el valor del atributo idPedido. | Para qué sirve / Destino: Suministra el ID del pedido hacia la capa que lo solicite.
    }

    public void setIdPedido( // Qué hace: Declara el método setter setIdPedido que recibe un String. | Para qué sirve / Destino: Permite modificar o asignar el identificador del pedido en la entidad.
            String idPedido
    ) {

        this.idPedido = // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad idPedido del objeto Pedido.
                idPedido;
    }

    public String getIdUsuario() { // Qué hace: Declara el método getter getIdUsuario que retorna un String. | Para qué sirve / Destino: Permite consultar el usuario asociado desde los DAOs o lógica de negocio.

        return idUsuario; // Qué hace: Retorna el valor del atributo idUsuario. | Para qué sirve / Destino: Proporciona la referencia del usuario hacia los componentes de consulta.
    }

    public void setIdUsuario( // Qué hace: Declara el método setter setIdUsuario que recibe un String. | Para qué sirve / Destino: Permite asociar o cambiar el usuario responsable del pedido en la entidad.
            String idUsuario
    ) {

        this.idUsuario = // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad idUsuario del objeto Pedido.
                idUsuario;
    }

    public String getNombreClienteOpcional() { // Qué hace: Declara el método getter getNombreClienteOpcional que retorna un String. | Para qué sirve / Destino: Permite consultar el nombre del cliente invitado desde los controladores.

        return nombreClienteOpcional; // Qué hace: Retorna el valor del atributo nombreClienteOpcional. | Para qué sirve / Destino: Suministra el nombre opcional hacia la interfaz o lógica de negocio.
    }

    public void setNombreClienteOpcional( // Qué hace: Declara el método setter setNombreClienteOpcional que recibe un String. | Para qué sirve / Destino: Permite asignar el nombre del cliente invitado en la entidad.
            String nombreClienteOpcional
    ) {

        this.nombreClienteOpcional = // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad nombreClienteOpcional del objeto Pedido.
                nombreClienteOpcional;
    }

    public String getTipoEntrega() { // Qué hace: Declara el método getter getTipoEntrega que retorna un String. | Para qué sirve / Destino: Permite consultar la modalidad de entrega seleccionada desde la lógica de negocio.

        return tipoEntrega; // Qué hace: Retorna el valor del atributo tipoEntrega. | Para qué sirve / Destino: Proporciona la información del tipo de entrega hacia los controladores.
    }

    public void setTipoEntrega( // Qué hace: Declara el método setter setTipoEntrega que recibe un String. | Para qué sirve / Destino: Permite asignar la modalidad de entrega en la entidad.
            String tipoEntrega
    ) {

        this.tipoEntrega = // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad tipoEntrega del objeto Pedido.
                tipoEntrega;
    }

    public Integer getNumeroMesa() { // Qué hace: Declara el método getter getNumeroMesa que retorna un Integer. | Para qué sirve / Destino: Permite consultar el número de mesa asignado desde los componentes de atención local.

        return numeroMesa; // Qué hace: Retorna el valor del atributo numeroMesa. | Para qué sirve / Destino: Suministra el número de mesa hacia las vistas o lógica de servicio.
    }

    public void setNumeroMesa( // Qué hace: Declara el método setter setNumeroMesa que recibe un Integer. | Para qué sirve / Destino: Permite asignar el número de mesa en la entidad.
            Integer numeroMesa
    ) {

        this.numeroMesa = // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad numeroMesa del objeto Pedido.
                numeroMesa;
    }

    public String getDireccionEntrega() { // Qué hace: Declara el método getter getDireccionEntrega que retorna un String. | Para qué sirve / Destino: Permite consultar la dirección de envío desde los servicios de despacho.

        return direccionEntrega; // Qué hace: Retorna el valor del atributo direccionEntrega. | Para qué sirve / Destino: Proporciona la ubicación física hacia los componentes logísticos.
    }

    public void setDireccionEntrega( // Qué hace: Declara el método setter setDireccionEntrega que recibe un String. | Para qué sirve / Destino: Permite asignar la dirección de entrega en la entidad.
            String direccionEntrega
    ) {

        this.direccionEntrega = // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad direccionEntrega del objeto Pedido.
                direccionEntrega;
    }

    public String getObservaciones() { // Qué hace: Declara el método getter getObservaciones que retorna un String. | Para qué sirve / Destino: Permite leer las notas especiales del pedido para su preparación en la cocina.

        return observaciones; // Qué hace: Retorna el valor del atributo observaciones. | Para qué sirve / Destino: Suministra el texto de observaciones hacia las vistas del personal de la cafetería.
    }

    public void setObservaciones( // Qué hace: Declara el método setter setObservaciones que recibe un String. | Para qué sirve / Destino: Permite asignar las notas adicionales del pedido en la entidad.
            String observaciones
    ) {

        this.observaciones = // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad observaciones del objeto Pedido.
                observaciones;
    }

    public double getTotal() { // Qué hace: Declara el método getter getTotal que retorna un double. | Para qué sirve / Destino: Permite leer el costo total del pedido para cálculos financieros o visualización.

        return total; // Qué hace: Retorna el valor del atributo total. | Para qué sirve / Destino: Suministra el monto monetario hacia las capas superiores.
    }

    public void setTotal( // Qué hace: Declara el método setter setTotal que recibe un double. | Para qué sirve / Destino: Permite asignar el valor financiero calculado del pedido en la entidad.
            double total
    ) {

        this.total = total; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad total del objeto Pedido.
    }

    public String getEstado() { // Qué hace: Declara el método getter getEstado que retorna un String. | Para qué sirve / Destino: Permite consultar el estatus operativo actual del pedido desde los controladores.

        return estado; // Qué hace: Retorna el valor del atributo estado. | Para qué sirve / Destino: Proporciona el estado del pedido hacia los componentes de interfaz.
    }

    public void setEstado( // Qué hace: Declara el método setter setEstado que recibe un String. | Para qué sirve / Destino: Permite modificar el estatus operativo del pedido en la entidad.
            String estado
    ) {

        this.estado = // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad estado del objeto Pedido.
                estado;
    }

    public String getFechaPedido() { // Qué hace: Declara el método getter getFechaPedido que retorna un String. | Para qué sirve / Destino: Permite consultar la marca temporal del pedido para su ordenamiento cronológico.

        return fechaPedido; // Qué hace: Retorna el valor del atributo fechaPedido. | Para qué sirve / Destino: Suministra el dato de tiempo hacia la interfaz o controladores.
    }

    public void setFechaPedido( // Qué hace: Declara el método setter setFechaPedido que recibe un String. | Para qué sirve / Destino: Permite asignar la marca temporal del pedido en la entidad.
            String fechaPedido
    ) {

        this.fechaPedido = // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad fechaPedido del objeto Pedido.
                fechaPedido;
    }

    private String estadoPago = "Sin pagar"; // Qué hace: Declara el atributo privado estadoPago inicializado por defecto en "Sin pagar". | Para qué sirve / Destino: Almacena el estatus financiero del pedido registrado en la base de datos MySQL.

    public String getEstadoPago() { // Qué hace: Declara el método getter getEstadoPago que retorna un String. | Para qué sirve / Destino: Permite consultar el estatus financiero actual del pedido desde los controladores o DAOs.
        return estadoPago; // Qué hace: Retorna el valor del atributo estadoPago. | Para qué sirve / Destino: Suministra el estado de pago hacia las capas superiores.
    }

    public void setEstadoPago(String estadoPago) { // Qué hace: Declara el método setter setEstadoPago que recibe un String. | Para qué sirve / Destino: Permite modificar el estatus financiero del pedido en la entidad.
        this.estadoPago = estadoPago; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad estadoPago del objeto Pedido.
    }
}