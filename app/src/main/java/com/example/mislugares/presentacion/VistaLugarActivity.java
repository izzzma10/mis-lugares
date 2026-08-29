package com.example.mislugares.presentacion;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;
import com.example.mislugares.casos_uso.GuardarLugar;
import com.example.mislugares.casos_uso.VerLugar;
import com.example.mislugares.datos.RepositorioLugares;
import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.Lugar;

import java.io.File;
import java.io.IOException;

public class VistaLugarActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";

    private RepositorioLugares repositorio;
    private Lugar lugar;
    private int id;
    private ImageView ivFoto;
    private Uri uriFotoCamara;
    private TextView tvNombre;
    private TextView tvDireccion;
    private TextView tvTelefono;
    private TextView tvGps;
    private RatingBar rbValoracion;

    /** Selector de imágenes de la galería: no requiere permiso de lectura (Storage Access Framework). */
    private final ActivityResultLauncher<String> selectorGaleria =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    return;
                }
                try {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignorado) {
                    // Algunos proveedores no permiten persistir el permiso; la foto
                    // igual se ve mientras dure esta sesión de la app.
                }
                asignarFoto(uri.toString());
            });

    /** Lanza la app de cámara del sistema; no requiere permiso de CÁMARA porque no accedemos
     *  directamente al hardware, delegamos la captura en otra app (ver nota del libro, sec. 3.11.1). */
    private final ActivityResultLauncher<Uri> tomarFoto =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), exito -> {
                if (exito && uriFotoCamara != null) {
                    asignarFoto(uriFotoCamara.toString());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vista_lugar);

        Aplicacion aplicacion = (Aplicacion) getApplication();
        repositorio = aplicacion.repositorio;

        id = getIntent().getIntExtra(EXTRA_ID, -1);
        if (id >= 0) {
            lugar = new VerLugar(repositorio).ver(id);
        } else {
            lugar = lugarDePrueba();
        }

        tvNombre = findViewById(R.id.nombre);
        tvDireccion = findViewById(R.id.direccion);
        tvTelefono = findViewById(R.id.telefono);
        tvGps = findViewById(R.id.gps);
        rbValoracion = findViewById(R.id.valoracion);
        ivFoto = findViewById(R.id.foto);

        mostrarLugar();

        // Permite calificar el lugar arrastrando las estrellas desde esta
        // misma pantalla; solo se guarda si el lugar es real (tiene id).
        rbValoracion.setOnRatingBarChangeListener((ratingBar, valor, fromUser) -> {
            if (!fromUser) return;
            lugar.setValoracion(valor);
            if (id >= 0) {
                new GuardarLugar(repositorio).actualizar(id, lugar);
            }
        });

        Button btnEditar = findViewById(R.id.btn_editar);

        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(this, EdicionLugarActivity.class);
            intent.putExtra(EdicionLugarActivity.EXTRA_ID, id);
            startActivity(intent);
        });

        Button btnMapa = findViewById(R.id.btn_mapa);

        btnMapa.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapaLugarActivity.class);
            intent.putExtra(MapaLugarActivity.EXTRA_ID_LUGAR, id);
            startActivity(intent);
        });

        Button btnCompartir = findViewById(R.id.btnCompartir);

        btnCompartir.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, lugar.getNombre() + " " + lugar.getUrl());
            startActivity(intent);
        });

        tvTelefono.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + lugar.getTelefono()));
            startActivity(intent);
        });

        tvDireccion.setOnClickListener(v -> {
            GeoPunto posicion = lugar.getPosicion();
            String uri;
            if (posicion.getLatitud() != 0 || posicion.getLongitud() != 0) {
                uri = "geo:" + posicion.getLatitud() + "," + posicion.getLongitud();
            } else {
                uri = "geo:0,0?q=" + Uri.encode(lugar.getDireccion());
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
        });

        ImageView ivGaleria = findViewById(R.id.galeria);
        ivGaleria.setOnClickListener(v -> selectorGaleria.launch("image/*"));

        ImageView ivCamara = findViewById(R.id.camara);
        ivCamara.setOnClickListener(v -> {
            uriFotoCamara = crearArchivoFoto();
            if (uriFotoCamara != null) {
                tomarFoto.launch(uriFotoCamara);
            }
        });

        ImageView ivEliminarFoto = findViewById(R.id.eliminar_foto);
        ivEliminarFoto.setOnClickListener(v -> asignarFoto(""));
    }

    /** Refresca los campos de la pantalla a partir de {@link #lugar}. */
    private void mostrarLugar() {
        tvNombre.setText(lugar.getNombre());
        tvDireccion.setText(lugar.getDireccion());
        tvTelefono.setText(String.valueOf(lugar.getTelefono()));
        tvGps.setText("GPS: " + lugar.getPosicion());
        rbValoracion.setRating(lugar.getValoracion());
        mostrarFoto(lugar.getFoto());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Vuelve a pedir el lugar al repositorio: si venimos de editarlo en
        // otra pantalla, aquí es donde se refleja el cambio. Si ya no existe
        // (lo acaban de eliminar), no hay nada que mostrar: se cierra la pantalla.
        if (id >= 0) {
            if (id < repositorio.tamaño()) {
                lugar = new VerLugar(repositorio).ver(id);
                mostrarLugar();
            } else {
                Toast.makeText(this, "Este lugar ya no existe", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /** Guarda la nueva foto en el lugar, la muestra en pantalla, y persiste el cambio si el lugar es real. */
    private void asignarFoto(String uriFoto) {
        lugar.setFoto(uriFoto);
        mostrarFoto(uriFoto);
        if (id >= 0) {
            new GuardarLugar(repositorio).actualizar(id, lugar);
        }
    }

    /** La foto puede ser un nombre de drawable (lugares de ejemplo) o una URI real
     *  (content://, file://) elegida por el usuario desde galería o cámara. */
    private void mostrarFoto(String foto) {
        if (foto == null || foto.isEmpty()) {
            ivFoto.setImageResource(R.drawable.foto_epsg);
        } else if (foto.contains("://")) {
            ivFoto.setImageURI(Uri.parse(foto));
        } else {
            ivFoto.setImageResource(idFoto(foto));
        }
    }

    /** Crea un fichero vacío en el almacenamiento privado de la app y devuelve su URI
     *  a través del FileProvider, para que la app de cámara pueda escribir la foto ahí. */
    private Uri crearArchivoFoto() {
        try {
            File directorio = getExternalFilesDir("Pictures");
            File archivo = File.createTempFile(
                    "foto_" + System.currentTimeMillis() / 1000, ".jpg", directorio);
            return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", archivo);
        } catch (IOException e) {
            return null;
        }
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
                "8112755584",
                "http://www.epsg.upv.es",
                "Uno de los mejores lugares para formarse.",
                4.0f,
                posicion,
                "foto_epsg");
    }
}
