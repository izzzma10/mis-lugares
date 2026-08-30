package com.example.mislugares.presentacion;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;
import com.example.mislugares.casos_uso.BorrarLugar;
import com.example.mislugares.casos_uso.GuardarLugar;
import com.example.mislugares.casos_uso.VerLugar;
import com.example.mislugares.datos.RepositorioLugares;
import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.Lugar;
import com.example.mislugares.modelo.TipoLugar;

public class EdicionLugarActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id";

    private EditText campoNombre;
    private EditText campoDireccion;
    private EditText campoTelefono;
    private EditText campoWeb;
    private EditText campoLatitud;
    private EditText campoLongitud;
    private RatingBar campoValoracion;
    private LinearLayout contenedorTipos;

    private TipoLugar tipoSeleccionado = TipoLugar.OTROS;

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
        campoValoracion = findViewById(R.id.campo_valoracion);
        contenedorTipos = findViewById(R.id.contenedor_tipos);

        construirChipsTipo();

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
            seleccionarTipo(TipoLugar.OTROS);
        }

        findViewById(R.id.btn_guardar).setOnClickListener(v -> guardar());
        findViewById(R.id.btn_gps).setOnClickListener(v -> rellenarGps());
    }

    /** Crea un chip TextView por cada valor del enum TipoLugar y lo mete en el contenedor. */
    private void construirChipsTipo() {
        for (TipoLugar tipo : TipoLugar.values()) {
            TextView chip = new TextView(this);
            chip.setText(tipo.name());
            chip.setTag(tipo);
            chip.setAllCaps(true);
            chip.setGravity(Gravity.CENTER);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
            chip.setTypeface(ResourcesCompat.getFont(this, R.font.silkscreen));
            chip.setTextColor(ContextCompat.getColorStateList(this, R.color.chip_texto));
            chip.setBackgroundResource(R.drawable.chip_pixel);
            chip.setPadding(dp(8), dp(6), dp(8), dp(6));
            chip.setClickable(true);
            chip.setFocusable(true);
            chip.setOnClickListener(v -> seleccionarTipo((TipoLugar) v.getTag()));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(6));
            chip.setLayoutParams(lp);

            contenedorTipos.addView(chip);
        }
    }

    /** Marca visualmente el chip elegido y guarda el tipo. */
    private void seleccionarTipo(TipoLugar tipo) {
        tipoSeleccionado = tipo;
        for (int i = 0; i < contenedorTipos.getChildCount(); i++) {
            View chip = contenedorTipos.getChildAt(i);
            chip.setSelected(chip.getTag() == tipo);
        }
    }

    /** Rellena lat/lon con la última ubicación conocida por la app. */
    private void rellenarGps() {
        GeoPunto pos = ((Aplicacion) getApplication()).posicionActual;
        if (pos == null || pos.equals(GeoPunto.SIN_POSICION)) {
            Toast.makeText(this, "Sin ubicación disponible todavía", Toast.LENGTH_SHORT).show();
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
        campoValoracion.setRating(lugar.getValoracion());
        seleccionarTipo(lugar.getTipo());
    }

    private void guardar() {
        String nombre = campoNombre.getText().toString();
        String direccion = campoDireccion.getText().toString();

        if (nombre.isEmpty() || direccion.isEmpty()) {
            Toast.makeText(this,
                    "El nombre y la dirección son obligatorios",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String telefono = campoTelefono.getText().toString().trim();

        if (!telefono.matches("\\d{10}")) {
            Toast.makeText(this,
                    "El teléfono debe tener 10 digítos",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String textoLatitud = campoLatitud.getText().toString().trim();
        String textoLongitud = campoLongitud.getText().toString().trim();

        if (!textoLatitud.isEmpty() && !esNumeroValido(textoLatitud)) {
            Toast.makeText(this, "La latitud no es un número válido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!textoLongitud.isEmpty() && !esNumeroValido(textoLongitud)) {
            Toast.makeText(this, "La longitud no es un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitud = parseDoubleSeguro(textoLatitud);
        double longitud = parseDoubleSeguro(textoLongitud);

        float valoracion = campoValoracion.getRating();
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

        Toast.makeText(this, "Lugar guardado: " + nombre, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void eliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar lugar")
                .setMessage("¿Seguro que querés eliminar este lugar? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialogo, boton) -> {
                    if (id >= 0) {
                        new BorrarLugar(repositorio).borrar(id);
                    }
                    Toast.makeText(this, "Lugar eliminado", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
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
