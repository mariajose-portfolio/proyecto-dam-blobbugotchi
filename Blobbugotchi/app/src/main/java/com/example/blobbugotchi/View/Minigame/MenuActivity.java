package com.example.blobbugotchi.View.Minigame;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.blobbugotchi.Controller.ScoreManager;
import com.example.blobbugotchi.Controller.SoundManager;
import com.example.blobbugotchi.R;
import com.example.blobbugotchi.View.BaseActivity;
import com.example.blobbugotchi.View.ConfigurationActivity;

public class MenuActivity extends BaseActivity {

    private boolean launchingGame = false;
    private ScoreManager scoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_minigame);
        hideSystemUI();

        scoreManager = new ScoreManager(this);

        SoundManager.getInstance(this).playMinigameBGM();
        updateHighScoreUI();

        AppCompatButton btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(v -> {
            launchingGame = true;
            Intent intent = new Intent(this, MinigameActivity.class);
            intent.putExtra(MinigameActivity.EXTRA_LEVEL, 1);
            intent.putExtra(MinigameActivity.EXTRA_SCORE, 0);
            startActivity(intent);
        });

        AppCompatButton btnSettings = findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v ->
                    startActivity(new Intent(this, ConfigurationActivity.class)));
        }

        AppCompatButton btnExit = findViewById(R.id.btnExit);
        if (btnExit != null) {
            btnExit.setOnClickListener(v -> {
                // Restaura la música principal antes de salir
                SoundManager.getInstance(this).playMainBGM();
                finish();
            });
        }
    }

    private void updateHighScoreUI() {
        TextView tvHighScore = findViewById(R.id.tvHighScore);
        if (tvHighScore != null) {
            int record = scoreManager.getHighScore();
            tvHighScore.setText(record > 0 ? "🏆 Récord: " + record : "🏆 Sin récord aún");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        launchingGame = false;
        hideSystemUI();
        SoundManager.getInstance(this).playMinigameBGM();
        updateHighScoreUI();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!launchingGame && !isChangingConfigurations()) {
            SoundManager.getInstance(this).playMainBGM();
        }
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().getDecorView().post(() -> {
                getWindow().setDecorFitsSystemWindows(false);
                WindowInsetsController c = getWindow().getInsetsController();

                if (c != null) {
                    c.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                    c.setSystemBarsBehavior(
                            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                }
            });
        }
        else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUI();
    }
}