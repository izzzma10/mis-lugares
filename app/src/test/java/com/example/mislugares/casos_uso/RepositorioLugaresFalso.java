package com.example.mislugares.casos_uso;

import com.example.mislugares.datos.RepositorioLugares;
import com.example.mislugares.modelo.Lugar;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Implementación falsa (un "doble de prueba") de RepositorioLugares: guarda
 * todo en un Map en memoria, sin SQLite ni ningún objeto de Android.
 *
 * Sirve para probar los casos de uso (ListarLugares, GuardarLugar, BorrarLugar)
 * de forma aislada: si un test falla, el error está en el caso de uso, nunca
 * en la base de datos real, porque acá no hay base de datos real.
 */
public class RepositorioLugaresFalso implements RepositorioLugares {

    private final Map<Integer, Lugar> lugares = new LinkedHashMap<>();
    private int siguienteId = 0;

    @Override
    public Lugar elemento(int id) {
        Lugar lugar = lugares.get(id);
        if (lugar == null) {
            throw new NoSuchElementException("No existe el lugar con id " + id);
        }
        return lugar;
    }

    @Override
    public void añade(Lugar lugar) {
        lugares.put(siguienteId, lugar);
        siguienteId++;
    }

    @Override
    public int nuevo() {
        int id = siguienteId;
        lugares.put(id, new Lugar());
        siguienteId++;
        return id;
    }

    @Override
    public void borrar(int id) {
        lugares.remove(id);
    }

    @Override
    public int tamaño() {
        return lugares.size();
    }

    @Override
    public void actualiza(int id, Lugar lugar) {
        lugares.put(id, lugar);
    }

    @Override
    public Map<Integer, Lugar> todos() {
        return lugares;
    }
}
