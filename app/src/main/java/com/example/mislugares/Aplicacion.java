package com.example.mislugares;

import android.app.Application;

import com.example.mislugares.datos.LugaresLista;
import com.example.mislugares.datos.RepositorioLugares;
import com.example.mislugares.modelo.GeoPunto;

public class Aplicacion extends Application {
    public GeoPunto posicionActual = new GeoPunto(0.0, 0.0);

    // Repositorio compartido por todas las pantallas (misma instancia para
    // que lo que se guarde/borre en una pantalla se vea en las demás).
    // Es LugaresLista (en memoria, con datos de ejemplo) mientras la capa
    // datos no esté conectada a LugaresBD (SQLite) de forma definitiva.
    public RepositorioLugares repositorio = new LugaresLista();
}
