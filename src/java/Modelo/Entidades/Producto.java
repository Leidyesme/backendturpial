package Modelo.Entidades; // Qué hace: Declara el paquete Modelo.Entidades. | Para qué sirve / Destino: Ubica la clase lógicamente en la Capa de Entidades del modelo de datos de la arquitectura del proyecto.

public class Producto { // Qué hace: Declara la clase pública Producto. | Para qué sirve / Destino: Actúa como la entidad de persistencia en memoria para mapear los registros de productos del catálogo provenientes de o hacia la base de datos MySQL.

    private String idProducto; // Qué hace: Declara el atributo privado idProducto. | Para qué sirve / Destino: Almacena el identificador único del producto mapeado desde la clave primaria en la base de datos MySQL.
    private String idCategoria; // Qué hace: Declara el atributo privado idCategoria. | Para qué sirve / Destino: Almacena la clave foránea que relaciona el producto con su respectiva categoría en la base de datos MySQL.
    private String nombre; // Qué hace: Declara el atributo privado nombre. | Para qué sirve / Destino: Almacena el nombre descriptivo del artículo del menú o catálogo.
    private String descripcion; // Qué hace: Declara el atributo privado descripcion. | Para qué sirve / Destino: Almacena el detalle o ingredientes del producto para su visualización en las vistas de la cafetería.
    private double precio; // Qué hace: Declara el atributo privado precio de tipo double. | Para qué sirve / Destino: Almacena el costo monetario del producto para los cálculos de los pedidos y persistencia en MySQL.
    private int stock; // Qué hace: Declara el atributo privado stock de tipo int. | Para qué sirve / Destino: Almacena la cantidad disponible en inventario del producto en el sistema.
    private String estado; // Qué hace: Declara el atributo privado estado. | Para qué sirve / Destino: Almacena el estatus operativo del producto (ej: activo, agotado) utilizado por la lógica de negocio y controladores.
    private String imagen; // Qué hace: Declara el atributo privado imagen. | Para qué sirve / Destino: Almacena la ruta o nombre del archivo de imagen asociado al producto para mostrarlo en la interfaz web.

    public Producto() { // Qué hace: Define el constructor sin parámetros de la clase Producto. | Para qué sirve / Destino: Permite instanciar objetos vacíos en los controladores o formularios para ser poblados posteriormente mediante setters.
    }

    public String getIdProducto() { // Qué hace: Declara el método getter getIdProducto que retorna un String. | Para qué sirve / Destino: Permite a los controladores o DAOs consultar el identificador único del producto.
        return idProducto; // Qué hace: Retorna el valor del atributo idProducto. | Para qué sirve / Destino: Suministra el ID del producto hacia la capa que lo solicite.
    }

    public void setIdProducto(String idProducto) { // Qué hace: Declara el método setter setIdProducto que recibe un String. | Para qué sirve / Destino: Permite modificar o asignar el identificador del producto en la entidad.
        this.idProducto = idProducto; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad idProducto del objeto Producto.
    }

    public String getIdCategoria() { // Qué hace: Declara el método getter getIdCategoria que retorna un String. | Para qué sirve / Destino: Permite consultar la categoría vinculada desde los DAOs o lógica de negocio.
        return idCategoria; // Qué hace: Retorna el valor del atributo idCategoria. | Para qué sirve / Destino: Proporciona la referencia de la categoría hacia los componentes de consulta.
    }

    public void setIdCategoria(String idCategoria) { // Qué hace: Declara el método setter setIdCategoria que recibe un String. | Para qué sirve / Destino: Permite asociar o cambiar la categoría del producto en la entidad.
        this.idCategoria = idCategoria; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad idCategoria del objeto Producto.
    }

    public String getNombre() { // Qué hace: Declara el método getter getNombre que retorna un String. | Para qué sirve / Destino: Permite consultar el nombre del artículo para mostrarlo en el catálogo o pedidos.
        return nombre; // Qué hace: Retorna el valor del atributo nombre. | Para qué sirve / Destino: Suministra el nombre del producto hacia las vistas o controladores.
    }

    public void setNombre(String nombre) { // Qué hace: Declara el método setter setNombre que recibe un String. | Para qué sirve / Destino: Permite asignar el nombre del producto en la entidad.
        this.nombre = nombre; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad nombre del objeto Producto.
    }

    public String getDescripcion() { // Qué hace: Declara el método getter getDescripcion que retorna un String. | Para qué sirve / Destino: Permite leer los detalles o ingredientes del producto desde los controladores.
        return descripcion; // Qué hace: Retorna el valor del atributo descripcion. | Para qué sirve / Destino: Proporciona la descripción del artículo hacia la interfaz de usuario.
    }

    public void setDescripcion(String descripcion) { // Qué hace: Declara el método setter setDescripcion que recibe un String. | Para qué sirve / Destino: Permite asignar la descripción o detalles del producto en la entidad.
        this.descripcion = descripcion; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad descripcion del objeto Producto.
    }

    public double getPrecio() { // Qué hace: Declara el método getter getPrecio que retorna un double. | Para qué sirve / Destino: Permite leer el precio del producto para los cálculos de la orden de compra.
        return precio; // Qué hace: Retorna el valor del atributo precio. | Para qué sirve / Destino: Suministra el valor monetario del producto hacia las capas superiores.
    }

    public void setPrecio(double precio) { // Qué hace: Declara el método setter setPrecio que recibe un double. | Para qué sirve / Destino: Permite asignar el precio unitario del producto en la entidad.
        this.precio = precio; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad precio del objeto Producto.
    }

    public int getStock() { // Qué hace: Declara el método getter getStock que retorna un int. | Para qué sirve / Destino: Permite consultar la cantidad disponible en inventario desde la lógica de negocio.
        return stock; // Qué hace: Retorna el valor del atributo stock. | Para qué sirve / Destino: Suministra el dato de existencias hacia los componentes de validación de pedidos.
    }

    public void setStock(int stock) { // Qué hace: Declara el método setter setStock que recibe un int. | Para qué sirve / Destino: Permite modificar el inventario disponible del producto en la entidad.
        this.stock = stock; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad stock del objeto Producto.
    }

    public String getEstado() { // Qué hace: Declara el método getter getEstado que retorna un String. | Para qué sirve / Destino: Permite consultar el estatus operativo actual del producto desde los controladores.
        return estado; // Qué hace: Retorna el valor del atributo estado. | Para qué sirve / Destino: Proporciona el estado del producto hacia los componentes de interfaz.
    }

    public void setEstado(String estado) { // Qué hace: Declara el método setter setEstado que recibe un String. | Para qué sirve / Destino: Permite modificar el estatus operativo del producto en la entidad.
        this.estado = estado; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad estado del objeto Producto.
    }

    public String getImagen() { // Qué hace: Declara el método getter getImagen que retorna un String. | Para qué sirve / Destino: Permite consultar la ruta de la imagen multimedia para renderizarla en las vistas web.
        return imagen; // Qué hace: Retorna el valor del atributo imagen. | Para qué sirve / Destino: Suministra la referencia visual del producto hacia la interfaz gráfica.
    }

    public void setImagen(String imagen) { // Qué hace: Declara el método setter setImagen que recibe un String. | Para qué sirve / Destino: Permite asignar la ruta de la imagen del producto en la entidad.
        this.imagen = imagen; // Qué hace: Asigna el valor recibido al atributo de la clase. | Para qué sirve / Destino: Actualiza internamente la propiedad imagen del objeto Producto.
    }
}