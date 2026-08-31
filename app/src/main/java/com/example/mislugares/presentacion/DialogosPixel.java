package com.example.mislugares.presentacion;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.example.mislugares.R;

/**
 * Diálogo pixel propio (reemplaza el AlertDialog de Material).
 * El botón de acción va siempre a la derecha; "cancelar" nunca es rojo.
 */
final class DialogosPixel {

    private DialogosPixel() {
    }

    /**
     * @param destructivo true = franja tinta + acción roja; false = franja azul + acción azul.
     * @param onAccion    se ejecuta al confirmar (antes de cerrar el diálogo).
     */
    static void mostrar(Context ctx, String titulo, String mensaje, String textoAccion,
                        boolean destructivo, Runnable onAccion) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.dialogo_pixel, null);
        AlertDialog dialog = new AlertDialog.Builder(ctx, R.style.DialogoPixel)
                .setView(v)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        v.findViewById(R.id.franja).setBackgroundColor(ContextCompat.getColor(ctx,
                destructivo ? R.color.pixel_tinta : R.color.pixel_azul));

        TextView icono = v.findViewById(R.id.dialogo_icono);
        icono.setText(destructivo ? "!" : "?");
        icono.setBackgroundColor(destructivo
                ? ContextCompat.getColor(ctx, R.color.pixel_amarillo)
                : Color.WHITE);

        ((TextView) v.findViewById(R.id.titulo)).setText(titulo);
        ((TextView) v.findViewById(R.id.mensaje)).setText(mensaje);

        TextView btnAccion = v.findViewById(R.id.btn_accion);
        btnAccion.setText(textoAccion);
        btnAccion.setBackgroundColor(ContextCompat.getColor(ctx,
                destructivo ? R.color.pixel_rojo : R.color.pixel_azul));
        btnAccion.setOnClickListener(x -> {
            onAccion.run();
            dialog.dismiss();
        });

        v.findViewById(R.id.btn_cancelar).setOnClickListener(x -> dialog.dismiss());
        dialog.show();
    }
}
