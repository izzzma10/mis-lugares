package com.example.mislugares.presentacion;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.example.mislugares.R;

public class PreferenciasActivity extends AppCompatActivity {

    private GestorLocalizacion gestorLocalizacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferencias);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contenedor_preferencias, new FragmentoPreferencias())
                .commit();

        gestorLocalizacion = new GestorLocalizacion(this);
        gestorLocalizacion.solicitarUbicacionActual();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        gestorLocalizacion.manejarResultadoPermiso(requestCode, grantResults);
    }

    public static class FragmentoPreferencias extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferencias, rootKey);
        }
    }
}
