package com.example.mislugares.presentacion;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mislugares.R;
import com.example.mislugares.casos_uso.PuntosCercanos;
import com.example.mislugares.datos.RepositorioPuntosReferencia;
import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.PuntoReferencia;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter de la pantalla "CERCA": muestra los puntos de referencia fijos
 * (assets/puntos_referencia_mty.json) dentro de un radio de la posición
 * actual del usuario, en vez de los lugares guardados por el usuario.
 */
public class AdaptadorPuntosReferencia extends
        RecyclerView.Adapter<AdaptadorPuntosReferencia.ViewHolder> {

    private final PuntosCercanos casoUsoPuntosCercanos;
    private List<PuntoReferencia> puntos = new ArrayList<>();
    private GeoPunto posicionActual = GeoPunto.SIN_POSICION;

    public AdaptadorPuntosReferencia(Context contexto) {
        RepositorioPuntosReferencia repositorio = new RepositorioPuntosReferencia(contexto);
        this.casoUsoPuntosCercanos = new PuntosCercanos(repositorio);
    }

    /** Recalcula los puntos cercanos a partir de la posición actual y el radio inicial (metros). */
    public void actualizar(GeoPunto posicionActual, double radioInicialMetros) {
        this.posicionActual = posicionActual;
        if (posicionActual.equals(GeoPunto.SIN_POSICION)) {
            puntos = new ArrayList<>();
        } else {
            puntos = casoUsoPuntosCercanos.buscar(posicionActual, radioInicialMetros);
        }
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.elemento_punto_referencia, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int posicion) {
        holder.personaliza(puntos.get(posicion), posicionActual);
    }

    @Override
    public int getItemCount() {
        return puntos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView pin;
        public TextView nombre, categoria, distancia;

        public ViewHolder(View itemView) {
            super(itemView);
            pin = itemView.findViewById(R.id.pr_pin);
            nombre = itemView.findViewById(R.id.pr_nombre);
            categoria = itemView.findViewById(R.id.pr_categoria);
            distancia = itemView.findViewById(R.id.pr_distancia);
        }

        public void personaliza(PuntoReferencia punto, GeoPunto posicionActual) {
            nombre.setText(punto.getNombre());
            categoria.setText(punto.getCategoria());
            pin.setImageResource(CategoriasReferencia.pin(CategoriasReferencia.de(punto.getCategoria())));

            float km = (float) posicionActual.distancia(punto.getPosicion()) / 1000f;
            distancia.setText(String.format(Locale.US,
                    itemView.getContext().getString(R.string.km_formato), km));
        }
    }
}
