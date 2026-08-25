# Contexto del Proyecto "Mis Lugares"

## 1. Qué es el proyecto

- **Aplicación:** "Mis Lugares" (gestor de lugares: nombre, dirección, posición GPS, teléfono, valoración...).
- **Lenguaje:** Java (Android).
- **Referencia principal:** libro PDF `libro-android-11.pdf` (*El gran libro de Android 11*, Jesús Tomás Gironés). Aplicación de ejemplo del libro.
- **Equipo:** 5 personas, división de trabajo en `EQUIPO.md`.

## 2. Estructura del repositorio

- `app/` (raíz del repo) — único proyecto: Android Gradle real (`build.gradle`, `settings.gradle`, `gradlew`, módulo `app/` con `AndroidManifest.xml`, `src/main/java/...`, `src/main/res/...`). Este es el que genera el `.apk`.
- El sandbox de Java plano (`mislugares/`, usado en las primeras lecciones para aprender clases/constructores con `javac`/`java`) y `hola_mundo.java` (la primera lección) se eliminaron una vez que el modelo real ya vivía en `app/` — eran duplicados sin uso, no los usaba el build.

## 3. Estado actual (última sesión: 23-ago-2026)

### Navegación entre pantallas conectada (nuevo, 23-ago-2026)
- Antes de esta sesión, `EdicionLugarActivity` (editar), `MapaLugarActivity` y `PreferenciasActivity` ya existían y compilaban, pero no había ningún botón/menú en la app que las abriera.
- `VistaLugarActivity.java`: se agregaron dos botones nuevos en `vista_lugar.xml` (`btn_editar`, `btn_mapa`) conectados con `Intent` — "Editar" abre `EdicionLugarActivity` pasando el id del lugar (`EdicionLugarActivity.EXTRA_ID`); "Mapa" abre `MapaLugarActivity` pasando el id (`MapaLugarActivity.EXTRA_ID_LUGAR`).
- `MainActivity.java` (pantalla de lista): se agregó menú de opciones (`res/menu/menu_lista.xml`, `onCreateOptionsMenu`/`onOptionsItemSelected`) con la opción "Preferencias", que abre `PreferenciasActivity`.
- `./gradlew assembleDebug` compila correctamente con estos tres cambios.
- Pendiente (no tocado esta sesión, ver `docs/EQUIPO.md` sección 3 para detalle): confirmación antes de eliminar en `EdicionLugarActivity`, validación de teléfono/latitud/longitud (hoy toma `0` en silencio si el texto no es válido).

### Intenciones implícitas en VistaLugarActivity (nuevo, 25-ago-2026)
- Siguiendo la sección 3.11 del libro ("Las intenciones"), se conectaron dos íconos que ya existían en `vista_lugar.xml` sin usar: el botón `btnCompartir` y el `TextView` de teléfono.
- **Compartir** (`ACTION_SEND`): envía nombre + URL del lugar como texto plano a cualquier app que sepa manejarlo (WhatsApp, Gmail, SMS...).
- **Llamar** (`ACTION_DIAL`, tocando el número de teléfono): abre el marcador con el número precargado. No pide permiso porque no llama directo, el usuario confirma — a diferencia de `ACTION_CALL`, que sí requiere el permiso `CALL_PHONE` (queda como práctica opcional del libro, no implementada).
- Pendiente del mismo apartado del libro: abrir la dirección del lugar en una app de mapas externa (`ACTION_VIEW` con URI `geo:`).
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
- Flujo que funciona: `./gradlew assembleDebug` (compilar) → lanzar emulador manualmente (`~/Android/Sdk/emulator/emulator -avd mislugares_avd`, sin `-no-window`) → `adb install -r app/build/outputs/apk/debug/app-debug.apk` → `adb shell am start -n com.example.mislugares/.VistaLugarActivity`.
- Pendiente: llevar este mismo flujo a las otras pantallas (lista, crear/editar) cuando el equipo integre su trabajo.

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

- **Sistema:** Fedora (dnf), Linux.
- **Ruta del proyecto:** `/home/izma/Documentos/facu/dismov/app/`
- **JDK:** solo hay `java-25-openjdk-devel` en los repos de dnf (no hay 17). Se instaló el 25.
- **sudo:** requiere contraseña del usuario.
- **PDF del libro:** no está en la carpeta del proyecto (pesa 156 MB, excluido vía `.gitignore`). Hay copia en caché de VS Code si se necesita.
- **Repo remoto:** `https://github.com/izzzma10/mis-lugares.git`
- **Ramas del equipo:** `nucleo` (Ismael), `angel/pantalla-detalle`, `lista` (Julio — construyó capa `datos` en vez de la pantalla, revisar solape con núcleo), `Ale/crear-editar` (sin código aún).

## 7. Comandos útiles

```bash
# --- Proyecto Android real (raíz del repo) ---
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
./gradlew assembleDebug          # genera app/build/outputs/apk/debug/app-debug.apk

# Crear/listar AVD (emulador)
export ANDROID_HOME=~/Android/Sdk
~/Android/Sdk/cmdline-tools/latest/bin/avdmanager create avd -n mislugares_avd -k "system-images;android-34;google_apis;x86_64" -d "pixel_5"
~/Android/Sdk/emulator/emulator -avd mislugares_avd
```
