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
    private List<PuntoReferencia> cercanos = new ArrayList<>();   // dentro del radio
    private List<PuntoReferencia> puntos = new ArrayList<>();     // los que se ven (cercanos + texto)
    private String textoBusqueda = "";                            // ya normalizado
    private GeoPunto posicionActual = GeoPunto.SIN_POSICION;

    /** Quién quiere enterarse de que tocaron una fila (lo decide el Fragment). */
    public interface OnPuntoClickListener {
        void alTocar(PuntoReferencia punto);
    }

    private OnPuntoClickListener onPuntoClickListener;

    public void setOnPuntoClickListener(OnPuntoClickListener listener) {
        this.onPuntoClickListener = listener;
    }

    public AdaptadorPuntosReferencia(Context contexto) {
        RepositorioPuntosReferencia repositorio = new RepositorioPuntosReferencia(contexto);
        this.casoUsoPuntosCercanos = new PuntosCercanos(repositorio);
    }

    /** Recalcula los puntos cercanos a partir de la posición actual y el radio inicial (metros). */
    public void actualizar(GeoPunto posicionActual, double radioInicialMetros) {
        this.posicionActual = posicionActual;
        if (posicionActual.equals(GeoPunto.SIN_POSICION)) {
            cercanos = new ArrayList<>();
        } else {
            cercanos = casoUsoPuntosCercanos.buscar(posicionActual, radioInicialMetros);
        }
        filtrarPorTexto();
    }

    /** Texto del buscador. Filtra dentro de los puntos que ya están dentro del radio. */
    public void setTexto(String texto) {
        this.textoBusqueda = Texto.normalizar(texto);
        filtrarPorTexto();
    }

    /** Se queda con los cercanos cuyo nombre, categoría o municipio contienen el texto. */
    private void filtrarPorTexto() {
        puntos = new ArrayList<>();
        for (PuntoReferencia punto : cercanos) {
            if (textoBusqueda.isEmpty()
                    || Texto.normalizar(punto.getNombre()).contains(textoBusqueda)
                    || Texto.normalizar(punto.getCategoria()).contains(textoBusqueda)
                    || Texto.normalizar(punto.getMunicipio()).contains(textoBusqueda)) {
                puntos.add(punto);
            }
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
        PuntoReferencia punto = puntos.get(posicion);
        holder.personaliza(punto, posicionActual);
        holder.itemView.setOnClickListener(v -> {
            if (onPuntoClickListener != null) {
                onPuntoClickListener.alTocar(punto);
            }
        });
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
