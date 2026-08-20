package com.example.mislugares;

import android.app.Application;

import com.example.mislugares.modelo.GeoPunto;

public class Aplicacion extends Application {
    public GeoPunto posicionActual = new GeoPunto(0.0, 0.0);
}
