package com.example.mislugares.presentacion;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/** Acciones de navegación compartidas por varias pantallas. */
final class Navegacion {

    private Navegacion() { }

    /**
     * Abre la navegación hacia esas coordenadas en Google Maps, o en el navegador
     * si la app de Google Maps no está instalada. Debe recibir un contexto de
     * Activity (así startActivity no necesita banderas extra).
     */
    static void irA(Context contexto, double latitud, double longitud) {
        String destino = latitud + "," + longitud;
        Intent maps = new Intent(Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + destino));
        maps.setPackage("com.google.android.apps.maps");
        if (maps.resolveActivity(contexto.getPackageManager()) != null) {
            contexto.startActivity(maps);
        } else {
            contexto.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://www.google.com/maps/dir/?api=1&destination=" + destino)));
        }
    }
}
