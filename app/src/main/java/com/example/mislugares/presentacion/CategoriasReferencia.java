package com.example.mislugares.presentacion;

import com.example.mislugares.R;

import java.util.Locale;

/**
 * Clasifica el texto libre de {@code PuntoReferencia.categoria} (ej. "Museo / Cultura",
 * "Parque recreativo") en 5 grupos, para colorear el pin y filtrar en el mapa.
 * No es un TipoLugar: los puntos de referencia son museos/parques/plazas/estaciones,
 * no negocios, así que las categorías son otras.
 */
final class CategoriasReferencia {

    enum Categoria { DEPORTE_PARQUES, COMERCIAL, ESPECTACULOS, CULTURA, OTROS }

    private CategoriasReferencia() {
    }

    static Categoria de(String categoriaTexto) {
        String c = categoriaTexto.toLowerCase(Locale.ROOT);

        // "museo" manda siempre a CULTURA, aunque la categoría también mencione
        // un parque o deporte (ej. "Museo deportivo", "Museo / Parque Niños Héroes").
        if (c.contains("museo")) {
            return Categoria.CULTURA;
        }
        if (c.contains("deport") || c.contains("estadio") || c.contains("parque")
                || c.contains("campo") || c.contains("recreativ") || c.contains("ecológic")
                || c.contains("ecologic") || c.contains("natural") || c.contains("zoológic")
                || c.contains("zoologic") || c.contains("reserva") || c.contains("plaza")) {
            return Categoria.DEPORTE_PARQUES;
        }
        if (c.contains("comercial") || c.contains("mercado")) {
            return Categoria.COMERCIAL;
        }
        if (c.contains("escenario") || c.contains("teatro") || c.contains("auditorio")
                || c.contains("concierto") || c.contains("feria") || c.contains("convenci")
                || c.contains("espect")) {
            return Categoria.ESPECTACULOS;
        }
        if (c.contains("arte") || c.contains("cultura") || c.contains("patrimonio")
                || c.contains("religios") || c.contains("histor") || c.contains("biblioteca")
                || c.contains("escultura") || c.contains("campus") || c.contains("universi")
                || c.contains("estudio") || c.contains("escuela") || c.contains("cívic")
                || c.contains("civic")) {
            return Categoria.CULTURA;
        }
        return Categoria.OTROS;
    }

    static int pin(Categoria categoria) {
        switch (categoria) {
            case DEPORTE_PARQUES: return R.drawable.ic_pin_verde;
            case COMERCIAL:       return R.drawable.ic_pin_rojo;
            case ESPECTACULOS:    return R.drawable.ic_pin_amarillo;
            case CULTURA:         return R.drawable.ic_pin_azul;
            default:               return R.drawable.ic_pin_otros;
        }
    }
}
