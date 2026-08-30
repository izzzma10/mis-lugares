package com.example.mislugares.presentacion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;
import com.example.mislugares.casos_uso.ListarLugares;
import com.example.mislugares.datos.RepositorioLugares;
import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.Lugar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.RecyclerView;

public class AdaptadorLugares extends
        RecyclerView.Adapter<AdaptadorLugares.ViewHolder> {

    protected final ListarLugares casoUsoListar;
    protected Map<Integer, Lugar> lugares;
    protected List<Integer> ids;
    protected View.OnClickListener onClickListener;

    // Estado de la fila de filtros
    protected boolean soloFavoritos = false;
    protected int orden = 0;   // 0 normal · 1 cercania · 2 A-Z asc · 3 A-Z desc
    protected GeoPunto posicion = GeoPunto.SIN_POSICION;

    public AdaptadorLugares(RepositorioLugares repositorio) {
        this.casoUsoListar = new ListarLugares(repositorio);
        actualizar();
    }

    public void actualizar() {
        lugares = casoUsoListar.listar();
        aplicarFiltro();
    }

    public void setPosicion(GeoPunto posicion) {
        this.posicion = posicion;
    }

    public void setFiltro(boolean soloFavoritos, int orden) {
        this.soloFavoritos = soloFavoritos;
        this.orden = orden;
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        ids = new ArrayList<>();
        for (Map.Entry<Integer, Lugar> entrada : lugares.entrySet()) {
            if (!soloFavoritos || entrada.getValue().isFavorito()) {
                ids.add(entrada.getKey());
            }
        }
        if (orden == 1) {
            Collections.sort(ids, (a, b) -> Double.compare(
                    posicion.distancia(lugares.get(a).getPosicion()),
                    posicion.distancia(lugares.get(b).getPosicion())));
        } else if (orden == 2) {
            Collections.sort(ids, (a, b) ->
                    lugares.get(a).getNombre().compareToIgnoreCase(lugares.get(b).getNombre()));
        } else if (orden == 3) {
            Collections.sort(ids, (a, b) ->
                    lugares.get(b).getNombre().compareToIgnoreCase(lugares.get(a).getNombre()));
        }
        notifyDataSetChanged();
    }

    public int idEnPosicion(int posicion) {
        return ids.get(posicion);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nombre, direccion, distancia;
        public ImageView foto, favorito;
        public ImageView[] estrellas = new ImageView[5];

        public ViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nombre);
            direccion = itemView.findViewById(R.id.direccion);
            distancia = itemView.findViewById(R.id.distancia);
            foto = itemView.findViewById(R.id.foto);
            favorito = itemView.findViewById(R.id.favorito);
            estrellas[0] = itemView.findViewById(R.id.estrella_1);
            estrellas[1] = itemView.findViewById(R.id.estrella_2);
            estrellas[2] = itemView.findViewById(R.id.estrella_3);
            estrellas[3] = itemView.findViewById(R.id.estrella_4);
            estrellas[4] = itemView.findViewById(R.id.estrella_5);
        }

        public void personaliza(Lugar lugar) {
            nombre.setText(lugar.getNombre());
            direccion.setText(lugar.getDireccion());

            int idPin;
            switch (lugar.getTipo()) {
                case EDUCACION:
                    idPin = R.drawable.ic_pin_azul; break;
                case NATURALEZA:
                    idPin = R.drawable.ic_pin_verde; break;
                case RESTAURANTE: case BAR: case COPAS:
                    idPin = R.drawable.ic_pin_rojo; break;
                case ESPECTACULO:
                    idPin = R.drawable.ic_pin_amarillo; break;
                default:
                    idPin = R.drawable.ic_pin_otros; break;
            }
            foto.setImageResource(idPin);

            favorito.setVisibility(lugar.isFavorito() ? View.VISIBLE : View.GONE);

            int llenas = Math.round(lugar.getValoracion());
            for (int i = 0; i < 5; i++) {
                estrellas[i].setImageResource((i < llenas)
                        ? R.drawable.ic_estrella_llena
                        : R.drawable.ic_estrella_vacia);
            }

            GeoPunto pos = ((Aplicacion) itemView.getContext()
                    .getApplicationContext()).posicionActual;
            if (pos.equals(GeoPunto.SIN_POSICION)
                    || lugar.getPosicion().equals(GeoPunto.SIN_POSICION)) {
                distancia.setText("... KM");
            } else {
                float km = (float) pos.distancia(lugar.getPosicion()) / 1000f;
                distancia.setText(String.format(java.util.Locale.US, "%.1f KM", km));
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.elemento_lista, parent, false);
        v.setOnClickListener(onClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int posicion) {
        Lugar lugar = lugares.get(idEnPosicion(posicion));
        holder.personaliza(lugar);
    }

    @Override public int getItemCount() {
        return ids.size();
    }

    public void setOnItemClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
