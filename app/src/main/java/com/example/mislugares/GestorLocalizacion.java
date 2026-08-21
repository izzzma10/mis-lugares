package com.example.mislugares;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.mislugares.modelo.GeoPunto;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class GestorLocalizacion {

    public static final int CODIGO_PERMISO_UBICACION = 100;

    private final Activity actividad;
    private final FusedLocationProviderClient proveedorUbicacion;

    public GestorLocalizacion(Activity actividad) {
        this.actividad = actividad;
        this.proveedorUbicacion = LocationServices.getFusedLocationProviderClient(actividad);
    }

    public void solicitarUbicacionActual() {
        if (ContextCompat.checkSelfPermission(actividad, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(actividad,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    CODIGO_PERMISO_UBICACION);
            return;
        }
        obtenerUbicacion();
    }

    @SuppressLint("MissingPermission")
    private void obtenerUbicacion() {
        proveedorUbicacion.getLastLocation().addOnSuccessListener(actividad, this::guardarUbicacion);
    }

    private void guardarUbicacion(Location ubicacion) {
        if (ubicacion == null) {
            return;
        }
        GeoPunto punto = new GeoPunto(ubicacion.getLatitude(), ubicacion.getLongitude());
        ((Aplicacion) actividad.getApplication()).posicionActual = punto;
    }

    public void manejarResultadoPermiso(int codigoSolicitud, int[] resultados) {
        if (codigoSolicitud == CODIGO_PERMISO_UBICACION
                && resultados.length > 0
                && resultados[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion();
        }
    }
}
