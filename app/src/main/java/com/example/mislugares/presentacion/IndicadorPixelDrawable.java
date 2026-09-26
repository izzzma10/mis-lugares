package com.example.mislugares.presentacion;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.mislugares.R;

/**
 * Indicador de carga retro: un cuadrado con borde grueso y 4 bloques (2x2).
 * Un bloque se enciende por turno, en sentido horario, con un color de la paleta.
 * Se dibuja en una cuadrícula de 20x20 "píxeles" para que todo quede alineado.
 */
public class IndicadorPixelDrawable extends Drawable implements Animatable {

    private static final long PASO_MS = 200;
    private static final int CUADRICULA = 20;   // ancho total = 20 píxeles lógicos

    // Posición (columna, fila) de cada bloque en la cuadrícula 2x2, en sentido horario.
    private static final int[][] BLOQUES = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};

    private final Paint pincel = new Paint();
    private final int colorBorde, colorFondo, colorApagado;
    private final int[] coloresBloque;
    private int paso = 0;
    private boolean corriendo = false;

    private final Runnable siguientePaso = new Runnable() {
        @Override
        public void run() {
            paso = (paso + 1) % BLOQUES.length;
            invalidateSelf();
            if (corriendo) {
                scheduleSelf(this, SystemClock.uptimeMillis() + PASO_MS);
            }
        }
    };

    public IndicadorPixelDrawable(Context contexto) {
        pincel.setAntiAlias(false);   // bordes duros: nada de suavizado
        colorBorde = ContextCompat.getColor(contexto, R.color.pixel_tinta);
        colorFondo = ContextCompat.getColor(contexto, R.color.pixel_fondo);
        colorApagado = ContextCompat.getColor(contexto, R.color.pixel_divisor);
        coloresBloque = new int[]{
                ContextCompat.getColor(contexto, R.color.pixel_azul),
                ContextCompat.getColor(contexto, R.color.pixel_rojo),
                ContextCompat.getColor(contexto, R.color.pixel_verde),
                ContextCompat.getColor(contexto, R.color.pixel_amarillo)};
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect area = getBounds();
        float p = area.width() / (float) CUADRICULA;   // tamaño de un píxel lógico

        // Borde (1 px) y fondo
        rellenar(canvas, area.left, area.top, 20 * p, 20 * p, colorBorde);
        rellenar(canvas, area.left + p, area.top + p, 18 * p, 18 * p, colorFondo);

        // 4 bloques de 6x6 px, separados por 2 px, con 2 px de margen interior
        for (int i = 0; i < BLOQUES.length; i++) {
            float x = area.left + (3 + BLOQUES[i][0] * 8) * p;
            float y = area.top + (3 + BLOQUES[i][1] * 8) * p;
            rellenar(canvas, x, y, 6 * p, 6 * p, i == paso ? coloresBloque[i] : colorApagado);
        }
    }

    private void rellenar(Canvas canvas, float x, float y, float ancho, float alto, int color) {
        pincel.setColor(color);
        canvas.drawRect(x, y, x + ancho, y + alto, pincel);
    }

    @Override
    public void start() {
        if (!corriendo) {
            corriendo = true;
            scheduleSelf(siguientePaso, SystemClock.uptimeMillis() + PASO_MS);
        }
    }

    @Override
    public void stop() {
        corriendo = false;
        unscheduleSelf(siguientePaso);
        paso = 0;
        invalidateSelf();
    }

    @Override
    public boolean isRunning() {
        return corriendo;
    }

    @Override public void setAlpha(int alpha) { pincel.setAlpha(alpha); }

    @Override public void setColorFilter(@Nullable ColorFilter filtro) { pincel.setColorFilter(filtro); }

    @SuppressWarnings("deprecation")
    @Override public int getOpacity() { return PixelFormat.OPAQUE; }
}
