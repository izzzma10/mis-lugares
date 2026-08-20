# Organización del equipo — "Mis Lugares"

> Documento de reparto de trabajo entre los integrantes del proyecto.
> Para el contexto técnico del proyecto, ver `AGENTS.md`.

---

## 1. Cómo se divide el trabajo

El proyecto se separa en capas (Clean Architecture, ver `AGENTS.md` sección 4). Una persona arma el núcleo (las capas internas, de las que depende todo lo demás) y el resto arma una pantalla cada uno.

```
com.example.mislugares/
├── modelo/        <- NÚCLEO
├── datos/         <- NÚCLEO
├── casos_uso/     <- NÚCLEO
└── presentacion/  <- pantallas, una por persona
```

Regla de dependencia: las pantallas (`presentacion`) llaman a `casos_uso`, nunca al revés.

**Prioridad alta:** definir el contrato de `casos_uso` cuanto antes, para que las pantallas puedan programarse contra esa interfaz.

## 2. Núcleo — 1 persona (Ismael)

Responsables de las capas de las que dependen todas las pantallas.

| Capa | Contenido | Estado | Páginas del libro |
|---|---|---|---|
| `modelo` | `Lugar`, `GeoPunto`, `TipoLugar` | Hecho | 91-102 (sección 1.13) |
| `datos` | `LugaresBD`, `LugaresLista`, `RepositorioLugares` | Hecho (integrado del PR de Julio, adaptado al modelo real) | 632-669 (sección 9.7) |
| `casos_uso` | Operaciones: listar, guardar, borrar, ver un lugar | Hecho | 214-215 (sección 3.5) |

**Cómo usar `casos_uso` desde una pantalla:**

Todas las pantallas comparten el mismo repositorio a través de `Aplicacion` (así lo que se guarda/borra en una pantalla se ve en las demás):

```java
RepositorioLugares repositorio = ((Aplicacion) getApplication()).repositorio;

// Listar (Pantalla 1)
Map<Integer, Lugar> lugares = new ListarLugares(repositorio).listar();

// Ver un lugar por id (Pantalla 2)
Lugar lugar = new VerLugar(repositorio).ver(id);

// Guardar — crear nuevo (Pantalla 3, formulario vacío)
int id = new GuardarLugar(repositorio).guardarNuevo(lugar);

// Guardar — actualizar existente (Pantalla 3, formulario prellenado)
new GuardarLugar(repositorio).actualizar(id, lugar);

// Borrar
new BorrarLugar(repositorio).borrar(id);
```

Las clases están en el paquete `com.example.mislugares.casos_uso`. Por ahora el repositorio real es `LugaresLista` (en memoria, con los 5 lugares de ejemplo) — cuando se conecte `LugaresBD` (SQLite) de forma definitiva, solo cambia la línea en `Aplicacion.java`, nada en las pantallas.

## 3. Pantallas (capa `presentacion`) — detalle por persona

Cada pantalla se apoya directo en los capítulos correspondientes del libro (`libro-android-11.pdf`). Como referencia de patrón, mirar `VistaLugarActivity.java` + `vista_lugar.xml` (pantalla 2, ya terminada): así se ve una Activity que muestra datos de un `Lugar`.

### Pantalla 1 — Lista de lugares (Julio)

- **Crear:** `app/src/main/java/com/example/mislugares/ListaLugaresActivity.java`
- **Layout:** `app/src/main/res/layout/activity_lista.xml` (ya existe, revisar que sirva)
- **Qué hace:** un `RecyclerView` que muestra todos los lugares guardados (nombre + dirección de cada uno). Necesita un `Adapter` propio (ej. `LugaresAdapter`) — no existe todavía, hay que crearlo.
- **Al tocar un lugar de la lista:** abre `VistaLugarActivity` pasándole el id del lugar (con un `Intent` y `putExtra`).
- **Depende de:** `casos_uso` para pedir la lista completa de lugares (todavía no existe — mientras tanto se puede probar con datos de ejemplo de `LugaresLista.añadeEjemplos()`).
- **Complejidad:** media. Páginas del libro: 260+ (sección 3.10, RecyclerView).

### Pantalla 2 — Ver detalle de un lugar (Angel — terminada)

- `VistaLugarActivity.java` + `vista_lugar.xml`. Ya compila y corre con datos reales.
- Pendiente menor: hoy usa un `Lugar` fijo de prueba (hardcodeado) en vez de recibir el id de un lugar real por `Intent` — falta conectarlo cuando exista la pantalla de lista.
- Páginas del libro: 219-226 (sección 3.6.1).

### Pantalla 3 — Crear / editar lugar (Ale)

- **Crear:** `app/src/main/java/com/example/mislugares/EdicionLugarActivity.java`
- **Layout:** `app/src/main/res/layout/edicion_lugar.xml` (crear) — un formulario con campos de texto para: nombre, dirección, teléfono, url, comentario, valoración (un `RatingBar` o similar), tipo (un selector con los valores de `TipoLugar`).
- **Qué hace:** sirve para dos casos — crear un lugar nuevo (formulario vacío) o editar uno existente (formulario prellenado, recibe el id por `Intent`). Un botón "Guardar" al final.
- **Depende de:** `casos_uso` para guardar/actualizar el lugar (todavía no existe).
- **Complejidad:** media-alta. Páginas del libro: 234-239 (sección 3.6.2).

