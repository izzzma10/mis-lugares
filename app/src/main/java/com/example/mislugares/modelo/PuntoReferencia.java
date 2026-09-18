package com.example.mislugares.modelo;

public class PuntoReferencia {
    private int id;
    private String nombre;
    private String categoria;
    private String municipio;
    private GeoPunto posicion;
    
    
    public PuntoReferencia(int id, String nombre, String categoria, String municipio, GeoPunto posicion){
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.municipio = municipio;
        this.posicion = posicion;
    }

    public int id(){
        return id;
    }

    public String getNombre(){
        return nombre;
    }

    public String getCategoria(){
        return categoria;
    }

    public String getMunicipio(){
        return municipio;
    }

    public GeoPunto getPosicion(){
        return posicion;
    }

    @Override 
    public String toString(){
        return "PuntoReferencia {id=" + id + ", nombre=" + nombre +", categoria=" + categoria +", municipio=" + municipio +", posicion=" + posicion +"}";
    }

}
