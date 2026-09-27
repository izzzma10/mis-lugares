package com.example.mislugares.casos_uso;

import com.example.mislugares.datos.FuentePuntosReferencia;
import com.example.mislugares.modelo.PuntoReferencia;

import java.util.List;

/**
 * Doble de prueba de FuentePuntosReferencia: entrega una lista fija que el
 * propio test armó a mano, en vez de leer assets/puntos_referencia_mty.json.
 * Así PuntosCercanosTest prueba el filtrado y la expansión de radio con datos
 * conocidos, sin depender del contenido real del archivo ni de un Context.
 */
public class FuentePuntosReferenciaFalsa implements FuentePuntosReferencia {

    private final List<PuntoReferencia> puntos;

    public FuentePuntosReferenciaFalsa(List<PuntoReferencia> puntos) {
        this.puntos = puntos;
    }

    @Override
    public List<PuntoReferencia> cargarTodos() {
        return puntos;
    }
}
