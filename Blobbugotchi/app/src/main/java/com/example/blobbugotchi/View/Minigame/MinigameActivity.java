package com.example.blobbugotchi.View.Minigame;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import com.example.blobbugotchi.Controller.GameController;
import com.example.blobbugotchi.Controller.MinigameFragment;
import com.example.blobbugotchi.Controller.ScoreManager;
import com.example.blobbugotchi.Controller.SoundManager;
import com.example.blobbugotchi.R;
import com.example.blobbugotchi.View.BaseActivity;

public class MinigameActivity extends BaseActivity implements MinigameFragment.GameListener {

    public static final String EXTRA_LEVEL = "level";
    public static final String EXTRA_SCORE  = "accumulated_score";

    private MinigameFragment gameView;
    private ScoreManager scoreManager;
    private boolean endSoundPlayed  = false;
    // Evita que onDestroy restaure la BGM principal al hacer transición entre niveles
    private boolean levelCompleted  = false;
    private int currentLevel;
    private int accumulatedScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemUI();

        currentLevel = getIntent().getIntExtra(EXTRA_LEVEL, 1);
        accumulatedScore = getIntent().getIntExtra(EXTRA_SCORE,  0);
        scoreManager = new ScoreManager(this);

        SoundManager.getInstance(this).playMinigameBGM();

        gameView = new MinigameFragment(this, this, currentLevel, accumulatedScore);
        setContentView(gameView);
    }

    @Override
    public void onLevelComplete(int nextLevel, int score) {
        levelCompleted = true; // la música debe continuar, no resetear

        gameView.postDelayed(() -> {
            Intent intent = new Intent(this, MinigameActivity.class);
            intent.putExtra(EXTRA_LEVEL, nextLevel);
            intent.putExtra(EXTRA_SCORE,  score);
            startActivity(intent);
            finish();
        }, 2000);
    }

    @Override
    public void onGameOver(int score) {
        runOnUiThread(() -> playEndSound(R.raw.music_lose, score));
    }

    @Override
    public void onWin(int score) {
        runOnUiThread(() -> playEndSound(R.raw.music_win, score));
    }

    @Override
    public void onPlaySound(int soundType) {
        SoundManager sm = SoundManager.getInstance(this);
        switch (soundType) {
            case 0: sm.playSfxCoin(); break;
            case 1: sm.playSfxHit();  break;
            case 2: sm.playSfxStar(); break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
        hideSystemUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Solo restauramos la BGM principal si el usuario salió manualmente
        if (!endSoundPlayed && !levelCompleted) {
            SoundManager.getInstance(this).playMainBGM();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void playEndSound(int resId, int score) {
        if (endSoundPlayed) {
            return;
        }

        endSoundPlayed = true;
        scoreManager.submitScore(score);
        GameController.getInstance(this).updateMaxScore(score); // sincroniza memoria
        GameController.getInstance(this).rewardMinigame(); // saveProgress no machaca el récord
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
}