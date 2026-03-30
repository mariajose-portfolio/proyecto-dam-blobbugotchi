package com.tamagotchi.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.tamagotchi.Controller.SoundManager;
import com.tamagotchi.DataLayer.DatabaseHelper;
import com.tamagotchi.Model.Config.Configuration;
import com.tamagotchi.R;
import com.tamagotchi.Controller.GameFragment;
import android.widget.PopupWindow;
import com.tamagotchi.Model.Blobbu.BlobbuAction;

public class MainActivity extends BaseActivity {
    private LinearLayout statsBars, menuButtons;
    private ImageButton btn_menu, btn_accept, btn_cancel;
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
        statsBars   = findViewById(R.id.statsBars);
        menuButtons = findViewById(R.id.menuButtons);

        // Botones de la carcasa del tamagotchi
        btn_menu   = findViewById(R.id.btn_menu);
        btn_accept = findViewById(R.id.btn_accept);
        btn_cancel = findViewById(R.id.btn_cancel);

        btn_menu.setOnClickListener(v -> showStats());
        btn_accept.setOnClickListener(v -> toggleActionsPopup());
        btn_cancel.setOnClickListener(v -> hideStats());

        // Botón del pomodoro
        ImageButton pomodoro = findViewById(R.id.btn_pomodoro);
        pomodoro.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PomodoroActivity.class);
            startActivity(intent);
        });

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
            menuButtons.setVisibility(View.VISIBLE);
        }
        else{
            menuButtons.setVisibility(View.VISIBLE);
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
        statsBars.setVisibility(View.GONE);
        menuButtons.setVisibility(View.GONE);
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