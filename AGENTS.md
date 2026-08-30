# Contexto del Proyecto "Mis Lugares"

## 1. Qué es el proyecto

- **Aplicación:** "Mis Lugares" (gestor de lugares: nombre, dirección, posición GPS, teléfono, valoración...).
- **Lenguaje:** Java (Android).
- **Referencia principal:** libro PDF `libro-android-11.pdf` (*El gran libro de Android 11*, Jesús Tomás Gironés). Aplicación de ejemplo del libro.
- **Equipo:** 5 personas, división de trabajo en `EQUIPO.md`.

## 2. Estructura del repositorio

- `app/` (raíz del repo) — único proyecto: Android Gradle real (`build.gradle`, `settings.gradle`, `gradlew`, módulo `app/` con `AndroidManifest.xml`, `src/main/java/...`, `src/main/res/...`). Este es el que genera el `.apk`.
- El sandbox de Java plano (`mislugares/`, usado en las primeras lecciones para aprender clases/constructores con `javac`/`java`) y `hola_mundo.java` (la primera lección) se eliminaron una vez que el modelo real ya vivía en `app/` — eran duplicados sin uso, no los usaba el build.

## 3. Estado actual (última sesión: 25-ago-2026)

### Navegación entre pantallas conectada (nuevo, 23-ago-2026)
- Antes de esta sesión, `EdicionLugarActivity` (editar), `MapaLugarActivity` y `PreferenciasActivity` ya existían y compilaban, pero no había ningún botón/menú en la app que las abriera.
- `VistaLugarActivity.java`: se agregaron dos botones nuevos en `vista_lugar.xml` (`btn_editar`, `btn_mapa`) conectados con `Intent` — "Editar" abre `EdicionLugarActivity` pasando el id del lugar (`EdicionLugarActivity.EXTRA_ID`); "Mapa" abre `MapaLugarActivity` pasando el id (`MapaLugarActivity.EXTRA_ID_LUGAR`).
- `MainActivity.java` (pantalla de lista): se agregó menú de opciones (`res/menu/menu_lista.xml`, `onCreateOptionsMenu`/`onOptionsItemSelected`) con la opción "Preferencias", que abre `PreferenciasActivity`.
- `./gradlew assembleDebug` compila correctamente con estos tres cambios.
- Ya no queda ningún pendiente de la lista original — ver más abajo, `LugaresBD` quedó conectada esta misma sesión.

### `LugaresBD` (SQLite) conectada de verdad (nuevo, 25-ago-2026)
- `Aplicacion.java`: `repositorio` ahora es `new LugaresBD(this)` en vez de `new LugaresLista()`. Los datos persisten de verdad entre aperturas de la app (antes se perdían siempre, porque vivían en memoria).
- `LugaresBD.java` tenía dos bugs que nunca se habían notado porque el archivo nunca se había usado en la app real:
  - `tamaño()` devolvía `0` hardcodeado — se arregló con una consulta real (`SELECT COUNT(*) FROM lugares`).
  - Riesgo de seguridad: `nuevo()`, `actualiza()` y `añade()` armaban el SQL concatenando texto a mano (un nombre con un apóstrofe, ej. `McDonald's`, rompía la sentencia). Se reescribieron los 3 usando `ContentValues` + `insert()`/`update()` de Android, que arman la consulta de forma segura sin concatenar nada — de paso, `nuevo()` quedó más simple, porque `insert()` devuelve directo el id de la fila nueva (ya no hace falta el truco viejo de insertar y después buscar por fecha).
  - Los datos de ejemplo de `onCreate()` (los 5 lugares hardcodeados que se cargan la primera vez que se crea la base) se actualizaron de los viejos de España (Escuela Politécnica de Gandía, etc.) a los mismos 5 de Monterrey que usa `LugaresLista`, y también arman el `ContentValues` con `valoresDe()` en vez de concatenar SQL a mano.
