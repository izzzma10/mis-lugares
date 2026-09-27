package com.example.mislugares.presentacion;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;
import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.Lugar;
import com.example.mislugares.modelo.PuntoReferencia;
import com.example.mislugares.modelo.TipoLugar;
import com.example.mislugares.datos.RepositorioLugares;
import com.example.mislugares.datos.RepositorioPuntosReferencia;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapaLugarActivity extends AppCompatActivity {

    public static final String EXTRA_ID_LUGAR = "com.example.mislugares.ID_LUGAR";
    public static final String EXTRA_ID_PUNTO_REFERENCIA = "com.example.mislugares.ID_PUNTO_REFERENCIA";

    // Centro por defecto (Monterrey) si no hay lugares ni ubicación conocida.
    private static final GeoPoint CENTRO_DEFECTO = new GeoPoint(25.6866, -100.3161);

    // El buscador del mapa se limita a la zona metropolitana de MTY (50km desde
    // Monterrey), a diferencia del buscador de EdicionLugarActivity, que no tiene límite.
    private static final double RADIO_BUSQUEDA_METROS = 50_000;

    private MapView mapa;
    private RepositorioLugares repositorio;

    private View fichaInferior;
    private ImageView fichaPin;
    private TextView fichaNombre;
    private TextView fichaDireccion;
    private TextView fichaVer;
    private TextView fichaIr;
    private TextView fichaGuardar;

    // Buscador (cualquier lugar de la zona metropolitana, vía Photon)
    private final Handler manejador = new Handler(Looper.getMainLooper());
    private EditText campoBuscar;
    private View btnLimpiarBusqueda;
    private View panelSugerencias;
    private TextView contadorSugerencias;
    private RecyclerSugerencias listaSugerencias;
    private AdaptadorSugerencias adaptadorSug;
    private Runnable busquedaPendiente;
    private long tokenBusqueda = 0;
    private boolean rellenandoDesdeSugerencia = false;
    private Marker marcadorBusqueda;

    // Puntos de referencia fijos (assets/puntos_referencia_mty.json) + su filtro por categoría
    private List<PuntoReferencia> puntosReferencia;
    private final List<Marker> marcadoresReferencia = new ArrayList<>();
    private final Set<CategoriasReferencia.Categoria> filtrosActivos =
            new HashSet<>(Arrays.asList(CategoriasReferencia.Categoria.values()));
    private TextView chipRefTodos, chipRefDeporte, chipRefComercial, chipRefEspectaculos,
            chipRefCultura, chipRefOtros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // osmdroid pide esta configuración antes que nada.
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_lugar);

        repositorio = ((Aplicacion) getApplication()).repositorio;

        // Barra
        ((TextView) findViewById(R.id.barra_titulo)).setText(getString(R.string.barra_mapa));
        findViewById(R.id.barra_atras).setOnClickListener(v -> finish());
        TextView chipGps = findViewById(R.id.barra_chip);
        chipGps.setVisibility(View.VISIBLE);
        chipGps.setText(getString(R.string.etiqueta_gps));
        chipGps.setOnClickListener(v -> centrarEnMiUbicacion());

        // Ficha inferior
        fichaInferior = findViewById(R.id.ficha_inferior);
        fichaPin = findViewById(R.id.ficha_pin);
        fichaNombre = findViewById(R.id.ficha_nombre);
        fichaDireccion = findViewById(R.id.ficha_direccion);
        fichaVer = findViewById(R.id.ficha_ver);
        fichaIr = findViewById(R.id.ficha_ir);
        fichaGuardar = findViewById(R.id.ficha_guardar);

        // Mapa: tiles de Esri "World Street Map", sin API key. Esri sirve las
        // teselas como z/y/x (fila antes que columna), por eso el getTileURLString.
        mapa = findViewById(R.id.mapa);
        OnlineTileSourceBase esri = new OnlineTileSourceBase(
                "EsriWorldStreetMap", 0, 19, 256, "",
                new String[]{
                        "https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/"
                },
                "Esri, HERE, Garmin, USGS, NGA") {
            @Override
            public String getTileURLString(long pMapTileIndex) {
                return getBaseUrl()
                        + MapTileIndex.getZoom(pMapTileIndex) + "/"
                        + MapTileIndex.getY(pMapTileIndex) + "/"
                        + MapTileIndex.getX(pMapTileIndex);
            }
        };
        mapa.setTileSource(esri);
        mapa.setMultiTouchControls(true);
        // Ocultar el zoom nativo: usamos los botones propios.
        mapa.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        findViewById(R.id.zoom_mas).setOnClickListener(v -> mapa.getController().zoomIn());
        findViewById(R.id.zoom_menos).setOnClickListener(v -> mapa.getController().zoomOut());

        // Tocar el mapa (no un marcador) esconde la ficha.
        mapa.setOnClickListener(v -> fichaInferior.setVisibility(View.GONE));

        configurarBuscador();
        configurarFiltroReferencia();

        int idLugar = getIntent().getIntExtra(EXTRA_ID_LUGAR, -1);
        int idPunto = getIntent().getIntExtra(EXTRA_ID_PUNTO_REFERENCIA, -1);
        if (idLugar >= 0) {
            mostrarUnLugar(idLugar);
        } else {
            mostrarTodos();
            if (idPunto >= 0) {
                enfocarPuntoReferencia(idPunto);
            }
        }
    }

    @Override
    protected void onDestroy() {
        manejador.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /** Cablea el input de búsqueda y el panel de sugerencias (mismo patrón que
     *  EdicionLugarActivity, pero acá el resultado no llena un formulario:
     *  centra el mapa, pone un marcador temporal y muestra la ficha inferior. */
    private void configurarBuscador() {
        campoBuscar = findViewById(R.id.campo_buscar);
        btnLimpiarBusqueda = findViewById(R.id.btn_limpiar_busqueda);
        panelSugerencias = findViewById(R.id.panel_sugerencias_mapa);
        contadorSugerencias = findViewById(R.id.contador_sugerencias_mapa);

        listaSugerencias = findViewById(R.id.lista_sugerencias_mapa);
        listaSugerencias.setLayoutManager(new LinearLayoutManager(this));
        listaSugerencias.addItemDecoration(new AdaptadorSugerencias.Divisor(this));
        adaptadorSug = new AdaptadorSugerencias(this::aplicarSugerenciaBusqueda);
        listaSugerencias.setAdapter(adaptadorSug);

        campoBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                btnLimpiarBusqueda.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                programarBusqueda(s.toString());
            }
        });
        campoBuscar.setOnFocusChangeListener((v, tieneFoco) -> {
            if (!tieneFoco) {
                ocultarPanelSugerencias();
            }
        });
        btnLimpiarBusqueda.setOnClickListener(v -> limpiarBusqueda());
    }

    /** Lanza una búsqueda 350 ms después de la última tecla (mín. 3 caracteres). */
    private void programarBusqueda(String texto) {
        if (rellenandoDesdeSugerencia) {
            return;
        }
        if (busquedaPendiente != null) {
            manejador.removeCallbacks(busquedaPendiente);
        }
        final String consulta = texto.trim();
        if (consulta.length() < 3) {
            ocultarPanelSugerencias();
            return;
        }
        // Sesgo hacia la posición actual (o Monterrey si aún no hay GPS).
        GeoPunto pos = ((Aplicacion) getApplication()).posicionActual;
        double biasLat = (pos != null && !pos.equals(GeoPunto.SIN_POSICION))
                ? pos.getLatitud() : CENTRO_DEFECTO.getLatitude();
        double biasLon = (pos != null && !pos.equals(GeoPunto.SIN_POSICION))
                ? pos.getLongitud() : CENTRO_DEFECTO.getLongitude();

        final long token = ++tokenBusqueda;
        busquedaPendiente = () -> BuscadorLugares.buscar(
                consulta, biasLat, biasLon, RADIO_BUSQUEDA_METROS, resultados -> {
            if (token != tokenBusqueda || isFinishing()) {
                return;   // llegó tarde: ya hay otra búsqueda más nueva
            }
            mostrarSugerenciasBusqueda(resultados);
        });
        manejador.postDelayed(busquedaPendiente, 350);
    }

    private void mostrarSugerenciasBusqueda(List<BuscadorLugares.Resultado> resultados) {
        if (rellenandoDesdeSugerencia || !campoBuscar.hasFocus()) {
            return;
        }
        if (resultados.isEmpty()) {
            ocultarPanelSugerencias();
            return;
        }
        GeoPunto pos = ((Aplicacion) getApplication()).posicionActual;
        adaptadorSug.actualizar(resultados, pos);
        contadorSugerencias.setText(String.valueOf(resultados.size()));
        panelSugerencias.setVisibility(View.VISIBLE);
    }

    private void ocultarPanelSugerencias() {
        if (panelSugerencias != null) {
            panelSugerencias.setVisibility(View.GONE);
        }
    }

    /** El usuario eligió un resultado: centra el mapa, marca el lugar y muestra
     *  la ficha inferior (sin botón "VER", porque no es un lugar guardado). */
    private void aplicarSugerenciaBusqueda(BuscadorLugares.Resultado r) {
        rellenandoDesdeSugerencia = true;
        if (busquedaPendiente != null) {
            manejador.removeCallbacks(busquedaPendiente);
            busquedaPendiente = null;
        }
        tokenBusqueda++;

        campoBuscar.setText(r.nombre);
        campoBuscar.setSelection(campoBuscar.getText().length());
        ocultarPanelSugerencias();
        rellenandoDesdeSugerencia = false;

        campoBuscar.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(campoBuscar.getWindowToken(), 0);
        }

        if (marcadorBusqueda != null) {
            mapa.getOverlays().remove(marcadorBusqueda);
        }
        GeoPoint punto = new GeoPoint(r.latitud, r.longitud);
        marcadorBusqueda = new Marker(mapa);
        marcadorBusqueda.setPosition(punto);
        marcadorBusqueda.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marcadorBusqueda.setIcon(ContextCompat.getDrawable(this, Tipos.pin(r.tipo)));
        marcadorBusqueda.setTitle(r.nombre);
        mapa.getOverlays().add(marcadorBusqueda);

        mapa.getController().setZoom(16.0);
        mapa.getController().animateTo(punto);

        fichaPin.setImageResource(Tipos.pin(r.tipo));
        fichaNombre.setText(r.nombre);
        fichaDireccion.setText(r.direccion);
        fichaVer.setVisibility(View.GONE);
        // No es un lugar guardado: en vez de VER se ofrece GUARDAR (abre el formulario precargado).
        fichaGuardar.setVisibility(View.VISIBLE);
        fichaGuardar.setOnClickListener(v ->
                startActivity(EdicionLugarActivity.intentPrecargado(this, r)));
        fichaIr.setOnClickListener(v -> Navegacion.irA(this, r.latitud, r.longitud));
        fichaInferior.setVisibility(View.VISIBLE);
    }

    /** Limpia el buscador: quita texto, panel, marcador temporal y ficha. */
    private void limpiarBusqueda() {
        rellenandoDesdeSugerencia = true;
        campoBuscar.setText("");
        rellenandoDesdeSugerencia = false;
        btnLimpiarBusqueda.setVisibility(View.GONE);
        ocultarPanelSugerencias();
        if (marcadorBusqueda != null) {
            mapa.getOverlays().remove(marcadorBusqueda);
            marcadorBusqueda = null;
            mapa.invalidate();
        }
        fichaInferior.setVisibility(View.GONE);
    }

    /** Modo "vengo del detalle": un solo lugar, centrado y con la ficha abierta. */
    private void mostrarUnLugar(int id) {
        Lugar lugar;
        try {
            lugar = repositorio.elemento(id);
        } catch (Exception e) {
            finish();
            return;
        }
        GeoPoint punto = puntoDe(lugar);
        agregarMarcador(id, lugar);
        mapa.getController().setZoom(16.0);
        mapa.getController().setCenter(punto);
        mostrarFicha(id, lugar);
    }

    /** Modo "vengo del tab MAPA": los 200 puntos de referencia (no los lugares guardados). */
    private void mostrarTodos() {
        puntosReferencia = new RepositorioPuntosReferencia(this).cargarTodos();
        dibujarPuntosReferencia();
        mapa.getController().setZoom(12.0);
        mapa.getController().setCenter(CENTRO_DEFECTO);
    }

    /** Modo "vengo de la lista CERCA": el mismo mapa de siempre, pero centrado en ese
     *  punto y con su ficha abierta. Se ve el resto de los puntos para dar contexto. */
    private void enfocarPuntoReferencia(int id) {
        for (PuntoReferencia punto : puntosReferencia) {
            if (punto.id() == id) {
                mapa.getController().setZoom(16.0);
                mapa.getController().setCenter(new GeoPoint(
                        punto.getPosicion().getLatitud(), punto.getPosicion().getLongitud()));
                mostrarFichaPunto(punto);
                return;
            }
        }
    }

    /** Cablea los chips TODOS/DEPORTE/COMERCIAL/ESPECTÁCULOS/CULTURA/OTROS. */
    private void configurarFiltroReferencia() {
        chipRefTodos = findViewById(R.id.chip_ref_todos);
        chipRefDeporte = findViewById(R.id.chip_ref_deporte);
        chipRefComercial = findViewById(R.id.chip_ref_comercial);
        chipRefEspectaculos = findViewById(R.id.chip_ref_espectaculos);
        chipRefCultura = findViewById(R.id.chip_ref_cultura);
        chipRefOtros = findViewById(R.id.chip_ref_otros);

        chipRefTodos.setOnClickListener(v -> {
            filtrosActivos.clear();
            filtrosActivos.addAll(Arrays.asList(CategoriasReferencia.Categoria.values()));
            refrescarFiltroReferencia();
        });
        chipRefDeporte.setOnClickListener(v ->
                alternarFiltro(CategoriasReferencia.Categoria.DEPORTE_PARQUES));
        chipRefComercial.setOnClickListener(v ->
                alternarFiltro(CategoriasReferencia.Categoria.COMERCIAL));
        chipRefEspectaculos.setOnClickListener(v ->
                alternarFiltro(CategoriasReferencia.Categoria.ESPECTACULOS));
        chipRefCultura.setOnClickListener(v ->
                alternarFiltro(CategoriasReferencia.Categoria.CULTURA));
        chipRefOtros.setOnClickListener(v ->
                alternarFiltro(CategoriasReferencia.Categoria.OTROS));

        refrescarFiltroReferencia();
    }

    /** Prende/apaga una categoría del filtro (multi-selección independiente). */
    private void alternarFiltro(CategoriasReferencia.Categoria categoria) {
        if (filtrosActivos.contains(categoria)) {
            filtrosActivos.remove(categoria);
        } else {
            filtrosActivos.add(categoria);
        }
        refrescarFiltroReferencia();
    }

    private void refrescarFiltroReferencia() {
        boolean todasActivas = filtrosActivos.size() == CategoriasReferencia.Categoria.values().length;
        chipRefTodos.setSelected(todasActivas);
        chipRefDeporte.setSelected(
                filtrosActivos.contains(CategoriasReferencia.Categoria.DEPORTE_PARQUES));
        chipRefComercial.setSelected(
                filtrosActivos.contains(CategoriasReferencia.Categoria.COMERCIAL));
        chipRefEspectaculos.setSelected(
                filtrosActivos.contains(CategoriasReferencia.Categoria.ESPECTACULOS));
        chipRefCultura.setSelected(
                filtrosActivos.contains(CategoriasReferencia.Categoria.CULTURA));
        chipRefOtros.setSelected(
                filtrosActivos.contains(CategoriasReferencia.Categoria.OTROS));
        dibujarPuntosReferencia();
    }

    /** Redibuja los marcadores de puntos de referencia según el filtro activo. */
    private void dibujarPuntosReferencia() {
        for (Marker m : marcadoresReferencia) {
            mapa.getOverlays().remove(m);
        }
        marcadoresReferencia.clear();
        if (puntosReferencia == null) {
            return;
        }
        for (PuntoReferencia punto : puntosReferencia) {
            CategoriasReferencia.Categoria categoria = CategoriasReferencia.de(punto.getCategoria());
            if (!filtrosActivos.contains(categoria)) {
                continue;
            }
            Marker marcador = new Marker(mapa);
            marcador.setPosition(new GeoPoint(
                    punto.getPosicion().getLatitud(), punto.getPosicion().getLongitud()));
            marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marcador.setIcon(ContextCompat.getDrawable(this, CategoriasReferencia.pin(categoria)));
            marcador.setTitle(punto.getNombre());
            marcador.setOnMarkerClickListener((m, mapView) -> {
                mostrarFichaPunto(punto);
                mapa.getController().animateTo(m.getPosition());
                return true;
            });
            mapa.getOverlays().add(marcador);
            marcadoresReferencia.add(marcador);
        }
        mapa.invalidate();
    }

    /** Ficha inferior para un punto de referencia (sin botón "VER": no es un lugar guardado). */
    private void mostrarFichaPunto(PuntoReferencia punto) {
        CategoriasReferencia.Categoria categoria = CategoriasReferencia.de(punto.getCategoria());
        fichaPin.setImageResource(CategoriasReferencia.pin(categoria));
        fichaNombre.setText(punto.getNombre());
        fichaDireccion.setText(punto.getCategoria());
        fichaVer.setVisibility(View.GONE);
        fichaGuardar.setVisibility(View.GONE);
        fichaIr.setOnClickListener(v -> Navegacion.irA(this, 
                punto.getPosicion().getLatitud(), punto.getPosicion().getLongitud()));
        fichaInferior.setVisibility(View.VISIBLE);
    }

    private void agregarMarcador(int id, Lugar lugar) {
        Marker marcador = new Marker(mapa);
        marcador.setId(String.valueOf(id));
        marcador.setPosition(puntoDe(lugar));
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marcador.setIcon(ContextCompat.getDrawable(this, pinDe(lugar.getTipo())));
        marcador.setTitle(lugar.getNombre());
        marcador.setOnMarkerClickListener((m, mapView) -> {
            int idm = Integer.parseInt(m.getId());
            mostrarFicha(idm, repositorio.elemento(idm));
            mapa.getController().animateTo(m.getPosition());
            return true;
        });
        mapa.getOverlays().add(marcador);
    }

    private void mostrarFicha(int id, Lugar lugar) {
        fichaPin.setImageResource(pinDe(lugar.getTipo()));
        fichaNombre.setText(lugar.getNombre());
        fichaDireccion.setText(lugar.getDireccion());
        fichaVer.setVisibility(View.VISIBLE);   // por si quedó oculto de un resultado de búsqueda
        fichaGuardar.setVisibility(View.GONE);
        fichaVer.setOnClickListener(v -> {
            Intent intent = new Intent(this, VistaLugarActivity.class);
            intent.putExtra(VistaLugarActivity.EXTRA_ID, id);
            startActivity(intent);
        });
        fichaIr.setOnClickListener(v -> Navegacion.irA(this, lugar.getPosicion().getLatitud(),
                lugar.getPosicion().getLongitud()));
        fichaInferior.setVisibility(View.VISIBLE);
    }

    private void centrarEnMiUbicacion() {
        GeoPunto pos = ((Aplicacion) getApplication()).posicionActual;
        if (pos == null || pos.equals(GeoPunto.SIN_POSICION)) {
            return;
        }
        mapa.getController().animateTo(new GeoPoint(pos.getLatitud(), pos.getLongitud()));
    }

    private static GeoPoint puntoDe(Lugar lugar) {
        GeoPunto p = lugar.getPosicion();
        return new GeoPoint(p.getLatitud(), p.getLongitud());
    }

    private static int pinDe(TipoLugar tipo) {
        return Tipos.pin(tipo);
    }
}
