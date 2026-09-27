package com.example.mislugares.presentacion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;
import com.example.mislugares.casos_uso.BorrarLugar;
import com.example.mislugares.casos_uso.GuardarLugar;
import com.example.mislugares.casos_uso.VerLugar;
import com.example.mislugares.datos.RepositorioLugares;
import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.Lugar;
import com.example.mislugares.modelo.TipoLugar;

public class EdicionLugarActivity extends AppCompatActivity
        implements HojaTipoFragment.Listener {

    public static final String EXTRA_ID = "id";

    // Datos para abrir el formulario de "nuevo lugar" ya llenado (ver intentPrecargado).
    private static final String EXTRA_PRE_NOMBRE = "pre_nombre";
    private static final String EXTRA_PRE_DIRECCION = "pre_direccion";
    private static final String EXTRA_PRE_LATITUD = "pre_latitud";
    private static final String EXTRA_PRE_LONGITUD = "pre_longitud";
    private static final String EXTRA_PRE_TIPO = "pre_tipo";

    /** Intent para crear un lugar nuevo con el formulario precargado desde un resultado
     *  de búsqueda. Las claves de los extras quedan encapsuladas en esta clase: quien
     *  llama no necesita conocerlas. */
    static Intent intentPrecargado(Context contexto, BuscadorLugares.Resultado r) {
        Intent intent = new Intent(contexto, EdicionLugarActivity.class);
        intent.putExtra(EXTRA_PRE_NOMBRE, r.nombre);
        intent.putExtra(EXTRA_PRE_DIRECCION, r.direccion);
        intent.putExtra(EXTRA_PRE_LATITUD, r.latitud);
        intent.putExtra(EXTRA_PRE_LONGITUD, r.longitud);
        if (r.tipo != null) {
            intent.putExtra(EXTRA_PRE_TIPO, r.tipo.name());
        }
        return intent;
    }

    private EditText campoNombre;
    private EditText campoDireccion;
    private EditText campoTelefono;
    private EditText campoWeb;
    private EditText campoNota;
    private EditText campoLatitud;
    private EditText campoLongitud;
    private LinearLayout tiraValoracion;
    private ImageView pinTipo;
    private TextView textoTipo;
    private TextView errorNombre;

    private View fotoVacio;
    private View fotoMarco;
    private View fotoAcciones;
    private ImageView fotoPreview;

    private TipoLugar tipoSeleccionado = TipoLugar.OTROS;
    private float valoracionSeleccionada = 0f;
    private String fotoSeleccionada = "";
    private Uri uriFotoCamara;

    // Panel de sugerencias del campo NOMBRE
    private final Handler manejador = new Handler(Looper.getMainLooper());
    private View panelSugerencias;
    private RecyclerSugerencias listaSugerencias;
    private AdaptadorSugerencias adaptadorSug;
    private TextView contadorSugerencias;
    private TextView textoUsarTalCual;
    private Runnable busquedaPendiente;
    private long tokenBusqueda = 0;
    private boolean rellenandoDesdeSugerencia = false;

    private RepositorioLugares repositorio;
    private int id = -1;
    private Lugar lugarExistente;

    /** Elegir imagen de la galería (SAF, sin permiso de lectura). */
    private final ActivityResultLauncher<String> selectorGaleria =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    return;
                }
                try {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignorado) {
                    // Algunos proveedores no permiten persistir el permiso.
                }
                asignarFoto(uri.toString());
            });

    /** Capturar foto con la app de cámara del sistema (sin permiso de CÁMARA). */
    private final ActivityResultLauncher<Uri> tomarFoto =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), exito -> {
                if (exito && uriFotoCamara != null) {
                    asignarFoto(uriFotoCamara.toString());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edicion_lugar);

        repositorio = ((Aplicacion) getApplication()).repositorio;

        campoNombre = findViewById(R.id.campo_nombre);
        campoDireccion = findViewById(R.id.campo_direccion);
        campoTelefono = findViewById(R.id.campo_telefono);
        campoWeb = findViewById(R.id.campo_web);
        campoNota = findViewById(R.id.campo_nota);
        campoLatitud = findViewById(R.id.campo_latitud);
        campoLongitud = findViewById(R.id.campo_longitud);
        tiraValoracion = findViewById(R.id.tira_valoracion);
        pinTipo = findViewById(R.id.pin_tipo);
        textoTipo = findViewById(R.id.texto_tipo);
        findViewById(R.id.selector_tipo).setOnClickListener(v -> abrirHojaTipo());

        errorNombre = findViewById(R.id.error_nombre);
        errorNombre.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_triangulo, 0, 0, 0);
        campoNombre.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                limpiarErrorNombre();
                programarBusqueda(s.toString());
            }
        });
        configurarPanelSugerencias();

        configurarTiraValoracion();
        Estrellas.pintarValoracion(tiraValoracion, valoracionSeleccionada, true);
        mostrarTipoEnSelector();

        fotoVacio = findViewById(R.id.foto_vacio);
        fotoMarco = findViewById(R.id.foto_marco);
        fotoAcciones = findViewById(R.id.foto_acciones);
        fotoPreview = findViewById(R.id.foto_preview);
        findViewById(R.id.foto_camara).setOnClickListener(v -> abrirCamara());
        findViewById(R.id.foto_recamara).setOnClickListener(v -> abrirCamara());
        findViewById(R.id.foto_galeria).setOnClickListener(v -> selectorGaleria.launch("image/*"));
        findViewById(R.id.foto_regaleria).setOnClickListener(v -> selectorGaleria.launch("image/*"));
        findViewById(R.id.foto_borrar).setOnClickListener(v -> asignarFoto(""));
        mostrarFoto();

        id = getIntent().getIntExtra(EXTRA_ID, -1);

        // Barra: título y color según crear / editar
        boolean editando = id >= 0;
        ((TextView) findViewById(R.id.barra_titulo))
                .setText(getString(editando ? R.string.barra_editar_lugar : R.string.barra_nuevo_lugar));
        findViewById(R.id.barra_atras).setOnClickListener(v -> finish());
        findViewById(R.id.barra_fondo).setBackgroundColor(ContextCompat.getColor(
                this, editando ? R.color.pixel_azul : R.color.pixel_verde));

        View btnEliminar = findViewById(R.id.btn_eliminar);
        if (editando) {
            lugarExistente = new VerLugar(repositorio).ver(id);
            rellenaFormulario(lugarExistente);
            btnEliminar.setOnClickListener(v -> eliminar());
        } else {
            btnEliminar.setVisibility(View.GONE);
            precargarDesdeIntent();
        }

        findViewById(R.id.btn_guardar).setOnClickListener(v -> guardar());
        findViewById(R.id.btn_gps).setOnClickListener(v -> rellenarGps());
    }

    /** Si nos abrieron desde un resultado de búsqueda del mapa, llena el formulario
     *  reusando aplicarSugerencia (la misma vía que el panel de sugerencias del NOMBRE). */
    private void precargarDesdeIntent() {
        Intent intent = getIntent();
        if (!intent.hasExtra(EXTRA_PRE_NOMBRE)) {
            return;
        }
        TipoLugar tipo = null;
        String nombreTipo = intent.getStringExtra(EXTRA_PRE_TIPO);
        if (nombreTipo != null) {
            try {
                tipo = TipoLugar.valueOf(nombreTipo);
            } catch (IllegalArgumentException e) {
                // Tipo desconocido: se deja el valor por defecto.
            }
        }
        aplicarSugerencia(new BuscadorLugares.Resultado(
                intent.getStringExtra(EXTRA_PRE_NOMBRE),
                intent.getStringExtra(EXTRA_PRE_DIRECCION),
                intent.getDoubleExtra(EXTRA_PRE_LATITUD, 0),
                intent.getDoubleExtra(EXTRA_PRE_LONGITUD, 0),
                tipo));
    }

    @Override
    protected void onDestroy() {
        manejador.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /** Abre la hoja inferior para elegir el tipo, con el actual visible. */
    private void abrirHojaTipo() {
        HojaTipoFragment.nueva(tipoSeleccionado)
                .show(getSupportFragmentManager(), "hoja_tipo");
    }

    @Override
    public void onTipoElegido(TipoLugar tipo) {
        tipoSeleccionado = tipo;
        mostrarTipoEnSelector();
    }

    /** Refresca el pin y el texto del selector de tipo. */
    private void mostrarTipoEnSelector() {
        pinTipo.setImageResource(Tipos.pin(tipoSeleccionado));
        textoTipo.setText(Tipos.nombre(this, tipoSeleccionado));
    }

    /** Cablea el panel de sugerencias que aparece bajo el input NOMBRE. */
    private void configurarPanelSugerencias() {
        panelSugerencias = findViewById(R.id.panel_sugerencias);
        contadorSugerencias = findViewById(R.id.contador_sugerencias);
        textoUsarTalCual = findViewById(R.id.texto_usar_tal_cual);

        listaSugerencias = findViewById(R.id.lista_sugerencias);
        listaSugerencias.setLayoutManager(new LinearLayoutManager(this));
        listaSugerencias.addItemDecoration(new AdaptadorSugerencias.Divisor(this));
        adaptadorSug = new AdaptadorSugerencias(this::aplicarSugerencia);
        listaSugerencias.setAdapter(adaptadorSug);

        findViewById(R.id.fila_usar_tal_cual).setOnClickListener(v -> ocultarPanel());
        campoNombre.setOnFocusChangeListener((v, tieneFoco) -> {
            if (!tieneFoco) {
                ocultarPanel();
            }
        });
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
            ocultarPanel();
            return;
        }
        // Sesgo hacia la posición actual (o Monterrey si aún no hay GPS) para
        // priorizar lugares cercanos; el filtro por país va dentro del buscador.
        double sesgoLat = 25.6866, sesgoLon = -100.3161;
        GeoPunto pos = ((Aplicacion) getApplication()).posicionActual;
        if (pos != null && !pos.equals(GeoPunto.SIN_POSICION)) {
            sesgoLat = pos.getLatitud();
            sesgoLon = pos.getLongitud();
        }
        final double biasLat = sesgoLat, biasLon = sesgoLon;

        final long token = ++tokenBusqueda;
        busquedaPendiente = () -> BuscadorLugares.buscar(consulta, biasLat, biasLon, resultados -> {
            if (token != tokenBusqueda || isFinishing()) {
                return;   // llegó tarde: ya hay otra búsqueda más nueva
            }
            mostrarSugerencias(resultados);
        });
        manejador.postDelayed(busquedaPendiente, 350);
    }

    private void mostrarSugerencias(List<BuscadorLugares.Resultado> resultados) {
        if (rellenandoDesdeSugerencia || !campoNombre.hasFocus()) {
            return;
        }
        if (resultados.isEmpty()) {
            ocultarPanel();
            return;
        }
        GeoPunto pos = ((Aplicacion) getApplication()).posicionActual;
        adaptadorSug.actualizar(resultados, pos);
        contadorSugerencias.setText(String.valueOf(resultados.size()));
        textoUsarTalCual.setText(
                getString(R.string.usar_tal_cual, campoNombre.getText().toString().trim()));
        panelSugerencias.setVisibility(View.VISIBLE);
        panelSugerencias.announceForAccessibility(
                getString(R.string.sugerencias_disponibles, resultados.size()));
    }

    private void ocultarPanel() {
        if (panelSugerencias != null) {
            panelSugerencias.setVisibility(View.GONE);
        }
    }

    /** Vuelca la sugerencia en cascada; el flag evita que el panel se reabra. */
    private void aplicarSugerencia(BuscadorLugares.Resultado r) {
        rellenandoDesdeSugerencia = true;
        if (busquedaPendiente != null) {
            manejador.removeCallbacks(busquedaPendiente);
            busquedaPendiente = null;
        }
        tokenBusqueda++;

        campoNombre.setText(r.nombre);
        campoNombre.setSelection(campoNombre.getText().length());
        if (!r.direccion.isEmpty()) {
            campoDireccion.setText(r.direccion);
        }
        campoLatitud.setText(String.valueOf(r.latitud));
        campoLongitud.setText(String.valueOf(r.longitud));
        if (r.tipo != null) {
            tipoSeleccionado = r.tipo;
            mostrarTipoEnSelector();
        }

        ocultarPanel();
        rellenandoDesdeSugerencia = false;

        // Enfoca el siguiente campo vacío.
        if (campoTelefono.getText().toString().trim().isEmpty()) {
            campoTelefono.requestFocus();
        } else if (campoWeb.getText().toString().trim().isEmpty()) {
            campoWeb.requestFocus();
        }
    }

    /** Calificar tocando o deslizando sobre la tira: pasos de media estrella.
     *  Mismo comportamiento que el detalle; acá solo se guarda al pulsar GUARDAR. */
    @SuppressLint("ClickableViewAccessibility")
    private void configurarTiraValoracion() {
        tiraValoracion.setOnTouchListener((v, evento) -> {
            float ancho = v.getWidth();
            if (ancho <= 0f) {
                return false;
            }
            float x = Math.max(0f, Math.min(evento.getX(), ancho));
            float valor = Math.round(x / (ancho / 5f) * 2f) / 2f;   // pasos de 0.5
            valor = Math.max(0.5f, Math.min(5f, valor));

            int accion = evento.getActionMasked();
            if (accion == MotionEvent.ACTION_DOWN) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
            }
            if (accion == MotionEvent.ACTION_DOWN
                    || accion == MotionEvent.ACTION_MOVE
                    || accion == MotionEvent.ACTION_UP) {
                valoracionSeleccionada = valor;
                Estrellas.pintarValoracion(tiraValoracion, valor, true);
                return true;
            }
            return false;
        });
    }

    /** Crea un fichero vacío en el almacenamiento privado y lanza la cámara sobre él. */
    private void abrirCamara() {
        try {
            File directorio = getExternalFilesDir("Pictures");
            File archivo = File.createTempFile(
                    "foto_" + System.currentTimeMillis() / 1000, ".jpg", directorio);
            uriFotoCamara = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", archivo);
            tomarFoto.launch(uriFotoCamara);
        } catch (IOException e) {
            SnackPixel.error(this, getString(R.string.err_foto));
        }
    }

    /** Guarda la URI elegida (o "" para quitarla) y refresca el bloque de foto. */
    private void asignarFoto(String uriFoto) {
        fotoSeleccionada = uriFoto == null ? "" : uriFoto;
        mostrarFoto();
    }

    /** Alterna entre la caja "añadir foto" y la vista previa con sus botones. */
    private void mostrarFoto() {
        boolean hay = fotoSeleccionada != null && !fotoSeleccionada.isEmpty();
        fotoVacio.setVisibility(hay ? View.GONE : View.VISIBLE);
        fotoMarco.setVisibility(hay ? View.VISIBLE : View.GONE);
        fotoAcciones.setVisibility(hay ? View.VISIBLE : View.GONE);
        if (!hay) {
            return;
        }
        if (fotoSeleccionada.contains("://")) {
            fotoPreview.setImageURI(Uri.parse(fotoSeleccionada));
        } else {
            int recurso = getResources().getIdentifier(
                    fotoSeleccionada, "drawable", getPackageName());
            fotoPreview.setImageResource(
                    recurso != 0 ? recurso : R.drawable.foto_placeholder);
        }
    }

    /** Rellena lat/lon con la última ubicación conocida por la app. */
    private void rellenarGps() {
        GeoPunto pos = ((Aplicacion) getApplication()).posicionActual;
        if (pos == null || pos.equals(GeoPunto.SIN_POSICION)) {
            SnackPixel.error(this, getString(R.string.err_sin_ubicacion));
            return;
        }
        campoLatitud.setText(String.valueOf(pos.getLatitud()));
        campoLongitud.setText(String.valueOf(pos.getLongitud()));
    }

    private void rellenaFormulario(Lugar lugar) {
        rellenandoDesdeSugerencia = true;   // no dispares autocompletado al precargar
        campoNombre.setText(lugar.getNombre());
        rellenandoDesdeSugerencia = false;
        campoDireccion.setText(lugar.getDireccion());
        campoTelefono.setText(String.valueOf(lugar.getTelefono()));
        campoWeb.setText(lugar.getUrl());
        campoNota.setText(lugar.getComentario());
        campoLatitud.setText(String.valueOf(lugar.getPosicion().getLatitud()));
        campoLongitud.setText(String.valueOf(lugar.getPosicion().getLongitud()));
        fotoSeleccionada = lugar.getFoto() != null ? lugar.getFoto() : "";
        mostrarFoto();
        valoracionSeleccionada = lugar.getValoracion();
        Estrellas.pintarValoracion(tiraValoracion, valoracionSeleccionada, true);
        tipoSeleccionado = lugar.getTipo();
        mostrarTipoEnSelector();
    }

    private void guardar() {
        String nombre = campoNombre.getText().toString();
        String direccion = campoDireccion.getText().toString();

        if (nombre.isEmpty()) {
            mostrarErrorNombre();
            return;
        }
        if (direccion.isEmpty()) {
            SnackPixel.error(this, getString(R.string.err_direccion));
            return;
        }

        String telefono = campoTelefono.getText().toString().trim();

        if (!telefono.matches("\\d{10}")) {
            SnackPixel.error(this, getString(R.string.err_telefono));
            return;
        }

        String textoLatitud = campoLatitud.getText().toString().trim();
        String textoLongitud = campoLongitud.getText().toString().trim();

        if (!textoLatitud.isEmpty() && !esNumeroValido(textoLatitud)) {
            SnackPixel.error(this, getString(R.string.err_latitud));
            return;
        }
        if (!textoLongitud.isEmpty() && !esNumeroValido(textoLongitud)) {
            SnackPixel.error(this, getString(R.string.err_longitud));
            return;
        }

        double latitud = parseDoubleSeguro(textoLatitud);
        double longitud = parseDoubleSeguro(textoLongitud);

        float valoracion = valoracionSeleccionada;
        TipoLugar tipo = tipoSeleccionado;

        String url = campoWeb.getText().toString().trim();
        String comentario = campoNota.getText().toString().trim();
        String foto = fotoSeleccionada;

        Lugar lugar = new Lugar(nombre, direccion, telefono, url, comentario,
                valoracion, new GeoPunto(longitud, latitud), foto);
        lugar.setTipo(tipo);

        GuardarLugar guardarLugar = new GuardarLugar(repositorio);
        if (id >= 0) {
            guardarLugar.actualizar(id, lugar);
        } else {
            guardarLugar.guardarNuevo(lugar);
        }

        SnackPixel.dejarPendiente(getString(R.string.lugar_guardado), true);
        setResult(RESULT_OK);
        finish();
    }

    private void eliminar() {
        DialogosPixel.mostrar(this, getString(R.string.dlg_eliminar_lugar_titulo),
                getString(R.string.dlg_eliminar_lugar_msg),
                getString(R.string.dlg_eliminar), true, () -> {
                    if (id >= 0) {
                        new BorrarLugar(repositorio).borrar(id);
                    }
                    SnackPixel.dejarPendiente(getString(R.string.lugar_eliminado), true);
                    setResult(RESULT_CANCELED);
                    finish();
                });
    }

    private void mostrarErrorNombre() {
        errorNombre.setText(getString(R.string.err_nombre_vacio));
        errorNombre.setVisibility(View.VISIBLE);
        fondoCampoNombre(R.drawable.input_pixel_error);
    }

    private void limpiarErrorNombre() {
        if (errorNombre.getVisibility() == View.VISIBLE) {
            errorNombre.setVisibility(View.GONE);
            fondoCampoNombre(R.drawable.input_pixel);
        }
    }

    /** Cambia el fondo del campo sin perder el padding fijado en el XML. */
    private void fondoCampoNombre(int recurso) {
        int l = campoNombre.getPaddingLeft(), t = campoNombre.getPaddingTop(),
                r = campoNombre.getPaddingRight(), b = campoNombre.getPaddingBottom();
        campoNombre.setBackgroundResource(recurso);
        campoNombre.setPadding(l, t, r, b);
    }

    private double parseDoubleSeguro(String texto) {
        try {
            return Double.parseDouble(texto.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private boolean esNumeroValido(String texto) {
        try {
            Double.parseDouble(texto);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