- **Bug encontrado probando en dispositivo real** (no se veía con `LugaresLista`): `VistaLugarActivity.onResume()` decidía si un lugar seguía existiendo comparando `id < repositorio.tamaño()` — funciona por casualidad con `LugaresLista` porque ahí los ids son posiciones de lista (0-indexados, sin huecos), pero **no** con SQLite, donde los `_id` (`AUTOINCREMENT`) arrancan en 1 y nunca se renumeran al borrar. Con 5 lugares, el último (`_id = 5`) fallaba la comparación `5 < 5` y la pantalla se cerraba sola con "Este lugar ya no existe", aunque sí existía. Se corrigió reemplazando la comparación por un `try { ver(id); } catch (Exception e) { ... }` — intenta buscar el lugar de verdad, y solo si eso falla (con cualquier excepción, sea de `LugaresLista` o `LugaresBD`) se considera que no existe.
- `./gradlew assembleDebug` compila correctamente. Probado a fondo en dispositivo físico: listar, ver detalle, crear, editar, borrar, y cerrar/reabrir la app para confirmar persistencia real — todo funciona.

### Validación de campos en EdicionLugarActivity (25-ago-2026)
- **`Lugar.telefono` cambió de `int` a `String`** — bug real: un teléfono de 10 dígitos que empiece en 3 o más se desborda en un `int` (máximo `2 147 483 647`). Un teléfono tampoco es "un número" con el que se opera matemáticamente, es un identificador de texto. Cambio en cascada por toda la app: `Lugar.java` (campo/constructor/getter/setter), `LugaresLista.java` (`creaLugar` y sus 5 llamadas), `VistaLugarActivity.lugarDePrueba()`, y `LugaresBD.java` (columna `telefono` de `INTEGER` a `TEXT`, `extraeLugar()` lee con `getString()` en vez de `getInt()`, y las sentencias `INSERT`/`UPDATE` armadas a mano ahora encierran el valor entre comillas simples).
- `EdicionLugarActivity.guardar()`: el teléfono ahora se valida con `telefono.matches("\\d{10}")` — tiene que ser exactamente 10 dígitos, si no se corta el guardado con un `Toast` de error.
- Latitud/longitud: son opcionales (vacío = sin GPS, igual que antes), pero si el usuario escribe algo que no es un número válido, ahora también se corta el guardado con error (`esNumeroValido()`), en vez de guardar `0` en silencio como pasaba antes.
- `./gradlew assembleDebug` compila correctamente, probado en dispositivo físico.

### Confirmación al eliminar + fotos en Mis Lugares (25-ago-2026)
- `EdicionLugarActivity.eliminar()`: ahora muestra un `AlertDialog` de confirmación ("¿Seguro que querés eliminar este lugar?") antes de borrar. Ya no borra al primer toque del botón.
- `VistaLugarActivity.java`: se conectaron los íconos de cámara y galería que ya estaban en `vista_lugar.xml` sin usar (sección 3.11.1 del libro), más un ícono nuevo para eliminar la foto asignada.
  - **Galería:** `ActivityResultContracts.GetContent()` — abre el selector de archivos del sistema, sin pedir permiso de almacenamiento (Storage Access Framework).
  - **Cámara:** `ActivityResultContracts.TakePicture()` — delega la captura en la app de cámara del dispositivo; no pide permiso de `CAMERA` porque la app no accede al hardware directamente. Necesita un `FileProvider` (declarado en `AndroidManifest.xml` + `res/xml/file_paths.xml`, nuevo) para darle a la app de cámara un lugar seguro donde guardar la foto.
  - Se usa la API moderna `registerForActivityResult` (no el `onActivityResult` que enseña el libro, que está deprecado) — por eso `lugar`, `id` y `repositorio` pasaron de variables locales de `onCreate` a campos de la clase, para que los callbacks (declarados como campos) puedan acceder a ellos.
  - `mostrarFoto()` ahora distingue entre nombre de drawable (los 5 lugares de ejemplo, ej. `"foto_epsg"`) y una URI real (`content://...`) elegida por el usuario.
