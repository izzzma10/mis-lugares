package com.example.mislugares.presentacion;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

    private EditText campoNombre;
    private EditText campoDireccion;
    private EditText campoTelefono;
    private EditText campoWeb;
    private EditText campoLatitud;
    private EditText campoLongitud;
    private LinearLayout tiraValoracion;
    private ImageView pinTipo;
    private TextView textoTipo;
    private TextView errorNombre;

    private TipoLugar tipoSeleccionado = TipoLugar.OTROS;
    private float valoracionSeleccionada = 0f;

    private RepositorioLugares repositorio;
    private int id = -1;
    private Lugar lugarExistente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edicion_lugar);

        repositorio = ((Aplicacion) getApplication()).repositorio;

        campoNombre = findViewById(R.id.campo_nombre);
        campoDireccion = findViewById(R.id.campo_direccion);
        campoTelefono = findViewById(R.id.campo_telefono);
        campoWeb = findViewById(R.id.campo_web);
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
            @Override public void afterTextChanged(Editable s) { limpiarErrorNombre(); }
        });

        configurarTiraValoracion();
        Estrellas.pintarValoracion(tiraValoracion, valoracionSeleccionada, true);
        mostrarTipoEnSelector();

        id = getIntent().getIntExtra(EXTRA_ID, -1);

        // Barra: título y color según crear / editar
        boolean editando = id >= 0;
        ((TextView) findViewById(R.id.barra_titulo))
                .setText(editando ? "EDITAR LUGAR" : "NUEVO LUGAR");
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
        }

        findViewById(R.id.btn_guardar).setOnClickListener(v -> guardar());
        findViewById(R.id.btn_gps).setOnClickListener(v -> rellenarGps());
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
        textoTipo.setText(Tipos.nombre(tipoSeleccionado));
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

    /** Rellena lat/lon con la última ubicación conocida por la app. */
    private void rellenarGps() {
        GeoPunto pos = ((Aplicacion) getApplication()).posicionActual;
        if (pos == null || pos.equals(GeoPunto.SIN_POSICION)) {
            SnackPixel.error(this, "Sin ubicación disponible todavía");
            return;
        }
        campoLatitud.setText(String.valueOf(pos.getLatitud()));
        campoLongitud.setText(String.valueOf(pos.getLongitud()));
    }

    private void rellenaFormulario(Lugar lugar) {
        campoNombre.setText(lugar.getNombre());
        campoDireccion.setText(lugar.getDireccion());
        campoTelefono.setText(String.valueOf(lugar.getTelefono()));
        campoWeb.setText(lugar.getUrl());
        campoLatitud.setText(String.valueOf(lugar.getPosicion().getLatitud()));
        campoLongitud.setText(String.valueOf(lugar.getPosicion().getLongitud()));
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
            SnackPixel.error(this, "La dirección es obligatoria");
            return;
        }

        String telefono = campoTelefono.getText().toString().trim();

        if (!telefono.matches("\\d{10}")) {
            SnackPixel.error(this, "El teléfono debe tener 10 dígitos");
            return;
        }

        String textoLatitud = campoLatitud.getText().toString().trim();
        String textoLongitud = campoLongitud.getText().toString().trim();

        if (!textoLatitud.isEmpty() && !esNumeroValido(textoLatitud)) {
            SnackPixel.error(this, "La latitud no es un número válido");
            return;
        }
        if (!textoLongitud.isEmpty() && !esNumeroValido(textoLongitud)) {
            SnackPixel.error(this, "La longitud no es un número válido");
            return;
        }

        double latitud = parseDoubleSeguro(textoLatitud);
        double longitud = parseDoubleSeguro(textoLongitud);

        float valoracion = valoracionSeleccionada;
        TipoLugar tipo = tipoSeleccionado;

        String url = campoWeb.getText().toString().trim();
        String comentario = lugarExistente != null ? lugarExistente.getComentario() : "";
        String foto = lugarExistente != null ? lugarExistente.getFoto() : "";

        Lugar lugar = new Lugar(nombre, direccion, telefono, url, comentario,
                valoracion, new GeoPunto(longitud, latitud), foto);
        lugar.setTipo(tipo);

        GuardarLugar guardarLugar = new GuardarLugar(repositorio);
        if (id >= 0) {
            guardarLugar.actualizar(id, lugar);
        } else {
            guardarLugar.guardarNuevo(lugar);
        }

        SnackPixel.dejarPendiente("Lugar guardado", true);
        setResult(RESULT_OK);
        finish();
    }

    private void eliminar() {
        DialogosPixel.mostrar(this, "Eliminar lugar",
                "¿Seguro que quieres eliminar este lugar? No se puede deshacer.",
                "Eliminar", true, () -> {
                    if (id >= 0) {
                        new BorrarLugar(repositorio).borrar(id);
                    }
                    SnackPixel.dejarPendiente("Lugar eliminado", true);
                    setResult(RESULT_CANCELED);
                    finish();
                });
    }

    private void mostrarErrorNombre() {
        errorNombre.setText("El nombre no puede quedar vacío");
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