### Pantalla 4 — Preferencias + mapa (Daniela) — son 3 partes

- **4a. Preferencias de usuario**
  - Crear: `app/src/main/java/com/example/mislugares/PreferenciasActivity.java` + `app/src/main/res/xml/preferencias.xml`
  - Dos opciones configurables ya esperadas por `LugaresBD` (ver `extraeCursor()`): orden de la lista (`"orden"`) y máximo de resultados (`"maximo"`).
  - Páginas del libro: 247+ (sección 3.8).
- **4b. Localización (GPS)**
  - Obtener la ubicación actual del usuario y guardarla en `Aplicacion.posicionActual` (esa clase y ese campo ya existen, ver `app/src/main/java/com/example/mislugares/Aplicacion.java`).
  - Páginas del libro: 482-499 (sección 7.2).
- **4c. Google Maps**
  - Mostrar un mapa con la posición del lugar (dentro de la pantalla de detalle o aparte, a definir).
  - Necesita una API key de Google Maps (ver sección 7.3.1 del libro para sacarla).
  - Páginas del libro: 508-523 (sección 7.3).
  - Complejidad alta, dejar para el final si falta tiempo — las otras dos partes (4a, 4b) son más urgentes.

## 4. Asignación y próximos pasos

| Parte | Asignado a | Próximo paso |
|---|---|---|
| Núcleo (`modelo`/`datos`/`casos_uso`) | Ismael | `casos_uso` listo (ver sección 2) — apoyar al equipo a integrarlo en sus pantallas |
| Pantalla 1 — Lista | Julio | Crear `ListaLugaresActivity` + `Adapter` del RecyclerView (ver sección 3) |
| Pantalla 2 — Detalle | Angel | Conectar con la pantalla de lista (recibir el id por `Intent` en vez del dato de prueba) |
| Pantalla 3 — Crear/editar | Ale | Crear `EdicionLugarActivity` + layout del formulario (ver sección 3) |
| Pantalla 4 — Preferencias + mapa | Daniela | Empezar por 4a (preferencias) y 4b (GPS); 4c (mapa) al final (ver sección 3) |

## 5. Convenciones importantes (para no repetir errores ya vistos)

- **Ruta de los archivos Java:** siempre dentro de `app/src/main/java/com/example/mislugares/<capa>/...` (no en la raíz del repo). Ya pasó una vez que se subieron archivos sueltos en la raíz y Gradle no los compilaba.
- **Ruta de los layouts XML:** siempre dentro de `app/src/main/res/layout/`.
- **Nombre de paquete:** `com.example.mislugares`, siempre.
- **Antes de empezar una pantalla nueva:** confirmar que el método de `casos_uso` que se necesita ya existe (o pedirlo). No inventar acceso directo a `datos` desde una pantalla.

## 6. Configurar tu propio entorno (para cualquier integrante + su IA)

La mayoría del equipo usa **Windows** — instrucciones para eso. (Ismael usa Linux/Fedora, ver nota al final).

Todo esto en **PowerShell** (o Git Bash, si lo tienes instalado):

1. **Android Studio** (más simple en Windows, trae JDK y facilita el resto): descargar de https://developer.android.com/studio e instalar normal. Al abrirlo la primera vez, deja que baje el SDK (se instala solo en `C:\Users\<tu_usuario>\AppData\Local\Android\Sdk`).

2. **Clonar el repo:**
   ```powershell
   git clone https://github.com/izzzma10/mis-lugares.git
   cd mis-lugares
   ```

3. **Crear `local.properties`** apuntando a tu SDK (ajusta `<tu_usuario>`):
   ```powershell
   echo "sdk.dir=C:\\Users\\<tu_usuario>\\AppData\\Local\\Android\\Sdk" > local.properties
   ```

4. **Compilar** (usa el `.bat`, no el `gradlew` sin extensión):
   ```powershell
   .\gradlew.bat assembleDebug
   ```

5. **Abrir el proyecto directo en Android Studio** también funciona: `File > Open`, seleccionar la carpeta `mis-lugares` — Android Studio detecta y sincroniza todo solo, sin pasos manuales de SDK.

6. **Para que tu IA (opencode u otra) pueda hacer `git fetch`/`push` por ti**, instala y autentica GitHub CLI una vez:
   ```powershell
   winget install GitHub.cli
   gh auth login --hostname github.com --git-protocol https --web
   gh auth setup-git
   ```

**Nota (solo Linux/Fedora, caso de Ismael):** JDK vía `sudo dnf install java-latest-openjdk-devel`, SDK vía `cmdline-tools` descargado manual en `~/Android/Sdk`, build con `./gradlew` (sin `.bat`). Detalle completo en `AGENTS.md` sección 3.

## 7. Flujo de trabajo en Git

Repo: `https://github.com/izzzma10/mis-lugares.git`

1. Antes de empezar algo nuevo, traer cambios:
   ```bash
   git pull
   ```
2. Trabajar siempre en una rama propia (nunca commitear directo a `main`):
   ```bash
   git checkout -b nombre/tarea      # ej: daniela/pantalla-mapa
   ```
3. Al terminar una parte:
   ```bash
   git add .
   git commit -m "que hice"
   git push -u origin nombre/tarea
   ```
4. Abrir un Pull Request en GitHub. Alguien del equipo revisa antes de mergear a `main`.
