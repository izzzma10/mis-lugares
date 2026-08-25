package com.example.mislugares.presentacion;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;
import com.example.mislugares.casos_uso.VerLugar;
import com.example.mislugares.datos.RepositorioLugares;
import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.Lugar;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapaLugarActivity extends AppCompatActivity {

    public static final String EXTRA_ID_LUGAR = "com.example.mislugares.ID_LUGAR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // osmdroid pide esta línea de configuración antes que nada, es fija, siempre igual.
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));
        // El servidor de OpenStreetMap exige un User-Agent que identifique la app,
        // o rechaza las imágenes del mapa con error 403.
        Configuration.getInstance().setUserAgentValue(getPackageName());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_lugar);

        int idLugar = getIntent().getIntExtra(EXTRA_ID_LUGAR, -1);
        RepositorioLugares repositorio = ((Aplicacion) getApplication()).repositorio;
        // VerLugar.ver() no devuelve null con un id inválido, lanza una excepción:
        // hay que validar el id antes de pedirlo.
        if (idLugar < 0 || idLugar >= repositorio.tamaño()) {
            finish();
            return;
        }
        Lugar lugar = new VerLugar(repositorio).ver(idLugar);

        MapView mapa = findViewById(R.id.mapa);
        // CARTO ofrece mapas basados en OpenStreetMap de uso libre para apps externas.
        ITileSource carto = new XYTileSource(
                "CartoVoyager", 0, 20, 256, ".png",
                new String[]{
                        "https://a.basemaps.cartocdn.com/rastertiles/voyager/",
                        "https://b.basemaps.cartocdn.com/rastertiles/voyager/",
                        "https://c.basemaps.cartocdn.com/rastertiles/voyager/"
                });
        mapa.setTileSource(carto);
        mapa.setMultiTouchControls(true);

        GeoPunto posicion = lugar.getPosicion();
        GeoPoint punto = new GeoPoint(posicion.getLatitud(), posicion.getLongitud());
        mapa.getController().setZoom(16.0);
        mapa.getController().setCenter(punto);

        Marker marcador = new Marker(mapa);
        marcador.setPosition(punto);
        marcador.setTitle(lugar.getNombre());
        mapa.getOverlays().add(marcador);
    }
}
