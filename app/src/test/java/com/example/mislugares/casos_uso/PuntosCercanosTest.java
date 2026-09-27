package com.example.mislugares.casos_uso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.PuntoReferencia;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Los 4 puntos de prueba están todos al norte del centro, a la MISMA longitud.
 * Con longitud igual, la fórmula de distancia de GeoPunto da la distancia
 * exacta a lo largo del meridiano (sin aproximar), así los números de acá
 * no dependen de ningún redondeo:
 *   0.01° de latitud ≈ 1112 m  →  PUNTO_MUY_CERCA
 *   0.05° de latitud ≈ 5560 m  →  PUNTO_MEDIO
 *   0.20° de latitud ≈ 22241 m →  PUNTO_LEJOS
 *   0.50° de latitud ≈ 55602 m →  PUNTO_FUERA_DE_RANGO (más allá del tope de 30km)
 */
public class PuntosCercanosTest {

    private static final GeoPunto CENTRO = new GeoPunto(-100.3161, 25.6866);

    private static final PuntoReferencia PUNTO_MUY_CERCA =
            new PuntoReferencia(1, "Muy cerca", "Parque", "Monterrey",
                    new GeoPunto(-100.3161, 25.6966));      // ≈ 1112 m
    private static final PuntoReferencia PUNTO_MEDIO =
            new PuntoReferencia(2, "A media distancia", "Museo", "Monterrey",
                    new GeoPunto(-100.3161, 25.7366));      // ≈ 5560 m
    private static final PuntoReferencia PUNTO_LEJOS =
            new PuntoReferencia(3, "Lejos", "Plaza", "Monterrey",
                    new GeoPunto(-100.3161, 25.8866));      // ≈ 22241 m
    private static final PuntoReferencia PUNTO_FUERA_DE_RANGO =
            new PuntoReferencia(4, "Fuera de rango", "Estación", "Monterrey",
                    new GeoPunto(-100.3161, 26.1866));      // ≈ 55602 m

    private PuntosCercanos casoUsoCon(PuntoReferencia... puntos) {
        FuentePuntosReferenciaFalsa fuente =
                new FuentePuntosReferenciaFalsa(Arrays.asList(puntos));
        return new PuntosCercanos(fuente);
    }

    @Test
    public void buscar_conRadioSuficiente_incluyeSoloLosPuntosDentroDelRadio() {
        PuntosCercanos casoUso = casoUsoCon(PUNTO_MUY_CERCA, PUNTO_MEDIO, PUNTO_LEJOS);

        // 10 km: entran MUY_CERCA (1.1km) y MEDIO (5.6km), no LEJOS (22.2km).
        List<PuntoReferencia> resultado = casoUso.buscar(CENTRO, 10_000);

        assertEquals(2, resultado.size());
        assertTrue(resultado.contains(PUNTO_MUY_CERCA));
        assertTrue(resultado.contains(PUNTO_MEDIO));
    }

    @Test
    public void buscar_devuelveLosResultadosOrdenadosDeMasCercaAMasLejos() {
        // A propósito los cargo en un orden distinto al esperado: el caso de
        // uso tiene que reordenarlos, no confiar en el orden de la fuente.
        PuntosCercanos casoUso = casoUsoCon(PUNTO_LEJOS, PUNTO_MUY_CERCA, PUNTO_MEDIO);

        List<PuntoReferencia> resultado = casoUso.buscar(CENTRO, 25_000);

        assertEquals(Arrays.asList(PUNTO_MUY_CERCA, PUNTO_MEDIO, PUNTO_LEJOS), resultado);
    }

    @Test
    public void buscar_siNoHayNadaEnElRadioInicial_loDuplicaHastaEncontrarAlgo() {
        PuntosCercanos casoUso = casoUsoCon(PUNTO_MUY_CERCA, PUNTO_MEDIO);

        // 500 m no alcanza a nadie. El caso de uso debe duplicar el radio
        // (500 → 1000 → 2000) hasta que MUY_CERCA (1112 m) entre.
        List<PuntoReferencia> resultado = casoUso.buscar(CENTRO, 500);

        assertEquals(Arrays.asList(PUNTO_MUY_CERCA), resultado);
    }

    @Test
    public void buscar_siNadaEntraNiAlDoblarElRadioHastaElTope_devuelveVacio() {
        PuntosCercanos casoUso = casoUsoCon(PUNTO_FUERA_DE_RANGO);

        // PUNTO_FUERA_DE_RANGO está a 55.6 km. Aunque el radio se vaya
        // duplicando, nunca debe superar los 30 km del tope: la búsqueda
        // tiene que terminar (no colgarse) y devolver una lista vacía.
        List<PuntoReferencia> resultado = casoUso.buscar(CENTRO, 1_000);

        assertTrue(resultado.isEmpty());
    }
}
