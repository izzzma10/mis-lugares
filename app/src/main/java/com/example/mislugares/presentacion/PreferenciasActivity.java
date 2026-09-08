package com.example.mislugares.presentacion;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.os.LocaleListCompat;
import androidx.preference.PreferenceManager;

import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;

/**
 * Preferencias con controles propios (segmentado / stepper / toggles) en vez de
 * PreferenceScreen. Escribe en las SharedPreferences por defecto con las mismas
 * claves que lee LugaresBD.extraeCursor(): "orden" y "maximo", ambas como String.
 */
public class PreferenciasActivity extends AppCompatActivity {

    private static final int MAX_MIN = 4;
    private static final int MAX_MAX = 50;

    private GestorLocalizacion gestorLocalizacion;
    private SharedPreferences prefs;

    private TextView[] chipsOrden;
    private TextView maximoValor;
    private int maximo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferencias);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Barra
        ((TextView) findViewById(R.id.barra_titulo)).setText(getString(R.string.barra_preferencias));
        findViewById(R.id.barra_atras).setOnClickListener(v -> finish());
        findViewById(R.id.barra_fondo).setBackgroundColor(
                ContextCompat.getColor(this, R.color.pixel_tinta));

        configurarOrden();
        configurarMaximo();
        configurarToggles();
        configurarIdioma();
        configurarPie();

        gestorLocalizacion = new GestorLocalizacion(this);
        gestorLocalizacion.solicitarUbicacionActual();
    }

    // ---- ORDEN (segmentado): "0" sin orden · "1" valoración · "2" cercanía ----
    private void configurarOrden() {
        chipsOrden = new TextView[]{
                findViewById(R.id.orden_0),
                findViewById(R.id.orden_1),
                findViewById(R.id.orden_2)
        };
        int actual = leerEntero("orden", 0);
        marcarOrden(actual);
        for (int i = 0; i < chipsOrden.length; i++) {
            final int valor = i;
            chipsOrden[i].setOnClickListener(v -> {
                prefs.edit().putString("orden", String.valueOf(valor)).apply();
                marcarOrden(valor);
            });
        }
    }

    private void marcarOrden(int seleccion) {
        for (int i = 0; i < chipsOrden.length; i++) {
            chipsOrden[i].setSelected(i == seleccion);
        }
    }

    // ---- MAXIMO (stepper) ----
    private void configurarMaximo() {
        maximoValor = findViewById(R.id.maximo_valor);
        maximo = leerEntero("maximo", 12);
        maximo = Math.max(MAX_MIN, Math.min(MAX_MAX, maximo));
        mostrarMaximo();

        findViewById(R.id.maximo_menos).setOnClickListener(v -> cambiarMaximo(-1));
        findViewById(R.id.maximo_mas).setOnClickListener(v -> cambiarMaximo(1));
    }

    private void cambiarMaximo(int delta) {
        int nuevo = Math.max(MAX_MIN, Math.min(MAX_MAX, maximo + delta));
        if (nuevo == maximo) return;
        maximo = nuevo;
        prefs.edit().putString("maximo", String.valueOf(maximo)).apply();
        mostrarMaximo();
    }

    private void mostrarMaximo() {
        maximoValor.setText(String.valueOf(maximo));
    }

    // ---- TOGGLES (aún no los lee nadie; quedan guardados para más adelante) ----
    private void configurarToggles() {
        engancharToggle(findViewById(R.id.toggle_km), "mostrarDistanciaKm", true);
        engancharToggle(findViewById(R.id.toggle_fav), "soloFavoritosAlAbrir", false);
    }

    private void engancharToggle(View toggle, String clave, boolean porDefecto) {
        toggle.setSelected(prefs.getBoolean(clave, porDefecto));
        toggle.setOnClickListener(v -> {
            boolean nuevo = !v.isSelected();
            v.setSelected(nuevo);
            prefs.edit().putBoolean(clave, nuevo).apply();
        });
    }

    private void configurarPie() {
        int n = ((Aplicacion) getApplication()).repositorio.tamaño();
        ((TextView) findViewById(R.id.pie)).setText(getString(R.string.pref_pie, n));
    }

    // ---- IDIOMA: aplica el locale de la app (appcompat lo persiste solo) ----
    private void configurarIdioma() {
        TextView idiomaEs = findViewById(R.id.idioma_es);
        TextView idiomaEn = findViewById(R.id.idioma_en);
        marcarIdioma(idiomaEs, idiomaEn);
        idiomaEs.setOnClickListener(v -> aplicarIdioma("es"));
        idiomaEn.setOnClickListener(v -> aplicarIdioma("en"));
    }

    private void marcarIdioma(TextView es, TextView en) {
        boolean ingles = idiomaActualEsIngles();
        es.setSelected(!ingles);
        en.setSelected(ingles);
    }

    private boolean idiomaActualEsIngles() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        return !locales.isEmpty() && "en".equals(locales.get(0).getLanguage());
    }

    private void aplicarIdioma(String etiqueta) {
        // Recrea las activities con el nuevo idioma; no hace falta más.
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(etiqueta));
    }

    /** Lee una preferencia guardada como String y la devuelve como int. */
    private int leerEntero(String clave, int porDefecto) {
        try {
            return Integer.parseInt(prefs.getString(clave, String.valueOf(porDefecto)));
        } catch (NumberFormatException | ClassCastException e) {
            return porDefecto;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        gestorLocalizacion.manejarResultadoPermiso(requestCode, grantResults);
    }
}
