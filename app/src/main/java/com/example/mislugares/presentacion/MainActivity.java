package com.example.mislugares.presentacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mislugares.R;

/**
 * Solo aloja ListaLugaresFragment (Anexo B del libro, "Fragments").
 */
public class MainActivity extends AppCompatActivity {

    private GestorLocalizacion gestorLocalizacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        ((TextView) findViewById(R.id.barra_titulo)).setText(getString(R.string.barra_lista));
        findViewById(R.id.barra_atras).setVisibility(View.GONE);

        // FAB "+"
        findViewById(R.id.fab_agregar).setOnClickListener(v ->
                startActivity(new Intent(this, EdicionLugarActivity.class)));

        // Barra de tabs (LISTA es la pantalla actual → activo)
        findViewById(R.id.tab_lista).setSelected(true);
        findViewById(R.id.tab_mapa).setOnClickListener(v ->
                startActivity(new Intent(this, MapaLugarActivity.class)));
        findViewById(R.id.tab_ajustes).setOnClickListener(v ->
                startActivity(new Intent(this, PreferenciasActivity.class)));

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedor_lista, new ListaLugaresFragment())
                    .commit();
        }

        // Al entrar a la app pedimos el permiso de ubicación para que la lista
        // muestre los KM desde el primer momento, sin tener que ir a Ajustes.
        gestorLocalizacion = new GestorLocalizacion(this);
        gestorLocalizacion.setAlObtenerUbicacion(this::refrescarLista);
        gestorLocalizacion.solicitarUbicacionActual();
    }

    /** Reordena/repinta la lista cuando ya hay una ubicación conocida. */
    private void refrescarLista() {
        Fragment fragmento = getSupportFragmentManager()
                .findFragmentById(R.id.contenedor_lista);
        if (fragmento instanceof ListaLugaresFragment) {
            ((ListaLugaresFragment) fragmento).refrescar();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        gestorLocalizacion.manejarResultadoPermiso(requestCode, grantResults);
    }
}
