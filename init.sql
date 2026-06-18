-- =========================================================================
-- SCRIPT DE INICIALIZACIÓN DE LA BASE DE DATOS 'turpial'
-- =========================================================================

CREATE DATABASE IF NOT EXISTS turpial;
USE turpial;

-- 1. TABLA CATEGORIA
CREATE TABLE IF NOT EXISTS Categoria (
    id_categoria VARCHAR(10) NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(150) NOT NULL,
    estado ENUM ('Activo','Inactivo') DEFAULT 'Activo',
    PRIMARY KEY (id_categoria)
);

-- Inserciones en Categoria
INSERT INTO Categoria (id_categoria,nombre,descripcion,estado) VALUES 
('CAT-001','Desayunos','Variedades de deayunos típicos y americanos','Activo'),
('CAT-002','Almuerzos','Deliciosos almuerzos ejecutivos y platos a la carta','Activo'),
('CAT-003','Bebidas','Variedad de bebidas calientes y frías','Activo'),
('CAT-004','Panadería y repostería','Panes frecos, tortas y postres del día','Activo'),
('CAT-005','Comidas Rapidas', 'Platos rápidos como hamburguesas, perros calientes y papas fritas','Activo')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre), descripcion=VALUES(descripcion), estado=VALUES(estado);

-- 2. TABLA PRODUCTO
CREATE TABLE IF NOT EXISTS Producto (
    id_producto VARCHAR(10) NOT NULL,
    id_categoria VARCHAR(10) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(250) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    estado VARCHAR(15) GENERATED ALWAYS AS (IF(stock > 0, 'Disponible', 'Agotado')) VIRTUAL, 
    fecha_vencimiento DATE NULL,
    unidades_medida VARCHAR(50) NOT NULL,
    CHECK(precio > 0),
    CHECK(stock >= 0),
    PRIMARY KEY (id_producto),
    FOREIGN KEY (id_categoria) REFERENCES Categoria (id_categoria) ON UPDATE CASCADE 
);

CREATE INDEX IF NOT EXISTS idx_producto_categoria ON Producto(id_categoria);

-- Inserciones en Producto
INSERT INTO Producto (id_producto,id_categoria,nombre,descripcion,precio,stock,fecha_vencimiento,unidades_medida) VALUES 
('PROD-001', 'CAT-001', 'Huevos revueltos', 'Huevos revueltos acompañados de pan artesanal', 6000.00, 50, '2026-12-31', 'Porción'),
('PROD-002', 'CAT-001', 'Panqueques y hot cakes caceros', 'Panqueques esponjosos preparados de forma casera con miel y frutas', 8000.00, 40, '2026-12-31', '3 unidades'),
('PROD-003', 'CAT-001', 'Sandwich de pav, queso y lechuga', 'Sándwich fresco de pavo con queso y lechuga en pan artesanal', 8000.00, 20, '2026-04-20', 'Unidad'),
('PROD-004', 'CAT-001', 'Sandwich de jamos y queso en pan integral', 'Sándwich saludable de jamón y queso servido en pan integral', 5000.00, 10, '2026-04-18', 'Unidad'),
('PROD-005', 'CAT-001', 'Yogur griego con cereales y frutos secos', 'Yogur griego acompañado de cereales crocantes y frutos secos', 7000.00, 15, '2026-04-15', 'Porción'),
('PROD-006', 'CAT-001', 'waffles de zanahoria con avena', 'Waffles nutritivos elaborados con zanahoria y avena', 7000.00, 0, '2026-04-14', '3 Unidad'),
('PROD-007', 'CAT-001', 'Tostadas de aguacate y huevo', 'Tostadas integrales con aguacate fresco y huevo', 6000.00, 8, '2026-08-28', '2 Unidades'),
('PROD-008', 'CAT-001', 'Tortilla de espinacas y huevo', 'Tortilla preparada con espinacas frescas y huevo', 5000.00, 12,'2026-05-01', 'Porción'),
('PROD-009','CAT-002','Arroz con pollo','Arroz tradicional acompañado de pollo desmechado y verduras',12000.00,15, '2026-05-10', 'Porción'),
('PROD-010','CAT-002','Ajiaco Santafereño','Sopa típica colombiana preparada con pollo, papa y mazorca',15000.00,15, '2026-05-10', 'Porción'),
('PROD-011','CAT-002','Sudado de pollo y papa','Pollo guisado acompañado de papa y arroz blanco',13000.00,15, '2026-05-10', 'Porción'),
('PROD-012','CAT-003','Smoothie de frutas y espinacas','Bebida natural con frutas frescas y espinacas',7000.00,15,'2026-04-30','500 ml'),
('PROD-013','CAT-003','Licuado de manzana y canela','Licuado refrescante de manzana con toque de canela',6000.00,15,'2026-04-30','500 ml'),
('PROD-014','CAT-003','Smoothie de fresa','Bebida cremosa preparada con fresas naturales',7000.00,15,'2026-04-30','500 ml'),
('PROD-015','CAT-004','Pastel de pollo','Pastel horneado relleno de pollo sazonado',4000.00,15,'2026-07-12','Unidad'),
('PROD-016','CAT-004','Pandeyuca','Pan tradicional de queso con textura suave',2000.00,15,'2026-06-18','Unidad'),
('PROD-017','CAT-004','Almojabana','Panecillo típico elaborado con queso y maíz',2000.00,15,'2026-07-15','Unidad'),
('PROD-018','CAT-005','Salchipapa','Papas fritas acompañadas de salchicha, salsas y queso rallado',8000.00,15,'2026-05-15','Porción'),
('PROD-019','CAT-005','Choripapa','Papas fritas servidas con chorizo y salsas especiales',9000.00,15,'2026-05-15','Porción'),
('PROD-020','CAT-005','Empanadas','Empanadas crujientes rellenas de carne y papa',2500.00,15,'2026-05-15','Unidad'),
('PROD-021','CAT-005','Papas','Porción de papas fritas crocantes con salsa de tomate',4000.00,15,'2026-05-15','Porción'),
('PROD-022','CAT-005','Perros','Pan con salchicha, papas trituradas, queso y salsas',8000.00,15,'2026-05-15','Unidad'),
('PROD-023','CAT-005','Hamburguesas','Hamburguesa artesanal con carne, queso, lechuga y tomate',12000.00,15,'2026-05-15','Unidad')
ON DUPLICATE KEY UPDATE id_categoria=VALUES(id_categoria), nombre=VALUES(nombre), descripcion=VALUES(descripcion), precio=VALUES(precio), stock=VALUES(stock), fecha_vencimiento=VALUES(fecha_vencimiento), unidades_medida=VALUES(unidades_medida);

