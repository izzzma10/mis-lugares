package com.example.mislugares.presentacion;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;
import com.example.mislugares.modelo.GeoPunto;

/**
 * Pantalla 1 (lista de lugares) como Fragment. MainActivity solo lo aloja.
 */
public class ListaLugaresFragment extends Fragment implements View.OnClickListener {

    private static final int[] RADIOS_KM = {5, 10, 15, 20};

    private RecyclerView recyclerView;
    private AdaptadorLugares adaptador;
    private AdaptadorPuntosReferencia adaptadorCerca;
    private TextView textoVacio;
    private SwipeRefreshLayout refrescarLista;
    private IndicadorPixelDrawable indicadorRefresco;
    private EditText campoFiltroTexto;
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
        configurarBuscadorLocal(view);

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
        configurarRefrescar(view);
    }

    /** Tirar hacia abajo: recarga los lugares guardados y pide una ubicación nueva. */
    private void configurarRefrescar(View view) {
        refrescarLista = view.findViewById(R.id.refrescar_lista);
        reemplazarCirculoPorPixelArt();
        refrescarLista.setOnRefreshListener(() -> {
            indicadorRefresco.start();
            adaptador.actualizar();
            if (orden != 1) {
                actualizarVacioLista();
            }
            ((MainActivity) requireActivity()).actualizarUbicacion();
            // La ubicación llega sola a refrescar(), pero si no llega (sin GPS, sin permiso)
            // el círculo no debe quedar girando para siempre: tope de 3 segundos.
            refrescarLista.postDelayed(this::terminarRefresco, 3000);
        });
    }

    /** SwipeRefreshLayout crea su indicador como PRIMER hijo (antes del contenido). Es un
     *  ImageView: le cambiamos el dibujo redondo por el nuestro y le dejamos un fondo transparente, sin sombra. */
    private void reemplazarCirculoPorPixelArt() {
        indicadorRefresco = new IndicadorPixelDrawable(requireContext());
        View primero = refrescarLista.getChildAt(0);
        if (primero instanceof ImageView) {
            ImageView circulo = (ImageView) primero;
            circulo.setImageDrawable(indicadorRefresco);
            // La librería hace getBackground().setAlpha(...): no puede ser null, pero sí transparente.
            circulo.setBackground(new ColorDrawable(Color.TRANSPARENT));
            circulo.setElevation(0f);
        }
    }

    private void terminarRefresco() {
        refrescarLista.setRefreshing(false);
        indicadorRefresco.stop();
    }

    /** Buscador por nombre: cada tecla le pasa el texto al adaptador, que reconstruye la lista. */
    private void configurarBuscadorLocal(View view) {
        campoFiltroTexto = view.findViewById(R.id.campo_filtro_texto);
        campoFiltroTexto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                adaptador.setTexto(s.toString());
                if (adaptadorCerca != null) {
                    adaptadorCerca.setTexto(s.toString());
                }
                if (orden == 1) {
                    actualizarVacioCerca();
                } else {
                    actualizarVacioLista();
                }
            }
        });
    }

    /** Mensaje de "sin resultados" del modo lista normal (el de CERCA lo maneja mostrarPuntosCercanos). */
    private void actualizarVacioLista() {
        boolean sinResultados = adaptador.getItemCount() == 0
                && campoFiltroTexto.getText().length() > 0;
        textoVacio.setText(R.string.sin_resultados_lista);
        textoVacio.setVisibility(sinResultados ? View.VISIBLE : View.GONE);
    }

    /** Vacío de CERCA. Tres casos, en este orden: todavía sin GPS, sin coincidencias
     *  con el texto escrito, o simplemente ningún punto dentro del radio. */
    private void actualizarVacioCerca() {
        Aplicacion aplicacion = (Aplicacion) requireActivity().getApplication();
        boolean sinGps = aplicacion.posicionActual.equals(GeoPunto.SIN_POSICION);
        boolean hayTexto = campoFiltroTexto.getText().length() > 0;

        int mensaje;
        if (sinGps) {
            mensaje = R.string.buscando_ubicacion;
        } else if (hayTexto) {
            mensaje = R.string.sin_resultados_lista;
        } else {
            mensaje = R.string.sin_puntos_cerca;
        }
        textoVacio.setText(mensaje);
        textoVacio.setVisibility(adaptadorCerca.getItemCount() == 0 ? View.VISIBLE : View.GONE);
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
            campoFiltroTexto.setHint(R.string.hint_buscar_cerca);
            fabAgregar.setVisibility(View.GONE);
            fabRadio.setVisibility(View.VISIBLE);
            mostrarPuntosCercanos();
        } else {
            chipAz.setVisibility(View.VISIBLE);
            campoFiltroTexto.setHint(R.string.hint_buscar_lista);
            fabAgregar.setVisibility(View.VISIBLE);
            fabRadio.setVisibility(View.GONE);
            recyclerView.setAdapter(adaptador);
            adaptador.setFiltro(soloFav, orden);
            actualizarVacioLista();
        }
    }

    /** CERCA: en vez de ordenar los lugares guardados, muestra los puntos de
     *  referencia fijos (assets/puntos_referencia_mty.json) dentro del radio
     *  elegido con fab_radio, ampliándolo si no encuentra ninguno. */
    private void mostrarPuntosCercanos() {
        if (adaptadorCerca == null) {
            adaptadorCerca = new AdaptadorPuntosReferencia(requireContext());
            adaptadorCerca.setOnPuntoClickListener(punto -> {
                Intent intent = new Intent(requireContext(), MapaLugarActivity.class);
                intent.putExtra(MapaLugarActivity.EXTRA_ID_PUNTO_REFERENCIA, punto.id());
                startActivity(intent);
            });
        }
        recyclerView.setAdapter(adaptadorCerca);
        adaptadorCerca.setTexto(campoFiltroTexto.getText().toString());

        Aplicacion aplicacion = (Aplicacion) requireActivity().getApplication();
        int radioKm = leerRadioKmActual();
        adaptadorCerca.actualizar(aplicacion.posicionActual, radioKm * 1000.0);
        actualizarVacioCerca();
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
        if (orden != 1) {
            actualizarVacioLista();
        }
        SnackPixel.mostrarPendiente(requireActivity());
    }

    /** La llama MainActivity cuando llega una ubicación nueva: repinta los KM. */
    public void refrescar() {
        if (adaptador == null) {
            return;
        }
        terminarRefresco();
        Aplicacion aplicacion = (Aplicacion) requireActivity().getApplication();
        adaptador.setPosicion(aplicacion.posicionActual);
        if (orden == 1) {
            mostrarPuntosCercanos();
        } else {
            adaptador.actualizar();
            actualizarVacioLista();
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
