# Uso de Git y GitHub en DonCrepePOS

Este documento explica como se incorporo Git al proyecto **DonCrepePOS**, dejando evidencia de los avances y actualizaciones en la plataforma GitHub.

Repositorio del proyecto:

```bat
https://github.com/dudeakira6-art/Doncrepe.git
```

Documento Word de evidencia con formato similar al ejemplo:

```bat
docs/Uso_Git_DonCrepePOS_Estilo_Ejemplo.docx
```

Rama principal:

```bat
main
```

## 1. Objetivo

El objetivo de usar Git en este proyecto es mantener un historial ordenado de cambios del sistema de ventas para la tienda de crepes.

Con Git se puede demostrar:

- Que el proyecto fue desarrollado por etapas.
- Que los avances quedaron guardados en commits.
- Que el proyecto fue subido a GitHub.
- Que se puede revisar el historial completo desde NetBeans, consola o GitHub.

## 2. Verificacion de Git instalado

Primero se verifico que Git estuviera instalado en Windows:

```bat
git --version
```

Resultado esperado:

```bat
git version 2.54.0.windows.1
```

Esto confirma que el equipo puede usar comandos de Git.

## 3. Configuracion del autor

Para que los commits tengan autor, se configuro el nombre y correo:

```bat
git config user.name "Despairoz"
git config user.email "mijail_perez@hotmail.com"
```

Esta configuracion permite identificar quien realizo los cambios del proyecto.

## 4. Inicializacion del repositorio local

Dentro de la carpeta del proyecto:

```bat
C:\Users\USER\Documents\Codex\2026-05-11\analiza-esta-imagend-etalladamente-son-5\DonCrepePOS
```

se inicializo Git:

```bat
git init
```

Esto crea la carpeta oculta `.git`, donde Git guarda el historial del proyecto.

## 5. Archivo .gitignore

Se agrego un archivo `.gitignore` para evitar subir archivos temporales o generados automaticamente.

Ejemplos de archivos ignorados:

- `build/`
- `dist/`
- `reportes/`
- `*.class`
- `*.log`
- `~$*.docx`

Esto mantiene el repositorio limpio y profesional.

## 6. Commits realizados

El proyecto fue guardado por etapas usando commits descriptivos:

```bat
62982c3 Limpia archivos temporales y completa fuentes
f7fa1ae Agrega pruebas TDD y documentacion Git
3b83457 Mejora interfaz grafica con assets del mockup
6a83633 Implementa arquitectura MVC DAO y servicios
d3fa9c7 Agrega librerias externas del sistema
480b1e5 Configura proyecto NetBeans y base de datos
```

Cada commit representa un avance importante del proyecto.

## 7. Comandos usados para registrar avances

Para revisar cambios pendientes:

```bat
git status
```

Para preparar archivos:

```bat
git add .
```

Para guardar un avance:

```bat
git commit -m "Descripcion clara del avance"
```

Para ver el historial:

```bat
git log --oneline
```

## 8. Conexion con GitHub

Se conecto el repositorio local con GitHub usando el remoto `origin`:

```bat
git remote add origin https://github.com/dudeakira6-art/Doncrepe.git
```

Si el remoto ya existia, se actualizo con:

```bat
git remote set-url origin https://github.com/dudeakira6-art/Doncrepe.git
```

Para verificar la conexion:

```bat
git remote -v
```

Resultado:

```bat
origin  https://github.com/dudeakira6-art/Doncrepe.git (fetch)
origin  https://github.com/dudeakira6-art/Doncrepe.git (push)
```

## 9. Subida del proyecto a GitHub

La rama principal se subio a GitHub con:

```bat
git push -u origin main
```

Resultado:

```bat
branch 'main' set up to track 'origin/main'.
To https://github.com/dudeakira6-art/Doncrepe.git
 * [new branch] main -> main
```

Esto confirma que el proyecto ya esta publicado en GitHub.

## 10. Como demostrar el uso de Git

Para la exposicion o entrega se puede demostrar de estas formas:

- Abrir GitHub y mostrar el repositorio `dudeakira6-art/Doncrepe`.
- Entrar a la seccion **Commits** y mostrar los avances guardados.
- Abrir NetBeans y mostrar que el proyecto esta versionado.
- Ejecutar `git log --oneline` para ver el historial local.
- Ejecutar `git status` para mostrar que no hay cambios pendientes.
- Explicar que cada commit representa una etapa del sistema.

Comandos de demostracion:

```bat
git status
git log --oneline
git remote -v
git branch -vv
```

## 11. Flujo recomendado para proximos avances

Cada vez que se mejore el proyecto:

```bat
git status
git add .
git commit -m "Describe el avance realizado"
git push origin main
```

Ejemplo:

```bat
git add .
git commit -m "Mejora validacion de pedidos delivery"
git push origin main
```

## 12. Conclusion

Git se incorporo correctamente al proyecto DonCrepePOS. El sistema tiene historial local de cambios y tambien esta publicado en GitHub.

Esto permite cumplir con la rubrica porque los avances del proyecto quedan registrados, organizados y disponibles en la plataforma GitHub.
