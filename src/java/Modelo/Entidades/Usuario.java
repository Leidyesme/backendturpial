package Modelo.Entidades; // Qué hace: Declara el paquete Modelo.Entidades. | Para qué sirve / Destino: Ubica la clase lógicamente en la Capa de Entidades del modelo de datos de la arquitectura del proyecto.

public class Usuario { // Qué hace: Declara la clase pública Usuario. | Para qué sirve / Destino: Actúa como la entidad de persistencia en memoria para mapear los registros de los usuarios y clientes del sistema provenientes de o hacia la base de datos MySQL.
    private String idUsuario; // Qué hace: Declara el atributo privado idUsuario. | Para qué sirve / Destino: Almacena el identificador único del usuario mapeado desde la clave primaria en la base de datos MySQL.
    private String idRol; // Qué hace: Declara el atributo privado idRol. | Para qué sirve / Destino: Almacena la clave foránea que relaciona al usuario con su rol de permisos o privilegios en la base de datos MySQL.
    private String name; // Qué hace: Declara el atributo privado name. | Para qué sirve / Destino: Almacena el nombre completo del usuario para su visualización en las vistas y perfiles del sistema.
    private String email; // Qué hace: Declara el atributo privado email. | Para qué sirve / Destino: Almacena el correo electrónico utilizado para la autenticación y contacto, persistido en MySQL.
    private String phone; // Qué hace: Declara el atributo privado phone. | Para qué sirve / Destino: Almacena el número telefónico de contacto del usuario dentro de la entidad.
    private String password; // Qué hace: Declara el atributo privado password. | Para qué sirve / Destino: Almacena la contraseña de acceso al sistema del usuario para validar las credenciales de inicio de sesión.
    private String estado; // Qué hace: Declara el atributo privado estado. | Para qué sirve / Destino: Almacena el estatus operativo del usuario (ej: activo, inactivo) gestionado por los controladores y la base de datos.
    private String direccion; // Qué hace: Declara el atributo privado direccion. | Para qué sirve / Destino: Almacena la ubicación física o de entrega predeterminada asociada al perfil del usuario.

    // Constructor vacío 
    public Usuario() { // Qué hace: Define el constructor sin parámetros de la clase Usuario. | Para qué sirve / Destino: Permite instanciar objetos vacíos en los controladores o formularios de registro para ser poblados posteriormente mediante setters.
    }

    // Constructor completo para cuando recuperamos datos de la BD
    public Usuario(String idUsuario, String idRol, String name, String email, String phone, String password, String estado, String direccion) { // Qué hace: Define el constructor sobrecargado que recibe todos los atributos de la clase. | Para qué sirve / Destino: Facilita la instanciación rápida de un objeto Usuario completo al ser recuperado mediante los DAOs desde la base de datos MySQL.
        this.idUsuario = idUsuario; // Qué hace: Asigna el parámetro idUsuario al atributo de la instancia. | Para qué sirve / Destino: Inicializa el ID del usuario en memoria.
        this.idRol = idRol; // Qué hace: Asigna el parámetro idRol al atributo de la instancia. | Para qué sirve / Destino: Inicializa la relación con el rol del usuario en el objeto.
        this.name = name; // Qué hace: Asigna el parámetro name al atributo de la instancia. | Para qué sirve / Destino: Inicializa el nombre del usuario en memoria.
        this.email = email; // Qué hace: Asigna el parámetro email al atributo de la instancia. | Para qué sirve / Destino: Inicializa el correo electrónico en el objeto.
        this.phone = phone; // Qué hace: Asigna el parámetro phone al atributo de la instancia. | Para qué sirve / Destino: Inicializa el teléfono de contacto en memoria.
        this.password = password; // Qué hace: Asigna el parámetro password al atributo de la instancia. | Para qué sirve / Destino: Inicializa la contraseña de acceso en el objeto.
        this.estado = estado; // Qué hace: Asigna el parámetro estado al atributo de la instancia. | Para qué sirve / Destino: Inicializa el estatus operativo del usuario en memoria.
        this.direccion = direccion; // Qué hace: Asigna el parámetro direccion al atributo de la instancia. | Para qué sirve / Destino: Inicializa la dirección física del usuario en el objeto.
    }

    // Getters y Setters
    public String getIdUsuario() { // Qué hace: Declara el método getter getIdUsuario que retorna un String. | Para qué sirve / Destino: Permite a los controladores o DAOs consultar el identificador único del usuario.
        return idUsuario; // Qué hace: Retorna el valor del atributo idUsuario. | Para qué sirve / Destino: Suministra el ID del usuario hacia la capa que lo solicite.
    }

