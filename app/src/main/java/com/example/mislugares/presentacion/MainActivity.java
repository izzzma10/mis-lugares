package com.example.mislugares.presentacion;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mislugares.Aplicacion;
import com.example.mislugares.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RecyclerView recyclerView;
    private AdaptadorLugares adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        Aplicacion aplicacion = (Aplicacion) getApplication();
        adaptador = new AdaptadorLugares(aplicacion.repositorio);
        adaptador.setOnItemClickListener(this);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adaptador);
    }

    @Override
    public void onClick(View v) {
        int posicion = recyclerView.getChildAdapterPosition(v);
        int id = adaptador.idEnPosicion(posicion);
        Intent intent = new Intent(this, VistaLugarActivity.class);
        intent.putExtra(VistaLugarActivity.EXTRA_ID, id);
        startActivity(intent);
    }
}
