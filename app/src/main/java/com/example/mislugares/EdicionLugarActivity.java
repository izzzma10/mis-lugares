package com.example.mislugares;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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
    private EditText campoLatitud;
    private EditText campoLongitud;
    private RatingBar campoValoracion;
    private Spinner campoTipo;
    private Button btnGuardar;
    private Button btnEliminar;

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
        campoLatitud = findViewById(R.id.campo_latitud);
        campoLongitud = findViewById(R.id.campo_longitud);
        campoValoracion = findViewById(R.id.campo_valoracion);
        campoTipo = findViewById(R.id.campo_tipo);
        btnGuardar = findViewById(R.id.btn_guardar);
        btnEliminar = findViewById(R.id.btn_eliminar);

        ArrayAdapter<TipoLugar> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                TipoLugar.values()
        );
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        campoTipo.setAdapter(adapter);

        id = getIntent().getIntExtra(EXTRA_ID, -1);
        if (id >= 0) {
            lugarExistente = new VerLugar(repositorio).ver(id);
            rellenaFormulario(lugarExistente);
        } else {
            btnEliminar.setEnabled(false);
        }

        btnGuardar.setOnClickListener(v -> guardar());
        btnEliminar.setOnClickListener(v -> eliminar());
    }

    private void rellenaFormulario(Lugar lugar) {
        campoNombre.setText(lugar.getNombre());
        campoDireccion.setText(lugar.getDireccion());
        campoTelefono.setText(String.valueOf(lugar.getTelefono()));
        campoLatitud.setText(String.valueOf(lugar.getPosicion().getLatitud()));
        campoLongitud.setText(String.valueOf(lugar.getPosicion().getLongitud()));
        campoValoracion.setRating(lugar.getValoracion());
        campoTipo.setSelection(lugar.getTipo().ordinal());
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

        int telefono = parseIntSeguro(campoTelefono.getText().toString());
        double latitud = parseDoubleSeguro(campoLatitud.getText().toString());
        double longitud = parseDoubleSeguro(campoLongitud.getText().toString());
        float valoracion = campoValoracion.getRating();
        TipoLugar tipo = (TipoLugar) campoTipo.getSelectedItem();

        String url = lugarExistente != null ? lugarExistente.getUrl() : "";
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
        if (id >= 0) {
            new BorrarLugar(repositorio).borrar(id);
        }
        Toast.makeText(this, "Lugar eliminado", Toast.LENGTH_SHORT).show();
        setResult(RESULT_CANCELED);
        finish();
    }

    private int parseIntSeguro(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDoubleSeguro(String texto) {
        try {
            return Double.parseDouble(texto.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