- `./gradlew assembleDebug` compila correctamente, probado en dispositivo físico.

### Intenciones implícitas en VistaLugarActivity (25-ago-2026)
- Siguiendo la sección 3.11 del libro ("Las intenciones"), se conectaron dos íconos que ya existían en `vista_lugar.xml` sin usar: el botón `btnCompartir` y el `TextView` de teléfono.
- **Compartir** (`ACTION_SEND`): envía nombre + URL del lugar como texto plano a cualquier app que sepa manejarlo (WhatsApp, Gmail, SMS...).
- **Llamar** (`ACTION_DIAL`, tocando el número de teléfono): abre el marcador con el número precargado. No pide permiso porque no llama directo, el usuario confirma — a diferencia de `ACTION_CALL`, que sí requiere el permiso `CALL_PHONE` (queda como práctica opcional del libro, no implementada).
- **Ver en el mapa** (`ACTION_VIEW` con URI `geo:`, tocando la dirección): abre la app de mapas que el usuario tenga instalada. Si el lugar tiene coordenadas usa `geo:lat,lon`; si son `0,0` cae a `geo:0,0?q=<dirección>` para que igual busque por texto. Implementado en `VistaLugarActivity` (listener de `tvDireccion`).
- **Abrir la web** (`ACTION_VIEW` con la URL del lugar, tocando un `TextView` nuevo): se agregó el campo `url` a `vista_lugar.xml` (no existía) y su listener en `VistaLugarActivity`. Con esto quedan las 4 intenciones implícitas de la sección 3.11 del libro completas (Compartir, Llamar, Ver en mapa, Abrir la web) — era el último ítem "core" pendiente de todo lo que pide el libro para Mis Lugares.
- `./gradlew assembleDebug` compila correctamente, probado en dispositivo físico.

### Mapa: se abandonó Google Maps por osmdroid + CARTO (nuevo, 23-ago-2026)
- Se intentó configurar `MAPS_API_KEY` leyéndola desde `local.properties` (en vez de hardcodeada en `app/build.gradle`, ya corregido y funcionando) para usar Google Maps SDK, pero la creación de la cuenta de facturación de Google Cloud falló repetidamente con el error `[OR_BACR2_44]` — probado con dos cuentas de Google distintas (de dos personas del equipo), con tarjetas y RFCs distintos, mismo error las dos veces. No se pudo resolver del lado de Google/banco.
- **Decisión: se reemplazó Google Maps por `osmdroid`** (librería open source, sin API key ni facturación). Cambios: dependencia `org.osmdroid:osmdroid-android:6.1.20` en `app/build.gradle`, permiso `INTERNET` agregado en `AndroidManifest.xml`, `activity_mapa_lugar.xml` ahora usa `<org.osmdroid.views.MapView>` en vez del `<fragment>` de Google, y `MapaLugarActivity.java` reescrito con las clases de osmdroid (`MapView`, `GeoPoint`, `Marker`).
- **Ojo con la fuente de tiles (las imágenes del mapa):** ni `TileSourceFactory.MAPNIK` (servidor oficial de OpenStreetMap) ni `TileSourceFactory.WIKIMEDIA` funcionan — ambos devuelven 403, están restringidos a uso interno de sus organizaciones (confirmado en logs de `adb logcat`, no es un problema de configuración). Se terminó usando una fuente manual (`XYTileSource`) apuntando a los tiles públicos de **CARTO** (`basemaps.cartocdn.com/rastertiles/voyager/`), que sí permite uso libre de apps externas. Confirmado funcionando de verdad en dispositivo físico (no emulador).
- Como quedó sin usar: la dependencia `play-services-maps` y el mecanismo de `MAPS_API_KEY` en `build.gradle`/`local.properties`/`AndroidManifest.xml` siguen en el proyecto (no rompen nada, por si se retoma Google Maps más adelante), pero no se están usando.