    public void setIdUsuario(String idUsuario) { // Qué hace: Declara el método setter setIdUsuario que recibe un String. | Para qué sirve / Destino: Permite modificar o asignar el identificador del usuario en la entidad.
        this.idUsuario = idUsuario; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad idUsuario del objeto Usuario.
    }

    public String getIdRol() { // Qué hace: Declara el método getter getIdRol que retorna un String. | Para qué sirve / Destino: Permite consultar el rol asignado desde los DAOs o la lógica de autenticación.
        return idRol; // Qué hace: Retorna el valor del atributo idRol. | Para qué sirve / Destino: Proporciona la referencia del rol de usuario hacia los componentes de control.
    }

    public void setIdRol(String idRol) { // Qué hace: Declara el método setter setIdRol que recibe un String. | Para qué sirve / Destino: Permite asociar o cambiar el rol de privilegios del usuario en la entidad.
        this.idRol = idRol; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad idRol del objeto Usuario.
    }

    public String getName() { // Qué hace: Declara el método getter getName que retorna un String. | Para qué sirve / Destino: Permite consultar el nombre del usuario para mostrarlo en las vistas o interfaz web.
        return name; // Qué hace: Retorna el valor del atributo name. | Para qué sirve / Destino: Suministra el nombre del usuario hacia los controladores.
    }

    public void setName(String name) { // Qué hace: Declara el método setter setName que recibe un String. | Para qué sirve / Destino: Permite asignar el nombre del usuario en la entidad.
        this.name = name; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad name del objeto Usuario.
    }

    public String getEmail() { // Qué hace: Declara el método getter getEmail que retorna un String. | Para qué sirve / Destino: Permite consultar el correo electrónico para procesos de login o notificaciones.
        return email; // Qué hace: Retorna el valor del atributo email. | Para qué sirve / Destino: Proporciona el correo electrónico hacia la lógica de negocio o DAOs.
    }

    public void setEmail(String email) { // Qué hace: Declara el método setter setEmail que recibe un String. | Para qué sirve / Destino: Permite asignar el correo electrónico del usuario en la entidad.
        this.email = email; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad email del objeto Usuario.
    }

    public String getPhone() { // Qué hace: Declara el método getter getPhone que retorna un String. | Para qué sirve / Destino: Permite consultar el número telefónico del usuario desde los controladores.
        return phone; // Qué hace: Retorna el valor del atributo phone. | Para qué sirve / Destino: Suministra el teléfono hacia las vistas o componentes de soporte.
    }

    public void setPhone(String phone) { // Qué hace: Declara el método setter setPhone que recibe un String. | Para qué sirve / Destino: Permite asignar el número telefónico del usuario en la entidad.
        this.phone = phone; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad phone del objeto Usuario.
    }

    public String getPassword() { // Qué hace: Declara el método getter getPassword que retorna un String. | Para qué sirve / Destino: Permite consultar la contraseña cifrada o de texto para validar el inicio de sesión en los DAOs.
        return password; // Qué hace: Retorna el valor del atributo password. | Para qué sirve / Destino: Proporciona la credencial de acceso hacia los servicios de autenticación.
    }

    public void setPassword(String password) { // Qué hace: Declara el método setter setPassword que recibe un String. | Para qué sirve / Destino: Permite asignar o actualizar la contraseña del usuario en la entidad.
        this.password = password; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad password del objeto Usuario.
    }

    public String getEstado() { // Qué hace: Declara el método getter getEstado que retorna un String. | Para qué sirve / Destino: Permite consultar el estatus operativo actual del usuario desde los controladores.
        return estado; // Qué hace: Retorna el valor del atributo estado. | Para qué sirve / Destino: Suministra el estado de actividad del usuario hacia los componentes de interfaz.
    }

    public void setEstado(String estado) { // Qué hace: Declara el método setter setEstado que recibe un String. | Para qué sirve / Destino: Permite modificar el estatus operativo del usuario en la entidad.
        this.estado = estado; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad estado del objeto Usuario.
    }

    public String getDireccion() { // Qué hace: Declara el método getter getDireccion que retorna un String. | Para qué sirve / Destino: Permite consultar la dirección física del usuario para envíos o domicilios desde la lógica de negocio.
        return direccion; // Qué hace: Retorna el valor del atributo direccion. | Para qué sirve / Destino: Proporciona la dirección postal hacia los servicios logísticos o controladores.
    }

    public void setDireccion(String direccion) { // Qué hace: Declara el método setter setDireccion que recibe un String. | Para qué sirve / Destino: Permite asignar la dirección física del usuario en la entidad.
        this.direccion = direccion; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad direccion del objeto Usuario.
    }
}