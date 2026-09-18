package com.example.mislugares.presentacion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;

/**
 * Pantalla 1 (lista de lugares) como Fragment. MainActivity solo lo aloja.
 */
public class ListaLugaresFragment extends Fragment implements View.OnClickListener {

    private static final int[] RADIOS_KM = {5, 10, 15, 20};

    private RecyclerView recyclerView;
    private AdaptadorLugares adaptador;
    private AdaptadorPuntosReferencia adaptadorCerca;
    private View textoVacio;
    private View fabAgregar;
    private TextView fabRadio;

    // Estado de la fila de filtros
    private boolean soloFav = false;
    private int orden = 0;   // 0 normal · 1 CERCA (puntos de referencia) · 2 A-Z asc · 3 A-Z desc
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

        textoVacio = view.findViewById(R.id.texto_vacio);

        // fab_radio y fab_agregar viven en activity_lista.xml, no en fragment_lista.xml,
        // y ocupan el mismo lugar: solo uno de los dos está visible a la vez.
        fabAgregar = requireActivity().findViewById(R.id.fab_agregar);
        fabRadio = requireActivity().findViewById(R.id.fab_radio);
        fabRadio.setOnClickListener(v -> ciclarRadio());

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

    /** Pinta los chips segun el estado y muestra la lista que corresponda. */
    private void refrescarChips() {
        chipTodos.setSelected(!soloFav && orden == 0);
        chipFav.setSelected(soloFav);
        chipCerca.setSelected(orden == 1);
        chipAz.setSelected(orden == 2 || orden == 3);

        if (orden == 1) {
            chipAz.setVisibility(View.GONE);
            fabAgregar.setVisibility(View.GONE);
            fabRadio.setVisibility(View.VISIBLE);
            mostrarPuntosCercanos();
        } else {
            chipAz.setVisibility(View.VISIBLE);
            fabAgregar.setVisibility(View.VISIBLE);
            fabRadio.setVisibility(View.GONE);
            recyclerView.setAdapter(adaptador);
            textoVacio.setVisibility(View.GONE);
            adaptador.setFiltro(soloFav, orden);
        }
    }

    /** CERCA: en vez de ordenar los lugares guardados, muestra los puntos de
     *  referencia fijos (assets/puntos_referencia_mty.json) dentro del radio
     *  elegido con fab_radio, ampliándolo si no encuentra ninguno. */
    private void mostrarPuntosCercanos() {
        if (adaptadorCerca == null) {
            adaptadorCerca = new AdaptadorPuntosReferencia(requireContext());
        }
        recyclerView.setAdapter(adaptadorCerca);

        Aplicacion aplicacion = (Aplicacion) requireActivity().getApplication();
        int radioKm = leerRadioKmActual();
        adaptadorCerca.actualizar(aplicacion.posicionActual, radioKm * 1000.0);
        textoVacio.setVisibility(adaptadorCerca.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        fabRadio.setText(radioKm + " KM");
    }

    /** Pasa fab_radio al siguiente radio preestablecido (5→10→15→20→5...) y refresca. */
    private void ciclarRadio() {
        int actual = leerRadioKmActual();
        int indice = 0;
        for (int i = 0; i < RADIOS_KM.length; i++) {
            if (RADIOS_KM[i] == actual) {
                indice = i;
                break;
            }
        }
        int siguiente = RADIOS_KM[(indice + 1) % RADIOS_KM.length];
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit().putString("radioKm", String.valueOf(siguiente)).apply();
        mostrarPuntosCercanos();
    }

    /** Lee el radio de búsqueda actual (clave "radioKm", 5 km por defecto). */
    private int leerRadioKmActual() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        try {
            return Integer.parseInt(prefs.getString("radioKm", "5"));
        } catch (NumberFormatException e) {
            return 5;
        }
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
        Aplicacion aplicacion = (Aplicacion) requireActivity().getApplication();
        adaptador.setPosicion(aplicacion.posicionActual);
        if (orden == 1) {
            mostrarPuntosCercanos();
        } else {
            adaptador.actualizar();
        }
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
