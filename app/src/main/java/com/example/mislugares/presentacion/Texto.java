package com.example.mislugares.presentacion;

import java.text.Normalizer;
import java.util.Locale;

/** Utilidades de texto compartidas por los buscadores de las listas. */
final class Texto {

    private Texto() { }

    /** Minúsculas y sin acentos, para que "cafe" encuentre "Café". */
    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String descompuesto = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD);
        return descompuesto.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
