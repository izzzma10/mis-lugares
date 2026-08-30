package com.example.mislugares.presentacion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;
import com.example.mislugares.casos_uso.ListarLugares;
import com.example.mislugares.datos.RepositorioLugares;
import com.example.mislugares.modelo.GeoPunto;
import com.example.mislugares.modelo.Lugar;
import com.example.mislugares.modelo.TipoLugar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.RecyclerView;

public class AdaptadorLugares extends
        RecyclerView.Adapter<AdaptadorLugares.ViewHolder> {

    protected final ListarLugares casoUsoListar;   // Caso de uso: listar lugares
    protected Map<Integer, Lugar> lugares;         // id -> Lugar, cacheado
    protected List<Integer> ids;                   // orden de posiciones del RecyclerView (ya filtrado)
    protected View.OnClickListener onClickListener;
    protected TipoLugar filtro;                    // null = sin filtro, muestra todos

    public AdaptadorLugares(RepositorioLugares repositorio) {
        this.casoUsoListar = new ListarLugares(repositorio);
        actualizar();
    }

    /** Vuelve a pedir la lista de lugares al caso de uso y refresca la vista,
     *  manteniendo el filtro que estuviera activo. */
    public void actualizar() {
        lugares = casoUsoListar.listar();
        aplicarFiltro();
    }

    /** Filtra la lista para mostrar solo los lugares de ese tipo.
     *  Pasar null muestra todos los lugares de nuevo. */
    public void filtrarPorTipo(TipoLugar tipo) {
        this.filtro = tipo;
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        if (filtro == null) {
            ids = new ArrayList<>(lugares.keySet());
        } else {
            ids = new ArrayList<>();
            for (Map.Entry<Integer, Lugar> entrada : lugares.entrySet()) {
                if (entrada.getValue().getTipo() == filtro) {
                    ids.add(entrada.getKey());
                }
            }
        }
        notifyDataSetChanged();
    }

    /** Id real (en el repositorio) del elemento que está en esa posición del RecyclerView. */
    public int idEnPosicion(int posicion) {
        return ids.get(posicion);
    }

    //Creamos nuestro ViewHolder, con los tipos de elementos a modificar
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nombre, direccion;
        public ImageView foto;
        public RatingBar valoracion;
        public TextView distancia;
        public ImageView favorito;
        public ViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nombre);
            direccion = itemView.findViewById(R.id.direccion);
            foto = itemView.findViewById(R.id.foto);
            valoracion= itemView.findViewById(R.id.valoracion);
            distancia = itemView.findViewById(R.id.distancia);
            favorito = itemView.findViewById(R.id.favorito);
        }
        // Personalizamos un ViewHolder a partir de un lugar
        public void personaliza(Lugar lugar) {
            nombre.setText(lugar.getNombre());
            direccion.setText(lugar.getDireccion());
            int id = R.drawable.otros;
            switch(lugar.getTipo()) {
                case RESTAURANTE:id = R.drawable.restaurante; break;
                case BAR:    id = R.drawable.bar;     break;
                case COPAS:   id = R.drawable.copas;    break;
                case ESPECTACULO:id = R.drawable.espectaculos; break;
                case HOTEL:   id = R.drawable.hotel;    break;
                case COMPRAS:  id = R.drawable.compras;   break;
                case EDUCACION: id = R.drawable.educacion;  break;
                case DEPORTE:  id = R.drawable.deporte;   break;
                case NATURALEZA: id = R.drawable.naturaleza; break;
                case GASOLINERA: id = R.drawable.gasolinera; break;  }
            foto.setImageResource(id);
            foto.setScaleType(ImageView.ScaleType.FIT_END);
            valoracion.setRating(lugar.getValoracion());
            favorito.setVisibility(lugar.isFavorito() ? View.VISIBLE : View.GONE);


            GeoPunto pos=((Aplicacion) itemView.getContext().getApplicationContext()).posicionActual;
            if (pos.equals(GeoPunto.SIN_POSICION) ||
                    lugar.getPosicion().equals(GeoPunto.SIN_POSICION)) {
                distancia.setText("... Km");
            } else {
                int d=(int) pos.distancia(lugar.getPosicion());
                if (d < 2000) distancia.setText(d + " m");
                else          distancia.setText(d / 1000 + " Km");
            }

        }
    }

    // Creamos el ViewHolder con la vista de un elemento sin personalizar
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflamos la vista desde el xml
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.elemento_lista, parent, false);
        v.setOnClickListener(onClickListener);
        return new ViewHolder(v);
    }

    // Usando como base el ViewHolder y lo personalizamos
    @Override
    public void onBindViewHolder(ViewHolder holder, int posicion) {
        Lugar lugar = lugares.get(idEnPosicion(posicion));
        holder.personaliza(lugar);
    }
    // Indicamos el número de elementos de la lista
    @Override public int getItemCount() {
        return ids.size();
    }
    public void setOnItemClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}