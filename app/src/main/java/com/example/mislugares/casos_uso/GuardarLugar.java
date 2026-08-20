package com.example.mislugares.casos_uso;

import com.example.mislugares.datos.RepositorioLugares;
import com.example.mislugares.modelo.Lugar;

public class GuardarLugar {
    private final RepositorioLugares repositorio;

    public GuardarLugar(RepositorioLugares repositorio) {
        this.repositorio = repositorio;
    }

    /** Crea un lugar nuevo y devuelve el id que se le asignó. */
    public int guardarNuevo(Lugar lugar) {
        int id = repositorio.nuevo();
        repositorio.actualiza(id, lugar);
        return id;
    }

    /** Reemplaza los datos del lugar con el id indicado. */
    public void actualizar(int id, Lugar lugar) {
        repositorio.actualiza(id, lugar);
    }
}
