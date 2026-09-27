package com.example.mislugares.casos_uso;

import com.example.mislugares.datos.FuentePuntosReferencia;
import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.PuntoReferencia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PuntosCercanos {
    private static final double RADIO_MAXIMO_METROS = 30000;

    private final FuentePuntosReferencia repositorio;

    public PuntosCercanos(FuentePuntosReferencia repositorio) {
        this.repositorio = repositorio;
    }

    public List<PuntoReferencia> buscar(GeoPunto posicionActual, double radioInicialMetros) { 
        List<PuntoReferencia> todos = repositorio.cargarTodos();
        double radio = radioInicialMetros;

        List<PuntoReferencia> resultado = filtrarPorRadio(todos, posicionActual, radio);

        while (resultado.isEmpty() && radio < RADIO_MAXIMO_METROS) {
            radio = radio * 2;
            resultado = filtrarPorRadio(todos, posicionActual, radio);
        }

        Collections.sort(resultado, (a, b) -> Double.compare(posicionActual.distancia(a.getPosicion()), posicionActual.distancia(b.getPosicion())));

        return resultado;
    }

    private List<PuntoReferencia> filtrarPorRadio(List<PuntoReferencia> puntos, GeoPunto posicionActual, double radioMetros) {
        List<PuntoReferencia> dentroDelRadio = new ArrayList<>();
        for (PuntoReferencia punto : puntos) {
            double distancia = posicionActual.distancia(punto.getPosicion());
            if (distancia <= radioMetros) {
                dentroDelRadio.add(punto);
            }
        }
        return dentroDelRadio;
    }
} 