-- 3. TABLA ROLES
CREATE TABLE IF NOT EXISTS Roles ( 
    id_rol VARCHAR(10) NOT NULL,
    nombre VARCHAR(50) UNIQUE NOT NULL,
    PRIMARY KEY (id_rol)
);

CREATE INDEX IF NOT EXISTS idx_rol ON Roles(nombre);

INSERT INTO Roles (id_rol,nombre) VALUES 
('ROL-001', 'Administrador'),
('ROL-002', 'Empleado'),
('ROL-003', 'Usuario')
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- 4. TABLA PERMISOS
CREATE TABLE IF NOT EXISTS Permisos (
    id_permiso VARCHAR(10) NOT NULL,
    tipo_permiso ENUM ("Editar_total","Leer","Pedir","Editar_personal","Gestionar") UNIQUE,
    PRIMARY KEY (id_permiso)
);

INSERT INTO Permisos (id_permiso,tipo_permiso) VALUES 
('PER-001', 'Gestionar'),
('PER-002', 'Editar_personal'),
('PER-003', 'Pedir'),
('PER-004', 'Leer'),
('PER-005', 'Editar_total')
ON DUPLICATE KEY UPDATE tipo_permiso=VALUES(tipo_permiso);

-- 5. TABLA ROLES_PERMISOS
CREATE TABLE IF NOT EXISTS RolesPermisos (
    id_rolespermisos VARCHAR(10) NOT NULL,
    id_permiso VARCHAR(10) NOT NULL,
    id_roles VARCHAR(10) NOT NULL, 
    PRIMARY KEY (id_rolespermisos),
    FOREIGN KEY (id_permiso) REFERENCES Permisos (id_permiso),
    FOREIGN KEY (id_roles) REFERENCES Roles (id_rol)
);

CREATE INDEX IF NOT EXISTS idx_permiso ON RolesPermisos(id_permiso);

INSERT INTO RolesPermisos (id_rolespermisos,id_permiso,id_roles) VALUES 
('RP-001','PER-001', 'ROL-001'),
('RP-002','PER-002', 'ROL-001'),
('RP-003','PER-003', 'ROL-001'),
('RP-004','PER-004', 'ROL-001'),
('RP-005','PER-005', 'ROL-001'),
('RP-006','PER-004', 'ROL-002'),
('RP-007','PER-003', 'ROL-002'),
('RP-008','PER-004', 'ROL-003'),
('RP-009','PER-003', 'ROL-003')
ON DUPLICATE KEY UPDATE id_permiso=VALUES(id_permiso), id_roles=VALUES(id_roles);

