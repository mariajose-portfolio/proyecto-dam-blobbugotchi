package com.example.blobbugotchi.Controller;

import android.content.Context;
import android.content.SharedPreferences;

public class ScoreManager {
    private static final String PREFS_NAME = "fishy_prefs";
    private static final String KEY_RECORD = "high_score";
    private final SharedPreferences prefs;

    public ScoreManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getHighScore() {
        return prefs.getInt(KEY_RECORD, 0);
    }

    /**
     * Compara la puntuación con el record actual.
     * Si es mayor, lo guarda y devuelve true.
     * Si no, devuelve false.
     */
    public boolean submitScore(int score) {
        if (score > getHighScore()) {
            prefs.edit().putInt(KEY_RECORD, score).apply();

            return true; // Nuevo record
        }

        return false;
    }
}
