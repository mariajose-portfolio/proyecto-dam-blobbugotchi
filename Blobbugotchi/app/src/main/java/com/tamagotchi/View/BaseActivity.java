package com.tamagotchi.View;

import androidx.appcompat.app.AppCompatActivity;

import com.tamagotchi.Controller.GameController;
import com.tamagotchi.Controller.SoundManager;
import com.tamagotchi.DataLayer.DatabaseHelper;
import com.tamagotchi.Model.Config.Configuration;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onPause() {
        super.onPause();
        // Guardar el estado del Blobbu automáticamente al ir a segundo plano
        GameController.getInstance(this).saveProgress();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Guardar configuración de volumen al salir de cualquier pantalla
        saveConfiguration();
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