-- 6. TABLA USUARIO
CREATE TABLE IF NOT EXISTS Usuario (
    id_usuario VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    id_rol VARCHAR(10) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL UNIQUE,
    direccion VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    status ENUM ('Activo','Inactivo') NOT NULL DEFAULT 'Activo',
    created_at DATE NOT NULL,
    CHECK (LENGTH(name)>=3),
    CHECK (LENGTH(direccion)>=10),
    PRIMARY KEY (id_usuario),
    FOREIGN KEY (id_rol) REFERENCES Roles (id_rol) ON DELETE RESTRICT ON UPDATE CASCADE 
);

CREATE INDEX IF NOT EXISTS idx_usuario ON Usuario(status);

INSERT INTO Usuario (id_usuario,name,id_rol,email,phone,direccion,password,status,created_at) VALUES 
('USR-001', 'Admin', 'ROL-001', 'admin@turpial.com', '3001234567','calle 1 #10-20' ,'admin123', 'Activo', '2026-04-01'),
('USR-002', 'Admin2', 'ROL-001', 'leidyedp@gmail.com', '3041234567','calle 1 #12-25' ,'Leidy.92021', 'Activo', '2026-04-01'),
('USR-003', 'Carlos', 'ROL-002', 'carlos.b@turpial.com', '3109876543','calle 2 #20-30' ,'empleado123', 'Activo', '2026-04-02'),
('USR-004', 'Elena', 'ROL-002', 'elena.m@turpial.com', '3204567890','calle 3 #30-40' ,'empleado456', 'Inactivo', '2026-04-05'),
('USR-005', 'Juan', 'ROL-003', 'juan.perez@email.com', '3151112233','calle 4 #40-50' ,'cliente789', 'Activo', '2026-04-10')
ON DUPLICATE KEY UPDATE name=VALUES(name), id_rol=VALUES(id_rol), email=VALUES(email), phone=VALUES(phone), direccion=VALUES(direccion), password=VALUES(password), status=VALUES(status);

-- 7. TABLA PEDIDO
CREATE TABLE IF NOT EXISTS Pedido (
    id_pedido VARCHAR(10) NOT NULL,
    id_usuario VARCHAR(10) NULL,
    customer_name VARCHAR(50) NULL,
    tipo_entrega ENUM ("A domicilio","Para recoger","Para consumir aquí") NOT NULL,
    numero_mesa INT NULL,
    direccion_entrega VARCHAR(100) NULL,
    observaciones TEXT NULL,
    total DECIMAL(10,2) NOT NULL,
    estado ENUM ('En preparación','Listo','En espera','Entregado') NOT NULL,
    fecha_pedido DATETIME NOT NULL,
    CHECK(id_usuario IS NOT NULL OR customer_name IS NOT NULL),
    CHECK (numero_mesa IS NULL OR numero_mesa > 0),
    PRIMARY KEY (id_pedido),
    FOREIGN KEY (id_usuario) REFERENCES Usuario (id_usuario)
);

CREATE INDEX IF NOT EXISTS idx_pedido_usuario ON Pedido(id_usuario);

INSERT INTO Pedido (id_pedido,id_usuario,customer_name,tipo_entrega,numero_mesa,direccion_entrega,observaciones,total,estado,fecha_pedido) VALUES 
('PED-001', 'USR-004', NULL, 'Para consumir aquí', 5, null, 'Sin azúcar', 13000.00, 'Entregado', '2026-04-13 08:30:00'), 
('PED-002', NULL, 'Marta Gómez', 'Para recoger', null, null, 'Empacar por separado', 5500.00, 'Listo', '2026-04-13 09:15:00'), 
('PED-003', 'USR-004', NULL, 'A domicilio', null, 'Calle 10 # 5-20, Girón', 'Tocar el timbre fuerte', 26500.00, 'En preparación', '2026-04-13 10:00:00')
ON DUPLICATE KEY UPDATE id_usuario=VALUES(id_usuario), customer_name=VALUES(customer_name), tipo_entrega=VALUES(tipo_entrega), numero_mesa=VALUES(numero_mesa), direccion_entrega=VALUES(direccion_entrega), observaciones=VALUES(observaciones), total=VALUES(total), estado=VALUES(estado);

