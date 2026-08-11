# REQUISITOS DEL PROYECTO "MIS LUGARES"
# App gestor de lugares en Java (objetivo: Android)

## Herramientas obligatorias

- **JDK 25 (OpenJDK)** — compilador y ejecutor de Java.
  - Instalación Fedora: `sudo dnf install -y java-25-openjdk-devel`
  - Descarga manual (otro sistema operativo):
    https://adoptium.net/es/temurin/releases/?version=25
  - Verificar: `javac -version` (debe mostrar 25.x)

- **VS Code** — editor de código.
  - Descarga oficial: https://code.visualstudio.com/download
  - Extensión: **Extension Pack for Java** (Microsoft) — resaltado de sintaxis,
    autocompletado y detección de errores en rojo.
    https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack

## Flujo de trabajo (por ahora, Java puro)

1. Escribir el archivo `.java`
2. Compilar: `javac NombreArchivo.java`
3. Ejecutar: `java NombreArchivo`

## Herramientas pendientes (fase Android, más adelante)

- **Gradle** — gestor de builds de Android.
- **Android SDK** — librerías de Android (UI, mapas, GPS...).
- **Emulador o celular físico** — para correr la app.

## Referencia

- Libro: `libro-android-11.pdf` (*El gran libro de Android 11*).
- Arquitectura: Clean Architecture en 4 capas
  (`modelo`, `datos`, `casos_uso`, `presentacion`).
- Más contexto y plan de lecciones: ver `AGENTS.md`.
