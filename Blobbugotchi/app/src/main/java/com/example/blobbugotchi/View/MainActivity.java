package com.example.blobbugotchi.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.blobbugotchi.Controller.GameFragment;
import com.example.blobbugotchi.Controller.SoundManager;
import com.example.blobbugotchi.DataLayer.DatabaseHelper;
import com.example.blobbugotchi.Model.Blobbu.BlobbuAction;
import com.example.blobbugotchi.Model.Config.Configuration;
import com.example.blobbugotchi.R;

public class MainActivity extends BaseActivity {
    private LinearLayout statsBars;
    private ImageButton btn_menu, btn_actions, btn_cancel;
    private GameFragment gameFragment;
    private PopupWindow actionsPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen);

        if (savedInstanceState == null) {
            gameFragment = new GameFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.gameFragmentContainer, gameFragment)
                    .commit();
        }
        else {
            gameFragment = (GameFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.gameFragmentContainer);
        }

        // Barras de estadísticas del blobbu
        statsBars = findViewById(R.id.statsBars);

        // Botones de la carcasa del tamagotchi
        btn_menu = findViewById(R.id.btn_menu);
        btn_actions = findViewById(R.id.btn_actions);
        btn_cancel = findViewById(R.id.btn_cancel);

        btn_menu.setOnClickListener(v -> showStats());
        btn_actions.setOnClickListener(v -> toggleActionsPopup());
        btn_cancel.setOnClickListener(v -> hideStats());

        // Botón de la configuración
        ImageButton config = findViewById(R.id.btn_config);
        config.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConfigurationActivity.class);
            startActivity(intent);
        });
    }

    private void showStats() {
        if (gameFragment != null && gameFragment.isBlobbuAlive()) {
            statsBars.setVisibility(View.VISIBLE);
        }
    }

    private void toggleActionsPopup() {
        if (gameFragment == null || !gameFragment.isBlobbuAlive()) return;

        if (actionsPopup != null && actionsPopup.isShowing()) {
            actionsPopup.dismiss();
            return;
        }

        // Inflar el layout del popup
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_actions, null);

        actionsPopup = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true // focusable — se cierra al tocar fuera
        );
        actionsPopup.setElevation(8f);

        // Botones del popup
        popupView.findViewById(R.id.btn_feed).setOnClickListener(v -> {
            gameFragment.performAction(BlobbuAction.FEED);
            actionsPopup.dismiss();
        });

        popupView.findViewById(R.id.btn_play).setOnClickListener(v -> {
            gameFragment.performAction(BlobbuAction.PLAY);
            actionsPopup.dismiss();
        });

        popupView.findViewById(R.id.btn_pomodoro).setOnClickListener(v ->{
            gameFragment.performAction(BlobbuAction.POMODORO);
            Intent intent = new Intent(MainActivity.this, PomodoroActivity.class);
            startActivity(intent);
        });

        // Alterna entre dormir y despertar
        ImageButton btnSleep = popupView.findViewById(R.id.btn_sleep);

        if (gameFragment.isSleeping()) {
            btnSleep.setImageResource(R.drawable.ic_moon_empty); // Luna vacía = despertar
        }
        else {
            btnSleep.setImageResource(R.drawable.ic_moon_filled); // Luna llena = dormir
        }

        btnSleep.setOnClickListener(v -> {
            gameFragment.performAction(BlobbuAction.SLEEP);
            actionsPopup.dismiss();
        });

        // Mostrar centrado encima de la carcasa
        FrameLayout deviceContainer = findViewById(R.id.deviceContainer);
        actionsPopup.showAtLocation(deviceContainer, Gravity.CENTER, 0, -200);
    }

    private void hideStats() {
        statsBars.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Cargar configuración guardada y aplicarla al SoundManager
        Configuration config = DatabaseHelper.getInstance(this).loadConfiguration();
        SoundManager sm = SoundManager.getInstance(this);
        sm.setMasterVolume(config.getMasterVolume());
        sm.setBgmVolume(config.getBgmVolume());
        sm.setSfxVolume(config.getSeVolume());

        // Siempre arrancar desde cero para garantizar que suena
        sm.playMainBGM();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SoundManager.getInstance(this).pauseBGM();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundManager.getInstance(this).release();
    }
}