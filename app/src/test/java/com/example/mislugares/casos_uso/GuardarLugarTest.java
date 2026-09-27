package com.example.mislugares.casos_uso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.Lugar;

import org.junit.Before;
import org.junit.Test;

public class GuardarLugarTest {

    private RepositorioLugaresFalso repositorio;
    private GuardarLugar casoUso;

    @Before
    public void antesDeCadaTest() {
        repositorio = new RepositorioLugaresFalso();
        casoUso = new GuardarLugar(repositorio);
    }

    @Test
    public void guardarNuevo_dejaElLugarRecuperableConEseId() {
        Lugar lugar = new Lugar("Tortas La Purísima", "Privada Lic. Verdad 759",
                "8181818181", "", "", 5f, new GeoPunto(-100.3086, 25.6699), "");

        int id = casoUso.guardarNuevo(lugar);

        assertEquals("Tortas La Purísima", repositorio.elemento(id).getNombre());
    }

    @Test
    public void guardarNuevo_dosLlamadasSeguidas_devuelvenIdsDistintos() {
        int id1 = casoUso.guardarNuevo(new Lugar("Lugar 1", "Dirección 1",
                "", "", "", 0f, GeoPunto.SIN_POSICION, ""));
        int id2 = casoUso.guardarNuevo(new Lugar("Lugar 2", "Dirección 2",
                "", "", "", 0f, GeoPunto.SIN_POSICION, ""));

        // Si el caso de uso reusara el mismo id, "Lugar 2" pisaría a "Lugar 1"
        // en el repositorio, y este assert lo detectaría.
        assertNotEquals(id1, id2);
        assertEquals("Lugar 1", repositorio.elemento(id1).getNombre());
        assertEquals("Lugar 2", repositorio.elemento(id2).getNombre());
    }

    @Test
    public void actualizar_reemplazaLosDatosDelLugarExistente() {
        int id = casoUso.guardarNuevo(new Lugar("Nombre viejo", "Dirección vieja",
                "", "", "", 2f, GeoPunto.SIN_POSICION, ""));

        Lugar datosNuevos = new Lugar("Nombre nuevo", "Dirección nueva",
                "", "", "", 4f, GeoPunto.SIN_POSICION, "");
        casoUso.actualizar(id, datosNuevos);

        Lugar guardado = repositorio.elemento(id);
        assertEquals("Nombre nuevo", guardado.getNombre());
        assertEquals("Dirección nueva", guardado.getDireccion());
        assertEquals(4f, guardado.getValoracion(), 0.001f);
    }
}
