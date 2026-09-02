package com.example.mislugares.presentacion;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mislugares.R;
import com.example.mislugares.modelo.GeoPunto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Filas del panel de sugerencias de ubicación. Ver design_handoff .../sugerencias. */
class AdaptadorSugerencias extends RecyclerView.Adapter<AdaptadorSugerencias.Fila> {

    interface AlElegir {
        void elegido(BuscadorLugares.Resultado resultado);
    }

    private final List<BuscadorLugares.Resultado> datos = new ArrayList<>();
    private final AlElegir alElegir;
    private GeoPunto posicion;   // null o SIN_POSICION -> sin columna de distancia

    AdaptadorSugerencias(AlElegir alElegir) {
        this.alElegir = alElegir;
    }

    void actualizar(List<BuscadorLugares.Resultado> nuevos, GeoPunto pos) {
        datos.clear();
        datos.addAll(nuevos);
        posicion = pos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup padre, int tipo) {
        View v = LayoutInflater.from(padre.getContext())
                .inflate(R.layout.fila_sugerencia, padre, false);
        return new Fila(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Fila h, int posicionFila) {
        BuscadorLugares.Resultado r = datos.get(posicionFila);

        h.pin.setImageResource(Tipos.pin(r.tipo));
        h.nombre.setText(r.nombre);
        h.direccion.setText(r.direccion);
        h.direccion.setVisibility(r.direccion.isEmpty() ? View.GONE : View.VISIBLE);

        // La primera fila (mejor resultado) va con fondo crema.
        h.itemView.setBackgroundResource(posicionFila == 0
                ? R.drawable.fondo_fila_sugerencia_mejor
                : R.drawable.fondo_fila_sugerencia);

        String distancia = distanciaTexto(r);
        h.distancia.setVisibility(distancia == null ? View.GONE : View.VISIBLE);
        if (distancia != null) {
            h.distancia.setText(distancia);
        }

        h.itemView.setContentDescription(r.nombre + ", " + r.direccion
                + (distancia != null ? ", a " + distancia.replace('\n', ' ') + "ilómetros" : ""));
        h.itemView.setOnClickListener(v -> alElegir.elegido(r));
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    /** "3.8\nKM" o null si no hay fix de GPS (entonces no se pinta la columna). */
    private String distanciaTexto(BuscadorLugares.Resultado r) {
        if (posicion == null || posicion.equals(GeoPunto.SIN_POSICION)) {
            return null;
        }
        double km = posicion.distancia(new GeoPunto(r.longitud, r.latitud)) / 1000.0;
        return String.format(Locale.US, "%.1f\nKM", km);
    }

    static class Fila extends RecyclerView.ViewHolder {
        final ImageView pin;
        final TextView nombre;
        final TextView direccion;
        final TextView distancia;

        Fila(View v) {
            super(v);
            pin = v.findViewById(R.id.sug_pin);
            nombre = v.findViewById(R.id.sug_nombre);
            direccion = v.findViewById(R.id.sug_direccion);
            distancia = v.findViewById(R.id.sug_distancia);
        }
    }

    /** Divisor de 2dp #241C10 al 18 % entre filas (no bajo la última). */
    static class Divisor extends RecyclerView.ItemDecoration {
        private final Paint pincel = new Paint();
        private final int alto;

        Divisor(Context contexto) {
            pincel.setColor(0x2E241C10);   // 18 % ~ 0x2E
            alto = Math.round(2 * contexto.getResources().getDisplayMetrics().density);
        }

        @Override
        public void onDraw(@NonNull Canvas lienzo, @NonNull RecyclerView rv,
                           @NonNull RecyclerView.State estado) {
            int izq = rv.getPaddingLeft();
            int der = rv.getWidth() - rv.getPaddingRight();
            for (int i = 0; i < rv.getChildCount() - 1; i++) {
                View hijo = rv.getChildAt(i);
                float y = hijo.getBottom() + hijo.getTranslationY();
                lienzo.drawRect(izq, y, der, y + alto, pincel);
            }
        }
    }
}
