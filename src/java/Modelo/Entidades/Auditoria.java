package Modelo.Entidades; // Qué hace: Declara el paquete Modelo.Entidades. | Para qué sirve / Destino: Ubica la clase lógicamente en la Capa de Entidades del modelo de datos de la arquitectura del proyecto.

/**
 * Entidad que representa un registro de auditoría o historial de actividad de un usuario.
 */
public class Auditoria { // Qué hace: Declara la clase pública Auditoria. | Para qué sirve / Destino: Actúa como la entidad de persistencia en memoria para mapear los registros de auditoría provenientes de o hacia la base de datos MySQL.

    private String idHistorial; // Qué hace: Declara el atributo privado idHistorial. | Para qué sirve / Destino: Almacena el identificador único del registro de auditoría mapeado desde la clave primaria en MySQL.
    private String idUsuario; // Qué hace: Declara el atributo privado idUsuario. | Para qué sirve / Destino: Almacena la clave foránea que relaciona la auditoría con el usuario que ejecutó la acción.
    private String accion; // Qué hace: Declara el atributo privado accion. | Para qué sirve / Destino: Almacena la descripción detallada de la actividad realizada por el usuario.
    private String tipoAccion; // Qué hace: Declara el atributo privado tipoAccion. | Para qué sirve / Destino: Almacena la categoría de la actividad (ej: LOGIN, COMPRA) para agilizar filtros en los DAOs.
    private String fecha; // Qué hace: Declara el atributo privado fecha. | Para qué sirve / Destino: Almacena la marca de tiempo de cuándo ocurrió el evento auditado.

