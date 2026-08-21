package com.example.mislugares.presentacion;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mislugares.R;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button btnMisLugares = findViewById(R.id.btn_mis_lugares);
        Button btnAgregarLugar = findViewById(R.id.btn_agregar_lugar);

        btnMisLugares.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

        btnAgregarLugar.setOnClickListener(v ->
                startActivity(new Intent(this, EdicionLugarActivity.class)));
    }
}
