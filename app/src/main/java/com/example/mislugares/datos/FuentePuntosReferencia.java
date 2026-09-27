package com.example.mislugares.datos;

import com.example.mislugares.modelo.PuntoReferencia;

import java.util.List;

/**
 * Quién puede entregar la lista completa de puntos de referencia.
 * RepositorioPuntosReferencia (assets/ + Context) es la única implementación real;
 * los tests usan una implementación falsa con datos fijos, sin depender de Android.
 */
public interface FuentePuntosReferencia {
    List<PuntoReferencia> cargarTodos();
}