-- 8. TABLA DETALLEPEDIDO
CREATE TABLE IF NOT EXISTS DetallePedido (
    id_detallepedido VARCHAR(10) NOT NULL,
    id_pedido VARCHAR(10) NOT NULL,
    id_producto VARCHAR(10) NOT NULL, 
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    CHECK(cantidad > 0),
    CHECK (precio_unitario > 0),
    PRIMARY KEY (id_detallepedido),
    FOREIGN KEY (id_pedido) REFERENCES Pedido (id_pedido) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES Producto (id_producto)
);

CREATE INDEX IF NOT EXISTS idx_detallepedido_pedido ON DetallePedido(id_pedido);
CREATE INDEX IF NOT EXISTS idx_detallepedido_producto ON DetallePedido(id_producto);

INSERT INTO DetallePedido (id_detallepedido,id_pedido,id_producto,cantidad,precio_unitario) VALUES 
('DET-001', 'PED-001', 'PROD-002', 1, 8000.00),
('DET-002', 'PED-001', 'PROD-004', 1, 5000.00),
('DET-003', 'PED-002', 'PROD-008', 1, 5000.00),
('DET-004', 'PED-003', 'PROD-009', 2, 12000.00),
('DET-005', 'PED-003', 'PROD-020', 1, 2500.00)
ON DUPLICATE KEY UPDATE id_pedido=VALUES(id_pedido), id_producto=VALUES(id_producto), cantidad=VALUES(cantidad), precio_unitario=VALUES(precio_unitario);

-- 9. TABLA PAGO
CREATE TABLE IF NOT EXISTS Pago (
    id_pago VARCHAR(10) NOT NULL,
    id_pedido VARCHAR(10) NOT NULL,
    metodo_pago ENUM ('Efectivo','Transferencia') NOT NULL,
    monto_recibido DECIMAL(10,2) NOT NULL,
    cambio DECIMAL(10,2) NOT NULL,
    estado_pago ENUM ('Pagado','Pendiente','Cancelado') NOT NULL,
    fecha_pago DATETIME NOT NULL,
    CHECK(monto_recibido >= 0),
    CHECK(cambio >= 0),
    PRIMARY KEY (id_pago),
    FOREIGN KEY (id_pedido) REFERENCES Pedido (id_pedido)
);

CREATE INDEX IF NOT EXISTS idx_pago_pedido ON Pago(id_pedido);

INSERT INTO Pago (id_pago, id_pedido, metodo_pago, monto_recibido, cambio, estado_pago, fecha_pago) VALUES 
('PAG-001', 'PED-001', 'Efectivo', 20000.00, 7000.00, 'Pagado', '2026-04-13 08:45:00'), 
('PAG-002', 'PED-002', 'Transferencia', 5500.00, 0.00, 'Pagado', '2026-04-13 09:20:00'), 
('PAG-003', 'PED-003', 'Efectivo', 0.00, 0.00, 'Pendiente', '2026-04-13 10:05:00')
ON DUPLICATE KEY UPDATE id_pedido=VALUES(id_pedido), metodo_pago=VALUES(metodo_pago), monto_recibido=VALUES(monto_recibido), cambio=VALUES(cambio), estado_pago=VALUES(estado_pago);

-- 10. TABLA HISTORIALPEDIDOS
CREATE TABLE IF NOT EXISTS HistorialPedidos (
    id_historialpedido VARCHAR(10) NOT NULL,
    id_pedido VARCHAR(10) NOT NULL,
    id_usuario VARCHAR(10) NOT NULL,
    fecha_movimiento DATETIME NOT NULL,
    estado ENUM ('Finalizado','Cancelado') NOT NULL,
    descripcion TEXT NOT NULL,
    PRIMARY KEY (id_historialpedido),
    FOREIGN KEY (id_pedido) REFERENCES Pedido (id_pedido),
    FOREIGN KEY (id_usuario) REFERENCES Usuario (id_usuario)
);

CREATE INDEX IF NOT EXISTS idx_historial_pedido ON HistorialPedidos(id_pedido);

