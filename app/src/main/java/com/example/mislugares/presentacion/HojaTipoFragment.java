package com.example.mislugares.presentacion;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.mislugares.R;
import com.example.mislugares.modelo.TipoLugar;

/**
 * Hoja inferior para elegir el tipo de lugar. Sube anclada al fondo, scrim al 70%,
 * sin animación de rebote. Se abre con la opción activa visible.
 */
public class HojaTipoFragment extends DialogFragment {

    public interface Listener {
        void onTipoElegido(TipoLugar tipo);
    }

    private static final String ARG_SEL = "seleccionado";

    private Listener listener;
    private TipoLugar seleccionado;

    static HojaTipoFragment nueva(TipoLugar seleccionado) {
        HojaTipoFragment fragmento = new HojaTipoFragment();
        Bundle args = new Bundle();
        if (seleccionado != null) {
            args.putString(ARG_SEL, seleccionado.name());
        }
        fragmento.setArguments(args);
        return fragmento;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            listener = (Listener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.HojaPixel);
        String s = getArguments() != null ? getArguments().getString(ARG_SEL) : null;
        seleccionado = s != null ? TipoLugar.valueOf(s) : null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View raiz = inflater.inflate(R.layout.hoja_tipo, container, false);
        raiz.findViewById(R.id.hoja_cerrar).setOnClickListener(x -> dismiss());

        LinearLayout lista = raiz.findViewById(R.id.lista_tipos);
        ScrollView scroll = raiz.findViewById(R.id.hoja_scroll);
        View filaActiva = null;

        for (TipoLugar tipo : TipoLugar.values()) {
            View fila = inflater.inflate(R.layout.fila_tipo, lista, false);
            ImageView pin = fila.findViewById(R.id.fila_pin);
            TextView nombre = fila.findViewById(R.id.fila_nombre);
            View check = fila.findViewById(R.id.fila_check);

            pin.setImageResource(Tipos.pin(tipo));
            nombre.setText(Tipos.nombre(requireContext(), tipo));

            boolean activa = tipo == seleccionado;
            fila.findViewById(R.id.fila_contenido).setSelected(activa);
            nombre.setTextColor(ContextCompat.getColor(requireContext(),
                    activa ? R.color.pixel_fondo : R.color.pixel_tinta));
            check.setVisibility(activa ? View.VISIBLE : View.INVISIBLE);
            if (activa) {
                filaActiva = fila;
            }

            final TipoLugar elegido = tipo;
            fila.setOnClickListener(x -> {
                if (listener != null) {
                    listener.onTipoElegido(elegido);
                }
                dismiss();
            });
            lista.addView(fila);
        }

        final View objetivo = filaActiva;
        if (objetivo != null) {
            scroll.post(() -> scroll.scrollTo(0, objetivo.getTop()));
        }
        return raiz;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null || getDialog().getWindow() == null) {
            return;
        }
        Window w = getDialog().getWindow();
        w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        w.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        w.setGravity(Gravity.BOTTOM);
        w.setDimAmount(0.70f);
    }
}
