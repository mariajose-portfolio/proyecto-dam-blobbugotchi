package com.tamagotchi.Controller;

import android.content.Context;

import com.tamagotchi.DataLayer.DatabaseHelper;
import com.tamagotchi.Model.Blobbu.Blobbu;
import com.tamagotchi.Model.Blobbu.BlobbuState;

public class GameController {

    private static GameController instance;
    private Blobbu blobbu;
    private StatsDegradationManager degradationManager;
    private boolean isPomodoroStarted = false;
    private DatabaseHelper dbHelper;

    private GameController(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);

        // Intentar cargar el Blobbu desde BD
        blobbu = dbHelper.getBlobbu();

        // Si no existe aún, crear uno nuevo y guardarlo
        if (blobbu == null) {
            blobbu = Blobbu.createBaby();
            dbHelper.insertBlobbu(blobbu);
        }

        degradationManager = new StatsDegradationManager(blobbu);
    }

    public static GameController getInstance(Context context) {
        if (instance == null) {
            instance = new GameController(context.getApplicationContext());
        }
        return instance;
    }

    // ---GESTIÓN DE LAS ACCIONES DEL USUARIO ---

    /**
     * Alimenta al Blobbu y guarda el progreso
     */
    public void feedBlobbu() {
        if (blobbu == null) return;
        blobbu.eat(20);
        saveProgress();
    }

    /**
     * Hace jugar al Blobbu y guarda el progreso
     */
    public void playWithBlobbu() {
        if (blobbu == null) return;
        blobbu.play(20);
        saveProgress();
    }

    /**
     * Pone a dormir o despierta al Blobbu y guarda el progreso
     */
    public void sleepBlobbu() {
        if (blobbu == null) return;
        blobbu.sleep(20);
        saveProgress();
    }

    // --- GESTIÓN DEL POMODORO ---

    /**
     * Activa o desactiva el estado pomodoro del Blobbu
     */
    public void setPomodoroState(boolean active) {
        this.isPomodoroStarted = active;
        degradationManager.setPomodoroActive(active);
        if (blobbu == null) return;
        if (active) {
            blobbu.setState(BlobbuState.POMODORO);
        } else {
            blobbu.updateState();
        }
    }

    public boolean isPomodoroStarted() {
        return isPomodoroStarted;
    }

    /**
     * Se llama al arrancar el timer — registra el inicio del pomodoro
     */
    public void startPomodoro(int minutes) {
        setPomodoroState(true);
        // TODO: guardar en BD el inicio del pomodoro cuando esté listo
    }

    /**
     * Recompensa al Blobbu al completar el pomodoro y guarda el progreso
     */
    public void completePomodoro(double hoursSpent) {
        if (blobbu == null) return;
        blobbu.play(5);                        // Sube felicidad por completar
        blobbu.addTimeTogether(hoursSpent);    // Acumula tiempo juntos
        setPomodoroState(false);
        saveProgress();
        dbHelper.savePomodoroTime(hoursSpent);
    }

    /**
     * Registra tiempo parcial al cancelar el pomodoro
     */
    public void cancelPomodoro(double hoursSpent) {
        if (blobbu == null) return;
        blobbu.addTimeTogether(hoursSpent);
        setPomodoroState(false);
        saveProgress();
        dbHelper.savePomodoroTime(hoursSpent);
    }

    // --- GESTIÓN DE LA EVOLUCIÓN ---

    /**
     * Comprueba si el Blobbu debe evolucionar
     * TODO: delegar en EvolutionManager cuando esté implementado
     */
    public void checkEvolution() {
        if (blobbu != null) blobbu.passTime();
    }

    // --- GESTIÓN DE LA PERSISTENCIA ---

    /**
     * Guarda el estado actual del Blobbu en la BD
     */
    public void saveProgress() {
        if (blobbu != null) dbHelper.updateBlobbu(blobbu);
    }

    /**
     * Actualiza las stats del Blobbu al terminar el pomodoro
     * @deprecated usar completePomodoro() en su lugar
     */
    public void updateStats() {
        if (blobbu != null) {
            blobbu.play(5);
            blobbu.passTime();
            saveProgress();
        }
    }

    public StatsDegradationManager getDegradationManager() {
        return degradationManager;
    }

    public Blobbu getBlobbu() {
        return blobbu;
    }
}