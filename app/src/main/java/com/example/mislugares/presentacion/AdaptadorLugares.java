package com.example.mislugares.presentacion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;
import com.example.mislugares.casos_uso.BorrarLugar;
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
    protected final BorrarLugar casoUsoBorrar;
    protected Map<Integer, Lugar> lugares;
    protected List<Integer> ids;
    protected View.OnClickListener onClickListener;

    // Estado de la fila de filtros
    protected boolean soloFavoritos = false;
    protected int orden = 0;   // 0 normal · 1 cercania · 2 A-Z asc · 3 A-Z desc
    protected GeoPunto posicion = GeoPunto.SIN_POSICION;
    protected String textoBusqueda = "";   // ya normalizado (ver normalizar())

    public AdaptadorLugares(RepositorioLugares repositorio) {
        this.casoUsoListar = new ListarLugares(repositorio);
        this.casoUsoBorrar = new BorrarLugar(repositorio);
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

    /** Texto del buscador local. Vacío = sin filtro por nombre. */
    public void setTexto(String texto) {
        this.textoBusqueda = Texto.normalizar(texto);
        aplicarFiltro();
    }

    private void aplicarFiltro() {
        ids = new ArrayList<>();
        for (Map.Entry<Integer, Lugar> entrada : lugares.entrySet()) {
            Lugar lugar = entrada.getValue();
            boolean pasaFavorito = !soloFavoritos || lugar.isFavorito();
            boolean pasaTexto = textoBusqueda.isEmpty()
                    || Texto.normalizar(lugar.getNombre()).contains(textoBusqueda);
            if (pasaFavorito && pasaTexto) {
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

    /** Deslizar para borrar, paso 1: oculta la fila (con su animación) y la
     *  saca también de "lugares", no solo de "ids" — así, si mientras tanto el
     *  buscador o un refresco reconstruyen la lista visible, este lugar no
     *  puede "resucitar" solo por accidente. Todavía NO toca el repositorio;
     *  eso pasa recién en confirmarBorrado(). Devuelve el lugar oculto, para
     *  poder armar el aviso de "deshacer" y, si se deshace, restaurarlo. */
    public Lugar ocultarTemporalmente(int posicionAdapter) {
        int id = ids.remove(posicionAdapter);
        Lugar lugar = lugares.remove(id);
        notifyItemRemoved(posicionAdapter);
        return lugar;
    }

    /** Deshace ocultarTemporalmente: vuelve a mostrar ese lugar en (aprox.) la
     *  misma posición. Math.min por si la lista se achicó mientras tanto. */
    public void restaurar(int id, Lugar lugar, int posicionAdapter) {
        lugares.put(id, lugar);
        int posicionValida = Math.min(posicionAdapter, ids.size());
        ids.add(posicionValida, id);
        notifyItemInserted(posicionValida);
    }

    /** Deslizar para borrar, paso 2 (cuando ya no se puede deshacer): recién
     *  ahora se borra de verdad en el repositorio. */
    public void confirmarBorrado(int id) {
        casoUsoBorrar.borrar(id);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nombre, direccion, distancia;
        public ImageView foto, favorito;
        public LinearLayout tiraValoracion;
        public TextView botonIr;

        public ViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.nombre);
            direccion = itemView.findViewById(R.id.direccion);
            distancia = itemView.findViewById(R.id.distancia);
            foto = itemView.findViewById(R.id.foto);
            favorito = itemView.findViewById(R.id.favorito);
            tiraValoracion = itemView.findViewById(R.id.tira_valoracion);
            botonIr = itemView.findViewById(R.id.boton_ir_fila);
        }

        public void personaliza(Lugar lugar) {
            nombre.setText(lugar.getNombre());
            direccion.setText(lugar.getDireccion());

            foto.setImageResource(Tipos.pin(lugar.getTipo()));

            favorito.setVisibility(lugar.isFavorito() ? View.VISIBLE : View.GONE);

            Estrellas.pintarValoracion(tiraValoracion, lugar.getValoracion(), true);

            // Atajo "cómo llegar": solo si el lugar tiene coordenadas. Al ser un botón
            // con su propio listener, el toque no llega a la fila (no abre el detalle).
            boolean tieneCoordenadas = !lugar.getPosicion().equals(GeoPunto.SIN_POSICION);
            botonIr.setVisibility(tieneCoordenadas ? View.VISIBLE : View.GONE);
            botonIr.setOnClickListener(v -> Navegacion.irA(itemView.getContext(),
                    lugar.getPosicion().getLatitud(), lugar.getPosicion().getLongitud()));

            GeoPunto pos = ((Aplicacion) itemView.getContext()
                    .getApplicationContext()).posicionActual;
            if (pos.equals(GeoPunto.SIN_POSICION)
                    || lugar.getPosicion().equals(GeoPunto.SIN_POSICION)) {
                distancia.setText(itemView.getContext().getString(R.string.km_desconocido));
            } else {
                float km = (float) pos.distancia(lugar.getPosicion()) / 1000f;
                distancia.setText(String.format(java.util.Locale.US,
                        itemView.getContext().getString(R.string.km_formato), km));
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
