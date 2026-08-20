package com.example.mislugares;

import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.Lugar;

public class VistaLugarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vista_lugar);

        GeoPunto posicion = new GeoPunto(-100.309, 25.686);
        Lugar lugar = new Lugar("Facultad de Ingenieria Mecanica y Electrica",
                "Pedro de Alba SN, Niños Héroes, Ciudad Universitaria, 66455 San Nicolás de los Garza, N.L.",
                818329402,
                "http://www.epsg.upv.es",
                "Uno de los mejores lugares para formarse.",
                4.0f,
                posicion,
                "foto_epsg");

        TextView tvNombre = findViewById(R.id.nombre);
        TextView tvDireccion = findViewById(R.id.direccion);
        TextView tvTelefono = findViewById(R.id.telefono);
        TextView tvGps = findViewById(R.id.gps);
        RatingBar rbValoracion = findViewById(R.id.valoracion);

        tvNombre.setText(lugar.getNombre());
        tvDireccion.setText(lugar.getDireccion());
        tvTelefono.setText(String.valueOf(lugar.getTelefono()));
        tvGps.setText("GPS: " + lugar.getPosicion());
        rbValoracion.setRating(lugar.getValoracion());
    }
}
