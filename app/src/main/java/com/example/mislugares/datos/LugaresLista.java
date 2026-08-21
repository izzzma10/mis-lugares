package com.example.mislugares.datos;

import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.Lugar;
import com.example.mislugares.modelo.TipoLugar;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LugaresLista implements RepositorioLugares {
    protected List<Lugar> listaLugares ;//= añadeEjemplos();//ejemploLugares();

    public LugaresLista() {
        //listaLugares = ejemploLugares();
        listaLugares = new ArrayList<Lugar>();
        añadeEjemplos();
    }

    public Lugar elemento(int id) {
        return listaLugares.get(id);
    }

    public void añade(Lugar lugar) {
        listaLugares.add(lugar);
    }

    public int nuevo() {
        Lugar lugar = new Lugar();
        listaLugares.add(lugar);
        return listaLugares.size()-1;
    }

    public void borrar(int id) {
        listaLugares.remove(id);
    }

    public int tamaño() {
        return listaLugares.size();
    }
    public void actualiza(int id, Lugar lugar) {
        listaLugares.set(id, lugar);
    }

    public Map<Integer, Lugar> todos() {
        Map<Integer, Lugar> mapa = new LinkedHashMap<>();
        for (int id = 0; id < listaLugares.size(); id++) {
            mapa.put(id, listaLugares.get(id));
        }
        return mapa;
    }

    private Lugar creaLugar(String nombre, String direccion, double longitud, double latitud,
                             TipoLugar tipo, int telefono, String url, String comentario,
                             float valoracion, String foto) {
        Lugar lugar = new Lugar(nombre, direccion, telefono, url, comentario, valoracion,
                new GeoPunto(longitud, latitud), foto);
        lugar.setTipo(tipo);
        return lugar;
    }

    public void añadeEjemplos() {
        añade(creaLugar("Facultad de Ingeniería Mecánica y Eléctrica",
                "Av. Universidad s/n, Cd. Universitaria, San Nicolás de los Garza, N.L.",
                -100.3151, 25.7269,
                TipoLugar.EDUCACION, 0, "https://fime.uanl.mx",
                "Facultad de Ingeniería Mecánica y Eléctrica, UANL.", 4, "fime"));
        añade(creaLugar("Parque Fundidora",
                "Av. Fundidora 501, Obrera, 64010 Monterrey, N.L.",
                -100.2843, 25.6773,
                TipoLugar.NATURALEZA, 0, "https://parquefundidora.org",
                "Parque urbano en las antiguas instalaciones de la fundidora.", 5, "fundidora"));
        añade(creaLugar("Estadio Universitario",
                "Av. Alfonso Reyes s/n, San Nicolás de los Garza, N.L.",
                -100.3103, 25.7239,
                TipoLugar.DEPORTE, 0, "",
                "Casa de los Tigres UANL.", 5, "estadio_universitario"));
        añade(creaLugar("MacroPlaza",
                "Zaragoza, Centro, 64000 Monterrey, N.L.",
                -100.3099, 25.6698,
                TipoLugar.OTROS, 0, "",
                "Gran Plaza del centro de Monterrey.", 4, "macroplaza"));
        añade(creaLugar("Tortas La Purísima",
                "Privada Lic. Verdad 759, Centro, 64000 Monterrey, N.L.",
                -100.3086, 25.6699,
                TipoLugar.RESTAURANTE, 0, "",
                "Tortas en el Barrio Antiguo.", 5, "tortas_purisima"));
    }
}