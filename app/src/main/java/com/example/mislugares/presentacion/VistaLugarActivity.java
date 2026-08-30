package com.example.mislugares.presentacion;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;

import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;
import com.example.mislugares.casos_uso.GuardarLugar;
import com.example.mislugares.casos_uso.VerLugar;
import com.example.mislugares.datos.RepositorioLugares;
import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.Lugar;

import java.io.File;
import java.io.IOException;
import java.util.Locale;


public class VistaLugarActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";

    private static final int CODIGO_PERMISO_LLAMAR = 200;
    private RepositorioLugares repositorio;
    private Lugar lugar;
    private int id;
    private ImageView ivFoto;
    private Uri uriFotoCamara;
    private TextView tvNombre;
    private TextView tvTipo;
    private TextView tvResumen;
    private TextView tvDireccion;
    private TextView tvTelefono;
    private TextView tvUrl;
    private TextView tvGps;
    private RatingBar rbValoracion;
    private ImageView ivFavorito;

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

        ((TextView) findViewById(R.id.barra_titulo)).setText("DETALLE");
        findViewById(R.id.barra_atras).setOnClickListener(v -> finish());

        Aplicacion aplicacion = (Aplicacion) getApplication();
        repositorio = aplicacion.repositorio;

        id = getIntent().getIntExtra(EXTRA_ID, -1);
        if (id >= 0) {
            lugar = new VerLugar(repositorio).ver(id);
        } else {
            lugar = lugarDePrueba();
        }

        tvNombre = findViewById(R.id.nombre);
        tvTipo = findViewById(R.id.tipo);
        tvResumen = findViewById(R.id.resumen_valoracion);
        tvDireccion = findViewById(R.id.direccion);
        tvTelefono = findViewById(R.id.telefono);
        tvUrl = findViewById(R.id.url);
        tvGps = findViewById(R.id.gps);
        rbValoracion = findViewById(R.id.valoracion);
        ivFoto = findViewById(R.id.foto);
        ivFavorito = findViewById(R.id.favorito);

        mostrarLugar();

        ivFavorito.setOnClickListener(v -> {
            lugar.setFavorito(!lugar.isFavorito());
            mostrarFavorito();
            if (id >= 0) {
                new GuardarLugar(repositorio).actualizar(id, lugar);
            }
        });

        // Permite calificar el lugar arrastrando las estrellas desde esta
        // misma pantalla; solo se guarda si el lugar es real (tiene id).
        rbValoracion.setOnRatingBarChangeListener((ratingBar, valor, fromUser) -> {
            if (!fromUser) return;
            lugar.setValoracion(valor);
            if (id >= 0) {
                new GuardarLugar(repositorio).actualizar(id, lugar);
            }
        });

        // "EDITAR" ahora es el chip de la barra superior
        TextView chipEditar = findViewById(R.id.barra_chip);
        chipEditar.setVisibility(View.VISIBLE);
        chipEditar.setText("EDITAR");
        chipEditar.setOnClickListener(v -> {
            Intent intent = new Intent(this, EdicionLugarActivity.class);
            intent.putExtra(EdicionLugarActivity.EXTRA_ID, id);
            startActivity(intent);
        });

        findViewById(R.id.btn_mapa).setOnClickListener(v -> {
            Intent intent = new Intent(this, MapaLugarActivity.class);
            intent.putExtra(MapaLugarActivity.EXTRA_ID_LUGAR, id);
            startActivity(intent);
        });

        findViewById(R.id.btnCompartir).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, lugar.getNombre() + " " + lugar.getUrl());
            startActivity(intent);
        });

        findViewById(R.id.btn_llamar).setOnClickListener(v -> llamar());

        tvTelefono.setOnClickListener(v -> llamar());

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

        tvUrl.setOnClickListener(v -> {
            if (!lugar.getUrl().isEmpty()) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(lugar.getUrl())));
            }
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
        ivEliminarFoto.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar foto")
                    .setMessage("¿Seguro que quieres eliminar la foto de este lugar?")
                    .setPositiveButton("Eliminar", (dialogo, boton)-> asignarFoto(""))
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }

    /** Refresca los campos de la pantalla a partir de {@link #lugar}. */
    private void mostrarLugar() {
        tvNombre.setText(lugar.getNombre());
        tvTipo.setText(lugar.getTipo().name());
        tvDireccion.setText(lugar.getDireccion());
        tvTelefono.setText(String.valueOf(lugar.getTelefono()));
        tvUrl.setText(lugar.getUrl());
        tvGps.setText(String.format(Locale.US, "%.4f, %.4f",
                lugar.getPosicion().getLatitud(), lugar.getPosicion().getLongitud()));
        rbValoracion.setRating(lugar.getValoracion());
        mostrarResumen();
        mostrarFoto(lugar.getFoto());
        mostrarFavorito();
    }

    /** "4.5" o "4.5 · 1.2 km" si hay posición conocida. */
    private void mostrarResumen() {
        GeoPunto yo = ((Aplicacion) getApplication()).posicionActual;
        GeoPunto suya = lugar.getPosicion();
        if (yo.equals(GeoPunto.SIN_POSICION) || suya.equals(GeoPunto.SIN_POSICION)) {
            tvResumen.setText(String.format(Locale.US, "%.1f", lugar.getValoracion()));
        } else {
            float km = (float) yo.distancia(suya) / 1000f;
            tvResumen.setText(String.format(Locale.US, "%.1f · %.1f km",
                    lugar.getValoracion(), km));
        }
    }

    private void mostrarFavorito() {
        ivFavorito.setImageResource(lugar.isFavorito()
                ? R.drawable.ic_estrella_llena
                : R.drawable.ic_estrella_vacia);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Vuelve a pedir el lugar al repositorio: si venimos de editarlo en
        // otra pantalla, aquí es donde se refleja el cambio. Si ya no existe
        // (lo acaban de eliminar), no hay nada que mostrar: se cierra la pantalla.
        if (id >= 0) {
            try {
                lugar = new VerLugar(repositorio).ver(id);
                mostrarLugar();
            } catch (Exception e) {
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
            ivFoto.setImageResource(R.drawable.foto_placeholder);
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
            return R.drawable.foto_placeholder;
        }
        int id = getResources().getIdentifier(nombreFoto, "drawable", getPackageName());
        return id != 0 ? id : R.drawable.foto_placeholder;
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

    private void llamar() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    CODIGO_PERMISO_LLAMAR);
            return;
        }
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + lugar.getTelefono())));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permission, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permission, grantResults);
        if (requestCode == CODIGO_PERMISO_LLAMAR
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            llamar();
        }
    }
}
