package com.example.mislugares.casos_uso;

import com.example.mislugares.datos.RepositorioLugares;
import com.example.mislugares.modelo.Lugar;

import java.util.Map;

public class ListarLugares {
    private final RepositorioLugares repositorio;

    public ListarLugares(RepositorioLugares repositorio) {
        this.repositorio = repositorio;
    }

    /** Devuelve todos los lugares guardados, indexados por su id. */
    public Map<Integer, Lugar> listar() {
        return repositorio.todos();
    }
}
