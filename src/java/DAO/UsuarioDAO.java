package DAO; // Qué hace: Declara el paquete DAO. | Para qué sirve / Destino: Ubica la clase lógicamente en la Capa de Acceso a Datos de la arquitectura del proyecto.

// Importar la clase de conexión para conectarse a la base de datos MySQL
import Modelo.Config.Conexion; // Qué hace: Importa la clase de conexión. | Para qué sirve / Destino: Permite obtener instancias de conexión JDBC hacia la base de datos MySQL.
// Importar la entidad Usuario para mapear registros relacionales a objetos Java
import Modelo.Entidades.Usuario; // Qué hace: Importa la entidad Usuario. | Para qué sirve / Destino: Facilita el mapeo de los datos tabulares de MySQL a objetos orientados a objetos en la capa de modelo.

// Importar interfaces necesarias de JDBC para interactuar con la base de datos relacional
import java.sql.Connection; // Qué hace: Importa la interfaz Connection. | Para qué sirve / Destino: Gestiona la sesión de conexión física con MySQL.
import java.sql.PreparedStatement; // Qué hace: Importa la interfaz PreparedStatement. | Para qué sirve / Destino: Ejecuta sentencias SQL parametrizadas para prevenir inyecciones SQL hacia MySQL.
import java.sql.ResultSet; // Qué hace: Importa la interfaz ResultSet. | Para qué sirve / Destino: Almacena y recorre los resultados devueltos por las consultas SELECT desde MySQL.
import java.sql.SQLException; // Qué hace: Importa la clase SQLException. | Para qué sirve / Destino: Captura y gestiona errores originados en las operaciones con la base de datos MySQL.

// Importar clases de colecciones estándar de Java para almacenar las listas de registros
import java.util.ArrayList; // Qué hace: Importa la clase ArrayList. | Para qué sirve / Destino: Permite instanciar listas dinámicas en memoria para almacenar múltiples entidades Usuario.
import java.util.List; // Qué hace: Importa la interfaz List. | Para qué sirve / Destino: Define el contrato de colecciones devuelto hacia los controladores y la lógica de negocio.

/**
 * Clase de Acceso a Datos (DAO) para la entidad Usuario.
 * Encargada de realizar operaciones CRUD y autenticación en la tabla 'usuario' de MySQL.
 */
public class UsuarioDAO { // Qué hace: Declara la clase pública UsuarioDAO. | Para qué sirve / Destino: Actúa como el componente DAO central para procesar operaciones de persistencia de usuarios hacia MySQL.

    /**
     * Constructor por defecto.
     * Se ha removido la ejecución de sentencias DDL (ALTER TABLE) para evitar fallos de
     * seguridad y permisos denegados cuando se usan roles de base de datos restringidos.
     */
    public UsuarioDAO() { // Qué hace: Define el constructor vacío de la clase. | Para qué sirve / Destino: Permite instanciar el DAO desde los controladores sin requerir parámetros iniciales.
        // Inicialización básica del DAO (sin operaciones DDL dinámicas)
    }

    /**
     * Recupera y lista todos los usuarios registrados en la tabla 'usuario'.
     *
     * @return Una lista de objetos de tipo Usuario.
     */
    public List<Usuario> listar() { // Qué hace: Declara el método público listar que retorna una lista de Usuario. | Para qué sirve / Destino: Comunica la solicitud de consulta general hacia los controladores.
        // Inicializar la lista de usuarios
        List<Usuario> lista = new ArrayList<>(); // Qué hace: Instancia un ArrayList vacío. | Para qué sirve / Destino: Almacena temporalmente los objetos Usuario mapeados para retornarlos a la capa superior.
        // Consulta SQL parametrizada para seleccionar todas las columnas de usuario
        String sql = "SELECT * FROM usuario"; // Qué hace: Define la cadena de consulta SQL. | Para qué sirve / Destino: Contiene la instrucción SELECT para extraer todos los registros de la tabla 'usuario' en MySQL.

        // Cargar conexión, preparar sentencia y ejecutar la consulta
        try (Connection con = Conexion.getConnection();  // Qué hace: Obtiene la conexión activa a MySQL. | Para qué sirve / Destino: Establece el canal de comunicación físico con la base de datos MySQL.
             PreparedStatement ps = con.prepareStatement(sql);  // Qué hace: Prepara la sentencia SQL. | Para qué sirve / Destino: Envía la consulta a la base de datos para su precompilación.
             ResultSet rs = ps.executeQuery()) { // Qué hace: Ejecuta la consulta y retorna el resultado. | Para qué sirve / Destino: Obtiene el conjunto de registros tabulares devueltos por MySQL.

            // Iterar por cada una de las filas retornadas
            while (rs.next()) { // Qué hace: Evalúa si existe una fila siguiente en el ResultSet. | Para qué sirve / Destino: Recorre iterativamente cada registro obtenido de MySQL.
                // Instanciar un nuevo objeto Usuario para mapear la fila
                Usuario u = new Usuario(); // Qué hace: Crea una nueva instancia de la entidad Usuario. | Para qué sirve / Destino: Contenedor en memoria para poblar con los datos de la fila actual de MySQL.
                // Asignar identificador del usuario
                u.setIdUsuario(rs.getString("id_usuario")); // Qué hace: Extrae y asigna el id_usuario. | Para qué sirve / Destino: Mapea la columna relacional de MySQL a la propiedad del objeto Usuario.
                // Asignar el ID de rol asignado
                u.setIdRol(rs.getString("id_rol")); // Qué hace: Extrae y asigna el id_rol. | Para qué sirve / Destino: Mapea la clave foránea de rol al objeto Usuario para uso en la lógica.
                // Asignar el nombre del usuario
                u.setName(rs.getString("name")); // Qué hace: Extrae y asigna el name. | Para qué sirve / Destino: Mapea el nombre del usuario hacia la entidad Java.
                // Asignar el correo electrónico
                u.setEmail(rs.getString("email")); // Qué hace: Extrae y asigna el email. | Para qué sirve / Destino: Mapea el correo electrónico al objeto Usuario.
                // Asignar el número de teléfono
                u.setPhone(rs.getString("phone")); // Qué hace: Extrae y asigna el phone. | Para qué sirve / Destino: Mapea el número telefónico al objeto Usuario.
                // Asignar la contraseña (guardada en texto plano actualmente)
                u.setPassword(rs.getString("password")); // Qué hace: Extrae y asigna la contraseña. | Para qué sirve / Destino: Mapea el campo password hacia la entidad Usuario.
                // Asignar el estado de la cuenta (status)
                u.setEstado(rs.getString("status")); // Qué hace: Extrae y asigna el estado. | Para qué sirve / Destino: Mapea el estado lógico del usuario (Activo/Inactivo) al objeto.
                // Asignar la dirección física
                u.setDireccion(rs.getString("direccion")); // Qué hace: Extrae y asigna la dirección. | Para qué sirve / Destino: Mapea la dirección física al objeto Usuario.
                // Agregar el usuario a la lista de resultados
                lista.add(u); // Qué hace: Añade el objeto Usuario poblado a la lista. | Para qué sirve / Destino: Acumula las entidades mapeadas para enviarlas de regreso al controlador.
            }
        } catch (SQLException e) { // Qué hace: Captura excepciones de tipo SQL. | Para qué sirve / Destino: Maneja errores de comunicación o sintaxis ocurridos en MySQL.
            // Registrar error de consulta SQL en consola
            System.err.println("ERROR SQL EN LISTAR USUARIOS: " + e.getMessage()); // Qué hace: Imprime el mensaje de error en la consola de errores. | Para qué sirve / Destino: Facilita la depuración técnica en el servidor Tomcat.
            e.printStackTrace(); // Qué hace: Imprime la traza completa del error. | Para qué sirve / Destino: Rastrea el origen exacto de la excepción JDBC.
        }
        // Retornar la lista obtenida
        return lista; // Qué hace: Retorna la colección de usuarios. | Para qué sirve / Destino: Envía la lista completa de entidades procesadas hacia el controlador o capa superior.
    }

