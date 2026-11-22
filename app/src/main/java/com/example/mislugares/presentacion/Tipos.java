package com.example.mislugares.presentacion;

import android.content.Context;

import com.example.mislugares.R;
import com.example.mislugares.modelo.TipoLugar;

/**
 * Fuente única del pin y del nombre de cada tipo de lugar. Lo usan la fila de lista,
 * el mapa y la hoja de selección de tipo.
 */
final class Tipos {

    private Tipos() {
    }

    /** Pin (gota con símbolo) del tipo. Tabla del handoff SB; los tipos sin pin propio se agrupan. */
    static int pin(TipoLugar tipo) {
        if (tipo == null) {
            return R.drawable.ic_pin_otros;
        }
        switch (tipo) {
            case RESTAURANTE: return R.drawable.ic_pin_restaurante;
            case BAR:
            case COPAS:       return R.drawable.ic_pin_bar;
            case ESPECTACULO: return R.drawable.ic_pin_espectaculos;
            case HOTEL:       return R.drawable.ic_pin_hotel;
            case COMPRAS:     return R.drawable.ic_pin_compras;
            case EDUCACION:   return R.drawable.ic_pin_educacion;
            case DEPORTE:
            case NATURALEZA:  return R.drawable.ic_pin_deporte;
            case GASOLINERA:  return R.drawable.ic_pin_gasolinera;
            default:          return R.drawable.ic_pin_otros;
        }
    }

    /** Recurso de texto (traducible) con el nombre del tipo. */
    static int nombreRes(TipoLugar tipo) {
        if (tipo == null) {
            return R.string.tipo_sin;
        }
        switch (tipo) {
            case RESTAURANTE: return R.string.tipo_restaurante;
            case BAR:         return R.string.tipo_bar;
            case COPAS:       return R.string.tipo_copas;
            case ESPECTACULO: return R.string.tipo_espectaculo;
            case HOTEL:       return R.string.tipo_hotel;
            case COMPRAS:     return R.string.tipo_compras;
            case EDUCACION:   return R.string.tipo_educacion;
            case DEPORTE:     return R.string.tipo_deporte;
            case NATURALEZA:  return R.string.tipo_naturaleza;
            case GASOLINERA:  return R.string.tipo_gasolinera;
            default:          return R.string.tipo_otros;
        }
    }

    static String nombre(Context contexto, TipoLugar tipo) {
        return contexto.getString(nombreRes(tipo));
    }
}
