package com.example.mislugares.presentacion;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mislugares.R;

/**
 * Conecta el gesto de "deslizar para borrar" (estilo Spotify) a la lista de
 * lugares guardados: se adjunta a un RecyclerView con ItemTouchHelper.attachToRecyclerView(),
 * y a partir de ahí Android le avisa a esta clase de cada arrastre y de cada
 * suelte. Solo dibuja y reacciona sobre filas de AdaptadorLugares: en el modo
 * CERCA, donde el RecyclerView usa AdaptadorPuntosReferencia, deslizar no hace nada.
 */
public class SwipeParaBorrar extends ItemTouchHelper.SimpleCallback {

    /** A quién avisar cuando el usuario terminó de deslizar una fila. */
    public interface AlDeslizar {
        void onDeslizado(int posicionAdapter);
    }

    private final Drawable iconoPapelera;
    private final int colorFondo;
    private final AlDeslizar alDeslizar;

    public SwipeParaBorrar(RecyclerView recyclerView, AlDeslizar alDeslizar) {
        // 0 = no se puede arrastrar hacia arriba/abajo para reordenar.
        // ItemTouchHelper.LEFT = solo se puede deslizar hacia la izquierda.
        super(0, ItemTouchHelper.LEFT);
        this.alDeslizar = alDeslizar;
        this.iconoPapelera = ContextCompat.getDrawable(recyclerView.getContext(),
                R.drawable.ic_papelera_pixel);
        this.colorFondo = ContextCompat.getColor(recyclerView.getContext(), R.color.pixel_rojo);
    }

    /** Se pregunta por CADA fila, antes de cada gesto: qué movimientos acepta. */
    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                 @NonNull RecyclerView.ViewHolder viewHolder) {
        if (!(viewHolder instanceof AdaptadorLugares.ViewHolder)) {
            return 0;   // fila de puntos de referencia: no se desliza
        }
        return super.getMovementFlags(recyclerView, viewHolder);
    }

    /** Arrastrar más o menos de un tercio del ancho ya cuenta como "confirmado":
     *  al soltar, Android anima la fila hasta sacarla de la pantalla y llama a
     *  onSwiped(). Con el 0.5 por defecto había que arrastrar la mitad. */
    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.33f;
    }

    /** No usamos el arrastre para reordenar filas (solo para borrar), así que
     *  este método —que ItemTouchHelper exige igual— no tiene nada que hacer. */
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                           @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    /** Soltó habiendo pasado el umbral: quien nos escucha decide qué hacer. */
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        alDeslizar.onDeslizado(viewHolder.getBindingAdapterPosition());
    }

    /** Se llama en cada frame mientras la fila se mueve con el dedo: dX es cuánto
     *  se desplazó (negativo = hacia la izquierda). Dibujamos el fondo rojo y el
     *  ícono ANTES de que Android dibuje la fila encima ya desplazada. */
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                             @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                             int actionState, boolean isCurrentlyActive) {
        View fila = viewHolder.itemView;

        // El fondo rojo ocupa justo el hueco que la fila fue dejando al deslizarse
        // (por eso el rectángulo va de "borde derecho + dX" hasta "borde derecho").
        c.save();
        c.clipRect(fila.getRight() + dX, fila.getTop(), fila.getRight(), fila.getBottom());
        c.drawColor(colorFondo);

        if (iconoPapelera != null) {
            int alto = iconoPapelera.getIntrinsicHeight();
            int ancho = iconoPapelera.getIntrinsicWidth();
            int margen = (fila.getHeight() - alto) / 2;
            int arriba = fila.getTop() + margen;
            int derecha = fila.getRight() - margen;
            int izquierda = derecha - ancho;
            // Recién dibuja el ícono cuando ya se deslizó lo suficiente para
            // que entre completo; si no, se vería "cortado" al empezar a arrastrar.
            if (izquierda >= fila.getRight() + dX) {
                iconoPapelera.setBounds(izquierda, arriba, derecha, arriba + alto);
                iconoPapelera.draw(c);
            }
        }
        c.restore();

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
