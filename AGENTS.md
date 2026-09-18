# Contexto del Proyecto "Mis Lugares"

## 1. Qué es el proyecto

- **Aplicación:** "Mis Lugares" (gestor de lugares: nombre, dirección, posición GPS, teléfono, valoración...).
- **Lenguaje:** Java (Android).
- **Referencia principal:** libro PDF `libro-android-11.pdf` (*El gran libro de Android 11*, Jesús Tomás Gironés). Aplicación de ejemplo del libro.
- **Equipo:** 5 personas, división de trabajo en `EQUIPO.md`.

## 2. Estructura del repositorio

- `app/` (raíz del repo) — único proyecto: Android Gradle real (`build.gradle`, `settings.gradle`, `gradlew`, módulo `app/` con `AndroidManifest.xml`, `src/main/java/...`, `src/main/res/...`). Este es el que genera el `.apk`.
- El sandbox de Java plano (`mislugares/`, usado en las primeras lecciones para aprender clases/constructores con `javac`/`java`) y `hola_mundo.java` (la primera lección) se eliminaron una vez que el modelo real ya vivía en `app/` — eran duplicados sin uso, no los usaba el build.

## 3. Estado actual (última sesión: 18-sep-2026)

### Los 100 puntos de referencia en el mapa, con filtro por categoría (nuevo, 18-sep-2026)
Pedido por Ismael: en vez de mostrar los lugares guardados al entrar por el tab "MAPA", mostrar los 100 puntos de referencia (`puntos_referencia_mty.json`), con un filtro por tipo de actividad. Aclarado con él: los puntos de referencia **no** tienen `TipoLugar` (restaurante/bar/hotel...) porque no son negocios — son museos/parques/plazas/estaciones — así que el filtro usa las 5 categorías propias que ya existían para colorear el pin en la lista CERCA (deporte/parques, comercial, espectáculos, museos/cultura, otros), no `TipoLugar`.
- **`CategoriasReferencia.java`** (nuevo, paquete `presentacion`): clasificador centralizado — antes vivía duplicado como método privado dentro de `AdaptadorPuntosReferencia`. Enum `Categoria` (`DEPORTE_PARQUES`, `COMERCIAL`, `ESPECTACULOS`, `CULTURA`, `OTROS`) + `de(String categoria)` (clasifica por palabras clave) + `pin(Categoria)` (drawable) + `nombreRes(Categoria)` (string del chip). Se auditaron las 100 categorías reales contra el clasificador antes de escribirlo: el chequeo de "museo" pasó a ir primero (antes "Salón de la Fama del Béisbol Mexicano", categoría "Museo deportivo", cala en DEPORTE por la palabra "deportivo" en vez de en CULTURA). `AdaptadorPuntosReferencia` (la lista CERCA) se actualizó para usar esta clase en vez de su copia propia.
- **`activity_mapa_lugar.xml`**: fila de chips nueva (TODOS + las 5 categorías) debajo del buscador, mismo estilo `chip_pixel` que ya usa `fragment_lista.xml`. Los controles de zoom se corrieron más abajo (120dp) para no toparse con buscador + chips.
- **`MapaLugarActivity.java`**: `mostrarTodos()` (el modo "vengo del tab MAPA") ya no dibuja lugares guardados — carga los 100 puntos con `RepositorioPuntosReferencia` y los dibuja con `dibujarPuntosReferencia()`, coloreados por categoría. Los chips son de **multi-selección independiente** (`filtrosActivos`, un `Set<Categoria>`, todas activas por defecto): tocar una categoría la prende/apaga sin afectar a las demás; "TODOS" las reactiva todas de una. Al tocar un punto en el mapa se abre la misma ficha inferior que ya existía, con "VER" oculto (no es un lugar guardado) y "IR" funcionando igual (navega por coordenadas).
- **Sin cambios:** el modo "vengo del detalle de un lugar" (`mostrarUnLugar`, cuando tocás "Mapa" desde `VistaLugarActivity`) sigue mostrando ese lugar guardado puntual — no tenía sentido reemplazarlo por landmarks, es una navegación distinta.
- `./gradlew assembleDebug` compila correctamente y genera el APK. Falta probar en dispositivo físico.