INSERT INTO HistorialPedidos (id_historialpedido, id_pedido, id_usuario, fecha_movimiento,estado,descripcion) VALUES 
('HIS-001', 'PED-001', 'USR-001', '2026-04-13 08:30:00','Finalizado','Pedido entregado correctamente'),
('HIS-002', 'PED-002', 'USR-002', '2026-04-13 09:15:00','Cancelado','Pedido cancelado por el cliente'),
('HIS-003', 'PED-003', 'USR-003', '2026-04-13 10:00:00','Finalizado','Pedido entregado correctamente')
ON DUPLICATE KEY UPDATE id_pedido=VALUES(id_pedido), id_usuario=VALUES(id_usuario), fecha_movimiento=VALUES(fecha_movimiento), estado=VALUES(estado), descripcion=VALUES(descripcion);

-- 11. TABLA PROMOCION
CREATE TABLE IF NOT EXISTS Promocion (
    id_promocion VARCHAR(10) NOT NULL,
    id_producto VARCHAR(10) NOT NULL,
    descripcion VARCHAR(100) NOT NULL,
    precio_oferta DECIMAL(10,2) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado ENUM ('Vigente','Vencida') NOT NULL,
    CHECK(precio_oferta > 0),
    CHECK(fecha_fin >= fecha_inicio),
    PRIMARY KEY (id_promocion),
    FOREIGN KEY (id_producto) REFERENCES Producto (id_producto)
);

INSERT INTO Promocion (id_promocion,id_producto,descripcion,precio_oferta,fecha_inicio,fecha_fin,estado) VALUES 
('PROM-001', 'PROD-009', 'Arroz con pollo', 10000.00, '2026-04-01', '2026-04-30', 'Vigente'),
('PROM-002', 'PROD-001', 'Huevos revueltos', 5000.00, '2026-04-10', '2026-04-20', 'Vigente'),
('PROM-003', 'PROD-014', 'Smoothie de fresa', 5000.00, '2026-04-01', '2026-04-15', 'Vigente')
ON DUPLICATE KEY UPDATE id_producto=VALUES(id_producto), descripcion=VALUES(descripcion), precio_oferta=VALUES(precio_oferta), fecha_inicio=VALUES(fecha_inicio), fecha_fin=VALUES(fecha_fin), estado=VALUES(estado);

-- 12. TABLA ADDRESS
CREATE TABLE IF NOT EXISTS Address (
    id_address VARCHAR(10) NOT NULL,
    id_usuario VARCHAR(10) NOT NULL,
    address_line VARCHAR(150) NOT NULL,
    city VARCHAR(50) NOT NULL,
    PRIMARY KEY(id_address),
    FOREIGN KEY(id_usuario) REFERENCES Usuario(id_usuario) ON DELETE CASCADE ON UPDATE CASCADE
);

-- 13. TABLA DEVOLUCION
DROP TABLE IF EXISTS Devolucion;
CREATE TABLE Devolucion (
    id_devolucion VARCHAR(10) NOT NULL,
    id_pedido VARCHAR(10) NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    fecha_solicitud DATETIME NOT NULL,
    estado_devolucion VARCHAR(50) NOT NULL DEFAULT 'Pendiente',
    PRIMARY KEY (id_devolucion),
    FOREIGN KEY (id_pedido) REFERENCES Pedido (id_pedido) ON DELETE CASCADE
);

-- 14. TABLA AUDITORIA
DROP TABLE IF EXISTS Auditoria;
CREATE TABLE Auditoria (
    id_historial VARCHAR(10) NOT NULL,
    id_usuario VARCHAR(10) NOT NULL,
    accion VARCHAR(255) NOT NULL,
    tipo_accion VARCHAR(50) NOT NULL,
    fecha DATETIME NOT NULL,
    PRIMARY KEY (id_historial),
    FOREIGN KEY (id_usuario) REFERENCES Usuario (id_usuario) ON DELETE CASCADE
);

-- =========================================================================
-- CONFIGURACIÓN DE USUARIOS DEL SERVIDOR Y PERMISOS SQL
-- =========================================================================

-- Crear usuarios locales de base de datos
CREATE USER IF NOT EXISTS 'admin'@'%' IDENTIFIED BY 'admin1234';
CREATE USER IF NOT EXISTS 'empl'@'%' IDENTIFIED BY 'empl1234';
CREATE USER IF NOT EXISTS 'clie'@'%' IDENTIFIED BY 'clie1234';

-- Otorgar privilegios a los usuarios sobre la base de datos turpial
GRANT ALL PRIVILEGES ON turpial.* TO 'admin'@'%';

GRANT SELECT, UPDATE, INSERT ON turpial.Usuario TO 'empl'@'%';

GRANT SELECT, INSERT ON turpial.Usuario TO 'clie'@'%';

-- Aplicar cambios inmediatamente
FLUSH PRIVILEGES;