    /**
     * Constructor vacío.
     */
    public Auditoria() { // Qué hace: Define el constructor sin parámetros de la clase Auditoria. | Para qué sirve / Destino: Permite instanciar objetos vacíos para ser poblados posteriormente mediante setters (ej: al recorrer un ResultSet desde un DAO).
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
    public Auditoria(String idHistorial, String idUsuario, String accion, String tipoAccion, String fecha) { // Qué hace: Define el constructor con todos los atributos de la clase. | Para qué sirve / Destino: Facilita la instanciación rápida de un objeto Auditoria con sus datos completos listos para ser usados en controladores o DAOs.
        this.idHistorial = idHistorial; // Qué hace: Asigna el parámetro idHistorial al atributo de la instancia. | Para qué sirve / Destino: Inicializa el identificador único del registro en memoria.
        this.idUsuario = idUsuario; // Qué hace: Asigna el parámetro idUsuario al atributo de la instancia. | Para qué sirve / Destino: Inicializa la relación con el usuario en el objeto.
        this.accion = accion; // Qué hace: Asigna el parámetro accion al atributo de la instancia. | Para qué sirve / Destino: Inicializa la descripción de la acción en memoria.
        this.tipoAccion = tipoAccion; // Qué hace: Asigna el parámetro tipoAccion al atributo de la instancia. | Para qué sirve / Destino: Inicializa la categoría de la acción en el objeto.
        this.fecha = fecha; // Qué hace: Asigna el parámetro fecha al atributo de la instancia. | Para qué sirve / Destino: Inicializa la marca temporal del evento en memoria.
    }

    /**
     * Obtiene el identificador único del registro de auditoría.
     *
     * @return El ID del historial.
     */
    public String getIdHistorial() { // Qué hace: Declara el método getter getIdHistorial que retorna un String. | Para qué sirve / Destino: Permite a los controladores o DAOs consultar el ID único del registro de auditoría.
        return idHistorial; // Qué hace: Retorna el valor del atributo idHistorial. | Para qué sirve / Destino: Suministra el identificador del registro hacia la capa que lo solicite.
    }

    /**
     * Establece el identificador único del registro de auditoría.
     *
     * @param idHistorial Identificador a asignar.
     */
    public void setIdHistorial(String idHistorial) { // Qué hace: Declara el método setter setIdHistorial que recibe un String. | Para qué sirve / Destino: Permite modificar o asignar el ID del historial en la entidad.
        this.idHistorial = idHistorial; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad idHistorial del objeto Auditoria.
    }

    /**
     * Obtiene el identificador del usuario asociado al registro.
     *
     * @return El ID del usuario.
     */
    public String getIdUsuario() { // Qué hace: Declara el método getter getIdUsuario que retorna un String. | Para qué sirve / Destino: Permite consultar el usuario responsable de la acción desde los DAOs o lógica de negocio.
        return idUsuario; // Qué hace: Retorna el valor del atributo idUsuario. | Para qué sirve / Destino: Proporciona la referencia del usuario hacia los componentes que auditan la actividad.
    }

    /**
     * Establece el identificador del usuario asociado al registro.
     *
     * @param idUsuario Identificador del usuario.
     */
    public void setIdUsuario(String idUsuario) { // Qué hace: Declara el método setter setIdUsuario que recibe un String. | Para qué sirve / Destino: Permite asociar o cambiar el usuario responsable de la actividad en la entidad.
        this.idUsuario = idUsuario; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad idUsuario del objeto Auditoria.
    }

    /**
     * Obtiene la descripción de la acción realizada.
     *
     * @return Descripción de la acción.
     */
    public String getAccion() { // Qué hace: Declara el método getter getAccion que retorna un String. | Para qué sirve / Destino: Permite leer el detalle de la actividad registrada para su visualización en vistas o reportes.
        return accion; // Qué hace: Retorna el valor del atributo accion. | Para qué sirve / Destino: Suministra el texto descriptivo del evento hacia las capas superiores.
    }

    /**
     * Establece la descripción de la acción realizada.
     *
     * @param accion Descripción de la acción.
     */
    public void setAccion(String accion) { // Qué hace: Declara el método setter setAccion que recibe un String. | Para qué sirve / Destino: Permite definir el detalle textual de la actividad ejecutada.
        this.accion = accion; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad accion del objeto Auditoria.
    }

    /**
     * Obtiene la categoría o tipo de acción realizada.
     *
     * @return Tipo de acción.
     */
    public String getTipoAccion() { // Qué hace: Declara el método getter getTipoAccion que retorna un String. | Para qué sirve / Destino: Permite consultar la clasificación del evento para aplicar filtros en los DAOs o lógica de negocio.
        return tipoAccion; // Qué sirve / Destino: Retorna el valor del atributo tipoAccion. | Proporciona la categoría del evento hacia los componentes de análisis.
        
    }

    /**
     * Establece la categoría o tipo de acción realizada.
     *
     * @param tipoAccion Tipo de acción.
     */
    public void setTipoAccion(String tipoAccion) { // Qué hace: Declara el método setter setTipoAccion que recibe un String. | Para qué sirve / Destino: Permite clasificar la naturaleza del evento registrado en la entidad.
        this.tipoAccion = tipoAccion; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad tipoAccion del objeto Auditoria.
    }

    /**
     * Obtiene la fecha y hora en que ocurrió el evento.
     *
     * @return Fecha del evento.
     */
    public String getFecha() { // Qué hace: Declara el método getter getFecha que retorna un String. | Para qué sirve / Destino: Permite consultar la marca temporal del registro para ordenamiento cronológico en reportes o vistas.
        return fecha; // Qué hace: Retorna el valor del atributo fecha. | Para qué sirve / Destino: Suministra el dato de tiempo hacia la interfaz o controladores.
    }

    /**
     * Establece la fecha y hora en que ocurrió el evento.
     *
     * @param fecha Fecha y hora.
     */
    public void setFecha(String fecha) { // Qué hace: Declara el método setter setFecha que recibe un String. | Para qué sirve / Destino: Permite asignar la marca temporal del suceso auditado en la entidad.
        this.fecha = fecha; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad fecha del objeto Auditoria.
    }
}