### Buscador en el mapa (nuevo, 17-sep-2026)
Pedido por Ismael: poder buscar cualquier lugar de la zona metropolitana de MTY desde `MapaLugarActivity` (no solo los lugares guardados) y ver su info como en Google Maps. No hizo falta ninguna dependencia nueva — se reusó el mismo mecanismo de `BuscadorLugares`/`AdaptadorSugerencias` que ya existía en `EdicionLugarActivity` para el panel de sugerencias del campo NOMBRE (Photon, gratis, sin API key).
- **`activity_mapa_lugar.xml`**: caja de búsqueda nueva arriba del mapa (input + botón "✕" para limpiar) y, debajo, el mismo panel de sugerencias que ya existía en el formulario de edición (cabecera "SUGERENCIAS" + contador + `RecyclerSugerencias`) — sin la fila "usar tal cual" del formulario, porque acá no hay nada que "usar tal cual", solo resultados con coordenadas. Los controles de zoom se corrieron 70dp más abajo para no toparse con la caja de búsqueda.
- **`MapaLugarActivity.java`**: mismo patrón de debounce que `EdicionLugarActivity` (`programarBusqueda`, 350ms, mínimo 3 caracteres, token para descartar respuestas tardías). Al elegir un resultado (`aplicarSugerenciaBusqueda`): centra el mapa (zoom 16, igual que al abrir el detalle de un lugar guardado), agrega un marcador temporal (`marcadorBusqueda`, se reemplaza en cada búsqueda nueva) y muestra la ficha inferior ya existente (pin coloreado por `Tipos.pin()`, nombre, dirección, botón "IR" que abre Google Maps/navegador).
- **Decisión de alcance (elegida por Ismael):** el botón "VER" de la ficha (que abre el detalle de un lugar guardado) se **oculta** para resultados de búsqueda, porque no son lugares guardados — no tienen `id` en el repositorio. `mostrarFicha()` (la ruta de lugares guardados) vuelve a mostrar "VER" explícitamente, por si había quedado oculto de una búsqueda anterior. Queda pendiente si algún día quieren un botón "Guardar" que abra `EdicionLugarActivity` precargada con los datos del resultado.
- `irEnMapaExterno(Lugar)` pasó a `irEnMapaExterno(double lat, double lon)` — ya no depende de un `Lugar` guardado, así lo puede usar tanto un marcador de la base como un resultado de búsqueda.
- **Radio de 50km (mismo día):** Ismael pidió limitar el buscador del mapa a la zona metropolitana (dio a elegir entre "solo Nuevo León" o "50km de radio" — se optó por el radio, más simple y confiable que filtrar por el campo `state` de Photon, que no siempre viene bien poblado). `BuscadorLugares.buscar()` ganó una sobrecarga con `radioMaximoMetros`; con ese parámetro activo, descarta cualquier resultado a más de esa distancia de un centro fijo de Monterrey (`CENTRO_MTY_LAT/LON`, no el sesgo de búsqueda que puede moverse con el GPS) usando `GeoPunto.distancia()`, y se salta el filtro/fallback por país (a 50km de Monterrey ya es México). **Solo afecta al mapa** — `MapaLugarActivity` pasa `RADIO_BUSQUEDA_METROS = 50_000`; `EdicionLugarActivity` sigue llamando a la sobrecarga vieja (sin límite), porque ahí buscar un lugar de otra parte del país al crear/editar sigue teniendo sentido.
- `./gradlew assembleDebug` compila correctamente y genera el APK. Falta probar en dispositivo físico.

