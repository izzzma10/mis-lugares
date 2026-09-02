package com.example.mislugares.presentacion;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView que nunca crece más de 310dp (5 filas de 62dp). Con eso el panel
 * de sugerencias no tapa el botón GUARDAR; a partir de la 6ª fila hace scroll.
 * {@code android:maxHeight} no funciona en RecyclerView, por eso la subclase.
 */
public class RecyclerSugerencias extends RecyclerView {

    private static final int MAX_DP = 310;

    public RecyclerSugerencias(Context context) {
        super(context);
    }

    public RecyclerSugerencias(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerSugerencias(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int maxPx = Math.round(MAX_DP * getResources().getDisplayMetrics().density);
        int acotado = MeasureSpec.makeMeasureSpec(maxPx, MeasureSpec.AT_MOST);
        super.onMeasure(widthSpec, acotado);
    }
}
