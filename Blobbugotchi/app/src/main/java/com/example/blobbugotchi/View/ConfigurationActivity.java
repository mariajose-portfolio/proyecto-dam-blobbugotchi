package com.example.blobbugotchi.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.example.blobbugotchi.Controller.SoundManager;
import com.example.blobbugotchi.DataLayer.DatabaseHelper;
import com.example.blobbugotchi.Model.Config.Configuration;
import com.example.blobbugotchi.R;

public class ConfigurationActivity extends BaseActivity {

    private SeekBar seekMaster, seekMusic, seekSfx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        // Botón X para salir de la vista
        ImageButton close = findViewById(R.id.btn_close);
        close.setOnClickListener(v -> finish());

        Button gallery = findViewById(R.id.btn_gallery);
        gallery.setOnClickListener(v -> {
            Intent intent = new Intent(ConfigurationActivity.this, GalleryActivity.class);
            startActivity(intent);
        });

        seekMaster = findViewById(R.id.seekMasterVolume);
        seekMusic = findViewById(R.id.seekMusicVolume);
        seekSfx = findViewById(R.id.seekSfxVolume);

        // Cargar valores guardados en los seekbars
        Configuration config = DatabaseHelper.getInstance(this).loadConfiguration();
        seekMaster.setProgress((int) (config.getMasterVolume() * 100));
        seekMusic.setProgress((int) (config.getBgmVolume() * 100));
        seekSfx.setProgress((int) (config.getSeVolume() * 100));

        seekMaster.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SoundManager.getInstance(ConfigurationActivity.this)
                        .setMasterVolume(progress / 100f);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveConfiguration(); // guardar al soltar
            }
        });

        seekMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SoundManager.getInstance(ConfigurationActivity.this)
                        .setBgmVolume(progress / 100f);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveConfiguration();
            }
        });

        seekSfx.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SoundManager.getInstance(ConfigurationActivity.this)
                        .setSfxVolume(progress / 100f);
            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveConfiguration();
            }
        });
    }

    private void saveConfiguration() {
        SoundManager sm = SoundManager.getInstance(this);
        Configuration config = new Configuration(
                sm.getBgmVolume(),
                sm.getBgmVolume(),
                sm.getBgmVolume(),
                sm.getSfxVolume(),
                sm.getMasterVolume()
        );

        DatabaseHelper.getInstance(this).saveConfiguration(config);
    }
}