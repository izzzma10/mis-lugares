package com.example.mislugares.presentacion;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.mislugares.R;

/**
 * Aviso pixel anclado al fondo (reemplaza Toast). Aparece y desaparece de golpe, 3s.
 * Para confirmaciones que ocurren justo antes de cerrar una pantalla, usar
 * {@link #dejarPendiente} y que la pantalla destino llame a {@link #mostrarPendiente}.
 */
final class SnackPixel {

    private static final long DURACION = 3000L;

    /** Cuánto tiempo queda visible el aviso "deshacer" antes de que el borrado
     *  se haga definitivo. Pública porque quien inicia el borrado (el Fragment)
     *  necesita el MISMO número para su propio temporizador: si usara otro
     *  valor a mano, un día alguien cambiaría uno y no el otro, y quedarían
     *  desincronizados. */
    static final long DURACION_DESHACER = 4000L;

    private static String textoPendiente;
    private static boolean exitoPendiente;

    private SnackPixel() {
    }

    static void exito(Activity actividad, String texto) {
        mostrar(actividad, texto, true);
    }

    static void error(Activity actividad, String texto) {
        mostrar(actividad, texto, false);
    }

    /** Guarda un aviso para que lo muestre la siguiente pantalla (tras un finish()). */
    static void dejarPendiente(String texto, boolean exito) {
        textoPendiente = texto;
        exitoPendiente = exito;
    }

    static void mostrarPendiente(Activity actividad) {
        if (textoPendiente == null) {
            return;
        }
        String texto = textoPendiente;
        boolean exito = exitoPendiente;
        textoPendiente = null;
        mostrar(actividad, texto, exito);
    }

    private static void mostrar(Activity actividad, String texto, boolean exito) {
        ViewGroup raiz = actividad.findViewById(android.R.id.content);
        if (raiz == null) {
            return;
        }
        View previo = raiz.findViewById(R.id.snack_pixel);
        if (previo != null) {
            raiz.removeView(previo);
        }

        View v = LayoutInflater.from(actividad).inflate(R.layout.snack_pixel, raiz, false);
        ImageView icono = v.findViewById(R.id.snack_icono);
        icono.setBackgroundColor(ContextCompat.getColor(actividad,
                exito ? R.color.pixel_verde : R.color.pixel_rojo));
        icono.setImageResource(exito ? R.drawable.ic_check_pixel : R.drawable.ic_equis_pixel);
        ((TextView) v.findViewById(R.id.snack_texto)).setText(texto);

        raiz.addView(v);
        v.postDelayed(() -> {
            if (v.getParent() != null) {
                ((ViewGroup) v.getParent()).removeView(v);
            }
        }, DURACION);
    }

    /** Aviso con un botón "DESHACER". Se usa para acciones que conviene poder
     *  arrepentirse (por ahora, borrar un lugar deslizando la fila). Si nadie
     *  toca DESHACER, el aviso se quita solo a los DURACION_DESHACER ms; quien
     *  llamó a este método es responsable de, en ese mismo momento, hacer
     *  definitiva la acción (acá no lo sabemos: solo mostramos el aviso). */
    static void deshacer(Activity actividad, String texto, Runnable alDeshacer) {
        ViewGroup raiz = actividad.findViewById(android.R.id.content);
        if (raiz == null) {
            return;
        }
        View previo = raiz.findViewById(R.id.snack_deshacer);
        if (previo != null) {
            raiz.removeView(previo);
        }

        View v = LayoutInflater.from(actividad).inflate(R.layout.snack_deshacer, raiz, false);
        ((TextView) v.findViewById(R.id.snack_deshacer_texto)).setText(texto);
        v.findViewById(R.id.snack_deshacer_boton).setOnClickListener(click -> {
            raiz.removeView(v);
            alDeshacer.run();
        });

        raiz.addView(v);
        v.postDelayed(() -> {
            if (v.getParent() != null) {
                ((ViewGroup) v.getParent()).removeView(v);
            }
        }, DURACION_DESHACER);
    }
}
