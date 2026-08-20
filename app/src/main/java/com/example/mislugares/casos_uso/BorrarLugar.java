package com.example.mislugares.casos_uso;

import com.example.mislugares.datos.RepositorioLugares;

public class BorrarLugar {
    private final RepositorioLugares repositorio;

    public BorrarLugar(RepositorioLugares repositorio) {
        this.repositorio = repositorio;
    }

    /** Elimina el lugar con el id indicado. */
    public void borrar(int id) {
        repositorio.borrar(id);
    }
}