    /**
     * Registra un nuevo usuario en la base de datos generando automáticamente
     * un identificador incremental con formato 'USR-XXX'.
     *
     * @param u Objeto Usuario con la información a insertar.
     * @return true si el registro fue exitoso, false en caso contrario.
     */
    public boolean registrar(Usuario u) { // Qué hace: Declara el método registrar que recibe un Usuario y retorna un booleano. | Para qué sirve / Destino: Orquesta la inserción de un nuevo usuario interactuando con MySQL.
        // Consulta SQL para obtener el último ID de usuario registrado ordenando numéricamente para evitar colisiones
        String queryMaxId = "SELECT id_usuario FROM usuario ORDER BY CAST(SUBSTRING(id_usuario, 5) AS UNSIGNED) DESC LIMIT 1"; // Qué hace: Define la consulta SQL para obtener el ID máximo. | Para qué sirve / Destino: Consulta a MySQL el último correlativo numérico de la tabla usuario.
        // Identificador por defecto si la tabla no contiene registros
        String nextId = "USR-001"; // Qué hace: Inicializa el ID por defecto. | Para qué sirve / Destino: Asigna el primer identificador base en caso de que la tabla de MySQL esté vacía.
        
        // Ejecutar consulta para obtener el último ID
        try (Connection con = Conexion.getConnection(); // Qué hace: Establece la conexión física con MySQL. | Para qué sirve / Destino: Abre el canal JDBC para consultar el ID máximo.
             PreparedStatement psMax = con.prepareStatement(queryMaxId); // Qué hace: Prepara la sentencia para el ID máximo. | Para qué sirve / Destino: Envía la consulta SQL de ordenamiento a MySQL.
             ResultSet rsMax = psMax.executeQuery()) { // Qué hace: Ejecuta la consulta y obtiene el resultado. | Para qué sirve / Destino: Recupera el registro con el identificador más alto desde MySQL.
            if (rsMax.next()) { // Qué hace: Verifica si se encontró un registro previo. | Para qué sirve / Destino: Evalúa la existencia de IDs anteriores en la base de datos.
                String maxId = rsMax.getString("id_usuario"); // Qué hace: Obtiene el valor del ID máximo. | Para qué sirve / Destino: Extrae el identificador en texto de la base de datos.
                // Verificar que cumpla con el prefijo esperado
                if (maxId != null && maxId.startsWith("USR-")) { // Qué hace: Valida el formato del ID obtenido. | Para qué sirve / Destino: Asegura que el identificador extraído de MySQL cumpla con la nomenclatura estándar.
                    try {
                        // Extraer la porción numérica del ID, incrementarla en 1
                        int num = Integer.parseInt(maxId.substring(4)); // Qué hace: Convierte la parte numérica del ID a entero. | Para qué sirve / Destino: Permite realizar la operación matemática de incremento.
                        // Formatear el nuevo ID de usuario
                        nextId = String.format("USR-%03d", num + 1); // Qué hace: Genera el nuevo ID formateado con ceros a la izquierda. | Para qué sirve / Destino: Produce el siguiente identificador correlativo (ej: USR-002).
                    } catch (NumberFormatException e) { // Qué hace: Captura errores de formato numérico. | Para qué sirve / Destino: Evita fallos de ejecución si el formato en MySQL difiere del esperado.
                        System.err.println("Error parseando el id de usuario máximo: " + e.getMessage()); // Qué hace: Registra el error de parseo en la consola. | Para qué sirve / Destino: Facilita la depuración de datos inconsistentes en MySQL.
                    }
                }
            }
        } catch (SQLException e) { // Qué hace: Captura excepciones SQL del bloque de ID máximo. | Para qué sirve / Destino: Maneja errores de lectura en la base de datos MySQL.
            System.err.println("Error obteniendo el ID máximo de usuario: " + e.getMessage()); // Qué hace: Muestra el error SQL en la consola. | Para qué sirve / Destino: Notifica fallos al intentar calcular el consecutivo en MySQL.
        }

        // Asignar el nuevo ID generado al objeto de usuario
        u.setIdUsuario(nextId); // Qué hace: Establece el ID calculado en el objeto Usuario. | Para qué sirve / Destino: Actualiza la entidad en memoria antes de persistirla en MySQL.

        // Hashear la contraseña usando SHA-256 antes de insertarla para mayor seguridad
        if (u.getPassword() != null) { // Qué hace: Valida que la contraseña no sea nula. | Para qué sirve / Destino: Previene errores de ejecución al intentar cifrar valores nulos.
            u.setPassword(Modelo.Config.SecurityUtils.hashPassword(u.getPassword())); // Qué hace: Aplica hash SHA-256 a la contraseña. | Para qué sirve / Destino: Cifra la contraseña mediante utilidades de seguridad antes de enviarla a MySQL.
        }

        // Consulta SQL parametrizada para realizar la inserción
        String sql = "INSERT INTO usuario (id_usuario, id_rol, name, email, phone, password, status, direccion, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"; // Qué hace: Define la sentencia SQL INSERT parametrizada. | Para qué sirve / Destino: Especifica la operación de inserción segura hacia la tabla usuario en MySQL.

        // Ejecutar inserción en la base de datos
        try (Connection con = Conexion.getConnection(); // Qué hace: Conecta con la base de datos MySQL. | Para qué sirve / Destino: Abre una nueva conexión JDBC para realizar la escritura.
             PreparedStatement ps = con.prepareStatement(sql)) { // Qué hace: Prepara la sentencia INSERT. | Para qué sirve / Destino: Compila la instrucción SQL para protegerla contra inyecciones SQL en MySQL.

            // Configurar los parámetros de la consulta INSERT
            ps.setString(1, u.getIdUsuario()); // Qué hace: Asigna el id_usuario al primer parámetro. | Para qué sirve / Destino: Envía el identificador generado a la columna correspondiente en MySQL.
            ps.setString(2, u.getIdRol()); // Qué hace: Asigna el id_rol al segundo parámetro. | Para qué sirve / Destino: Envía el rol del usuario a la base de datos MySQL.
            ps.setString(3, u.getName()); // Qué hace: Asigna el name al tercer parámetro. | Para qué sirve / Destino: Envía el nombre del usuario hacia la tabla en MySQL.
            ps.setString(4, u.getEmail()); // Qué hace: Asigna el email al cuarto parámetro. | Para qué sirve / Destino: Envía el correo electrónico del usuario a MySQL.
            ps.setString(5, u.getPhone()); // Qué hace: Asigna el phone al quinto parámetro. | Para qué sirve / Destino: Envía el número telefónico hacia la base de datos MySQL.
            ps.setString(6, u.getPassword()); // Qué hace: Asigna el password cifrado al sexto parámetro. | Para qué sirve / Destino: Envía la contraseña segura a la tabla en MySQL.
            ps.setString(7, u.getEstado()); // Qué hace: Asigna el status al séptimo parámetro. | Para qué sirve / Destino: Envía el estado lógico del usuario hacia MySQL.
            ps.setString(8, u.getDireccion()); // Qué hace: Asigna la direccion al octavo parámetro. | Para qué sirve / Destino: Envía la dirección física a la base de datos MySQL.
            // Asignar la fecha actual del sistema
            ps.setDate(9, new java.sql.Date(System.currentTimeMillis())); // Qué hace: Asigna la fecha actual del sistema al noveno parámetro. | Para qué sirve / Destino: Registra la fecha de creación en la columna created_at de MySQL.

            // Ejecutar consulta y retornar true si se insertó el registro
            return ps.executeUpdate() > 0; // Qué hace: Ejecuta el INSERT y evalúa filas afectadas. | Para qué sirve / Destino: Confirma la inserción exitosa en MySQL retornando true hacia el controlador.
        } catch (SQLException e) { // Qué hace: Captura errores de tipo SQLException en la inserción. | Para qué sirve / Destino: Maneja violaciones de restricciones o fallos de conexión con MySQL.
            System.err.println("ERROR SQL EN REGISTRO DE USUARIO: " + e.getMessage()); // Qué hace: Registra el error en la consola. | Para qué sirve / Destino: Muestra detalles del fallo de inserción para depuración.
            e.printStackTrace(); // Qué hace: Imprime la pila de excepciones. | Para qué sirve / Destino: Rastrea el origen técnico del error en JDBC.
        }

        return false; // Qué hace: Retorna false si ocurre un error. | Para qué sirve / Destino: Indica al controlador que el proceso de registro en MySQL falló.
    }

