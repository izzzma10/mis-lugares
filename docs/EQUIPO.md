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
| `datos` | `LugaresBD`, `LugaresLista`, `RepositorioLugares` | Hecho, **y `LugaresBD` (SQLite) ya está conectada de verdad** (25-ago-2026) — los datos persisten entre aperturas de la app | 632-669 (sección 9.7) |
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

Las clases están en el paquete `com.example.mislugares.casos_uso`. El repositorio real es `LugaresBD` (SQLite, conectado el 25-ago-2026) — los datos persisten en el teléfono entre aperturas de la app. Ver detalle en `AGENTS.md` sección 3, "`LugaresBD` (SQLite) conectada de verdad".

## 3. Pantallas (capa `presentacion`) — estado actual (25-ago-2026)

**Todas las pantallas están terminadas y funcionando.** Esta sección quedó bastante desactualizada durante el desarrollo (nombres de archivo distintos a los que se planearon al principio) — el detalle real, con fechas y explicación de cada cambio, está en `AGENTS.md` sección 3. Acá un resumen rápido, con los nombres de archivo **reales**:

- **`MenuActivity.java`** (`activity_menu.xml`) — pantalla de arranque, la única declarada como `LAUNCHER` en el manifest. Dos botones: "Mis Lugares" (abre la lista) y "Agregar lugar" (abre `EdicionLugarActivity` en modo crear).
- **Pantalla 1 — Lista de lugares:** `MainActivity.java` (no `ListaLugaresActivity`, como se planeó al principio) + `AdaptadorLugares.java` (no `LugaresAdapter`) + `activity_lista.xml`. `RecyclerView` funcionando, con menú de opciones (⋮) para abrir Preferencias.
- **Pantalla 2 — Ver detalle:** `VistaLugarActivity.java` + `vista_lugar.xml`. Muestra los datos reales del lugar (recibe el id por `Intent`), con: valoración editable arrastrando estrellas, botón Compartir, botón Editar, botón Mapa, teléfono/dirección/URL tocables (abren marcador, mapa externo, y navegador respectivamente), y fotos (cámara, galería, eliminar foto).
- **Pantalla 3 — Crear/editar:** `EdicionLugarActivity.java` + `activity_edicion_lugar.xml`. Formulario completo con validación (nombre/dirección obligatorios, teléfono exactamente 10 dígitos, lat/long deben ser números válidos si se cargan) y confirmación antes de eliminar.
- **Pantalla 4 — Preferencias + mapa:**
  - `PreferenciasActivity.java` + `res/xml/preferencias.xml` — orden de lista y máximo de resultados, ya usados por `LugaresBD.extraeCursor()`.
  - `GestorLocalizacion.java` — obtiene el GPS del usuario y lo guarda en `Aplicacion.posicionActual`.
  - `MapaLugarActivity.java` + `activity_mapa_lugar.xml` — **ya no usa Google Maps**, se cambió a `osmdroid` (mapas de OpenStreetMap vía CARTO) porque no se pudo crear una cuenta de facturación de Google Cloud (mismo error con dos cuentas distintas del equipo). No necesita ninguna API key. Ver detalle completo en `AGENTS.md`.

## 4. Pendientes (opcionales, baja prioridad)

Todo lo "core" que pide el libro para Mis Lugares está hecho. Queda, si alguien quiere sumarlo:

| Pendiente | Detalle |
|---|---|
| Confirmación al borrar solo la foto | Distinto de la confirmación al borrar el lugar completo, que ya está. Práctica opcional del libro, sección 3.11.1. |
| Permiso `ACTION_CALL` | Llamar directo sin que el usuario confirme (hoy usamos `ACTION_DIAL`, que no lo necesita). Práctica opcional del libro. |
| `MainActivity` como `Fragment` | Alternativa arquitectónica del Anexo B del libro — no cambia lo que hace la app. |

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
