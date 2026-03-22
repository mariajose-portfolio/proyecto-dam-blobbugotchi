package com.tamagotchi.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.tamagotchi.Controller.SoundManager;
import com.tamagotchi.R;

public class ConfigurationActivity extends AppCompatActivity {

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

        seekMaster.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SoundManager.getInstance(ConfigurationActivity.this)
                        .setMasterVolume(progress / 100f);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SoundManager.getInstance(ConfigurationActivity.this)
                        .setBgmVolume(progress / 100f);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekSfx.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SoundManager.getInstance(ConfigurationActivity.this)
                        .setSfxVolume(progress / 100f);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
}