### "CERCA" ahora muestra puntos de referencia fijos en vez de ordenar los lugares guardados (nuevo, 17-sep-2026)
Pedido por Ismael: el chip "CERCA" dejó de ordenar los lugares guardados del usuario por distancia — ahora muestra un listado fijo de 100 puntos de referencia de la zona metropolitana de Monterrey (Macroplaza, Paseo Santa Lucía, estaciones de metro, etc.) que caen dentro de un radio configurable alrededor de la posición GPS actual, no una lista completa ordenada.
- **Dataset**: `app/src/main/assets/puntos_referencia_mty.json` — 100 puntos (`id`, `nombre`, `categoria`, `municipio`, `latitud`, `longitud`), hardcodeados (no vienen de ninguna API en tiempo de ejecución). Se descartó usar la Places API de Google para esto: requiere la misma cuenta de facturación de Google Cloud que ya había fallado antes (ver "Mapa" más abajo) con el error `[OR_BACR2_44]`.
- **`modelo/PuntoReferencia.java`** (nuevo): POJO con `id`, `nombre`, `categoria`, `municipio`, `posicion` (`GeoPunto`). Sin setters — estos datos nunca se editan desde la app.
- **`datos/RepositorioPuntosReferencia.java`** (nuevo): lee el JSON de `assets/` con `AssetManager` + `org.json`, arma la `List<PuntoReferencia>` una vez en memoria (no usa SQLite, a diferencia de `LugaresBD` — son datos fijos, no hace falta persistencia).
- **`casos_uso/PuntosCercanos.java`** (nuevo): filtra por radio (no ordena-y-corta como antes) — se queda con los puntos cuya `GeoPunto.distancia()` sea ≤ al radio elegido. Si da vacío, **duplica el radio** (5km → 10km → 20km...) hasta un tope de **30km**, después de eso se rinde y devuelve vacío. Resultado ordenado de más cerca a más lejos.
- **Radio configurable**: no vive en Preferencias — Ismael pidió que el control estuviera en la propia pantalla de "CERCA". Es `fab_radio` en `activity_lista.xml`: un botón flotante azul apilado arriba del "+" de agregar lugar, visible solo con el chip "CERCA" activo. Cada toque cicla entre 5/10/15/20 km (`RADIOS_KM` en `ListaLugaresFragment`) y refresca la lista al momento; el valor se guarda en `SharedPreferences` con la clave `"radioKm"` (mismo mecanismo que ya usaba `"maximo"`). Se había armado primero como stepper -/+ dentro de `PreferenciasActivity` (mismo patrón que "MAXIMO") pero se descartó ese lugar y esa interacción a pedido de Ismael.
- El chip "A-Z" de la fila de filtros se oculta mientras "CERCA" está activo (no tiene sentido para puntos de referencia) y vuelve a aparecer al salir de ese modo.
- **Rediseño del botón (mismo día, iteración final):** con "20KM" el texto de `fab_radio` se salía del círculo de 52dp, y a Ismael le gustaba tener el "KM" visible. `fab_radio` pasó a ser un **rectángulo horizontal ("acostado")**, 110dp × 52dp (mismo drawable `boton_pixel_azul` — ya era `shape="rectangle"` sin radio de esquina, no hizo falta uno nuevo), con el texto en una sola línea (`"20 KM"`). Ocupa el **mismo lugar** que `fab_agregar` (el "+" de agregar lugar) en vez de estar apilado arriba — `ListaLugaresFragment` alterna la visibilidad de los dos según el chip "CERCA" (nunca los dos visibles a la vez). El "+" sigue funcionando igual en TODOS/FAV/A-Z; solo desaparece dentro de CERCA (ahí también existe "Agregar lugar" en el menú de inicio, así que no se pierde la función).
- **Pin coloreado por categoría (mismo día):** los puntos de referencia no tienen `TipoLugar` (son texto libre en `categoria`, ej. "Museo / Cultura", "Parque recreativo"), así que no pueden usar `Tipos.pin()` tal cual. Se agregó `AdaptadorPuntosReferencia.pinPara(String categoria)`: clasifica por palabras clave (deporte/parque/campo → verde, comercial/mercado → rojo, escenario/teatro/feria → amarillo, museo/arte/cultura/campus/cívico → azul, el resto → gris/`ic_pin_otros`), reusando los mismos drawables `ic_pin_*` que ya existían para los lugares guardados. No es exacto para los 100 puntos (es texto libre variado), pero cubre bien las categorías más comunes del dataset.
- **`presentacion/AdaptadorPuntosReferencia.java`** (nuevo): adapter de `RecyclerView` propio para `PuntoReferencia` (no reusa `AdaptadorLugares`, porque un punto de referencia no tiene teléfono/foto/favorito/valoración). Fila nueva `elemento_punto_referencia.xml` (pin genérico + nombre + categoría + distancia). No son tocables (no abren ninguna pantalla de detalle, a diferencia de los lugares guardados).
- **`ListaLugaresFragment.java`**: el chip "CERCA" (`orden == 1`) ahora intercambia el adapter del `RecyclerView` entre `adaptador` (lugares guardados) y `adaptadorCerca` (puntos de referencia) en vez de solo reordenar. `fragment_lista.xml` ganó un `FrameLayout` envolviendo el `RecyclerView` con un `TextView` de estado vacío (`texto_vacio`, string `sin_puntos_cerca`) que se muestra si no hay ningún punto dentro de los 30km (incluye el caso de GPS todavía no disponible).
- Nota: la rama `orden == 1` dentro de `AdaptadorLugares.aplicarFiltro()` (ordenar lugares guardados por cercanía) quedó sin usar — ya no se llama con `orden == 1` desde `ListaLugaresFragment`, porque ese chip ahora usa el adapter nuevo. Se dejó el código ahí sin tocar (no rompe nada), pendiente de limpieza si se decide más adelante.
- `./gradlew assembleDebug` compila correctamente y genera el APK. Falta probar en dispositivo físico.

