package com.example.mislugares.presentacion;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.TipoLugar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Autocompletado de lugares reales usando Photon (photon.komoot.io), un
 * geocodificador gratuito sobre datos de OpenStreetMap pensado para
 * búsqueda mientras se escribe. Sin API key.
 */
final class BuscadorLugares {

    /** Una sugerencia ya lista para volcar en el formulario. */
    static final class Resultado {
        final String nombre;
        final String direccion;
        final double latitud;
        final double longitud;
        final TipoLugar tipo;   // puede ser null si no se pudo deducir

        Resultado(String nombre, String direccion, double lat, double lon, TipoLugar tipo) {
            this.nombre = nombre;
            this.direccion = direccion;
            this.latitud = lat;
            this.longitud = lon;
            this.tipo = tipo;
        }
    }

    private static final ExecutorService HILOS = Executors.newSingleThreadExecutor();
    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    // Centro de la zona metropolitana de Monterrey, usado como referencia fija
    // para el filtro por radio (independiente del sesgo de búsqueda, que puede
    // moverse con el GPS del usuario).
    private static final double CENTRO_MTY_LAT = 25.6866;
    private static final double CENTRO_MTY_LON = -100.3161;

    private BuscadorLugares() { }

    /**
     * Busca {@code consulta} en segundo plano y entrega los resultados en el hilo
     * principal. Si algo falla, entrega una lista vacía (nunca lanza).
     *
     * @param sesgoLat,sesgoLon punto para priorizar resultados cercanos (0,0 = sin sesgo).
     *                          Además solo se devuelven ubicaciones de México; si no
     *                          hubiera ninguna, se devuelven todas como último recurso.
     */
    static void buscar(@NonNull String consulta, double sesgoLat, double sesgoLon,
                       @NonNull Consumer<List<Resultado>> alListo) {
        buscar(consulta, sesgoLat, sesgoLon, 0, alListo);
    }

    /**
     * Igual que {@link #buscar(String, double, double, Consumer)}, pero además
     * descarta cualquier resultado a más de {@code radioMaximoMetros} del centro
     * de la zona metropolitana de Monterrey (0 = sin límite de radio).
     */
    static void buscar(@NonNull String consulta, double sesgoLat, double sesgoLon,
                       double radioMaximoMetros, @NonNull Consumer<List<Resultado>> alListo) {
        HILOS.execute(() -> {
            List<Resultado> resultados = new ArrayList<>();
            try {
                resultados = consultarPhoton(consulta, sesgoLat, sesgoLon, radioMaximoMetros);
            } catch (Exception ignorado) {
                // Sin red o respuesta rara: se devuelve lista vacía.
            }
            List<Resultado> finales = resultados;
            PRINCIPAL.post(() -> alListo.accept(finales));
        });
    }

