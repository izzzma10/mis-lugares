package com.example.mislugares.presentacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;
import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.Lugar;
import com.example.mislugares.modelo.TipoLugar;
import com.example.mislugares.datos.RepositorioLugares;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.Map;

public class MapaLugarActivity extends AppCompatActivity {

    public static final String EXTRA_ID_LUGAR = "com.example.mislugares.ID_LUGAR";

    // Centro por defecto (Monterrey) si no hay lugares ni ubicación conocida.
    private static final GeoPoint CENTRO_DEFECTO = new GeoPoint(25.6866, -100.3161);

    private MapView mapa;
    private RepositorioLugares repositorio;

    private View fichaInferior;
    private ImageView fichaPin;
    private TextView fichaNombre;
    private TextView fichaDireccion;
    private TextView fichaVer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // osmdroid pide esta configuración antes que nada.
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_lugar);

        repositorio = ((Aplicacion) getApplication()).repositorio;

        // Barra
        ((TextView) findViewById(R.id.barra_titulo)).setText("MAPA");
        findViewById(R.id.barra_atras).setOnClickListener(v -> finish());
        TextView chipGps = findViewById(R.id.barra_chip);
        chipGps.setVisibility(View.VISIBLE);
        chipGps.setText("GPS");
        chipGps.setOnClickListener(v -> centrarEnMiUbicacion());

        // Ficha inferior
        fichaInferior = findViewById(R.id.ficha_inferior);
        fichaPin = findViewById(R.id.ficha_pin);
        fichaNombre = findViewById(R.id.ficha_nombre);
        fichaDireccion = findViewById(R.id.ficha_direccion);
        fichaVer = findViewById(R.id.ficha_ver);

        // Mapa (tiles de CARTO, uso libre para apps externas)
        mapa = findViewById(R.id.mapa);
        ITileSource carto = new XYTileSource(
                "CartoVoyager", 0, 20, 256, ".png",
                new String[]{
                        "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
                        "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
                        "https://c.basemaps.cartocdn.com/rastertiles/voyager/"
                });
        mapa.setTileSource(carto);
        mapa.setMultiTouchControls(true);
        // Ocultar el zoom nativo: usamos los botones propios.
        mapa.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        findViewById(R.id.zoom_mas).setOnClickListener(v -> mapa.getController().zoomIn());
        findViewById(R.id.zoom_menos).setOnClickListener(v -> mapa.getController().zoomOut());

        // Tocar el mapa (no un marcador) esconde la ficha.
        mapa.setOnClickListener(v -> fichaInferior.setVisibility(View.GONE));

        int idLugar = getIntent().getIntExtra(EXTRA_ID_LUGAR, -1);
        if (idLugar >= 0) {
            mostrarUnLugar(idLugar);
        } else {
            mostrarTodos();
        }
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

    /** Modo "vengo del tab MAPA": todos los lugares. */
    private void mostrarTodos() {
        Map<Integer, Lugar> lugares = repositorio.todos();
        GeoPoint centro = null;
        for (Map.Entry<Integer, Lugar> entrada : lugares.entrySet()) {
            Lugar lugar = entrada.getValue();
            agregarMarcador(entrada.getKey(), lugar);
            if (centro == null && tienePosicion(lugar)) {
                centro = puntoDe(lugar);
            }
        }
        mapa.getController().setZoom(12.0);
        mapa.getController().setCenter(centro != null ? centro : CENTRO_DEFECTO);
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
        fichaVer.setOnClickListener(v -> {
            Intent intent = new Intent(this, VistaLugarActivity.class);
            intent.putExtra(VistaLugarActivity.EXTRA_ID, id);
            startActivity(intent);
        });
        fichaInferior.setVisibility(View.VISIBLE);
    }

    private void centrarEnMiUbicacion() {
        GeoPunto pos = ((Aplicacion) getApplication()).posicionActual;
        if (pos == null || pos.equals(GeoPunto.SIN_POSICION)) {
            return;
        }
        mapa.getController().animateTo(new GeoPoint(pos.getLatitud(), pos.getLongitud()));
    }

    private static boolean tienePosicion(Lugar lugar) {
        GeoPunto p = lugar.getPosicion();
        return p != null && !p.equals(GeoPunto.SIN_POSICION);
    }

    private static GeoPoint puntoDe(Lugar lugar) {
        GeoPunto p = lugar.getPosicion();
        return new GeoPoint(p.getLatitud(), p.getLongitud());
    }

    private static int pinDe(TipoLugar tipo) {
        return Tipos.pin(tipo);
    }
}
