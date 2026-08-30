package com.example.mislugares.presentacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;
import com.example.mislugares.modelo.TipoLugar;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantalla 1 (lista de lugares), reescrita como Fragment en vez de Activity
 * (Anexo B del libro, "Fragments") — mismo comportamiento que antes tenía
 * MainActivity, solo cambia cómo está construida por dentro. MainActivity
 * ahora solo aloja este Fragment (ver activity_lista.xml).
 */
public class ListaLugaresFragment extends Fragment implements View.OnClickListener {

    private RecyclerView recyclerView;
    private AdaptadorLugares adaptador;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Un Fragment no recibe onCreateOptionsMenu()/onOptionsItemSelected()
        // automáticamente como una Activity — hay que pedirlo explícitamente.
        setHasOptionsMenu(true);
    }

    // Un Fragment no tiene setContentView(): en su lugar, infla su propio
    // layout acá y lo devuelve como la vista del Fragment.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lista, container, false);
    }

    // Se llama justo después de onCreateView(), ya con la vista lista — acá
    // es donde un Fragment hace lo que una Activity hacía en onCreate()
    // después de setContentView(): buscar vistas y configurarlas.
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Aplicacion aplicacion = (Aplicacion) requireActivity().getApplication();
        adaptador = new AdaptadorLugares(aplicacion.repositorio);
        adaptador.setOnItemClickListener(this);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adaptador);

        configurarFiltro(view);
    }

    /** Arma el Spinner de arriba de la lista, con "Todos" + cada TipoLugar,
     *  y filtra el RecyclerView cuando el usuario elige una opción. */
    private void configurarFiltro(View view) {
        Spinner spinnerFiltro = view.findViewById(R.id.spinner_filtro);

        List<String> opciones = new ArrayList<>();
        opciones.add("Todos");
        for (TipoLugar tipo : TipoLugar.values()) {
            opciones.add(tipo.name());
        }

        ArrayAdapter<String> adapterFiltro = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, opciones);
        adapterFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltro.setAdapter(adapterFiltro);

        spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int posicion, long id) {
                if (posicion == 0) {
                    adaptador.filtrarPorTipo(null);
                } else {
                    adaptador.filtrarPorTipo(TipoLugar.values()[posicion - 1]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hace falta hacer nada acá — siempre queda algo seleccionado.
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresca la lista: si venimos de crear, editar o eliminar un lugar
        // en otra pantalla, aquí es donde se ve reflejado.
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

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_lista, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.accion_preferencias) {
            startActivity(new Intent(requireContext(), PreferenciasActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
