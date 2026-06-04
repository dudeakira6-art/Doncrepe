CREATE DATABASE IF NOT EXISTS don_crepe_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE don_crepe_db;

DROP TABLE IF EXISTS detalle_pedido;
DROP TABLE IF EXISTS caja;
DROP TABLE IF EXISTS pedidos;
DROP TABLE IF EXISTS productos;
DROP TABLE IF EXISTS mesas;
DROP TABLE IF EXISTS usuarios;

CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    rol VARCHAR(30) NOT NULL
);

CREATE TABLE mesas (
    id_mesa INT AUTO_INCREMENT PRIMARY KEY,
    numero INT NOT NULL UNIQUE,
    estado ENUM('LIBRE', 'OCUPADO') NOT NULL DEFAULT 'LIBRE'
);

CREATE TABLE productos (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    categoria VARCHAR(60) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    imagen VARCHAR(255),
    activo TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE pedidos (
    id_pedido INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(40) NOT NULL UNIQUE,
    id_usuario INT NOT NULL,
    id_mesa INT NULL,
    cliente VARCHAR(120) NOT NULL,
    total DECIMAL(10,2) NOT NULL,
    metodo_pago VARCHAR(40) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    fecha DATETIME NOT NULL,
    CONSTRAINT fk_pedidos_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    CONSTRAINT fk_pedidos_mesa FOREIGN KEY (id_mesa) REFERENCES mesas(id_mesa)
);

CREATE TABLE detalle_pedido (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalle_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);

CREATE TABLE caja (
    id_caja INT AUTO_INCREMENT PRIMARY KEY,
    id_pedido INT,
    monto DECIMAL(10,2) NOT NULL,
    metodo_pago VARCHAR(40) NOT NULL,
    fecha DATETIME NOT NULL,
    tipo_movimiento VARCHAR(30) NOT NULL,
    CONSTRAINT fk_caja_pedido FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido)
);

INSERT INTO usuarios (nombre, usuario, password, rol) VALUES
('Milo Perez', 'admin', 'admin', 'Empleado');

INSERT INTO mesas (numero, estado) VALUES
(1, 'OCUPADO'),
(2, 'LIBRE'),
(3, 'LIBRE'),
(4, 'OCUPADO'),
(5, 'OCUPADO'),
(6, 'OCUPADO');

INSERT INTO productos (nombre, categoria, precio, imagen, activo) VALUES
('Crepe de Fresa', 'Crepe', 12.00, 'Crepé de Fresa.png', 1),
('Crepe de Nutella', 'Crepe', 11.00, 'Crepe de Nutella.png', 1),
('Crepe de Platano', 'Crepe', 10.00, 'Crepe platano.png', 1),
('Crepe Dulce de Leche', 'Crepe', 12.00, 'Crepe Dulce de Leche.png', 1),
('Crepe de Arandanos', 'Crepe', 14.00, 'Crepé de Arándanos.png', 1),
('Crepe de Jamon y Queso', 'Crepe', 12.00, 'Crepe Jamón y Queso.png', 1),
('Crepe de Pollo', 'Crepe', 12.00, 'Crepé de Pollo.png', 1),
('Crepe de Champinones', 'Crepe', 11.00, 'Crepé de Champiñones.png', 1),
('Crepe Vegetariano', 'Crepe', 13.00, 'Crep+e Vegetariano.png', 1),
('Crepe de Huevos', 'Crepe', 13.00, 'Crepé de Huevo.png', 1),
('Taza de Cafe Pasado', 'Bebida', 5.00, 'cafe.png', 1),
('Frappe', 'Bebida', 9.00, 'Frapuccino.png', 1),
('Jugo de Naranja', 'Bebida', 7.00, 'Jugo de Naranja.png', 1),
('Batido de Frutas', 'Bebida', 8.50, 'Jugo de Frutas.png', 1),
('Coca Cola 1L', 'Bebida', 6.00, 'Coca Cola.png', 1);

INSERT INTO pedidos (codigo, id_usuario, id_mesa, cliente, total, metodo_pago, estado, fecha) VALUES
('P-058', 1, 2, 'Carlos Alberto', 34.00, 'Efectivo', 'COMPLETADO', NOW()),
('P-059', 1, 4, 'Andrea Ruiz', 20.00, 'Tarjeta', 'COMPLETADO', NOW());

INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, precio_unitario, subtotal) VALUES
(1, 1, 1, 12.00, 12.00),
(1, 2, 2, 11.00, 22.00),
(2, 11, 1, 5.00, 5.00),
(2, 12, 1, 9.00, 9.00),
(2, 15, 1, 6.00, 6.00);

INSERT INTO caja (id_pedido, monto, metodo_pago, fecha, tipo_movimiento) VALUES
(1, 34.00, 'Efectivo', NOW(), 'VENTA'),
(2, 20.00, 'Tarjeta', NOW(), 'VENTA');