    /**
     * Valida las credenciales de inicio de sesión de un usuario.
     *
     * @param email Correo electrónico ingresado.
     * @param password Contraseña ingresada.
     * @return El objeto Usuario correspondiente si las credenciales sąn válidas, null de lo contrario.
     */
    public Usuario login(String email, String password) { // Qué hace: Declara el método login que valida credenciales y retorna un objeto Usuario. | Para qué sirve / Destino: Gestiona el proceso de autenticación interactuando con MySQL.
        // Hashear la contraseña ingresada para la comparación segura
        String hashedPassword = Modelo.Config.SecurityUtils.hashPassword(password); // Qué hace: Cifra la contraseña ingresada. | Para qué sirve / Destino: Genera el hash SHA-256 para compararlo de forma segura con los registros en MySQL.
        
        // Consulta SQL para buscar coincidencia de correo y contraseña (ya sea hasheada o texto plano para legacy)
        String sql = "SELECT * FROM usuario WHERE email = ? AND (password = ? OR password = ?)"; // Qué hace: Define la consulta SELECT de autenticación. | Para qué sirve / Destino: Busca coincidencias en MySQL evaluando correo y contraseña (cifrada o texto plano).

        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) { // Qué hace: Abre la conexión y prepara la sentencia SQL. | Para qué sirve / Destino: Conecta con MySQL para ejecutar la validación de credenciales.

            // Asignar los valores a los parámetros del query
            ps.setString(1, email); // Qué hace: Asigna el correo electrónico al primer marcador. | Para qué sirve / Destino: Filtra la búsqueda por el correo ingresado en la base de datos.
            ps.setString(2, hashedPassword); // Qué hace: Asigna la contraseña cifrada al segundo marcador. | Para qué sirve / Destino: Compara contra contraseñas seguras almacenadas en MySQL.
            ps.setString(3, password); // Qué hace: Asigna la contraseña en texto plano al tercer marcador. | Para qué sirve / Destino: Permite compatibilidad temporal con registros antiguos en MySQL.

            // Ejecutar consulta de selección
            try (ResultSet rs = ps.executeQuery()) { // Qué hace: Ejecuta la consulta de login y obtiene el resultado. | Para qué sirve / Destino: Recupera la coincidencia de usuario desde MySQL si las credenciales son correctas.
                // Si existe coincidencia
                if (rs.next()) { // Qué hace: Verifica si se encontró el usuario. | Para qué sirve / Destino: Confirma que las credenciales son válidas en la base de datos.
                    // Instanciar y rellenar los datos del usuario autenticado
                    Usuario usuario = new Usuario(); // Qué hace: Instancia la entidad Usuario. | Para qué sirve / Destino: Contenedor para devolver los datos del usuario autenticado al controlador.
                    usuario.setIdUsuario(rs.getString("id_usuario")); // Qué hace: Asigna el ID del usuario autenticado. | Para qué sirve / Destino: Mapea el id_usuario desde MySQL al objeto Java.
                    usuario.setIdRol(rs.getString("id_rol")); // Qué hace: Asigna el rol del usuario autenticado. | Para qué sirve / Destino: Mapea el id_rol desde MySQL para control de sesiones.
                    usuario.setName(rs.getString("name")); // Qué hace: Asigna el nombre del usuario autenticado. | Para qué sirve / Destino: Mapea el nombre desde MySQL al objeto.
                    usuario.setEmail(rs.getString("email")); // Qué hace: Asigna el correo del usuario autenticado. | Para qué sirve / Destino: Mapea el correo electrónico desde MySQL.
                    usuario.setPhone(rs.getString("phone")); // Qué hace: Asigna el teléfono del usuario autenticado. | Para qué sirve / Destino: Mapea el número telefónico desde la base de datos.
                    usuario.setEstado(rs.getString("status")); // Qué hace: Asigna el estado del usuario autenticado. | Para qué sirve / Destino: Mapea el estado lógico de la cuenta desde MySQL.
                    usuario.setDireccion(rs.getString("direccion")); // Qué hace: Asigna la dirección del usuario autenticado. | Para qué sirve / Destino: Mapea la dirección física desde MySQL al objeto.
                    return usuario; // Qué hace: Retorna la entidad Usuario poblada. | Para qué sirve / Destino: Envía el objeto de sesión válido hacia el controlador de autenticación.
                }
            }

        } catch (SQLException e) { // Qué hace: Captura excepciones SQL durante el login. | Para qué sirve / Destino: Maneja errores de conexión o ejecución de la consulta en MySQL.
            System.err.println("Error login en UsuarioDAO: " + e.getMessage()); // Qué hace: Imprime el error de login en la consola. | Para qué sirve / Destino: Facilita la identificación de problemas de autenticación.
        }
        return null; // Qué hace: Retorna null si la autenticación falla. | Para qué sirve / Destino: Indica al controlador que las credenciales son incorrectas o hubo un error.
    }

    /**
     * Actualiza la información básica del perfil de un usuario.
     *
     * @param u Objeto Usuario con la nueva información.
     * @return true si la actualización fue exitosa, false de lo contrario.
     */
    public boolean actualizarUsuario(Usuario u) { // Qué hace: Declara el método actualizarUsuario que recibe un Usuario y retorna un booleano. | Para qué sirve / Destino: Gestiona la modificación de datos de perfil interactuando con MySQL.
        // Sentencia SQL parametrizada de actualización de perfil
        String sql = "UPDATE usuario SET name = ?, email = ?, phone = ?, direccion = ? WHERE id_usuario = ?"; // Qué hace: Define la sentencia SQL UPDATE. | Para qué sirve / Destino: Especifica la actualización de campos generales de usuario en la tabla de MySQL.

        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) { // Qué hace: Conecta con MySQL y prepara el UPDATE. | Para qué sirve / Destino: Abre el canal JDBC para modificar los datos en la base de datos.

            // Asignar parámetros a la sentencia UPDATE
            ps.setString(1, u.getName()); // Qué hace: Asigna el nuevo nombre al primer marcador. | Para qué sirve / Destino: Actualiza el nombre del usuario en MySQL.
            ps.setString(2, u.getEmail()); // Qué hace: Asigna el nuevo correo al segundo marcador. | Para qué sirve / Destino: Actualiza el correo electrónico en la base de datos.
            ps.setString(3, u.getPhone()); // Qué hace: Asigna el nuevo teléfono al tercer marcador. | Para qué sirve / Destino: Actualiza el número telefónico en MySQL.
            ps.setString(4, u.getDireccion()); // Qué hace: Asigna la nueva dirección al cuarto marcador. | Para qué sirve / Destino: Actualiza la dirección física en la base de datos.
            ps.setString(5, u.getIdUsuario()); // Qué hace: Asigna el id_usuario al quinto marcador (cláusula WHERE). | Para qué sirve / Destino: Filtra qué usuario específico será modificado en MySQL.

            // Retornar true si afectó al menos una fila
            return ps.executeUpdate() > 0; // Qué hace: Ejecuta la actualización y evalúa filas afectadas. | Para qué sirve / Destino: Confirma el éxito de la modificación en MySQL retornando true al controlador.
        } catch (SQLException e) { // Qué hace: Captura excepciones SQL en la actualización. | Para qué sirve / Destino: Maneja errores de ejecución o restricciones en MySQL.
            System.err.println("ERROR SQL AL ACTUALIZAR USUARIO: " + e.getMessage()); // Qué hace: Registra el error SQL en la consola. | Para qué sirve / Destino: Facilita la depuración de fallos de actualización.
            e.printStackTrace(); // Qué hace: Imprime la traza completa de la excepción. | Para qué sirve / Destino: Rastrea el origen del error JDBC.
        }
        return false; // Qué hires: Retorna false si la actualización falla. | Para qué sirve / Destino: Informa al controlador que el cambio de perfil no pudo completarse.
    }

    /**
     * Realiza el cambio de contraseña de un usuario validando primero su contraseña actual.
     *
     * @param idUsuario Identificador del usuario.
     * @param currentPassword Contraseña actual.
     * @param newPassword Nueva contraseña a establecer.
     * @return true si la contraseña se actualizó correctamente, false si la contraseña actual es incorrecta o falló.
     */
    public boolean cambiarPassword(String idUsuario, String currentPassword, String newPassword) { // Qué hace: Declara el método cambiarPassword que valida y actualiza la contraseña, retornando un booleano. | Para qué sirve / Destino: Controla el cambio seguro de credenciales con validación previa en MySQL.
        String hashedCurrent = Modelo.Config.SecurityUtils.hashPassword(currentPassword); // Qué hace: Cifra la contraseña actual ingresada. | Para qué sirve / Destino: Genera el hash SHA-256 para verificar la identidad en MySQL.
        String hashedNew = Modelo.Config.SecurityUtils.hashPassword(newPassword); // Qué hace: Cifra la nueva contraseña. | Para qué sirve / Destino: Genera el hash seguro que será almacenado en la base de datos.

        // Consulta para verificar la contraseña actual (admite texto plano temporalmente por compatibilidad)
        String verificarSql = "SELECT * FROM usuario WHERE id_usuario = ? AND (password = ? OR password = ?)"; // Qué hace: Define la consulta SELECT de verificación. | Para qué sirve / Destino: Comprueba en MySQL si la contraseña actual proporcionada es correcta.
        // Sentencia para actualizar a la nueva contraseña
        String updateSql = "UPDATE usuario SET password = ? WHERE id_usuario = ?"; // Qué hace: Define la sentencia UPDATE para la nueva contraseña. | Para qué sirve / Destino: Especifica la modificación de la contraseña en la tabla de MySQL.

        try (Connection con = Conexion.getConnection()) { // Qué hace: Abre la conexión única para ambas operaciones. | Para qué sirve / Destino: Establece la sesión JDBC con MySQL.

            // Ejecutar la verificación inicial
            try (PreparedStatement verificarPs = con.prepareStatement(verificarSql)) { // Qué hace: Prepara la sentencia de verificación. | Para qué sirve / Destino: Envía el query de validación a MySQL.
                verificarPs.setString(1, idUsuario); // Qué hace: Asigna el id_usuario al primer parámetro. | Para qué sirve / Destino: Filtra por el usuario objetivo en MySQL.
                verificarPs.setString(2, hashedCurrent); // Qué hace: Asigna el hash de la contraseña actual al segundo parámetro. | Para qué sirve / Destino: Compara con contraseñas seguras en la base de datos.
                verificarPs.setString(3, currentPassword); // Qué hace: Asigna la contraseña actual en texto plano al tercer parámetro. | Para qué sirve / Destino: Soporta compatibilidad con registros antiguos en MySQL.
                try (ResultSet rs = verificarPs.executeQuery()) { // Qué hace: Ejecuta la consulta de verificación y obtiene resultados. | Para qué sirve / Destino: Evalúa si existe coincidencia de la contraseña actual en MySQL.
                    // Si no coincide la contraseña actual, abortar el proceso retornando falso
                    if (!rs.next()) { // Qué hace: Evalúa si no se encontró coincidencia. | Para qué sirve / Destino: Determina si la contraseña actual ingresada es incorrecta.
                        return false; // Qué hace: Retorna false de inmediato. | Para qué sirve / Destino: Aborta el proceso de cambio de contraseña e informa al controlador.
                    }
                }
            }

            // Ejecutar la actualización de la contraseña
            try (PreparedStatement updatePs = con.prepareStatement(updateSql)) { // Qué hace: Prepara la sentencia UPDATE de contraseña. | Para qué sirve / Destino: Envía la instrucción de cambio de clave hacia MySQL.
                updatePs.setString(1, hashedNew); // Qué hace: Asigna el hash de la nueva contraseña al primer parámetro. | Para qué sirve / Destino: Guarda la nueva contraseña cifrada en la base de datos.
                updatePs.setString(2, idUsuario); // Qué hace: Asigna el id_usuario al segundo parámetro. | Para qué sirve / Destino: Filtra qué usuario actualizará su clave en MySQL.
                return updatePs.executeUpdate() > 0; // Qué hace: Ejecuta el UPDATE y retorna true si fue exitoso. | Para qué sirve / Destino: Confirma la actualización de contraseña en MySQL hacia el controlador.
            }
        } catch (SQLException e) { // Qué hace: Captura excepciones SQL en el cambio de contraseña. | Para qué sirve / Destino: Maneja errores de conexión o ejecución en MySQL.
            System.err.println("ERROR SQL AL CAMBIAR PASSWORD: " + e.getMessage()); // Qué hace: Registra el error SQL en la consola. | Para qué sirve / Destino: Facilita la depuración de fallos de seguridad.
            e.printStackTrace(); // Qué hace: Imprime la traza completa de la excepción. | Para qué sirve / Destino: Rastrea el origen técnico del error JDBC.
        }
        return false; // Qué hace: Retorna false si ocurre un error general. | Para qué sirve / Destino: Informa al controlador que el cambio de clave no pudo realizarse.
    }

    /**
     * Obtiene los datos de un usuario por su identificador único.
     *
     * @param idUsuario Identificador del usuario.
     * @return Objeto Usuario mapeado, o null si no se encuentra.
     */
   public Usuario obtenerUsuarioPorId(String idUsuario) { // Qué hace: Declara el método obtenerUsuarioPorId que recibe un ID y retorna un objeto Usuario. | Para qué sirve / Destino: Busca y mapea un usuario específico consultando la base de datos MySQL.
        // Consulta SQL parametrizada para buscar los datos del usuario coincidente en la tabla 'usuario' de MySQL.
        String sql = "SELECT * FROM usuario WHERE id_usuario = ?"; // Qué hace: Define la consulta SELECT filtrada por ID. | Para qué sirve / Destino: Especifica la búsqueda de un usuario puntual en la tabla de MySQL.
        // Bloque try-with-resources que administra automáticamente el ciclo de vida de la conexión física y el PreparedStatement.
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) { // Qué hace: Abre conexión y prepara el query. | Para qué sirve / Destino: Establece el enlace JDBC seguro con MySQL.
            // Asignar el parámetro idUsuario al primer marcador de posición (?) del PreparedStatement para mitigar Inyección SQL.
            ps.setString(1, idUsuario); // Qué hace: Asigna el ID al marcador de posición. | Para qué sirve / Destino: Evita inyecciones SQL al consultar MySQL.
            
            // Ejecutar la consulta SQL SELECT y recuperar el conjunto de resultados ResultSet desde MySQL.
            try (ResultSet rs = ps.executeQuery()) { // Qué hace: Ejecuta la consulta y obtiene el ResultSet. | Para qué sirve / Destino: Recupera el registro coincidente desde la base de datos MySQL.
                // Evaluar si existe al menos una fila coincidente en el ResultSet.
                if (rs.next()) { // Qué hace: Verifica si la consulta arrojó resultados. | Para qué sirve / Destino: Comprueba que el usuario buscado existe en la base de datos.
                    // Instanciar el objeto de tipo Modelo.Entidades.Usuario para el mapeo relacional.
                    Usuario usuario = new Usuario(); // Qué hace: Instancia la entidad Usuario. | Para qué sirve / Destino: Contenedor en memoria para poblar con los datos de MySQL.
                    
                    // Asignar el valor del campo 'id_usuario' al objeto Java.
                    usuario.setIdUsuario(rs.getString("id_usuario")); // Qué hace: Asigna el ID recuperado. | Para qué sirve / Destino: Mapea la columna id_usuario al objeto Java.
                    // Asignar el valor de la clave foránea 'id_rol' para el control posterior de visibilidad en el Servlet.
                    usuario.setIdRol(rs.getString("id_rol")); // Qué hace: Asigna el rol recuperado. | Para qué sirve / Destino: Mapea el id_rol al objeto Usuario para uso en la interfaz web.
                    // Asignar el nombre amigable de registro del usuario.
                    usuario.setName(rs.getString("name")); // Qué hace: Asigna el nombre recuperado. | Para qué sirve / Destino: Mapea el nombre del usuario hacia la entidad.
                    // Asignar la dirección de correo electrónico registrada.
                    usuario.setEmail(rs.getString("email")); // Qué hace: Asigna el correo recuperado. | Para qué sirve / Destino: Mapea el correo electrónico al objeto Java.
                    // Asignar el teléfono de contacto del usuario.
                    usuario.setPhone(rs.getString("phone")); // Qué hace: Asigna el teléfono recuperado. | Para qué sirve / Destino: Mapea el número telefónico a la entidad Usuario.
                    // Asignar el estado lógico ('Activo' / 'Inactivo') del usuario.
                    usuario.setEstado(rs.getString("status")); // Qué hace: Asigna el estado recuperado. | Para qué sirve / Destino: Mapea el estado lógico de la cuenta al objeto.
                    // Asignar la dirección física de domicilio.
                    usuario.setDireccion(rs.getString("direccion")); // Qué hace: Asigna la dirección recuperada. | Para qué sirve / Destino: Mapea la dirección física al objeto Usuario.
                    
                    // Retornar el objeto de entidad completamente rellenado con datos del registro MySQL.
                    return usuario; // Qué hace: Retorna la entidad Usuario poblada. | Para qué sirve / Destino: Envía el objeto de usuario encontrado hacia el controlador solicitante.
                }
            }
        } catch (SQLException e) { // Qué hace: Captura excepciones SQL en la búsqueda por ID. | Para qué sirve / Destino: Maneja errores de consulta en la base de datos MySQL.
            // Registrar errores en el flujo de consulta JDBC en la salida de error del sistema Tomcat.
            System.err.println("ERROR SQL AL OBTENER USUARIO POR ID: " + e.getMessage()); // Qué hace: Muestra el error en la consola de sistema. | Para qué sirve / Destino: Facilita el diagnóstico de fallos en el servidor.
            e.printStackTrace(); // Qué hace: Imprime la traza de la excepción. | Para qué sirve / Destino: Permite rastrear el origen exacto del fallo JDBC.
        }
        // Retornar null en caso de no encontrarse coincidencia de usuario o error técnico en la ejecución.
        return null; // Qué hace: Retorna null si no hay resultados o ocurre un error. | Para qué sirve / Destino: Informa al controlador que el usuario consultado no existe en MySQL.
    }

    /**
     * Verifica si un correo electrónico ya está registrado en la base de datos.
     *
     * @param email Correo electrónico a comprobar.
     * @return true si el correo existe, false de lo contrario.
     */
    public boolean existsEmail(String email) { // Qué hace: Declara existsEmail que recibe un correo y retorna un booleano. | Para qué sirve / Destino: Valida duplicidad de correos electrónicos consultando la base de datos MySQL.
        // Consulta agregada para contar registros con el correo
        String sql = "SELECT COUNT(*) FROM usuario WHERE email = ?"; // Qué hace: Define la consulta SELECT COUNT. | Para qué sirve / Destino: Cuenta cuántos usuarios tienen asignado el correo especificado en MySQL.
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) { // Qué hace: Conecta con MySQL y prepara el query. | Para qué sirve / Destino: Abre el canal JDBC para la validación de unicidad.
            ps.setString(1, email); // Qué hace: Asigna el correo al marcador de posición. | Para qué sirve / Destino: Filtra la cuenta por el correo recibido de manera segura.
            try (ResultSet rs = ps.executeQuery()) { // Qué hace: Ejecuta la consulta de conteo y obtiene resultados. | Para qué sirve / Destino: Recupera el número total de coincidencias desde MySQL.
                if (rs.next()) { // Qué hace: Verifica si hay registros en el resultado. | Para qué sirve / Destino: Evalúa el resultado numérico devuelto por la base de datos.
                    return rs.getInt(1) > 0; // Qué hace: Retorna true si el conteo es mayor a cero. | Para qué sirve / Destino: Informa al controlador si el correo ya está registrado en MySQL.
                }
            }
        } catch (SQLException e) { // Qué hace: Captura excepciones SQL en la validación de correo. | Para qué sirve / Destino: Maneja errores de consulta en la base de datos MySQL.
            System.err.println("Error comprobando existencia del correo: " + e.getMessage()); // Qué hace: Imprime el error en la consola. | Para qué sirve / Destino: Facilita el diagnóstico de fallos en validaciones.
        }
        return false; // Qué hace: Retorna false por defecto o si ocurre un error. | Para qué sirve / Destino: Indica al controlador que el correo no existe o hubo un fallo técnico.
    }

    /**
     * Verifica si un número de teléfono ya está registrado en la base de datos.
     *
     * @param phone Número de teléfono a comprobar.
     * @return true si el teléfono existe, false de lo contrario.
     */
    public boolean existsPhone(String phone) { // Qué hace: Declara existsPhone que recibe un teléfono y retorna un booleano. | Para qué sirve / Destino: Valida unicidad de números telefónicos consultando la base de datos MySQL.
        String sql = "SELECT COUNT(*) FROM usuario WHERE phone = ?"; // Qué hace: Define la consulta SELECT COUNT para teléfonos. | Para qué sirve / Destino: Cuenta cuántos usuarios comparten el número telefónico en MySQL.
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) { // Qué hace: Conecta con MySQL y prepara la sentencia. | Para qué sirve / Destino: Establece el canal JDBC seguro para la validación.
            ps.setString(1, phone); // Qué hace: Asigna el teléfono al marcador de posición. | Para qué sirve / Destino: Filtra de forma segura por el número ingresado.
            try (ResultSet rs = ps.executeQuery()) { // Qué hace: Ejecuta la consulta y procesa el resultado. | Para qué sirve / Destino: Obtiene el conteo de registros coincidentes desde MySQL.
                if (rs.next()) { // Qué hace: Evalúa si existe un resultado numérico. | Para qué sirve / Destino: Comprueba la respuesta de la base de datos.
                    return rs.getInt(1) > 0; // Qué hace: Retorna true si el conteo es mayor a cero. | Para qué sirve / Destino: Informa al controlador si el teléfono ya se encuentra registrado.
                }
            }
        } catch (SQLException e) { // Qué hace: Captura excepciones SQL en la validación de teléfono. | Para qué sirve / Destino: Maneja errores de ejecución en MySQL.
            System.err.println("Error comprobando existencia del teléfono: " + e.getMessage()); // Qué hace: Registra el error en la consola del sistema. | Para qué sirve / Destino: Facilita la depuración técnica.
        }
        return false; // Qué hace: Retorna false por defecto o ante errores. | Para qué sirve / Destino: Indica al controlador que el teléfono no está duplicado o falló la consulta.
    }

    /**
     * Verifica si un número de teléfono ya está registrado en la base de datos
     * por otro usuario diferente al indicado por idUsuario.
     *
     * @param phone Número de teléfono a comprobar.
     * @param idUsuario Identificador del usuario a excluir de la validación.
     * @return true si el teléfono existe y pertenece a otro usuario, false de lo contrario.
     */
    public boolean existsPhoneExcludeUser(String phone, String idUsuario) { // Qué hace: Declara existsPhoneExcludeUser que valida teléfonos excluyendo un ID y retorna un booleano. | Para qué sirve / Destino: Permite validar unicidad de teléfono al actualizar perfiles propios en MySQL.
        String sql = "SELECT COUNT(*) FROM usuario WHERE phone = ? AND id_usuario != ?"; // Qué hace: Define la consulta SELECT COUNT con exclusión de ID. | Para qué sirve / Destino: Cuenta teléfonos repetidos en MySQL omitiendo al usuario actual.
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) { // Qué hace: Conecta con MySQL y prepara el query. | Para qué sirve / Destino: Abre la conexión JDBC segura para la validación cruzada.
            ps.setString(1, phone); // Qué hace: Asigna el teléfono al primer marcador. | Para qué sirve / Destino: Filtra por el número telefónico a comprobar.
            ps.setString(2, idUsuario); // Qué hace: Asigna el ID del usuario al segundo marcador. | Para qué sirve / Destino: Excluye al propio usuario de la regla de duplicidad en MySQL.
            try (ResultSet rs = ps.executeQuery()) { // Qué hace: Ejecuta la consulta y obtiene el resultado. | Para qué sirve / Destino: Recupera el conteo de coincidencias ajenas desde MySQL.
                if (rs.next()) { // Qué hace: Verifica si hay resultados en el ResultSet. | Para qué sirve / Destino: Evalúa el total numérico retornado por la base de datos.
                    return rs.getInt(1) > 0; // Qué hace: Retorna true si otro usuario tiene el teléfono. | Para qué sirve / Destino: Informa al controlador si el teléfono ya pertenece a otra cuenta.
                }
            }
        } catch (SQLException e) { // Qué hace: Captura excepciones SQL durante la validación con exclusión. | Para qué sirve / Destino: Maneja errores de consulta en la base de datos MySQL.
            System.err.println("Error comprobando existencia del teléfono con exclusión: " + e.getMessage()); // Qué hace: Registra el error en la consola. | Para qué sirve / Destino: Facilita el diagnóstico de fallos técnicos.
        }
        return false; // Qué hace: Retorna false por defecto o ante errores. | Para qué sirve / Destino: Indica al controlador que no hay conflicto de teléfono con otros usuarios.
    }

    /**
     * Restablece la contraseña de un usuario a partir de su correo electrónico.
     *
     * @param email Correo electrónico del usuario.
     * @param newPassword Nueva contraseña.
     * @return true si se restableció exitosamente, false de lo contrario.
     */
    public boolean resetPassword(String email, String newPassword) { // Qué hace: Declara resetPassword que actualiza la contraseña por correo y retorna un booleano. | Para qué sirve / Destino: Gestiona el restablecimiento de claves interactuando con MySQL.
        String hashedNew = Modelo.Config.SecurityUtils.hashPassword(newPassword); // Qué hace: Cifra la nueva contraseña. | Para qué sirve / Destino: Genera el hash seguro SHA-256 para el restablecimiento.
        // Sentencia SQL parametrizada de actualización de contraseña por correo
        String sql = "UPDATE usuario SET password = ? WHERE email = ?"; // Qué hace: Define la sentencia SQL UPDATE por correo. | Para qué sirve / Destino: Especifica la actualización de la contraseña filtrando por email en MySQL.
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) { // Qué hace: Conecta con MySQL y prepara el UPDATE. | Para qué sirve / Destino: Abre el canal JDBC seguro para modificar la contraseña.
            ps.setString(1, hashedNew); // Qué hace: Asigna el hash de la nueva contraseña al primer marcador. | Para qué sirve / Destino: Guarda la nueva clave cifrada en la base de datos.
            ps.setString(2, email); // Qué hace: Asigna el correo electrónico al segundo marcador. | Para qué sirve / Destino: Filtra qué usuario actualizará su contraseña en MySQL.
            return ps.executeUpdate() > 0; // Qué hace: Ejecuta la actualización y retorna true si fue exitosa. | Para qué sirve / Destino: Confirma el restablecimiento de clave en MySQL hacia el controlador.
        } catch (SQLException e) { // Qué hace: Captura excepciones SQL en el restablecimiento. | Para qué sirve / Destino: Maneja errores de ejecución o conexión en MySQL.
            System.err.println("Error restableciendo password por correo: " + e.getMessage()); // Qué hace: Registra el error en la consola de sistema. | Para qué sirve / Destino: Facilita el diagnóstico de fallos en recuperación de cuenta.
        }
        return false; // Qué hace: Retorna false si ocurre un error. | Para qué sirve / Destino: Informa al controlador que el restablecimiento de contraseña falló.
    }

    /**
     * Lista todos los usuarios con roles de Empleado (ROL-002) o Administrador (ROL-001).
     * Nota: En este método el campo 'idRol' del objeto Usuario es asignado intencionalmente
     * con el *nombre* amigable del rol (ej: 'Administrador' o 'Empleado') para facilitar
     * la serialización directa y presentación en el frontend.
     *
     * @return Una lista de usuarios administrativos u operativos.
     */
    public List<Usuario> listarEmpleados() { // Qué hace: Declara listarEmpleados que retorna una lista de Usuario. | Para qué sirve / Destino: Consulta y filtra específicamente personal administrativo y operativo desde MySQL.
        List<Usuario> lista = new ArrayList<>(); // Qué hace: Instancia un ArrayList para almacenar empleados. | Para qué sirve / Destino: Almacena temporalmente los objetos Usuario mapeados para la vista de gestión.
        // Consulta SQL con INNER JOIN para recuperar los datos de usuario
        String sql = "SELECT u.*, r.nombre AS rol_nombre FROM usuario u JOIN roles r ON u.id_rol = r.id_rol WHERE u.id_rol IN ('ROL-001', 'ROL-002')"; // Qué hace: Define la consulta SQL con JOIN entre usuario y roles. | Para qué sirve / Destino: Extrae información combinada de usuarios administradores y empleados desde MySQL.
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) { // Qué hace: Conecta, prepara y ejecuta la consulta multitabla. | Para qué sirve / Destino: Establece la sesión JDBC para extraer los datos de empleados.
            while (rs.next()) { // Qué hace: Recorre cada fila del ResultSet devuelto. | Para qué sirve / Destino: Itera sobre cada empleado encontrado en MySQL.
                Usuario u = new Usuario(); // Qué hace: Instancia un nuevo objeto Usuario. | Para qué sirve / Destino: Contenedor en memoria para mapear los datos del empleado actual.
                u.setIdUsuario(rs.getString("id_usuario")); // Qué hace: Asigna el identificador del empleado. | Para qué sirve / Destino: Mapea la columna id_usuario al objeto Java.
                
                // Mapear la clave primaria real del rol para evitar violaciones de clave foránea en escrituras
                u.setIdRol(rs.getString("id_rol")); // Qué hace: Asigna el id_rol del empleado. | Para qué sirve / Destino: Mapea la clave de rol al objeto Usuario para mantener integridad relacional.
                
                u.setName(rs.getString("name")); // Qué hace: Asigna el nombre del empleado. | Para qué sirve / Destino: Mapea el nombre hacia la entidad Java.
                u.setEmail(rs.getString("email")); // Qué hace: Asigna el correo del empleado. | Para qué sirve / Destino: Mapea el correo electrónico al objeto Usuario.
                u.setPhone(rs.getString("phone")); // Qué hace: Asigna el teléfono del empleado. | Para qué sirve / Destino: Mapea el número telefónico a la entidad.
                u.setPassword(rs.getString("password")); // Qué hace: Asigna la contraseña del empleado. | Para qué sirve / Destino: Mapea el campo password al objeto Usuario.
                u.setEstado(rs.getString("status")); // Qué hace: Asigna el estado del empleado. | Para qué sirve / Destino: Mapea el estado lógico de la cuenta al objeto.
                u.setDireccion(rs.getString("direccion")); // Qué hace: Asigna la dirección del empleado. | Para qué sirve / Destino: Mapea la dirección física a la entidad Usuario.
                lista.add(u); // Qué hace: Agrega el empleado poblado a la lista. | Para qué sirve / Destino: Acumula las entidades de empleados para enviarlas al controlador.
            }
        } catch (SQLException e) { // Qué hace: Captura excepciones SQL al listar empleados. | Para qué sirve / Destino: Maneja errores de consulta en las tablas asociadas de MySQL.
            System.err.println("Error al listar empleados en DAO: " + e.getMessage()); // Qué hace: Registra el error en la consola del sistema. | Para qué sirve / Destino: Facilita la depuración de consultas multitabla.
        }
        return lista; // Qué hace: Retorna la lista de empleados. | Para qué sirve / Destino: Envía la colección de personal administrativo hacia el controlador solicitante.
    }

    /**
     * Registra un nuevo empleado asignando valores por defecto si no se ingresaron
     * y garantizando el cumplimiento de restricciones CHECK (name >= 3, direccion >= 10).
     *
     * @param u Objeto Usuario con la información del empleado.
     * @return true si el registro fue exitoso, false de lo contrario.
     */
    public boolean registrarEmpleado(Usuario u) { // Qué hace: Declara registrarEmpleado que recibe un Usuario y retorna un booleano. | Para qué sirve / Destino: Asegura valores por defecto válidos antes de insertar un empleado en MySQL.
        // Si no se asignó contraseña, proveer una por defecto
        if (u.getPassword() == null || u.getPassword().isEmpty()) { // Qué hace: Valida si la contraseña está vacía. | Para qué sirve / Destino: Aplica una clave temporal por defecto si el administrador no ingresó una.
            u.setPassword("empleado123"); // Qué hace: Asigna contraseña por defecto. | Para qué sirve / Destino: Suministra credenciales de respaldo para el nuevo empleado.
        }
        // Si no se asignó teléfono, proveer una cadena de ceros por defecto para cumplir la restricción
        if (u.getPhone() == null || u.getPhone().isEmpty()) { // Qué hace: Valida si el teléfono está vacío. | Para qué sirve / Destino: Aplica un número genérico por defecto para cumplir reglas de base de datos.
            u.setPhone("0000000000"); // Qué hace: Asigna teléfono genérico. | Para qué sirve / Destino: Suministra un valor válido para evitar errores de restricción en MySQL.
        }
        // Garantizar el cumplimiento del CHECK(length(direccion) >= 10) de la BD
        if (u.getDireccion() == null || u.getDireccion().trim().length() < 10) { // Qué hace: Valida la longitud mínima de la dirección. | Para qué sirve / Destino: Evita violaciones de la restricción CHECK de dirección en MySQL.
            u.setDireccion("Calle 1 # 10-20 Barrio Centro"); // Qué hace: Asigna dirección por defecto. | Para qué sirve / Destino: Suministra una dirección válida que cumple el requisito de longitud.
        }
        // Garantizar el cumplimiento del CHECK(length(name) >= 3) de la BD
        if (u.getName() == null || u.getName().trim().length() < 3) { // Qué hace: Valida la longitud mínima del nombre. | Para qué sirve / Destino: Evita violaciones de la restricción CHECK de nombre en MySQL.
            u.setName("Empleado Sistema"); // Qué hace: Asigna nombre por defecto. | Para qué sirve / Destino: Suministra un nombre válido que cumple el requisito de caracteres mínimos.
        }
        // Invocar el método de registro estándar
        return registrar(u); // Qué hace: Delega el proceso al método registrar general. | Para qué sirve / Destino: Ejecuta la persistencia final del empleado validado hacia la base de datos MySQL.
    }

    /**
     * Elimina físicamente un usuario de la base de datos por su identificador.
     *
     * @param idUsuario Identificador del usuario.
     * @return true si la eliminación fue exitosa, false de lo contrario.
     */
    public boolean eliminarEmpleado(String idUsuario) { // Qué hace: Declara eliminarEmpleado que recibe un ID y retorna un booleano. | Para qué sirve / Destino: Gestiona la eliminación física de un registro de empleado en MySQL.
        // Consulta SQL DELETE parametrizada
        String sql = "DELETE FROM usuario WHERE id_usuario = ?"; // Qué hace: Define la sentencia SQL DELETE. | Para qué sirve / Destino: Especifica la eliminación permanente del registro en la tabla de MySQL.
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) { // Qué hace: Conecta con MySQL y prepara el DELETE. | Para qué sirve / Destino: Abre el canal JDBC seguro para borrar el registro.
            ps.setString(1, idUsuario); // Qué hace: Asigna el ID al marcador de posición. | Para qué sirve / Destino: Filtra qué empleado específico será eliminado en MySQL.
            return ps.executeUpdate() > 0; // Qué hace: Ejecuta el DELETE y retorna true si afectó filas. | Para qué sirve / Destino: Confirma la eliminación exitosa en MySQL hacia el controlador.
        } catch (SQLException e) { // Qué hace: Captura excepciones SQL durante la eliminación. | Para qué sirve / Destino: Maneja errores de restricciones de clave foránea o conexión en MySQL.
            System.err.println("Error eliminando empleado en el DAO: " + e.getMessage()); // Qué hace: Registra el error en la consola del sistema. | Para qué sirve / Destino: Facilita el diagnóstico de fallos al borrar.
        }
        return false; // Qué hace: Retorna false si ocurre un error. | Para qué sirve / Destino: Informa al controlador que la eliminación del empleado no pudo efectuarse.
    }

    /**
     * Actualiza la información administrativa de un empleado.
     *
     * @param idUsuario Identificador del usuario a modificar.
     * @param name Nuevo nombre.
     * @param email Nuevo correo.
     * @param role Rol descriptivo del empleado (se traduce internamente a ROL-001 o ROL-002).
     * @param status Nuevo estado de la cuenta (Activo/Inactivo).
     * @return true si la actualización fue exitosa, false de lo contrario.
     */
    public boolean actualizarEmpleado(String idUsuario, String name, String email, String phone, String role, String status) { // Qué hace: Declara actualizarEmpleado que recibe datos administrativos y retorna un booleano. | Para qué sirve / Destino: Gestiona la modificación de información de empleados interactuando con MySQL.
        // Traducir el nombre amigable de rol a su correspondiente ID de base de datos
        String idRol = role.toLowerCase().contains("admin") ? "ROL-001" : "ROL-002"; // Qué hace: Traduce el rol descriptivo a identificador normalizado. | Para qué sirve / Destino: Asegura asignar el código de rol correcto ('ROL-001' o 'ROL-002') para MySQL.
        // Sentencia SQL de actualización parametrizada
        String sql = "UPDATE usuario SET name = ?, email = ?, phone = ?, id_rol = ?, status = ? WHERE id_usuario = ?"; // Qué hace: Define la sentencia SQL UPDATE para empleados. | Para qué sirve / Destino: Especifica la modificación de datos administrativos en la tabla de MySQL.
        try (Connection con = Conexion.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) { // Qué hace: Conecta con MySQL y prepara el UPDATE. | Para qué sirve / Destino: Abre el canal JDBC seguro para aplicar los cambios del empleado.
            ps.setString(1, name); // Qué hace: Asigna el nuevo nombre al primer marcador. | Para qué sirve / Destino: Actualiza el nombre del empleado en la base de datos.
            ps.setString(2, email); // Qué hace: Asigna el nuevo correo al segundo marcador. | Para qué sirve / Destino: Actualiza el correo electrónico en MySQL.
            ps.setString(3, phone); // Qué hace: Asigna el nuevo teléfono al tercer marcador. | Para qué sirve / Destino: Actualiza el número de contacto en la base de datos.
            ps.setString(4, idRol); // Qué hace: Asigna el id_rol traducido al cuarto marcador. | Para qué sirve / Destino: Actualiza el rol del empleado en la tabla de MySQL.
            ps.setString(5, status); // Qué hace: Asigna el nuevo estado al quinto marcador. | Para qué sirve / Destino: Actualiza el estado de la cuenta (Activo/Inactivo) en MySQL.
            ps.setString(6, idUsuario); // Qué hace: Asigna el id_usuario al sexto marcador (WHERE). | Para qué sirve / Destino: Filtra qué empleado específico será modificado en la base de datos.
            return ps.executeUpdate() > 0; // Qué hace: Ejecuta el UPDATE y retorna true si fue exitoso. | Para qué sirve / Destino: Confirma la actualización del empleado en MySQL hacia el controlador.
        } catch (SQLException e) { // Qué hace: Captura excepciones SQL en la actualización de empleado. | Para qué sirve / Destino: Maneja errores de ejecución o restricciones en MySQL.
            System.err.println("Error actualizando empleado en el DAO: " + e.getMessage()); // Qué hace: Registra el error en la consola del sistema. | Para qué sirve / Destino: Facilita el diagnóstico de fallos administrativos.
        }
        return false; // Qué hace: Retorna false si ocurre un error. | Para qué sirve / Destino: Informa al controlador que la actualización del empleado falló.
    }
}