### `MainActivity` reescrita como `Fragment` (nuevo, 29-ago-2026)
- Refactorización pura (Anexo B del libro, "Fragments") — sin cambio de comportamiento. `MainActivity.java` ahora solo aloja `ListaLugaresFragment.java` (clase nueva, con toda la lógica que antes tenía `MainActivity`: RecyclerView, adaptador, menú de opciones, refresco en `onResume()`).
- `activity_lista.xml` pasó a ser un `FrameLayout` contenedor (mismo patrón que ya usaba `PreferenciasActivity`/`activity_preferencias.xml`); el layout del RecyclerView se movió a `fragment_lista.xml` (nuevo).
- `./gradlew assembleDebug` compila correctamente, probado en dispositivo físico — comportamiento idéntico al de antes.

### Flecha de regreso en la barra superior (nuevo, 29-ago-2026)
- Pedido por el docente en una revisión. Se agregó `getSupportActionBar().setDisplayHomeAsUpEnabled(true)` + `onSupportNavigateUp() { finish(); return true; }` a las 5 Activities que no son la de arranque (`MainActivity`, `VistaLugarActivity`, `EdicionLugarActivity`, `MapaLugarActivity`, `PreferenciasActivity`). `MenuActivity` (la de arranque) no lo necesita.
- Se usó `finish()` en vez de `android:parentActivityName` en el manifest porque `EdicionLugarActivity` tiene dos puntos de entrada distintos (desde `MenuActivity` para crear, desde `VistaLugarActivity` para editar) — `finish()` siempre vuelve a quien la abrió, sin depender de una jerarquía fija.