### Capa `casos_uso` (20-ago-2026)
- Paquete `app/src/main/java/com/example/mislugares/casos_uso/`: `ListarLugares`, `VerLugar`, `GuardarLugar`, `BorrarLugar`. Cada uno recibe un `RepositorioLugares` por constructor.
- `RepositorioLugares` ganó un método `todos()` (devuelve `Map<Integer, Lugar>` con el id de cada uno) para que `ListarLugares` pueda listar con ids reales, no solo posiciones. Implementado en `LugaresLista` y `LugaresBD`.
- `Aplicacion` ahora expone `repositorio` (instancia compartida de `LugaresLista`, con los 5 lugares de ejemplo) para que todas las pantallas trabajen sobre los mismos datos. Ver ejemplo de uso en `docs/EQUIPO.md` sección 2.
- Pendiente de verificar: compilar con `./gradlew assembleDebug` en el entorno de Ismael (esta sesión no tuvo Android SDK disponible para compilar/probar).

### Herramientas instaladas
| Herramienta | Detalle |
|---|---|
| JDK 25 (OpenJDK) | `java-25-openjdk-devel`. `javac 25.0.4`, `java 25.0.4` |
| VS Code | Instalado (`code`) |
| Android Studio | Instalado vía Flatpak (`com.google.AndroidStudio`). Ejecutar: `flatpak run com.google.AndroidStudio` |
| Android SDK | En `~/Android/Sdk` (cmdline-tools, platform-tools, platforms;android-34, build-tools;34.0.0, emulator, system-images;android-34;google_apis;x86_64) |
| Gradle | 9.7.0 (necesario por JDK 25; Gradle 8.x no soporta esa versión de Java). Wrapper ya generado (`./gradlew`) |

### Avance del proyecto Android real (`app/`)
- Proyecto Gradle armado desde cero: `settings.gradle`, `build.gradle` (raíz y módulo `app`), `AndroidManifest.xml`, `gradlew` con wrapper (Gradle 9.7.0).
- `VistaLugarActivity.java`: crea un `Lugar`/`GeoPunto` de prueba y llena la pantalla `vista_lugar.xml` con esos datos reales (nombre, dirección, teléfono, GPS, valoración).
- Layout `vista_lugar.xml`: tomado de la rama `angel/pantalla-detalle` (ya construido por Angel, coincide con la pantalla del libro).
- Drawables placeholder creados a mano (`ic_tipo.xml`, `foto_epsg.xml`) porque el layout los referenciaba y no existían.
- **`./gradlew assembleDebug` compila correctamente y genera un APK real** en `app/build/outputs/apk/debug/app-debug.apk`.
- **App corriendo de verdad confirmada.** El emulador falla dentro del sandbox de la IA (crashea siempre, 3 intentos), pero corre perfecto cuando Ismael lo lanza desde su propia sesión/terminal (fuera del sandbox). Captura real en `docs/referencias/vista_lugar_real.png`.
- Flujo que funciona: `./gradlew assembleDebug` (compilar) → lanzar emulador manualmente (`~/Android/Sdk/emulator/emulator -avd mislugares_avd`, sin `-no-window`) → `adb install -r app/build/outputs/apk/debug/app-debug.apk` → `adb shell am start -n com.example.mislugares/.presentacion.MenuActivity`.
- **Corregido 25-ago-2026:** este flujo terminaba en `am start -n com.example.mislugares/.VistaLugarActivity`, que ya no funciona por dos motivos: la clase se movió al paquete `.presentacion`, y quedó con `android:exported="false"` en el `AndroidManifest.xml`. Hoy la única activity que se puede abrir con `am start` desde afuera es `MenuActivity` (la de arranque, `exported="true"`); para llegar a las demás hay que navegar desde el menú dentro de la app.
- **Navegación completa (verificado 25-ago-2026).** Todas las pantallas son alcanzables desde el menú de arranque, así que ya no hace falta abrir ninguna suelta con `am start`:

```
MenuActivity  (launcher, la única con exported="true")
├─ "Mis lugares"   → MainActivity (lista)
│   ├─ tap en item → VistaLugarActivity (detalle)
│   │   ├─ btn_editar  → EdicionLugarActivity
│   │   ├─ btn_mapa    → MapaLugarActivity
│   │   ├─ compartir   → ACTION_SEND  (app externa)
│   │   ├─ teléfono    → ACTION_DIAL  (marcador)
│   │   └─ dirección   → ACTION_VIEW  (geo:, app de mapas)
│   └─ menú ⋮        → PreferenciasActivity
└─ "Agregar lugar" → EdicionLugarActivity
```

## 4. Arquitectura objetivo (según libro, capítulo 3.5)

Clean Architecture simplificada en 4 capas (organizadas en paquetes):

```
com.example.mislugares/
├── modelo/        <- POJOs puros sin dependencias externas: Lugar, GeoPunto, TipoLugar
├── datos/         <- persistencia (bases de datos, archivos, servicios)
├── casos_uso/     <- operaciones del usuario (ver, borrar, compartir lugar...) = interactors
└── presentacion/  <- UI: Activities, Fragments, Adapters
```

Regla de dependencia: las capas internas no dependen de las externas. `modelo` es la más interna (Java puro, sin imports de Android).

## 5. Plan de lecciones y progreso

| # | Lección | Estado |
|---|---|---|
| 0 | Instalar JDK | Completada |
| 1 | Primer programa: estructura, `main`, compilar y ejecutar | Completada |
| 2 | Variables y tipos de datos | Pendiente |
| 3 | Condiciones (`if`/`else`) y bucles (`for`, `while`) | Pendiente |
| 4 | Métodos/funciones | Pendiente |
| 5 | Clases y objetos (POO) | En curso (vía `GeoPunto`/`Lugar`) |
| 6 | Armar capa `modelo` de Mis Lugares (Lugar, GeoPunto, TipoLugar) | En curso |
| 7 | Colecciones (List/ArrayList) | Pendiente |
| 8 | Capa `datos` (persistencia) | Pendiente |
| 9 | Capa `casos_uso` | Completada |
| 10 | Configurar Android SDK + Gradle | Completada |
| 11 | Capa `presentacion` (UI Android) | En curso (pantalla detalle armada y compilando a APK) |

## 6. Datos técnicos del entorno

### Entorno original — Fedora (Linux)

- **Sistema:** Fedora (dnf), Linux.
- **Ruta del proyecto:** `/home/izma/Documentos/facu/dismov/app/`
- **JDK:** solo hay `java-25-openjdk-devel` en los repos de dnf (no hay 17). Se instaló el 25.
- **sudo:** requiere contraseña del usuario.
- **PDF del libro:** no está en la carpeta del proyecto (pesa 156 MB, excluido vía `.gitignore`). Hay copia en caché de VS Code si se necesita.
- **Repo remoto:** `https://github.com/izzzma10/mis-lugares.git`
- **Ramas del equipo:** `nucleo` (Ismael), `angel/pantalla-detalle`, `lista` (Julio — construyó capa `datos` en vez de la pantalla, revisar solape con núcleo), `Ale/crear-editar` (sin código aún).

### Entorno Windows 10 (agregado 25-ago-2026)

Segunda máquina de Ismael. Todo lo de arriba sigue siendo válido para la máquina Fedora; acá solo se anota lo que cambia.

