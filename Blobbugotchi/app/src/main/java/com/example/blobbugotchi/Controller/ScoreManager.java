package com.example.blobbugotchi.Controller;

import android.content.Context;

import com.example.blobbugotchi.DataLayer.DatabaseHelper;
import com.example.blobbugotchi.Model.Blobbu.Blobbu;

public class ScoreManager {

    private final DatabaseHelper db;

    public ScoreManager(Context context) {
        db = DatabaseHelper.getInstance(context);
    }

    public int getHighScore() {
        Blobbu blobbu = db.getBlobbu();
        return blobbu != null ? blobbu.getMaxScore() : 0;
    }

    /**
     * Compara la puntuación con el récord actual.
     * Si es mayor, lo persiste en el Blobbu y devuelve true.
     * Si no, devuelve false.
     */
    public boolean submitScore(int score) {
        if (score > getHighScore()) {
            db.saveMaxScore(score);
            return true; // Nuevo récord
        }
        return false;
    }
}