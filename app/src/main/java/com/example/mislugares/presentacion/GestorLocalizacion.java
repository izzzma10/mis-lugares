package com.example.mislugares.presentacion;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.mislugares.Aplicacion;
import com.example.mislugares.modelo.GeoPunto;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

public class GestorLocalizacion {

    public static final int CODIGO_PERMISO_UBICACION = 100;

    private final Activity actividad;
    private final FusedLocationProviderClient proveedorUbicacion;
    private Runnable alObtenerUbicacion;
    private boolean buscando = false;   // evita pedir la ubicación dos veces a la vez

    public GestorLocalizacion(Activity actividad) {
        this.actividad = actividad;
        this.proveedorUbicacion = LocationServices.getFusedLocationProviderClient(actividad);
    }

    /** Se ejecuta cuando ya hay una ubicación nueva guardada (para refrescar la UI). */
    public void setAlObtenerUbicacion(Runnable accion) {
        this.alObtenerUbicacion = accion;
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

    /** Reintento silencioso (lo usa onResume): solo actúa si ya hay permiso y todavía no hay posición. */
    public void reintentarSiHaceFalta() {
        boolean conPermiso = ContextCompat.checkSelfPermission(actividad,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean sinPosicion = ((Aplicacion) actividad.getApplication())
                .posicionActual.equals(GeoPunto.SIN_POSICION);
        if (conPermiso && sinPosicion) {
            obtenerUbicacion();
        }
    }

    /** Primero la última ubicación en caché (instantánea); si no hay ninguna, pide una nueva. */
    @SuppressLint("MissingPermission")
    private void obtenerUbicacion() {
        if (buscando) {
            return;
        }
        buscando = true;
        proveedorUbicacion.getLastLocation().addOnSuccessListener(actividad, ubicacion -> {
            if (ubicacion != null) {
                buscando = false;
                guardarUbicacion(ubicacion);
            } else {
                pedirUbicacionNueva();
            }
        }).addOnFailureListener(actividad, e -> buscando = false);
    }

    /** La caché está vacía (ej. el GPS estaba apagado): le pide al sistema una lectura fresca. */
    @SuppressLint("MissingPermission")
    private void pedirUbicacionNueva() {
        proveedorUbicacion.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        new CancellationTokenSource().getToken())
                .addOnSuccessListener(actividad, ubicacion -> {
                    buscando = false;
                    guardarUbicacion(ubicacion);
                })
                .addOnFailureListener(actividad, e -> buscando = false);
    }

    private void guardarUbicacion(Location ubicacion) {
        if (ubicacion == null) {
            return;
        }
        GeoPunto punto = new GeoPunto(ubicacion.getLongitude(), ubicacion.getLatitude());
        ((Aplicacion) actividad.getApplication()).posicionActual = punto;
        if (alObtenerUbicacion != null) {
            alObtenerUbicacion.run();
        }
    }

    public void manejarResultadoPermiso(int codigoSolicitud, int[] resultados) {
        if (codigoSolicitud == CODIGO_PERMISO_UBICACION
                && resultados.length > 0
                && resultados[0] == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion();
        }
    }
}
