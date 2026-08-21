package com.example.mislugares.presentacion;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;
import com.example.mislugares.casos_uso.GuardarLugar;
import com.example.mislugares.casos_uso.VerLugar;
import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.Lugar;

public class VistaLugarActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vista_lugar);

        Aplicacion aplicacion = (Aplicacion) getApplication();
        Lugar lugar;
        int id = getIntent().getIntExtra(EXTRA_ID, -1);
        if (id >= 0) {
            lugar = new VerLugar(aplicacion.repositorio).ver(id);
        } else {
            lugar = lugarDePrueba();
        }

        TextView tvNombre = findViewById(R.id.nombre);
        TextView tvDireccion = findViewById(R.id.direccion);
        TextView tvTelefono = findViewById(R.id.telefono);
        TextView tvGps = findViewById(R.id.gps);
        RatingBar rbValoracion = findViewById(R.id.valoracion);
        ImageView ivFoto = findViewById(R.id.foto);

        tvNombre.setText(lugar.getNombre());
        tvDireccion.setText(lugar.getDireccion());
        tvTelefono.setText(String.valueOf(lugar.getTelefono()));
        tvGps.setText("GPS: " + lugar.getPosicion());
        rbValoracion.setRating(lugar.getValoracion());
        ivFoto.setImageResource(idFoto(lugar.getFoto()));

        // Permite calificar el lugar arrastrando las estrellas desde esta
        // misma pantalla; solo se guarda si el lugar es real (tiene id).
        int idFinal = id;
        rbValoracion.setOnRatingBarChangeListener((ratingBar, valor, fromUser) -> {
            if (!fromUser) return;
            lugar.setValoracion(valor);
            if (idFinal >= 0) {
                new GuardarLugar(aplicacion.repositorio).actualizar(idFinal, lugar);
            }
        });
    }

    /** Busca el drawable con ese nombre; si no existe o está vacío, usa el placeholder. */
    private int idFoto(String nombreFoto) {
        if (nombreFoto == null || nombreFoto.isEmpty()) {
            return R.drawable.foto_epsg;
        }
        int id = getResources().getIdentifier(nombreFoto, "drawable", getPackageName());
        return id != 0 ? id : R.drawable.foto_epsg;
    }

    private Lugar lugarDePrueba() {
        GeoPunto posicion = new GeoPunto(-100.309, 25.686);
        return new Lugar("Facultad de Ingenieria Mecanica y Electrica",
                "Pedro de Alba SN, Niños Héroes, Ciudad Universitaria, 66455 San Nicolás de los Garza, N.L.",
                818329402,
                "http://www.epsg.upv.es",
                "Uno de los mejores lugares para formarse.",
                4.0f,
                posicion,
                "foto_epsg");
    }
}
