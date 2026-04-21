package com.example.blobbugotchi.View;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.blobbugotchi.Controller.GameController;
import com.example.blobbugotchi.Controller.SoundManager;
import com.example.blobbugotchi.DataLayer.DatabaseHelper;
import com.example.blobbugotchi.Model.Config.Configuration;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableImmersiveMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Guardar el estado del Blobbu automáticamente al ir a segundo plano
        GameController.getInstance(this).saveProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Volver a aplicar al regresar desde otra app, ya que Android
        // puede restaurar las barras al volver al primer plano
        enableImmersiveMode();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Guardar configuración de volumen al salir de cualquier pantalla
        saveConfiguration();
    }

    /**
     * Activa el modo pantalla completa ocultando la barra de estado y navegación.
     * Al deslizar desde el borde reaparecen temporalmente y vuelven a ocultarse solas.
     */
    private void enableImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(
                getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.statusBars() |
                        WindowInsetsCompat.Type.navigationBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }


    /**
     * Guarda la configuración de audio actual en la BD
     */
    private void saveConfiguration() {
        SoundManager sm = SoundManager.getInstance(this);
        Configuration config = new Configuration(
                sm.getBgmVolume(),
                sm.getBgmVolume(), // bgsVolume — usa bgm por ahora
                sm.getBgmVolume(), // meVolume — usa bgm por ahora
                sm.getSfxVolume(),
                sm.getMasterVolume()
        );

        DatabaseHelper.getInstance(this).saveConfiguration(config);
    }
}