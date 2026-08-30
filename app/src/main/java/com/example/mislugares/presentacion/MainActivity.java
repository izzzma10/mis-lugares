package com.example.mislugares.presentacion;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mislugares.R;

/**
 * Solo aloja ListaLugaresFragment (Anexo B del libro, "Fragments") — toda la
 * lógica de la pantalla de lista vive ahora en el Fragment, no acá.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contenedor_lista, new ListaLugaresFragment())
                    .commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
