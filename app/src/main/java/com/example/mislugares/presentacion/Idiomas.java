package com.example.mislugares.presentacion;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.mislugares.R;

/**
 * Fuente única del idioma de la app: qué códigos hay, cuál está activo y cómo
 * aplicar uno nuevo. AppCompat se encarga de persistirlo (ver
 * AppLocalesMetadataHolderService en el manifest).
 */
final class Idiomas {

    static final String[] CODIGOS = {"es", "en"};

    private Idiomas() {
    }

    /** Código de idioma activo; "es" si todavía no se eligió ninguno. */
    static String actual() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (locales.isEmpty()) {
            return "es";
        }
        return "en".equals(locales.get(0).getLanguage()) ? "en" : "es";
    }

    /** Recrea las activities con el nuevo idioma. */
    static void aplicar(String codigo) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(codigo));
    }

    static int nombreRes(String codigo) {
        return "en".equals(codigo) ? R.string.pref_idioma_en : R.string.pref_idioma_es;
    }
}
