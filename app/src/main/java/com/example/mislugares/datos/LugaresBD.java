package com.example.mislugares.datos;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.content.ContentValues;

import com.example.mislugares.datos.LugaresBD;
import com.example.mislugares.Aplicacion;
import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.Lugar;
import com.example.mislugares.modelo.TipoLugar;

import java.util.LinkedHashMap;
import java.util.Map;

public class LugaresBD extends SQLiteOpenHelper implements RepositorioLugares {

    Context contexto;

    public LugaresBD(Context contexto) {
        super(contexto, "lugares", null, 2);
        this.contexto = contexto;
    }

    @Override public void onCreate(SQLiteDatabase bd) {
        bd.execSQL("CREATE TABLE lugares ("+
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "nombre TEXT, " +
                "direccion TEXT, " +
                "longitud REAL, " +
                "latitud REAL, " +
                "tipo INTEGER, " +
                "foto TEXT, " +
                "telefono TEXT, " +
                "url TEXT, " +
                "comentario TEXT, " +
                "fecha BIGINT, " +
                "valoracion REAL, " +
                "favorito INTEGER)");
        Lugar fime = new Lugar("Facultad de Ingeniería Mecánica y Eléctrica",
                "Av. Universidad s/n, Cd. Universitaria, San Nicolás de los Garza, N.L.",
                "", "https://fime.uanl.mx",
                "Facultad de Ingeniería Mecánica y Eléctrica, UANL.", 4,
                new GeoPunto(-100.3151, 25.7269), "fime");
        fime.setTipo(TipoLugar.EDUCACION);
        bd.insert("lugares", null, valoresDe(fime));

        Lugar fundidora = new Lugar("Parque Fundidora",
                "Av. Fundidora 501, Obrera, 64010 Monterrey, N.L.",
                "", "https://parquefundidora.org",
                "Parque urbano en las antiguas instalaciones de la fundidora.", 5,
                new GeoPunto(-100.2843, 25.6773), "fundidora");
        fundidora.setTipo(TipoLugar.NATURALEZA);
        bd.insert("lugares", null, valoresDe(fundidora));

        Lugar estadio = new Lugar("Estadio Universitario",
                "Av. Alfonso Reyes s/n, San Nicolás de los Garza, N.L.",
                "", "",
                "Casa de los Tigres UANL.", 5,
                new GeoPunto(-100.3103, 25.7239), "estadio_universitario");
        estadio.setTipo(TipoLugar.DEPORTE);
        bd.insert("lugares", null, valoresDe(estadio));

        Lugar macroplaza = new Lugar("MacroPlaza",
                "Zaragoza, Centro, 64000 Monterrey, N.L.",
                "", "",
                "Gran Plaza del centro de Monterrey.", 4,
                new GeoPunto(-100.3099, 25.6698), "macroplaza");
        macroplaza.setTipo(TipoLugar.OTROS);
        bd.insert("lugares", null, valoresDe(macroplaza));

        Lugar tortas = new Lugar("Tortas La Purísima",
                "Privada Lic. Verdad 759, Centro, 64000 Monterrey, N.L.",
                "", "",
                "Tortas en el Barrio Antiguo.", 5,
                new GeoPunto(-100.3086, 25.6699), "tortas_purisima");
        tortas.setTipo(TipoLugar.RESTAURANTE);
        bd.insert("lugares", null, valoresDe(tortas));
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion,
                                    int newVersion) {
        // Se llama automáticamente cuando el número de versión (arriba, en el
        // constructor) sube y ya existe una base vieja en el teléfono — hay
        // que "ponerla al día" a mano, agregando lo nuevo sin borrar los datos.
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE lugares ADD COLUMN favorito INTEGER DEFAULT 0");
        }
    }

    @Override public Lugar elemento(int id) {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT * FROM lugares WHERE _id = " + id, null);
        try {
            if (cursor.moveToNext())
                return extraeLugar(cursor);
            else
                throw new SQLException("Error al acceder al elemento _id = " + id);
        } catch (Exception e) {
            throw e;
        } finally {
            if (cursor!=null) cursor.close();
        }
    }

    @Override
    public void añade(Lugar lugar) {
        getWritableDatabase().insert("lugares", null, valoresDe(lugar));
    }

    @Override public int nuevo() {
        Lugar lugar = new Lugar();
        long id = getWritableDatabase().insert("lugares", null, valoresDe(lugar));
        return (int) id;
    }

    @Override
    public void borrar(int id) {
        getWritableDatabase().execSQL("DELETE FROM lugares WHERE _id = " + id);
    }

    @Override
    public int tamaño() {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM lugares", null);
        cursor.moveToNext();
        int cantidad = cursor.getInt(0);
        cursor.close();
        return cantidad;
    }

    @Override public void actualiza(int id, Lugar lugar) {
        getWritableDatabase().update("lugares", valoresDe(lugar), "_id = ?", new String[]{String.valueOf(id)});
    }

    @Override
    public Map<Integer, Lugar> todos() {
        Map<Integer, Lugar> mapa = new LinkedHashMap<>();
        Cursor cursor = getReadableDatabase().rawQuery("SELECT * FROM lugares", null);
        try {
            while (cursor.moveToNext())
                mapa.put(cursor.getInt(0), extraeLugar(cursor));
        } finally {
            cursor.close();
        }
        return mapa;
    }

    public static Lugar extraeLugar(Cursor cursor) {
        Lugar lugar = new Lugar();
        lugar.setNombre(cursor.getString(1));
        lugar.setDireccion(cursor.getString(2));
        lugar.setPosicion(new GeoPunto(cursor.getDouble(3),
                cursor.getDouble(4)));
        lugar.setTipo(TipoLugar.values()[cursor.getInt(5)]);
        lugar.setFoto(cursor.getString(6));
        lugar.setTelefono(cursor.getString(7));
        lugar.setUrl(cursor.getString(8));
        lugar.setComentario(cursor.getString(9));
        lugar.setFecha(cursor.getLong(10));
        lugar.setValoracion(cursor.getFloat(11));
        lugar.setFavorito(cursor.getInt(12) == 1);
        return lugar;
    }

    public Cursor extraeCursor() {
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(contexto);
        String consulta;
        switch (pref.getString("orden", "0")) {
            case "0":
                consulta = "SELECT * FROM lugares ";
                break;
            case "1":
                consulta = "SELECT * FROM lugares ORDER BY valoracion DESC";
                break;
            default:
                double lon = ((Aplicacion) contexto.getApplicationContext())
                        .posicionActual.getLongitud();
                double lat = ((Aplicacion) contexto.getApplicationContext())
                        .posicionActual.getLatitud();
                consulta = "SELECT * FROM lugares ORDER BY " +
                        "(" + lon + "-longitud)*(" + lon + "-longitud) + " +
                        "(" + lat + "-latitud )*(" + lat + "-latitud )";
                break;
        }
        consulta += " LIMIT "+pref.getString("maximo","12");
        SQLiteDatabase bd = getReadableDatabase();
        return bd.rawQuery(consulta, null);
    }

    private ContentValues valoresDe(Lugar lugar) {
        ContentValues valores = new ContentValues();
        valores.put("nombre", lugar.getNombre());
        valores.put("direccion", lugar.getDireccion());
        valores.put("longitud", lugar.getPosicion().getLongitud());
        valores.put("latitud", lugar.getPosicion().getLatitud());
        valores.put("tipo", lugar.getTipo().ordinal());
        valores.put("foto", lugar.getFoto());
        valores.put("telefono", lugar.getTelefono());
        valores.put("url", lugar.getUrl());
        valores.put("comentario", lugar.getComentario());
        valores.put("fecha", lugar.getFecha());
        valores.put("valoracion", lugar.getValoracion());
        valores.put("favorito", lugar.isFavorito() ? 1 : 0);
        return valores;
    }



}