### Favoritos (nuevo, 29-ago-2026)
Pedido por el docente. Alcance decidido: solo marcar/ver, sin filtro todavía (el filtro por favoritos se evalúa junto con el filtro por tipo, más abajo).
- `Lugar.java`: campo nuevo `favorito` (`boolean`) + `isFavorito()`/`setFavorito()`.
- `LugaresBD.java`: columna `favorito INTEGER` nueva en la tabla. **Se subió la versión de la base de 1 a 2** y se implementó `onUpgrade()` con `ALTER TABLE lugares ADD COLUMN favorito INTEGER DEFAULT 0` — necesario porque ya había una base vieja creada en el teléfono de prueba sin esa columna; sin la migración, la app hubiera crasheado al leer el índice de columna que no existía. `valoresDe()` y `extraeLugar()` actualizados para incluir el campo.
- `VistaLugarActivity`: ícono de estrella nuevo en `vista_lugar.xml` (debajo del tipo) — toca para marcar/desmarcar, persiste con `GuardarLugar`. Usa los drawables del sistema `btn_star_big_on`/`btn_star_big_off`.
- `AdaptadorLugares`/`elemento_lista.xml`: ícono de estrella chico junto a la valoración, visible solo si el lugar es favorito.
- Pendiente marcado por Ismael para la sesión de rediseño: cambiar el ícono de estrella (diseño) y su ubicación en `vista_lugar.xml` — no es urgente, se resuelve junto con el rediseño Y2K.
- `./gradlew assembleDebug` compila correctamente, probado en dispositivo físico (incluida la migración sobre una base ya existente).

### Pendientes nuevos del docente (29-ago-2026)
En una revisión, el docente pidió agregar: ~~favoritos~~ (hecho, ver arriba), ~~filtro por tipo de lugar~~ (hecho, ver abajo), y ~~un rediseño visual retro~~ (hecho, ver abajo).

