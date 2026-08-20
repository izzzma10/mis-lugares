package com.example.mislugares.casos_uso;

import com.example.mislugares.datos.RepositorioLugares;
import com.example.mislugares.modelo.Lugar;

public class VerLugar {
    private final RepositorioLugares repositorio;

    public VerLugar(RepositorioLugares repositorio) {
        this.repositorio = repositorio;
    }

    /** Devuelve el lugar con el id indicado. */
    public Lugar ver(int id) {
        return repositorio.elemento(id);
    }
}
