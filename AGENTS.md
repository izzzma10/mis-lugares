# Contexto del Proyecto "Mis Lugares"

## 1. Qué es el proyecto

- **Aplicación:** "Mis Lugares" (gestor de lugares: nombre, dirección, posición GPS, teléfono, valoración...).
- **Lenguaje:** Java (Android).
- **Referencia principal:** libro PDF `libro-android-11.pdf` (*El gran libro de Android 11*, Jesús Tomás Gironés). Aplicación de ejemplo del libro.
- **Equipo:** 5 personas, división de trabajo en `EQUIPO.md`.

## 2. Estructura del repositorio (dos partes)

- `mislugares/src/com/example/mislugares/modelo/` — proyecto Java plano (sin Android), usado para aprender y probar las clases del modelo por línea de comandos (`javac`/`java`). Tiene `GeoPunto.java`, `Lugar.java`, `Principal.java`.
- `app/` (raíz del repo) — proyecto Android Gradle real: `build.gradle`, `settings.gradle`, `gradlew`, módulo `app/` con `AndroidManifest.xml`, `src/main/java/...`, `src/main/res/...`. Este es el que genera el `.apk`.

Las clases del modelo existen duplicadas en ambos lados por ahora (`mislugares/.../modelo/` y `app/src/main/java/.../modelo/`) — al avanzar, unificar en un solo lugar.

## 3. Estado actual (última sesión: 19-ago-2026)

### Herramientas instaladas
| Herramienta | Detalle |
|---|---|
| JDK 25 (OpenJDK) | `java-25-openjdk-devel`. `javac 25.0.4`, `java 25.0.4` |
| VS Code | Instalado (`code`) |
| Android Studio | Instalado vía Flatpak (`com.google.AndroidStudio`). Ejecutar: `flatpak run com.google.AndroidStudio` |
| Android SDK | En `~/Android/Sdk` (cmdline-tools, platform-tools, platforms;android-34, build-tools;34.0.0, emulator, system-images;android-34;google_apis;x86_64) |
| Gradle | 9.7.0 (necesario por JDK 25; Gradle 8.x no soporta esa versión de Java). Wrapper ya generado (`./gradlew`) |

### Avance de la capa `modelo` (Java plano, en `mislugares/`)
- `GeoPunto.java`: completo (atributos, constructor, getters/setters, `toString()`). Compila.
- `Lugar.java`: completo (9 atributos: nombre, direccion, posicion, foto, telefono, url, comentario, fecha, valoracion). `fecha` se calcula sola en el constructor con `System.currentTimeMillis()`. Constructor, getters/setters y `toString()` completos. Compila.
- `Principal.java`: completo. Crea un `GeoPunto` y un `Lugar`, los imprime. Compila y ejecuta bien.
- `TipoLugar.java`: completo (enum: EDUCACION, BAR, NATURALEZA, COMPRAS, OTROS).
- Se agregó constructor vacío y atributo `tipo` a `Lugar` para poder integrar la capa `datos` de Julio (PR #1).

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
| 9 | Capa `casos_uso` | Pendiente |
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
# --- Java plano (mislugares/) ---
cd mislugares/src
javac com/example/mislugares/modelo/Lugar.java
java com.example.mislugares.modelo.Principal

# --- Proyecto Android real (raíz del repo) ---
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk
./gradlew assembleDebug          # genera app/build/outputs/apk/debug/app-debug.apk

# Crear/listar AVD (emulador)
export ANDROID_HOME=~/Android/Sdk
~/Android/Sdk/cmdline-tools/latest/bin/avdmanager create avd -n mislugares_avd -k "system-images;android-34;google_apis;x86_64" -d "pixel_5"
~/Android/Sdk/emulator/emulator -avd mislugares_avd
```
