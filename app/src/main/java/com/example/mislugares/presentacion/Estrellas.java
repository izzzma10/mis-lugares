package com.example.mislugares.presentacion;

import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.mislugares.R;

/**
 * Pinta una tira de 5 ImageView con las estrellas pixel (assets del handoff).
 * La estrella vacía es la misma silueta sólida al 20%, no un contorno hueco.
 */
final class Estrellas {

    private Estrellas() {
    }

    /**
     * @param tira        LinearLayout con exactamente 5 ImageView hijos
     * @param valor       valoración 0..5 (admite .5)
     * @param conContorno true en el formulario (assets 17x16 con anillo), false en lista/detalle
     */
    static void pintarValoracion(LinearLayout tira, float valor, boolean conContorno) {
        int llena = conContorno ? R.drawable.estrella_fav_activa : R.drawable.estrella_llena;
        int vacia = conContorno ? R.drawable.estrella_fav_inactiva : R.drawable.estrella_vacia;
        int media = conContorno ? R.drawable.estrella_media_contorno : R.drawable.estrella_media;
        for (int i = 0; i < 5; i++) {
            ImageView estrella = (ImageView) tira.getChildAt(i);
            int recurso;
            if (valor >= i + 1f) {
                recurso = llena;
            } else if (valor >= i + 0.5f) {
                recurso = media;
            } else {
                recurso = vacia;
            }
            estrella.setImageResource(recurso);
        }
        tira.setContentDescription("Valoración: " + valor + " de 5");
    }
}
