# Uso de Git en DonCrepePOS

Esta guia adapta el flujo del documento `Ejemplo.docx` al proyecto DonCrepePOS.

## 1. Configuracion inicial

Configure el autor de los commits:

```bat
git config --global user.name "Tu Nombre"
git config --global user.email "tu_correo@example.com"
```

## 2. Crear repositorio local

Desde la carpeta `DonCrepePOS`:

```bat
git init
```

Esto crea la carpeta oculta `.git`, donde Git guarda el historial del proyecto.

## 3. Primer commit

```bat
git add .
git commit -m "Version inicial del sistema DonCrepePOS"
```

## 4. Ramas de trabajo

Rama principal estable:

```bat
git branch -M main
```

Crear una rama para una mejora:

```bat
git branch feature/interfaz
git checkout feature/interfaz
```

Volver a `main`:

```bat
git checkout main
```

## 5. Flujo basico diario

Ver cambios:

```bat
git status
```

Preparar cambios:

```bat
git add .
```

Confirmar cambios:

```bat
git commit -m "Mejora interfaz de mesas y productos"
```

Ver historial:

```bat
git log --oneline --graph --decorate
```

## 6. Repositorio remoto

Agregar remoto:

```bat
git remote add origin URL_DEL_REPOSITORIO
```

Subir cambios:

```bat
git push origin main
```

Obtener cambios:

```bat
git pull origin main
```

## 7. Etiquetas

Crear una version:

```bat
git tag -a v1.0 -m "Lanzamiento de la version 1.0"
git push origin v1.0
```

## 8. Deshacer cambios

Revertir un commit manteniendo historial:

```bat
git revert <commit-id>
```

Restablecer a un commit anterior:

```bat
git reset --hard <commit-id>
```

Use `reset --hard` con cuidado porque descarta cambios locales.

## 9. Conflictos

Cuando Git marca un conflicto:

1. Abra el archivo afectado.
2. Busque marcas como `<<<<<<<`, `=======`, `>>>>>>>`.
3. Edite el archivo dejando la version correcta.
4. Confirme la resolucion:

```bat
git add archivo_afectado.java
git commit -m "Resuelve conflicto en archivo afectado"
```

## 10. Buenas practicas

- Hacer commits pequenos y claros.
- No subir `build/`, `dist/`, `reportes/` ni archivos temporales.
- Crear ramas para cambios grandes.
- Usar `git status` antes de cada commit.
- Usar mensajes descriptivos.