    private static List<Resultado> consultarPhoton(String consulta, double sesgoLat, double sesgoLon,
            double radioMaximoMetros) throws Exception {
        // Photon solo admite lang = default | de | en | fr. "default" devuelve
        // los nombres locales (lo que queremos en México).
        StringBuilder url = new StringBuilder(
                "https://photon.komoot.io/api/?limit=8&lang=default&q=").append(Uri.encode(consulta));
        if (sesgoLat != 0 || sesgoLon != 0) {
            url.append("&lat=").append(sesgoLat).append("&lon=").append(sesgoLon);
        }

        HttpURLConnection conexion = (HttpURLConnection) new URL(url.toString()).openConnection();
        conexion.setRequestProperty("User-Agent", "MisLugares/1.0 (proyecto escolar)");
        conexion.setConnectTimeout(4000);
        conexion.setReadTimeout(6000);

        StringBuilder cuerpo = new StringBuilder();
        try (BufferedReader lector = new BufferedReader(new InputStreamReader(
                conexion.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                cuerpo.append(linea);
            }
        } finally {
            conexion.disconnect();
        }

        List<Resultado> todas = new ArrayList<>();
        List<Resultado> mexico = new ArrayList<>();
        JSONArray features = new JSONObject(cuerpo.toString()).optJSONArray("features");
        if (features == null) {
            return todas;
        }
        for (int i = 0; i < features.length(); i++) {
            JSONObject f = features.optJSONObject(i);
            if (f == null) {
                continue;
            }
            JSONArray coord = f.optJSONObject("geometry") != null
                    ? f.getJSONObject("geometry").optJSONArray("coordinates") : null;
            JSONObject p = f.optJSONObject("properties");
            if (coord == null || coord.length() < 2 || p == null) {
                continue;
            }
            double lon = coord.getDouble(0);
            double lat = coord.getDouble(1);

            String nombre = primeraNoVacia(
                    p.optString("name", ""),
                    p.optString("street", ""),
                    p.optString("city", ""),
                    consulta);
            String direccion = componerDireccion(p);
            TipoLugar tipo = deducirTipo(p.optString("osm_key", ""), p.optString("osm_value", ""));

            Resultado r = new Resultado(nombre, direccion, lat, lon, tipo);
            todas.add(r);
            if ("MX".equalsIgnoreCase(p.optString("countrycode", ""))) {
                mexico.add(r);
            }
        }
        if (radioMaximoMetros > 0) {
            // Con radio activo alcanza con la distancia: cualquier resultado a
            // 50km de Monterrey ya está en México/Nuevo León, no hace falta
            // además el filtro/fallback por país.
            return dentroDelRadio(todas, radioMaximoMetros);
        }
        // Solo México; si la búsqueda no dio ninguna local, se devuelven todas.
        return !mexico.isEmpty() ? mexico : todas;
    }

    private static List<Resultado> dentroDelRadio(List<Resultado> resultados, double radioMetros) {
        GeoPunto centro = new GeoPunto(CENTRO_MTY_LON, CENTRO_MTY_LAT);
        List<Resultado> filtrados = new ArrayList<>();
        for (Resultado r : resultados) {
            if (centro.distancia(new GeoPunto(r.longitud, r.latitud)) <= radioMetros) {
                filtrados.add(r);
            }
        }
        return filtrados;
    }

    /** Arma "Calle 123, Colonia, Ciudad, Estado, País" con lo que haya. */
    private static String componerDireccion(JSONObject p) {
        String calle = p.optString("street", "");
        String numero = p.optString("housenumber", "");
        String lineaCalle = calle.isEmpty() ? numero
                : (numero.isEmpty() ? calle : calle + " " + numero);

        StringBuilder sb = new StringBuilder();
        anexar(sb, lineaCalle);
        anexar(sb, primeraNoVacia(p.optString("locality", ""), p.optString("district", "")));
        anexar(sb, p.optString("city", ""));
        anexar(sb, p.optString("postcode", ""));
        anexar(sb, p.optString("state", ""));
        anexar(sb, p.optString("country", ""));
        return sb.toString();
    }

    private static void anexar(StringBuilder sb, String parte) {
        if (parte == null || parte.isEmpty()) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(parte);
    }

    private static String primeraNoVacia(String... valores) {
        for (String v : valores) {
            if (v != null && !v.isEmpty()) {
                return v;
            }
        }
        return "";
    }

    /** Traduce la categoría OSM (osm_key/osm_value) al enum del proyecto. */
    private static TipoLugar deducirTipo(String clave, String valor) {
        switch (clave) {
            case "amenity":
                switch (valor) {
                    case "restaurant": case "fast_food": case "cafe": case "food_court":
                        return TipoLugar.RESTAURANTE;
                    case "bar": case "pub": case "biergarten":
                        return TipoLugar.BAR;
                    case "nightclub":
                        return TipoLugar.COPAS;
                    case "cinema": case "theatre": case "arts_centre":
                        return TipoLugar.ESPECTACULO;
                    case "school": case "university": case "college": case "library": case "kindergarten":
                        return TipoLugar.EDUCACION;
                    case "fuel":
                        return TipoLugar.GASOLINERA;
                    default:
                        return null;
                }
            case "tourism":
                switch (valor) {
                    case "hotel": case "motel": case "guest_house": case "hostel": case "apartment":
                        return TipoLugar.HOTEL;
                    case "museum": case "attraction": case "theme_park": case "gallery":
                        return TipoLugar.ESPECTACULO;
                    default:
                        return null;
                }
            case "shop":
                return TipoLugar.COMPRAS;
            case "leisure":
                switch (valor) {
                    case "park": case "garden": case "nature_reserve":
                        return TipoLugar.NATURALEZA;
                    case "stadium": case "sports_centre": case "pitch": case "fitness_centre": case "track":
                        return TipoLugar.DEPORTE;
                    default:
                        return null;
                }
            case "natural":
                return TipoLugar.NATURALEZA;
            default:
                return null;
        }
    }
}
