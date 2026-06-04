# Arquitectura aplicada: MVC, DAO, TDD y SOLID

## MVC

El proyecto separa la aplicacion en tres capas:

- Modelo: clases en `src/modelo`, como `Producto`, `Pedido`, `Mesa`, `Usuario` y `Caja`.
- Vista: pantallas en `src/vista`, como `LoginFrame`, `ProductosPanel`, `PedidosPanel` y `MesasPanel`.
- Controlador: clases en `src/controlador`, como `LoginController`, `PedidosController` y `ProductosController`.

La vista ya no habla directamente con la base de datos. La vista llama al controlador, y el controlador coordina los DAOs y modelos.

Ejemplo:

`PedidosPanel` recibe la accion del usuario, llama a `PedidosController`, y el controlador usa `PedidoDAO`, `MesaDAO` y `ProductoDAO`.

## DAO

El acceso a MySQL esta separado en `src/dao`.

Se agregaron interfaces DAO:

- `IUsuarioDAO`
- `IMesaDAO`
- `IProductoDAO`
- `IPedidoDAO`
- `ICajaDAO`

Y las clases concretas las implementan:

- `UsuarioDAO implements IUsuarioDAO`
- `MesaDAO implements IMesaDAO`
- `ProductoDAO implements IProductoDAO`
- `PedidoDAO implements IPedidoDAO`
- `CajaDAO implements ICajaDAO`

Esto permite cambiar la implementacion de acceso a datos sin modificar las vistas.

## TDD

Se agregaron pruebas simples en:

`test/tdd/PruebasTDD.java`

Las pruebas verifican:

- Subtotal de un detalle de pedido.
- Total de un pedido.
- Total cero cuando no hay detalles.
- Validacion de producto con precio invalido.
- Suma de movimientos de caja.

Se pueden ejecutar con:

```bat
ant test
```

Si no tienes Ant instalado, tambien se pueden ejecutar desde NetBeans abriendo la clase `PruebasTDD` y ejecutando su metodo `main`.

## SOLID

### S - Single Responsibility

Cada clase tiene una responsabilidad principal:

- `Producto` representa datos de producto.
- `ProductoDAO` accede a la tabla productos.
- `ProductosController` coordina reglas de productos.
- `ProductosPanel` muestra la interfaz.

### O - Open/Closed

Se pueden agregar nuevas pantallas o nuevos DAOs sin modificar todo el sistema.

Ejemplo: se podria agregar `ReportesController` y `ReportesPanel` sin romper pedidos o productos.

### L - Liskov Substitution

Las vistas como `InicioPanel`, `MesasPanel` y `PedidosPanel` heredan de `JPanel`, por eso pueden mostrarse en `MainFrame` como paneles intercambiables.

### I - Interface Segregation

No existe una interfaz gigante para toda la base de datos. Cada DAO tiene su propia interfaz especifica.

Ejemplo: `IProductoDAO` solo tiene operaciones de productos, y `IPedidoDAO` solo operaciones de pedidos.

### D - Dependency Inversion

Los controladores dependen de interfaces, no solo de clases concretas.

Ejemplo:

```java
private final IProductoDAO productoDAO;
```

Esto facilita pruebas y cambios futuros de base de datos.
