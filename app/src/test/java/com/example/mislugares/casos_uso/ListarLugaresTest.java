package com.example.mislugares.casos_uso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.Lugar;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class ListarLugaresTest {

    private RepositorioLugaresFalso repositorio;
    private ListarLugares casoUso;

    // @Before corre antes de CADA @Test: cada test arranca con un repositorio
    // limpio, así ninguno depende de lo que dejó el anterior.
    @Before
    public void antesDeCadaTest() {
        repositorio = new RepositorioLugaresFalso();
        casoUso = new ListarLugares(repositorio);
    }

    @Test
    public void listar_sinLugaresGuardados_devuelveMapaVacio() {
        Map<Integer, Lugar> resultado = casoUso.listar();

        assertTrue(resultado.isEmpty());
    }

    @Test
    public void listar_conLugaresGuardados_devuelveTodosIndexadosPorId() {
        int idParque = repositorio.nuevo();
        repositorio.actualiza(idParque, new Lugar("Parque Fundidora", "Av. Fundidora 501",
                "", "", "", 5f, new GeoPunto(-100.2843, 25.6773), ""));

        int idPlaza = repositorio.nuevo();
        repositorio.actualiza(idPlaza, new Lugar("MacroPlaza", "Zaragoza, Centro",
                "", "", "", 4f, new GeoPunto(-100.3099, 25.6698), ""));

        Map<Integer, Lugar> resultado = casoUso.listar();

        assertEquals(2, resultado.size());
        assertEquals("Parque Fundidora", resultado.get(idParque).getNombre());
        assertEquals("MacroPlaza", resultado.get(idPlaza).getNombre());
    }
}
