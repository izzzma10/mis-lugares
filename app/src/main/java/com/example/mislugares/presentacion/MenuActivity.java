package com.example.mislugares.presentacion;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mislugares.R;

/**
 * Splash: identidad de marca y entrada a la app. Salta a la lista a los 1.5s
 * o al tocar la pantalla. Es la activity de arranque (exported="true").
 */
public class MenuActivity extends AppCompatActivity {

    private static final long DEMORA_SPLASH = 1500L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean navegando = false;
    private View cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        cursor = findViewById(R.id.cursor);
        parpadearCursor();

        findViewById(R.id.raiz_splash).setOnClickListener(v -> irALista());
        handler.postDelayed(this::irALista, DEMORA_SPLASH);
    }

    /** Prende/apaga el cursor cada 500ms mientras la pantalla está viva. */
    private void parpadearCursor() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cursor.setVisibility(
                        cursor.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
                handler.postDelayed(this, 500);
            }
        }, 500);
    }

    private void irALista() {
        if (navegando) return;
        navegando = true;
        handler.removeCallbacksAndMessages(null);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
