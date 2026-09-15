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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.example.mislugares.R;

/**
 * Hoja inferior para elegir el idioma de la app. Mismo comportamiento que
 * HojaTipoFragment: ancla al fondo, scrim al 70%, sin animación de rebote.
 */
public class HojaIdiomaFragment extends DialogFragment {

    public interface Listener {
        void onIdiomaElegido(String codigo);
    }

    private Listener listener;

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View raiz = inflater.inflate(R.layout.hoja_idioma, container, false);
        raiz.findViewById(R.id.hoja_cerrar).setOnClickListener(x -> dismiss());

        LinearLayout lista = raiz.findViewById(R.id.lista_idiomas);
        String actual = Idiomas.actual();

        for (String codigo : Idiomas.CODIGOS) {
            View fila = inflater.inflate(R.layout.fila_tipo, lista, false);
            ImageView icono = fila.findViewById(R.id.fila_pin);
            TextView nombre = fila.findViewById(R.id.fila_nombre);
            View check = fila.findViewById(R.id.fila_check);

            icono.setImageResource(R.drawable.ic_globo_pixel);
            nombre.setText(Idiomas.nombreRes(codigo));

            boolean activo = codigo.equals(actual);
            fila.findViewById(R.id.fila_contenido).setSelected(activo);
            nombre.setTextColor(ContextCompat.getColor(requireContext(),
                    activo ? R.color.pixel_fondo : R.color.pixel_tinta));
            check.setVisibility(activo ? View.VISIBLE : View.INVISIBLE);

            fila.setOnClickListener(x -> {
                if (listener != null) {
                    listener.onIdiomaElegido(codigo);
                }
                dismiss();
            });
            lista.addView(fila);
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
