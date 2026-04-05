package com.tamagotchi.Controller;

import android.os.Handler;
import android.os.Looper;

import com.tamagotchi.Model.Blobbu.Blobbu;


public class StatsDegradationManager {
    private static final long TICK_INTERVAL = 5_000; // 5 segundos por tick

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Blobbu blobbu;
    private boolean running = false;
    private boolean isPomodoroActive = false;
    private Runnable onTickCallback; // Callback para notificar al Fragment que actualice la UI

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (blobbu != null && running) {
                statsDegradation();

                if (!isPomodoroActive) {
                    statsDegradation();
                }

                if (onTickCallback != null) onTickCallback.run();
            }

            handler.postDelayed(this, TICK_INTERVAL);
        }
    };

    public StatsDegradationManager(Blobbu blobbu) {
        this.blobbu = blobbu;
    }

    /**
     * Degrada las stats del blobbu con el paso del tiempo
     */
    public void statsDegradation() {
        blobbu.passTime();
    }

    /**
     * Callback que se ejecuta tras cada tick para que
     * el Fragment pueda actualizar la UI
     */
    public void setOnTickCallback(Runnable callback) {
        this.onTickCallback = callback;
    }

    /**
     * Actualiza el blobbu activo, útil tras una evolución
     */
    public void setBlobbu(Blobbu blobbu) {
        this.blobbu = blobbu;
    }

    public void setPomodoroActive(boolean active) {
        this.isPomodoroActive = active;
    }

    // Arranca el timer de degradación
    public void start() {
        if (!running) {
            running = true;
            handler.post(tickRunnable);
        }
    }

    // Pausa la degradación
    public void pause() {
        running = false;
    }

    // Reanuda la degradación
    public void resume() {
        running = true;
    }

    // Para el timer completamente
    public void stop() {
        running = false;
        handler.removeCallbacks(tickRunnable);
    }
}
