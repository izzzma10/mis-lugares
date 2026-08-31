package com.example.mislugares.presentacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;

/**
 * Pantalla 1 (lista de lugares) como Fragment. MainActivity solo lo aloja.
 */
public class ListaLugaresFragment extends Fragment implements View.OnClickListener {

    private RecyclerView recyclerView;
    private AdaptadorLugares adaptador;

    // Estado de la fila de filtros
    private boolean soloFav = false;
    private int orden = 0;   // 0 normal · 1 cercania · 2 A-Z asc · 3 A-Z desc
    private TextView chipTodos, chipFav, chipCerca, chipAz;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lista, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Aplicacion aplicacion = (Aplicacion) requireActivity().getApplication();
        adaptador = new AdaptadorLugares(aplicacion.repositorio);
        adaptador.setOnItemClickListener(this);
        adaptador.setPosicion(aplicacion.posicionActual);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adaptador);

        configurarFiltro(view);
    }

    /** Fila de chips: TODOS / ★ FAV / CERCA / A-Z. */
    private void configurarFiltro(View view) {
        chipTodos = view.findViewById(R.id.chip_todos);
        chipFav = view.findViewById(R.id.chip_fav);
        chipCerca = view.findViewById(R.id.chip_cerca);
        chipAz = view.findViewById(R.id.chip_az);

        chipTodos.setOnClickListener(v -> { soloFav = false; orden = 0; refrescarChips(); });
        chipFav.setOnClickListener(v -> { soloFav = !soloFav; refrescarChips(); });
        chipCerca.setOnClickListener(v -> { orden = (orden == 1) ? 0 : 1; refrescarChips(); });
        chipAz.setOnClickListener(v -> {
            orden = (orden == 2) ? 3 : (orden == 3) ? 0 : 2;
            refrescarChips();
        });

        refrescarChips();
    }

    /** Pinta los chips segun el estado y se lo pasa al adaptador. */
    private void refrescarChips() {
        chipTodos.setSelected(!soloFav && orden == 0);
        chipFav.setSelected(soloFav);
        chipCerca.setSelected(orden == 1);
        chipAz.setSelected(orden == 2 || orden == 3);
        adaptador.setFiltro(soloFav, orden);
    }

    @Override
    public void onResume() {
        super.onResume();
        adaptador.actualizar();
        SnackPixel.mostrarPendiente(requireActivity());
    }

    /** La llama MainActivity cuando llega una ubicación nueva: repinta los KM. */
    public void refrescar() {
        if (adaptador == null) {
            return;
        }
        adaptador.setPosicion(
                ((Aplicacion) requireActivity().getApplication()).posicionActual);
        adaptador.actualizar();
    }

    @Override
    public void onClick(View v) {
        int posicion = recyclerView.getChildAdapterPosition(v);
        int id = adaptador.idEnPosicion(posicion);
        Intent intent = new Intent(requireContext(), VistaLugarActivity.class);
        intent.putExtra(VistaLugarActivity.EXTRA_ID, id);
        startActivity(intent);
    }
}
