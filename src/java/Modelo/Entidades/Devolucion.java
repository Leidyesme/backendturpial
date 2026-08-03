package Modelo.Entidades; // Qué hace: Declara el paquete Modelo.Entidades. | Para qué sirve / Destino: Ubica la clase lógicamente en la Capa de Entidades del modelo de datos de la arquitectura del proyecto.

/**
 * Entidad que representa la solicitud de devolución de un pedido.
 */
public class Devolucion { // Qué hace: Declara la clase pública Devolucion. | Para qué sirve / Destino: Actúa como la entidad de persistencia en memoria para mapear los registros de devoluciones de pedidos provenientes de o hacia la base de datos MySQL.

    private String idDevolucion; // Qué hace: Declara el atributo privado idDevolucion. | Para qué sirve / Destino: Almacena el identificador único de la solicitud de devolución mapeado desde la clave primaria en MySQL.
    private String idPedido; // Qué hace: Declara el atributo privado idPedido. | Para qué sirve / Destino: Almacena la clave foránea que relaciona la devolución con el pedido específico afectado en MySQL.
    private String motivo; // Qué hace: Declara el atributo privado motivo. | Para qué sirve / Destino: Almacena la justificación o causa de la devolución descrita por el cliente.
    private String fechaSolicitud; // Qué hace: Declara el atributo privado fechaSolicitud. | Para qué sirve / Destino: Almacena la marca de tiempo de cuándo se radicó la solicitud de devolución.
    private String estadoDevolucion; // Qué hace: Declara el atributo privado estadoDevolucion. | Para qué sirve / Destino: Almacena el estatus actual de la solicitud (ej: Pendiente, Aprobada, Rechazada).
    private String respuestaAdmin; // Qué hace: Declara el atributo privado respuestaAdmin. | Para qué sirve / Destino: Almacena el comentario o contestación emitida por el administrador respecto a la devolución.

