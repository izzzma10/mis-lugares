# Organización del equipo — "Mis Lugares"

> Documento de reparto de trabajo entre los 5 integrantes del proyecto.
> Para el contexto de aprendizaje individual y el estado técnico del proyecto, ver `AGENTS.md`.

---

## 1. Cómo se divide el trabajo

El proyecto se separa en capas (Clean Architecture, ver `AGENTS.md` sección 4). Una personas arma el núcleo (las capas internas, de las que depende todo lo demás) y las otras cuatro arman una pantalla cada una.

```
com.example.mislugares/
├── modelo/        <- NÚCLEO
├── datos/         <- NÚCLEO
├── casos_uso/     <- NÚCLEO
└── presentacion/  <- 4 pantallas, una por persona
```

Regla de dependencia: las pantallas (`presentacion`) llaman a `casos_uso`, nunca al revés. Por eso el núcleo se define primero (aunque sea como "firma"/contrato) para que el resto del equipo pueda avanzar en paralelo sin esperar a que esté 100% terminado.

## 2. Núcleo — 1 personas

Responsables de las capas de las que dependen todas las pantallas.

| Capa | Contenido | Notas | Páginas del libro |
|---|---|---|---|
| `modelo` | Clases `Lugar`, `GeoPunto` | POJOs puros, sin dependencias de Android | 91-98 (sección 1.13.1) |
| `modelo` | Clase `TipoLugar` (tipo enumerado) | — | 99-102 (sección 1.13.2) |
| `modelo` | `RepositorioLugares` | — | 102, 210 |
| `modelo` (contexto) | La app "Mis Lugares" explicada de nuevo, con UI | Para entender qué datos necesita el modelo | 149 (sección 2.5) |
| `casos_uso` | Arquitectura Clean aplicada a Mis Lugares, capa de Casos de Uso | Explica cómo se organiza esta capa | 214-215 (sección 3.5) |
| `datos` | Bases de datos con SQLite (teoría) | `query()`, `rawQuery()` | 632-637 (sección 9.7) |
| `datos` | Uso de bases de datos en Mis Lugares | Aplicado directo al proyecto | 638-642 (sección 9.7.2) |
| `datos` | Adaptadores para bases de datos | — | 643-655 (sección 9.7.3) |
| `datos` | Operaciones con BD en Mis Lugares | CRUD real sobre `Lugar` | 656-669 |
| `datos` (opcional) | Content Provider | Si se necesita compartir datos entre apps | 675+ (sección 9.8) |

**Primer entregable del núcleo:** la firma de `casos_uso` (qué métodos existen, qué reciben, qué devuelven), aunque todavía no esté implementada por dentro. Con eso, el resto del equipo ya puede programar sus pantallas contra esa interfaz.

## 3. Pantallas — 4 personas (capa `presentacion`)

| # | Pantalla | Complejidad | Sugerido para | Páginas del libro |
|---|---|---|---|---|
| 1 | Lista de lugares (RecyclerView) | Media | — | 260+ (sección 3.10) |
| 2 | Ver detalle de un lugar (`VistaLugarActivity`) | Baja | El integrante con menos experiencia en Java/Android | 219-226 (sección 3.6.1) |
| 3 | Crear / editar lugar (`EdicionLugarActivity`) | Media-alta | — | 234-239 (sección 3.6.2) |
| 4a | Preferencias de usuario | Alta | El integrante con más experiencia | 247+ (sección 3.8) |
| 4b | Localización (GPS) | Alta | Mismo que 4a | 482-499 (sección 7.2) |
| 4c | Google Maps | Alta | Mismo que 4a | 508-523 (sección 7.3) |

Cada pantalla se apoya directo en los capítulos correspondientes del libro (`libro-android-11.pdf`), que trae los pasos ilustrados con capturas de pantalla.

## 4. Flujo de trabajo en Git

Repo: `https://github.com/izzzma10/mis-lugares.git`


1. Cada quien clona una sola vez:
   ```bash
   git clone https://github.com/izzzma10/mis-lugares.git
   ```
2. Antes de empezar algo nuevo, traer cambios:
   ```bash
   git pull
   ```
3. Trabajar siempre en una rama propia (nunca commitear directo a `main`):
   ```bash
   git checkout -b nombre/tarea      # ej: ana/pantalla-detalle
   ```
4. Al terminar una parte:
   ```bash
   git add .
   git commit -m "que hice"
   git push -u origin nombre/tarea
   ```
5. Abrir un Pull Request en GitHub. Alguien del equipo revisa antes de mergear a `main`.

## 5. Estado de la asignación

| Parte | Asignado a | Estado |
|---|---|---|
| Núcleo (`modelo`/`datos`/`casos_uso`) | Ismael  | En preparación (aún en fundamentos de Java) |
| Pantalla 1 — Lista | Julio | ⬜ Pendiente |
| Pantalla 2 — Detalle | Angel | ⬜ Pendiente |
| Pantalla 3 — Crear/editar | Ale | ⬜ Pendiente |
| Pantalla 4 — Preferencias + mapa | Daniela | ⬜ Pendiente |

