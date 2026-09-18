package com.example.mislugares.datos;

import android.content.Context;

import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.PuntoReferencia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RepositorioPuntosReferencia {
    private Context contexto;

    public RepositorioPuntosReferencia(Context contexto){
        this.contexto = contexto;
    }
    
    public List<PuntoReferencia> cargarTodos() {
        List<PuntoReferencia> puntos = new ArrayList<>(); 
        try {
            InputStream entrada = contexto.getAssets().open("puntos_referencia_mty.json");
            BufferedReader lector = new BufferedReader(new InputStreamReader(entrada));
            StringBuilder texto = new StringBuilder();
            String linea;
            while ((linea = lector.readLine()) !=null) {
                texto.append(linea);
            }
            lector.close();
    
            JSONArray arreglo = new JSONArray(texto.toString());
            for(int i = 0; i < arreglo.length(); i++) {
                JSONObject obj = arreglo.getJSONObject(i);

                int id = obj.getInt("id");
                String nombre = obj.getString("nombre");
                String categoria = obj.getString("categoria");
                String municipio = obj.getString("municipio");
                double longitud = obj.getDouble("longitud");
                double latitud = obj.getDouble("latitud");

                GeoPunto posicion = new GeoPunto(longitud, latitud);
                PuntoReferencia punto = new PuntoReferencia(id, nombre, categoria, municipio, posicion);
                puntos.add(punto);
            }



        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return puntos;
    }


}
