# DonCrepePOS

Sistema de ventas de escritorio para una tienda de crepes, desarrollado en Java Swing para abrirse desde NetBeans.

## Como abrir en NetBeans

1. Abra NetBeans.
2. Seleccione `File > Open Project`.
3. Elija la carpeta:
   `C:\Users\USER\Documents\Codex\2026-05-11\analiza-esta-imagend-etalladamente-son-5\DonCrepePOS`
4. Abra el proyecto `DonCrepePOS`.

## Base de datos MySQL

1. Abra MySQL Workbench, phpMyAdmin o la consola de MySQL.
2. Ejecute el archivo:
   `database/don_crepe_db.sql`
3. Esto crea la base de datos `don_crepe_db` con usuario, mesas, productos, pedidos y caja inicial.

Usuario demo:

- Usuario: `admin`
- Contrasena: `admin`

## Driver MySQL

El codigo usa JDBC con `com.mysql.cj.jdbc.Driver`.

En NetBeans agregue MySQL Connector/J al proyecto:

1. Clic derecho sobre el proyecto.
2. `Properties > Libraries`.
3. `Add JAR/Folder`.
4. Seleccione el archivo `mysql-connector-j-*.jar`.

Si usa XAMPP o MySQL local, normalmente la configuracion por defecto funciona:

- Host: `localhost`
- Puerto: `3306`
- Base de datos: `don_crepe_db`
- Usuario: `root`
- Contrasena: vacia

Para cambiar estos valores edite:

`src/conexion/ConexionBD.java`

## Funcionalidades

- Login con MySQL.
- Dashboard con ventas del dia, caja, pedidos y productos.
- Gestion visual de mesas con estado libre/ocupado.
- Catalogo de productos en tarjetas.
- Agregar, editar y eliminar productos.
- Crear pedidos con productos y cantidades.
- Registro automatico en caja.
- Historial de caja por fecha.
- Cerrar sesion.

## Arquitectura

El proyecto aplica MVC, DAO, TDD y principios SOLID. La explicacion para clase esta en:

`ARQUITECTURA.md`

Resumen:

- MVC: vistas en `vista`, controladores en `controlador`, modelos en `modelo`.
- DAO: interfaces y clases de acceso a datos en `dao`.
- TDD: pruebas simples en `test/tdd/PruebasTDD.java`.
- SOLID: responsabilidades separadas e interfaces DAO para depender de abstracciones.

## Librerias integradas

El proyecto incluye librerias externas en la carpeta `lib` y estan registradas en `nbproject/project.properties`.

- Google Guava: utilidades para colecciones, cadenas y validaciones.
- Apache POI: creacion y lectura de archivos Excel.
- Apache Commons: utilidades generales como `StringUtils` de Commons Lang.
- Logback: sistema de logs mediante SLF4J.

Tambien se incluye `lib-dependencies-pom.xml`, que sirve solo para descargar dependencias con Maven hacia `lib`; el proyecto principal sigue siendo Java Ant.

Implementacion dentro de funciones reales:

- Apache POI: el modulo `Historial Caja` tiene el boton `Exportar Excel`, que genera `reportes/caja_FECHA.xlsx`.
- Apache Commons: los controladores limpian y normalizan nombres de productos, categorias y clientes.
- Google Guava: el controlador de pedidos crea resumenes de productos usando `Joiner`.
- Logback: DAOs y controladores registran eventos importantes, como pedidos guardados o reportes exportados.

Clases principales:

- `src/servicio/ReporteCajaExcelService.java`
- `src/controlador/HistorialCajaController.java`
- `src/controlador/ProductosController.java`
- `src/controlador/PedidosController.java`

## Compilar fuera de NetBeans

Tambien puede compilar con Ant desde esta carpeta:

```bat
ant compile
```

Y ejecutar:

```bat
ant run
```

Para ejecucion con MySQL, recuerde agregar MySQL Connector/J al classpath del proyecto o de NetBeans.

## Ejecutar pruebas TDD

```bat
ant test
```

Tambien puede ejecutar `test/tdd/PruebasTDD.java` desde NetBeans.

## Git

Repositorio en GitHub:

`https://github.com/dudeakira6-art/Doncrepe.git`

La guia de uso de Git para este proyecto esta en:

`docs/GIT.md`

Documento Word para entregar:

`docs/Uso_Git_DonCrepePOS_Estilo_Ejemplo.docx`

Scripts incluidos:

- `scripts/git-init-doncrepe.ps1`: inicializa el repositorio y crea el primer commit.
- `scripts/git-workflow.ps1`: ayuda con `status`, `commit`, `log`, `pull`, `push` y `tag`.
