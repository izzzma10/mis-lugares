package com.example.mislugares.presentacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mislugares.R;

/**
 * Solo aloja ListaLugaresFragment (Anexo B del libro, "Fragments").
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        ((TextView) findViewById(R.id.barra_titulo)).setText("MIS LUGARES");
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
    }
}
