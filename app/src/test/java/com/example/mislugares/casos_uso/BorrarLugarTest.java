package com.example.mislugares.casos_uso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.Lugar;

import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;

public class BorrarLugarTest {

    private RepositorioLugaresFalso repositorio;
    private BorrarLugar casoUso;

    @Before
    public void antesDeCadaTest() {
        repositorio = new RepositorioLugaresFalso();
        casoUso = new BorrarLugar(repositorio);
    }

    @Test
    public void borrar_elLugarYaNoApareceEnElRepositorio() {
        int id = repositorio.nuevo();
        repositorio.actualiza(id, new Lugar("A borrar", "Dirección",
                "", "", "", 0f, GeoPunto.SIN_POSICION, ""));

        casoUso.borrar(id);

        // assertThrows verifica que la acción lance exactamente esa excepción.
        // Si no la lanzara (el lugar seguiría existiendo), el test fallaría.
        assertThrows(NoSuchElementException.class, () -> repositorio.elemento(id));
    }

    @Test
    public void borrar_noAfectaALosDemasLugares() {
        int idQueQueda = repositorio.nuevo();
        repositorio.actualiza(idQueQueda, new Lugar("Se queda", "Dirección",
                "", "", "", 0f, GeoPunto.SIN_POSICION, ""));
        int idABorrar = repositorio.nuevo();
        repositorio.actualiza(idABorrar, new Lugar("Se borra", "Dirección",
                "", "", "", 0f, GeoPunto.SIN_POSICION, ""));

        casoUso.borrar(idABorrar);

        assertEquals(1, repositorio.tamaño());
        assertEquals("Se queda", repositorio.elemento(idQueQueda).getNombre());
    }
}
