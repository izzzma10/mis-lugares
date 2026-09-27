package com.example.mislugares.presentacion;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.Manifest;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
    private TextView tvNota;
    private LinearLayout tiraValoracion;
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

        ((TextView) findViewById(R.id.barra_titulo)).setText(getString(R.string.barra_detalle));
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
        tvNota = findViewById(R.id.nota);
        ivFoto = findViewById(R.id.foto);
        ivFavorito = findViewById(R.id.favorito);

        tiraValoracion = findViewById(R.id.tira_valoracion);
        configurarTiraValoracion();

        mostrarLugar();

        ivFavorito.setOnClickListener(v -> {
            lugar.setFavorito(!lugar.isFavorito());
            mostrarFavorito();
            if (id >= 0) {
                new GuardarLugar(repositorio).actualizar(id, lugar);
            }
        });


        // "EDITAR" ahora es el chip de la barra superior
        TextView chipEditar = findViewById(R.id.barra_chip);
        chipEditar.setVisibility(View.VISIBLE);
        chipEditar.setText(getString(R.string.editar));
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

        findViewById(R.id.btnCompartir).setOnClickListener(v -> compartirUbicacion());

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
        ivEliminarFoto.setOnClickListener(v -> DialogosPixel.mostrar(this, getString(R.string.dlg_eliminar_foto_titulo),
                getString(R.string.dlg_eliminar_foto_msg),
                getString(R.string.dlg_eliminar), true, () -> asignarFoto("")));
    }

    /** Refresca los campos de la pantalla a partir de {@link #lugar}. */
    private void mostrarLugar() {
        tvNombre.setText(lugar.getNombre());
        tvTipo.setText(Tipos.nombre(this, lugar.getTipo()));
        tvDireccion.setText(lugar.getDireccion());
        tvTelefono.setText(String.valueOf(lugar.getTelefono()));
        tvUrl.setText(lugar.getUrl());
        tvGps.setText(String.format(Locale.US, getString(R.string.gps_formato),
                lugar.getPosicion().getLatitud(), lugar.getPosicion().getLongitud()));

        String nota = lugar.getComentario() != null ? lugar.getComentario().trim() : "";
        boolean hayNota = !nota.isEmpty();
        tvNota.setText(nota);
        tvNota.setVisibility(hayNota ? View.VISIBLE : View.GONE);
        findViewById(R.id.nota_fila).setVisibility(hayNota ? View.VISIBLE : View.GONE);
        findViewById(R.id.nota_divisor).setVisibility(hayNota ? View.VISIBLE : View.GONE);
        Estrellas.pintarValoracion(tiraValoracion, lugar.getValoracion(), true);
        mostrarResumen();
        mostrarFoto(lugar.getFoto());
        mostrarFavorito();
    }

    /** Calificar tocando o deslizando sobre la tira: pasos de media estrella. */
    @SuppressLint("ClickableViewAccessibility")
    private void configurarTiraValoracion() {
        tiraValoracion.setOnTouchListener((v, evento) -> {
            float ancho = v.getWidth();
            if (ancho <= 0f) {
                return false;
            }
            float x = Math.max(0f, Math.min(evento.getX(), ancho));
            float crudo = x / (ancho / 5f);                 // 0..5
            float valor = Math.round(crudo * 2f) / 2f;      // a pasos de 0.5
            valor = Math.max(0.5f, Math.min(5f, valor));

            int accion = evento.getActionMasked();
            if (accion == MotionEvent.ACTION_DOWN) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
            }
            if (accion == MotionEvent.ACTION_DOWN || accion == MotionEvent.ACTION_MOVE) {
                aplicarValoracion(valor, false);
                return true;
            }
            if (accion == MotionEvent.ACTION_UP) {
                aplicarValoracion(valor, true);
                return true;
            }
            return false;
        });
    }

    /** Pinta la valoración y, si {@code persistir}, la guarda. */
    private void aplicarValoracion(float valor, boolean persistir) {
        lugar.setValoracion(valor);
        Estrellas.pintarValoracion(tiraValoracion, valor, true);
        mostrarResumen();
        if (persistir && id >= 0) {
            new GuardarLugar(repositorio).actualizar(id, lugar);
        }
    }

    /** La valoración ya la muestran las estrellas; acá solo va la distancia, si se conoce. */
    private void mostrarResumen() {
        GeoPunto yo = ((Aplicacion) getApplication()).posicionActual;
        GeoPunto suya = lugar.getPosicion();
        if (yo.equals(GeoPunto.SIN_POSICION) || suya.equals(GeoPunto.SIN_POSICION)) {
            tvResumen.setVisibility(View.GONE);
        } else {
            float km = (float) yo.distancia(suya) / 1000f;
            tvResumen.setText(String.format(Locale.US, getString(R.string.resumen_distancia_km), km));
            tvResumen.setVisibility(View.VISIBLE);
        }
    }

    private void mostrarFavorito() {
        ivFavorito.setImageResource(lugar.isFavorito()
                ? R.drawable.estrella_fav_activa
                : R.drawable.estrella_fav_inactiva);
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
                SnackPixel.dejarPendiente(getString(R.string.lugar_no_existe), false);
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

    /** Comparte la ubicación del lugar como enlace de mapa (texto plano). */
    private void compartirUbicacion() {
        GeoPunto pos = lugar.getPosicion();
        String enlace;
        if (pos.getLatitud() != 0 || pos.getLongitud() != 0) {
            enlace = "https://maps.google.com/?q=" + pos.getLatitud() + "," + pos.getLongitud();
        } else {
            enlace = "https://maps.google.com/?q=" + Uri.encode(lugar.getDireccion());
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, lugar.getNombre() + "\n" + enlace);
        startActivity(intent);
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
