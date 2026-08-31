package com.example.mislugares.presentacion;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.example.mislugares.R;
import com.example.mislugares.modelo.TipoLugar;

/**
 * Fuente única del color de cada tipo de lugar. Lo usan la fila de lista,
 * el mapa y la hoja de selección de tipo.
 */
final class Tipos {

    private Tipos() {
    }

    /** Color (recurso) del pin según el tipo. Tabla del handoff. */
    static int colorRes(TipoLugar tipo) {
        if (tipo == null) {
            return R.color.pixel_tinta_suave;
        }
        switch (tipo) {
            case EDUCACION:
            case HOTEL:
                return R.color.pixel_azul;
            case RESTAURANTE:
            case COMPRAS:
                return R.color.pixel_rojo;
            case BAR:
            case COPAS:
                return R.color.pixel_morado;
            case ESPECTACULO:
                return R.color.pixel_amarillo;
            case DEPORTE:
            case NATURALEZA:
                return R.color.pixel_verde;
            default:                       // OTROS, GASOLINERA, sin tipo
                return R.color.pixel_tinta_suave;
        }
    }

    static int color(Context contexto, TipoLugar tipo) {
        return ContextCompat.getColor(contexto, colorRes(tipo));
    }

    /** Drawable del pin correspondiente al color del tipo. */
    static int pin(TipoLugar tipo) {
        int cr = colorRes(tipo);
        if (cr == R.color.pixel_azul) return R.drawable.ic_pin_azul;
        if (cr == R.color.pixel_rojo) return R.drawable.ic_pin_rojo;
        if (cr == R.color.pixel_morado) return R.drawable.ic_pin_morado;
        if (cr == R.color.pixel_amarillo) return R.drawable.ic_pin_amarillo;
        if (cr == R.color.pixel_verde) return R.drawable.ic_pin_verde;
        return R.drawable.ic_pin_otros;
    }

    static String nombre(TipoLugar tipo) {
        return tipo == null ? "SIN TIPO" : tipo.name();
    }
}
