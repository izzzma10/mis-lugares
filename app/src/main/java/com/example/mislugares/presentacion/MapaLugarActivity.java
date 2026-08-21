package com.example.mislugares.presentacion;

import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;
import com.example.mislugares.casos_uso.VerLugar;
import com.example.mislugares.datos.RepositorioLugares;
import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.Lugar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapaLugarActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String EXTRA_ID_LUGAR = "com.example.mislugares.ID_LUGAR";

    private GoogleMap mapa;
    private Lugar lugar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa_lugar);

        int idLugar = getIntent().getIntExtra(EXTRA_ID_LUGAR, -1);
        RepositorioLugares repositorio = ((Aplicacion) getApplication()).repositorio;
        lugar = new VerLugar(repositorio).ver(idLugar);

        SupportMapFragment fragmentoMapa = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapa);
        fragmentoMapa.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapa = googleMap;
        if (lugar == null) {
            return;
        }
        GeoPunto posicion = lugar.getPosicion();
        LatLng coordenadas = new LatLng(posicion.getLatitud(), posicion.getLongitud());
        mapa.addMarker(new MarkerOptions().position(coordenadas).title(lugar.getNombre()));
        mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenadas, 16));
    }
}