- **Sistema:** Windows 10 Pro (10.0.19045). Shells disponibles: PowerShell 5.1 y Git Bash.
- **Ruta del proyecto:** `C:\Users\IsmaelAlejandroElizo\GitHub\mis-lugares`
- **JDK:** Temurin **21** (LTS) en `C:\Users\IsmaelAlejandroElizo\.jdks\temurin-21`. `JAVA_HOME` ya está definida a nivel de sistema, no hace falta exportarla en cada terminal. Ojo: no es el JDK 25 de Fedora — Gradle 9.7 funciona con las dos versiones, así que no hay que reinstalar nada.
- **Gradle:** el mismo wrapper del repo (Gradle 9.7.0). En Windows se invoca `gradlew.bat` (PowerShell) o `./gradlew` (Git Bash). No hace falta instalar Gradle aparte.
- **Android SDK: NO está instalado todavía en esta máquina.** No existe `~/Android/Sdk` ni `%LOCALAPPDATA%\Android\Sdk`, `adb` no está en el `PATH` y Android Studio tampoco está instalado. Mientras siga así, `assembleDebug` **no compila acá** (el módulo pide `compileSdk 34`) y los comandos de `docs/COMANDOS ADB.md` no aplican tal cual.
- **`local.properties`:** no existe en esta máquina (está en `.gitignore`, no se versiona). Cuando se instale el SDK hay que crearla en la raíz con la línea `sdk.dir=C:/Users/IsmaelAlejandroElizo/AppData/Local/Android/Sdk` (con barras normales: en un archivo `.properties` la barra invertida de Windows hay que escribirla doble, así que es más simple evitarla). La `MAPS_API_KEY` que `app/build.gradle` lee de ese mismo archivo es opcional: si falta usa el placeholder `TU_API_KEY_AQUI` y no rompe el build, porque el mapa hoy va por osmdroid/CARTO.
- **Rama de trabajo actual:** `ui` (no figuraba en la lista de ramas de más arriba). En `origin` también hay `main-pruebas` y varias ramas `DanielaLomasOyervides-PARTE*` con fragmentos sueltos (`MapaLugarActivity`, `PreferenciasActivity`, layouts, trozos de `dependencies`) todavía sin integrar.

## 7. Comandos útiles

### En Fedora/Linux (máquina original)

```bash
# --- Proyecto Android real (raíz del repo) ---
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
./gradlew assembleDebug          # genera app/build/outputs/apk/debug/app-debug.apk

# Crear/listar AVD (emulador)
export ANDROID_HOME=~/Android/Sdk
~/Android/Sdk/cmdline-tools/latest/bin/avdmanager create avd -n mislugares_avd -k "system-images;android-34;google_apis;x86_64" -d "pixel_5"
~/Android/Sdk/emulator/emulator -avd mislugares_avd
```

### En Windows 10 (segunda máquina de Ismael)

`JAVA_HOME` ya está configurada a nivel de sistema (Temurin 21), así que **no** hace falta exportarla. Todo lo que use `adb`/emulador requiere instalar antes el Android SDK, que hoy falta en esta máquina (ver sección 6).

```powershell
# --- PowerShell, desde la raíz del repo ---
.\gradlew.bat assembleDebug      # genera app\build\outputs\apk\debug\app-debug.apk

# Definir dónde está el SDK (una vez instalado), en cada terminal nueva
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"

# Probar en teléfono real por USB
& "$env:ANDROID_HOME\platform-tools\adb.exe" devices
& "$env:ANDROID_HOME\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
& "$env:ANDROID_HOME\platform-tools\adb.exe" shell am start -n com.example.mislugares/.presentacion.MenuActivity
```

Ojo con PowerShell 5.1: **no** existe `&&` para encadenar comandos (da error de sintaxis). Encadenar con `;`, o con `if ($?) { ... }` si el segundo comando solo debe correr cuando el primero sale bien.

En Git Bash sirven los mismos comandos de la sección de Fedora salvo el `export JAVA_HOME` (ya está) y las rutas del SDK, que hay que apuntar a `$LOCALAPPDATA/Android/Sdk` en vez de `~/Android/Sdk`.