### Rediseño visual retro "8-bit" (30-ago-2026)
Se probaron 3 direcciones en una maqueta aparte (no en la app) antes de tocar código: cromo/Y2K y escritorio estilo Windows XP, ambas descartadas por el equipo; la aprobada fue un estilo "8-bit/pantalla de bolsillo" — colores planos, esquinas casi rectas, fuentes de píxel. Aplicado a la app real en esta sesión:
- **`colors.xml`**: 6 colores nuevos, prefijo `pixel_` — `pixel_fondo` (#F4EAD5, crema), `pixel_tinta` (#241C10, texto oscuro), `pixel_azul` (#2F6FE0), `pixel_rojo` (#E4432D), `pixel_verde` (#3FA34D), `pixel_amarillo` (#F2B807). Los `purple_500`/`purple_700`/`white` originales se dejaron (por si algo del tema los sigue usando).
- **`res/font/`** (carpeta nueva): `press_start_2p.ttf` y `vt323.ttf`, bajados de Google Fonts (licencia OFL, libres para usar en la app).
- **`styles.xml`** (archivo nuevo, no existía): `TextoPixelTitulo` (Press Start 2P, 16sp, blanco) para títulos cortos; `TextoPixelCuerpo` (VT323, 20sp) para el resto del texto; `BarraPixel` (`parent="Widget.AppCompat.Light.ActionBar.Solid.Inverse"`) para el título de la `ActionBar` — **importante**: poner `titleTextStyle` directo en el tema general (`Theme.MisLugares`) no funciona, la `ActionBar` lee su estilo de título desde `actionBarStyle`, que ya trae uno fijo — hay que sobreescribir `actionBarStyle` con un estilo propio que sí declare `titleTextStyle`.
- **`drawable/fondo_boton_pixel.xml`** (nuevo): un solo `<shape>` reutilizable (relleno blanco placeholder + esquinas de 2dp) para todos los botones — cada botón le pone su propio color con `android:backgroundTint` en vez de tener un drawable por color.
- **`themes.xml`**: `colorPrimary` → `pixel_azul`, `colorPrimaryDark` → `pixel_tinta`, `android:windowBackground` → `pixel_fondo` (cambia la barra de arriba y el fondo de todas las pantallas de una sola vez), `actionBarStyle` → `BarraPixel`.
- Pantallas actualizadas con `TextoPixelCuerpo`/colores nuevos y, donde corresponde, `fondo_boton_pixel` + `backgroundTint`: `activity_menu.xml` (título con `TextoPixelTitulo` azul, botones Mis Lugares=azul y Agregar Lugar=verde), `elemento_lista.xml` (nombre/dirección/distancia), `fragment_lista.xml` (fondo blanco del `Spinner` del filtro), `vista_lugar.xml` (nombre/tipo/dirección/teléfono/url/gps, botones Compartir=amarillo con texto oscuro, Editar=rojo, Mapa=azul), `activity_edicion_lugar.xml` (las 6 etiquetas del formulario, botones Eliminar=rojo y Guardar=verde).
- **Quedó fuera a propósito**: la estrella de favorito sigue con el ícono por defecto de Android (decisión del equipo); `PreferenciasActivity` no tiene layout propio (usa `PreferenceFragment`, un sistema de Android aparte, más complicado de re-estilar) y casi no se usa, se dejó igual; `MapaLugarActivity` no tenía nada propio que cambiar (es solo el `MapView` de osmdroid a pantalla completa) — su barra de arriba ya queda pixelada por el cambio en `themes.xml`; el texto de adentro del `Spinner` ("Todos", "RESTAURANTE"...) sigue con la letra del sistema, cambiarlo requiere un layout de fila propio + tocar `ListaLugaresFragment.java`, no se hizo.
- `./gradlew assembleDebug` compila correctamente, probado pantalla por pantalla en dispositivo físico.

### Filtro por tipo de lugar (29-ago-2026)
- `AdaptadorLugares.java`: nuevo campo `filtro` (`TipoLugar`, `null` = sin filtro); `filtrarPorTipo(TipoLugar)` lo cambia y reaplica; `aplicarFiltro()` (privado) reconstruye `ids` recorriendo `lugares` y quedándose solo con los que coinciden, o con todos si `filtro == null`; `actualizar()` ahora llama a `aplicarFiltro()` en vez de armar `ids` directo, así el filtro activo sobrevive a un refresco de la lista.
- `fragment_lista.xml`: pasó de ser un `RecyclerView` suelto a un `LinearLayout` vertical con un `Spinner` (`id/spinner_filtro`) arriba y el `RecyclerView` abajo con `layout_weight="1"`.
- `ListaLugaresFragment.java`: nuevo método `configurarFiltro(View)`, llamado desde `onViewCreated()` — arma el `Spinner` con "Todos" + cada valor de `TipoLugar.values()`, y un `AdapterView.OnItemSelectedListener` que llama a `adaptador.filtrarPorTipo(null)` en la posición 0 o a `adaptador.filtrarPorTipo(TipoLugar.values()[posicion - 1])` en el resto.
- `./gradlew assembleDebug` compila correctamente, probado en dispositivo físico.

### Navegación entre pantallas conectada (23-ago-2026)
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
  - **Confirmación al borrar la foto** (29-ago-2026, práctica opcional del libro sección 3.11.1): mismo patrón de `AlertDialog.Builder` que `EdicionLugarActivity.eliminar()`, aplicado al ícono de basura.
  - **Permiso `CALL_PHONE`** (29-ago-2026, práctica opcional del libro sección 7.1.4): tocar el teléfono ahora usa `ACTION_CALL` (llama directo) en vez de `ACTION_DIAL` (marca, el usuario confirma) — por eso pasó a ser una acción "peligrosa" que pide permiso en tiempo de ejecución. Mismo patrón que `GestorLocalizacion.java` ya usaba para el GPS: `checkSelfPermission`/`requestPermissions`/`onRequestPermissionsResult`, esta vez inline en `VistaLugarActivity` (métodos `llamar()` y `onRequestPermissionsResult()`).
- `./gradlew assembleDebug` compila correctamente, probado en dispositivo físico.

### Intenciones implícitas en VistaLugarActivity (25-ago-2026)
- Siguiendo la sección 3.11 del libro ("Las intenciones"), se conectaron dos íconos que ya existían en `vista_lugar.xml` sin usar: el botón `btnCompartir` y el `TextView` de teléfono.
- **Compartir** (`ACTION_SEND`): envía nombre + URL del lugar como texto plano a cualquier app que sepa manejarlo (WhatsApp, Gmail, SMS...).
- **Llamar** (`ACTION_DIAL`, tocando el número de teléfono): abre el marcador con el número precargado. No pide permiso porque no llama directo, el usuario confirma — a diferencia de `ACTION_CALL`, que sí requiere el permiso `CALL_PHONE` (queda como práctica opcional del libro, no implementada).
- **Ver en el mapa** (`ACTION_VIEW` con URI `geo:`, tocando la dirección): abre la app de mapas que el usuario tenga instalada. Si el lugar tiene coordenadas usa `geo:lat,lon`; si son `0,0` cae a `geo:0,0?q=<dirección>` para que igual busque por texto. Implementado en `VistaLugarActivity` (listener de `tvDireccion`).
- **Abrir la web** (`ACTION_VIEW` con la URL del lugar, tocando un `TextView` nuevo): se agregó el campo `url` a `vista_lugar.xml` (no existía) y su listener en `VistaLugarActivity`. Con esto quedan las 4 intenciones implícitas de la sección 3.11 del libro completas (Compartir, Llamar, Ver en mapa, Abrir la web) — era el último ítem "core" pendiente de todo lo que pide el libro para Mis Lugares.
- `./gradlew assembleDebug` compila correctamente, probado en dispositivo físico.

### Mapa: se abandonó Google Maps por osmdroid (nuevo, 23-ago-2026; fuente de tiles actualizada después)
- Se intentó configurar `MAPS_API_KEY` leyéndola desde `local.properties` (en vez de hardcodeada en `app/build.gradle`, ya corregido y funcionando) para usar Google Maps SDK, pero la creación de la cuenta de facturación de Google Cloud falló repetidamente con el error `[OR_BACR2_44]` — probado con dos cuentas de Google distintas (de dos personas del equipo), con tarjetas y RFCs distintos, mismo error las dos veces. No se pudo resolver del lado de Google/banco.
- **Decisión: se reemplazó Google Maps por `osmdroid`** (librería open source, sin API key ni facturación). Cambios: dependencia `org.osmdroid:osmdroid-android:6.1.20` en `app/build.gradle`, permiso `INTERNET` agregado en `AndroidManifest.xml`, `activity_mapa_lugar.xml` ahora usa `<org.osmdroid.views.MapView>` en vez del `<fragment>` de Google, y `MapaLugarActivity.java` reescrito con las clases de osmdroid (`MapView`, `GeoPoint`, `Marker`).
- **Ojo con la fuente de tiles (las imágenes del mapa):** ni `TileSourceFactory.MAPNIK` (servidor oficial de OpenStreetMap) ni `TileSourceFactory.WIKIMEDIA` funcionan — ambos devuelven 403, están restringidos a uso interno de sus organizaciones (confirmado en logs de `adb logcat`, no es un problema de configuración). En un primer momento se usó una fuente manual apuntando a los tiles públicos de CARTO (`basemaps.cartocdn.com/rastertiles/voyager/`).
- **Actualizado (fuente de tiles actual): Esri "World Street Map"** (`server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/`), sin API key. Implementado en `MapaLugarActivity.java` con un `OnlineTileSourceBase` a medida (`getTileURLString` arma la URL como `z/y/x` porque Esri sirve las teselas con la fila antes que la columna, al revés que el estándar `z/x/y`). Confirmado funcionando de verdad en dispositivo físico (no emulador).
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