    /**
     * Constructor vacío.
     */
    public Devolucion() { // Qué hace: Define el constructor sin parámetros de la clase Devolucion. | Para qué sirve / Destino: Permite instanciar objetos vacíos para ser poblados posteriormente mediante setters (ej: al recorrer un ResultSet desde un DAO).
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
    public Devolucion(String idDevolucion, String idPedido, String motivo, String fechaSolicitud, String estadoDevolucion) { // Qué hace: Define el constructor con los parámetros principales de la clase. | Para qué sirve / Destino: Facilita la instanciación rápida de un objeto Devolucion con sus datos base para uso en controladores o DAOs.
        this.idDevolucion = idDevolucion; // Qué hace: Asigna el parámetro idDevolucion al atributo de la instancia. | Para qué sirve / Destino: Inicializa el identificador de la devolución en memoria.
        this.idPedido = idPedido; // Qué hace: Asigna el parámetro idPedido al atributo de la instancia. | Para qué sirve / Destino: Inicializa la relación con el pedido en el objeto.
        this.motivo = motivo; // Qué hace: Asigna el parámetro motivo al atributo de la instancia. | Para qué sirve / Destino: Inicializa la justificación de la devolución en memoria.
        this.fechaSolicitud = fechaSolicitud; // Qué hace: Asigna el parámetro fechaSolicitud al atributo de la instancia. | Para qué sirve / Destino: Inicializa la fecha de radicación en el objeto.
        this.estadoDevolucion = estadoDevolucion; // Qué hace: Asigna el parámetro estadoDevolucion al atributo de la instancia. | Para qué sirve / Destino: Inicializa el estado operativo de la solicitud en memoria.
    }

    /**
     * Obtiene el identificador único de la solicitud de devolución.
     *
     * @return El ID de la devolución.
     */
    public String getIdDevolucion() { // Qué hace: Declara el método getter getIdDevolucion que retorna un String. | Para qué sirve / Destino: Permite a los controladores o DAOs consultar el identificador único de la devolución.
        return idDevolucion; // Qué hace: Retorna el valor del atributo idDevolucion. | Para qué sirve / Destino: Suministra el ID de la devolución hacia la capa que lo solicite.
    }

    /**
     * Establece el identificador único de la solicitud de devolución.
     *
     * @param idDevolucion Identificador a asignar.
     */
    public void setIdDevolucion(String idDevolucion) { // Qué hace: Declara el método setter setIdDevolucion que recibe un String. | Para qué sirve / Destino: Permite modificar o asignar el identificador de la devolución en la entidad.
        this.idDevolucion = idDevolucion; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad idDevolucion del objeto Devolucion.
    }

    /**
     * Obtiene el identificador del pedido asociado a la devolución.
     *
     * @return El ID del pedido.
     */
    public String getIdPedido() { // Qué hace: Declara el método getter getIdPedido que retorna un String. | Para qué sirve / Destino: Permite consultar el pedido vinculado a la devolución desde los DAOs o lógica de negocio.
        return idPedido; // Qué hace: Retorna el valor del atributo idPedido. | Para qué sirve / Destino: Proporciona la referencia del pedido hacia los componentes de gestión.
    }

    /**
     * Establece el identificador del pedido asociado a la devolución.
     *
     * @param idPedido Identificador del pedido.
     */
    public void setIdPedido(String idPedido) { // Qué hace: Declara el método setter setIdPedido que recibe un String. | Para qué sirve / Destino: Permite asociar o cambiar el pedido vinculado a la devolución en la entidad.
        this.idPedido = idPedido; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad idPedido del objeto Devolucion.
    }

    /**
     * Obtiene el motivo o justificación de la devolución.
     *
     * @return El motivo de la devolución.
     */
    public String getMotivo() { // Qué hace: Declara el método getter getMotivo que retorna un String. | Para qué sirve / Destino: Permite leer la causa del reclamo para su revisión en vistas o paneles de administración.
        return motivo; // Qué hace: Retorna el valor del atributo motivo. | Para qué sirve / Destino: Suministra el texto descriptivo del motivo hacia las capas superiores.
    }

    /**
     * Establece el motivo o justificación de la devolución.
     *
     * @param motivo Motivo de la devolución.
     */
    public void setMotivo(String motivo) { // Qué hace: Declara el método setter setMotivo que recibe un String. | Para qué sirve / Destino: Permite definir el detalle textual del motivo ingresado por el cliente.
        this.motivo = motivo; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad motivo del objeto Devolucion.
    }

    /**
     * Obtiene la fecha y hora en que se solicitó la devolución.
     *
     * @return Fecha de la solicitud.
     */
    public String getFechaSolicitud() { // Qué hace: Declara el método getter getFechaSolicitud que retorna un String. | Para qué sirve / Destino: Permite consultar la marca temporal de radicación para el ordenamiento en reportes o vistas.
        return fechaSolicitud; // Qué hace: Retorna el valor del atributo fechaSolicitud. | Para qué sirve / Destino: Suministra el dato de tiempo de solicitud hacia la interfaz o controladores.
    }

    /**
     * Establece la fecha y hora en que se solicitó la devolución.
     *
     * @param fechaSolicitud Fecha y hora de la solicitud.
     */
    public void setFechaSolicitud(String fechaSolicitud) { // Qué hace: Declara el método setter setFechaSolicitud que recibe un String. | Para qué sirve / Destino: Permite asignar la marca temporal de radicación de la devolución en la entidad.
        this.fechaSolicitud = fechaSolicitud; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad fechaSolicitud del objeto Devolucion.
    }

    /**
     * Obtiene el estado actual de la devolución.
     *
     * @return Estado de la devolución.
     */
    public String getEstadoDevolucion() { // Qué hace: Declara el método getter getEstadoDevolucion que retorna un String. | Para qué sirve / Destino: Permite consultar el estatus operativo de la solicitud desde los controladores o lógica de negocio.
        return estadoDevolucion; // Qué hace: Retorna el valor del atributo estadoDevolucion. | Para qué sirve / Destino: Proporciona el estado de la devolución hacia los componentes de evaluación.
    }

    /**
     * Establece el estado actual de la devolución.
     *
     * @param estadoDevolucion Estado de la devolución.
     */
    public void setEstadoDevolucion(String estadoDevolucion) { // Qué hace: Declara el método setter setEstadoDevolucion que recibe un String. | Para qué sirve / Destino: Permite modificar el estatus operativo de la solicitud tras una revisión administrativa.
        this.estadoDevolucion = estadoDevolucion; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad estadoDevolucion del objeto Devolucion.
    }
    
    /**
     * Obtiene la respuesta o comentario emitido por el administrador.
     *
     * @return Respuesta del administrador.
     */
    public String getRespuestaAdmin() { // Qué hace: Declara el método getter getRespuestaAdmin que retorna un String. | Para qué sirve / Destino: Permite consultar la contestación del administrador para mostrarla en el detalle de la devolución.
        return respuestaAdmin; // Qué hace: Retorna el valor del atributo respuestaAdmin. | Para qué sirve / Destino: Suministra el texto de respuesta hacia las vistas o controladores.
    }
    
    /**
     * Establece la respuesta o comentario emitido por el administrador.
     *
     * @param respuestaAdmin Respuesta del administrador.
     */
    public void setRespuestaAdmin(String respuestaAdmin) { // Qué hace: Declara el método setter setRespuestaAdmin que recibe un String. | Para qué sirve / Destino: Permite registrar el comentario del administrador sobre la solicitud en la entidad.
        this.respuestaAdmin = respuestaAdmin; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad respuestaAdmin del objeto Devolucion.
    }
}