# Contexto del Proyecto "Mis Lugares"

> Documento de continuidad. Cualquier IA o persona debe leer esto antes de seguir trabajando.
> Método de trabajo: **el estudiante escribe el código, la IA es solo tutora** (NO escribir el código por él).

---

## 1. Qué es el proyecto

- **Aplicación:** "Mis Lugares" (gestor de lugares: nombre, dirección, posición GPS, teléfono, valoración...).
- **Lenguaje:** Java (Android al final del camino).
- **Referencia principal:** libro PDF `libro-android-11.pdf` (*El gran libro de Android 11*, Jesús Tomás Gironés). Aplicación de ejemplo del libro.
- **Usuario:** total principiante en programación ("cero total"). Habla español. Aprende haciendo.

## 2. Rol de la IA (IMPORTANTE)

- Ser **tutor**, no programador. Guiar paso a paso, dar mini-ejercicios.
- **NUNCA escribir archivos de código por el estudiante.** El estudiante debe teclear el código.
- Dar las explicaciones DESPUÉS de que el estudiante intente, salvo conceptos nuevos.
- Si hay error: guiar para que lo encuentre él mismo (sin dar la respuesta directa).
- Usar analogías en criollo y poco vocabulario técnico de golpe.
- Lenguaje del proyecto: **español**. Respuestas cortas y claras.
- El estudiante puede pedir el código; recordarle que si lo copia no aprende. Preguntar conceptos, no entregar código.
- Excepción: sí se pueden dar "esqueletos" explicados línea por línea en la primera lección o cuando el estudiante lo pida para consulta, PERO siempre después de que él intente.

## 3. Estado actual (última sesión: 9-ago-2026)

### Herramientas instaladas ✅
| Herramienta | Detalle |
|---|---|
| JDK 25 (OpenJDK) | `java-25-openjdk-devel`. Verificado: `javac 25.0.4`, `java 25.0.4` |
| VS Code | Instalado (`code`) |
| Terminal integrada | Uso `Ctrl+ñ` |

### Pendiente de instalar (cuando lleguemos a Android)
- Extension Pack for Java (VS Code) — aún NO confirmado que esté instalado.
- Gradle, Android SDK, emulador — para la fase Android.

### Avance de la Lección 1 (EN CURSO)
- El estudiante creó (él mismo): carpeta `mislugares/` (vacía) y archivo `hola_mundo.java` en la raíz `/home/izma/Documentos/facu/dismov/app/`.
- Contenido actual de `hola_mundo.java` (NO compila ni ejecuta todavía, le falta `main`):

```java
public class hola_mundo {
    
}
```

- **Problemas detectados (para que el estudiante los corrija, guiado):**
  1. No tiene `public static void main(String[] args)` → no es ejecutable.
  2. No tiene `System.out.println(...)`.
  3. Está en la raíz, pero la consigna era crearlo dentro de `mislugares/`.
  4. Nombre en minúscula: en Java es VÁLIDO que el archivo y la clase sean `hola_mundo`, pero por convención se usa `HolaMundo` (CamelCase). La regla REAL es: nombre de clase pública == nombre del archivo.

### Consigna original de la Lección 1 (para retomar)
```java
public class HolaMundo {
    public static void main(String[] args) {
        System.out.println("Hola, esto es Mis Lugares!");
    }
}
```
Flujo: `javac HolaMundo.java` (compila, no imprime nada si sale bien) → `java HolaMundo` (ejecuta e imprime).

## 4. Arquitectura objetivo (según libro, capítulo 3.5)

Clean Architecture simplificada en **4 capas** (organizadas en paquetes):

```
com.example.mislugares/
├── modelo/        <- POJOs puros sin dependencias externas: Lugar, GeoPunto, TipoLugar
├── datos/         <- persistencia (bases de datos, archivos, servicios)
├── casos_uso/     <- operaciones del usuario (ver, borrar, compartir lugar...) = interactors
└── presentacion/  <- UI: Activities, Fragments, Adapters
```

Regla de dependencia: las capas internas no dependen de las externas. `modelo` es la más interna (Java puro, sin imports de Android).

La ruta física de carpetas refleja el paquete: `src/com/example/mislugares/modelo/...`

## 5. Plan de lecciones y progreso

| # | Lección | Estado |
|---|---|---|
| 0 | Instalar JDK | ✅ Completada |
| 1 | Primer programa: estructura, `main`, compilar y ejecutar | 🔶 EN CURSO |
| 2 | Variables y tipos de datos | ⬜ Pendiente |
| 3 | Condiciones (`if`/`else`) y bucles (`for`, `while`) | ⬜ Pendiente |
| 4 | Métodos/funciones | ⬜ Pendiente |
| 5 | Clases y objetos (POO) | ⬜ Pendiente |
| 6 | Armar capa `modelo` de Mis Lugares (Lugar, GeoPunto, TipoLugar) | ⬜ Pendiente |
| 7 | Colecciones (List/ArrayList) | ⬜ Pendiente |
| 8 | Capa `datos` (persistencia) | ⬜ Pendiente |
| 9 | Capa `casos_uso` | ⬜ Pendiente |
| 10 | Configurar VS Code + Android SDK + Gradle | ⬜ Pendiente |
| 11 | Capa `presentacion` (UI Android) | ⬜ Pendiente |

## 6. Datos técnicos del entorno

- **Sistema:** Fedora (dnf), Linux.
- **Ruta del proyecto:** `/home/izma/Documentos/facu/dismov/app/`
- **Carpeta actual:** el PDF del libro está en la raíz: `libro-android-11.pdf`.
- **JDK:** solo hay `java-25-openjdk-devel` en los repos de dnf (no hay 17). Se instaló el 25.
- **sudo:** requiere contraseña del usuario (la IA no puede instalarlo sola).

## 7. Comandos útiles

```bash
# Compilar (desde la carpeta donde está el .java)
javac HolaMundo.java

# Ejecutar (sin la extensión .java, con el nombre de la clase)
java HolaMundo

# Verificar versión
javac -version
```
