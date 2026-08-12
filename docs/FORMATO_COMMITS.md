# Guia de Estandar para Mensajes de Commit (Conventional Commits)

Este documento establece la convencion para el formato y redaccion de los mensajes de commit en los proyectos del equipo. Utilizar un estandar claro facilita la lectura del historial, la automatizacion del control de versiones (p. ej., semver) y la generacion automatica de changelogs.

---

## Estructura General de un Commit

```text
<tipo>(<alcance opcional>): <descripcion corta en presente o imperativo>

[cuerpo del commit opcional: explicacion detallada del cambio]

[pie del commit opcional: referencias a tickets, Breaking Changes, etc.]
```

### Ejemplo Corto
```text
feat(auth): agregar validacion de token JWT en middleware
```

### Ejemplo Completo
```text
fix(database): corregir fuga de conexiones en pool de MySQL

Se optimizo la liberacion de conexiones inactivas en la clase ConnectionPool
para evitar saturacion bajo carga alta de peticiones concurrentes.

Closes #142
```

---

## Tipos de Commits (Commit Types)

| Tipo | Descripcion | Ejemplo |
| :--- | :--- | :--- |
| **`feat`** | **Nueva funcionalidad** para el usuario final o sistema. | `feat(ui): agregar boton de exportar a Excel` |
| **`fix`** | **Correccion de un bug** o error existente. | `fix(cart): corregir calculo de impuestos en carrito` |
| **`docs`** | **Documentacion** (README, codigo docstrings, wikis). | `docs(readme): actualizar instrucciones de instalacion` |
| **`style`** | **Formato y estilo** (espacios, comas, indentacion; sin cambios en logica). | `style(lint): aplicar formato PEP8 a controladores` |
| **`refactor`** | **Refactorizacion** de codigo (ni anade funcionalidad ni corrige bugs). | `refactor(auth): simplificar logica de hashing` |
| **`test`** | Agregar, actualizar o corregir **pruebas unitarias o de integracion**. | `test(order): agregar tests unitarios para checkout` |
| **`chore`** | **Mantenimiento general**, tareas repetitivas o ajustes menores. | `chore(gitignore): ignorar carpeta de archivos temporales` |
| **`perf`** | Cambios enfocados en **mejora de rendimiento** y velocidad. | `perf(queries): indexar columna email en tabla usuarios` |
| **`build`** | Cambios en el **sistema de compilacion** o dependencias externas. | `build(deps): actualizar numpy a la version 1.26` |
| **`ci`** | Cambios en configuraciones de **Integracion Continua / CD** (GitHub Actions, etc.). | `ci(actions): configurar workflow de testing automatico` |
| **`revert`** | **Revertir un commit anterior**. | `revert: revertir commit "feat(auth): agregar login OAuth"` |
| **`wip`** | *Work In Progress* (Trabajo en progreso, no listo para produccion). | `wip(api): implementacion inicial de endpoint v2` |
| **`sec`** | Mejoras especificas de **seguridad** o parches de vulnerabilidades. | `sec(headers): agregar cabeceras de seguridad CSP` |

---

## Reglas y Buenas Practicas

1. **Usa el modo imperativo:** Escribe el mensaje como si fuera una orden o accion directa.
   * Correcto: `feat: agregar pantalla de login`
   * Incorrecto: `feat: agregue pantalla de login` / `agregando pantalla`
2. **Primera letra en minuscula:** Mantén la descripcion despues del tipo en minusculas para mantener consistencia.
3. **Sin punto final:** No agregues punto final `.` al titulo del commit.
4. **Claridad ante todo:** El titulo no debe exceder de 50 a 72 caracteres. Si necesitas dar mas contexto, utiliza el cuerpo del commit.

---

## Comandos Rapidos de Uso en Git

```bash
# Commit simple
git commit -m "feat(ui): agregar selector de tema oscuro"

# Commit con descripcion detallada (usando dos flags -m)
git commit -m "fix(core): corregir manejo de excepciones en API" -m "Se agrego un bloque try-except en el controlador principal para prevenir fallos 500 descontrolados."
```

---

> *Documento estandarizado para control de calidad en mensajes de commit.*
