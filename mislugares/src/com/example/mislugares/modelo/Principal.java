package com.example.mislugares.modelo;

public class Principal {
    public static void main(String[] args) {
        GeoPunto punto = new GeoPunto(-100.309, 25.686);
        Lugar lugar = new Lugar("Mi Casa", "Calle Falsa 123", 811234578, "http://ejemplo.com", "Un buen lugar", 4.5f, punto, "sin_foto.jpg");
        System.out.println(lugar);

    }
}
