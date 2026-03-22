package com.tamagotchi.Controller;

import android.content.Context;

import com.tamagotchi.Model.Blobbu.Blobbu;
import com.tamagotchi.Model.Blobbu.BlobbuState;


public class GameController {

    private static GameController instance;
    private Blobbu blobbu;
    private StatsDegradationManager degradationManager;
    private boolean isPomodoroStarted = false;

    // Constructor privado para Singleton
    private GameController(Context context) {
        // TODO: cuando DatabaseHelper esté listo, cargar el Blobbu desde BD
        // Por ahora creamos un Blobbu por defecto para poder probar
        blobbu = Blobbu.createBaby();
        degradationManager = new StatsDegradationManager(blobbu);
    }

    public static GameController getInstance(Context context) {
        if (instance == null) {
            instance = new GameController(context.getApplicationContext());
        }
        return instance;
    }

    public StatsDegradationManager getDegradationManager() {
        return degradationManager;
    }

    // -------------------------------------------------------
    // Métodos del diagrama de clases
    // -------------------------------------------------------
    public void feedBlobbu() {
        if (blobbu != null) {
            blobbu.eat(10);
            checkEvolution();
        }
    }

    public void playWithBlobbu() {
        if (blobbu != null) {
            blobbu.play(10);
            checkEvolution();
        }
    }

    public void setPomodoroState(boolean studying) {
        this.isPomodoroStarted = studying;
        degradationManager.setPomodoroActive(studying); // Sincroniza con el manager
        if (blobbu != null) {
            if (studying) {
                blobbu.setState(BlobbuState.POMODORO);
            } else {
                blobbu.updateState();
            }
        }
    }

    public boolean isPomodoroStarted() {
        return isPomodoroStarted;
    }

    public void startPomodoro(int minutes) {
        // Se llama al arrancar el timer en PomodoroActivity
        // TODO: guardar en BD con DatabaseHelper cuando esté listo
        System.out.println("Pomodoro iniciado: " + minutes + " minutos");
    }

    public void checkEvolution() {
        // TODO: delegar en EvolutionManager cuando esté implementado
        if (blobbu != null) {
            blobbu.passTime();
        }
    }

    public void updateStats() {
        // Se llama al terminar el Pomodoro para recompensar al Blobbu
        if (blobbu != null) {
            blobbu.play(5); // El pomodoro completado sube felicidad
            blobbu.passTime();
            saveProgress();
        }
    }

    public void saveProgress() {
        // TODO: persistir en BD con DatabaseHelper cuando esté listo
        System.out.println("Progreso guardado (stub)");
    }

    // -------------------------------------------------------
    // Getter del Blobbu por si lo necesitas en la UI
    // -------------------------------------------------------
    public Blobbu getBlobbu() {
        return blobbu;
    }
}