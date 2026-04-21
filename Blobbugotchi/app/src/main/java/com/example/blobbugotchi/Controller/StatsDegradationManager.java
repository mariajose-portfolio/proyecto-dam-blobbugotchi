package com.example.blobbugotchi.Controller;

import android.os.Handler;
import android.os.Looper;

import com.example.blobbugotchi.Model.Blobbu.Blobbu;


/**
 * Gestiona el paso del tiempo sobre las stats del Blobbu.
 * Cada tick (5 segundos) degrada o recupera las stats según el estado actual,
 * y notifica al Fragment para que actualice la UI.
 *
 * Estados posibles:
 * - Durmiendo: solo sube el sueño, el resto de stats no se toca
 * - Pomodoro activo: las stats no se degradan
 * - Normal: todas las stats se degradan progresivamente
 */
public class StatsDegradationManager {

    private static final long TICK_INTERVAL = 5_000; // Intervalo entre ticks en ms

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Blobbu blobbu;
    private boolean running = false;
    private boolean isPomodoroActive = false;
    private boolean isSleeping = false;
    private Runnable onTickCallback; // Notifica al Fragment tras cada tick

    /**
     * Lógica ejecutada en cada tick.
     * Decide qué hacer con las stats según el estado actual del Blobbu
     * y programa el siguiente tick al terminar.
     */
    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (blobbu != null && running) {
                if (isSleeping) {
                    // Mientras duerme: solo recupera sueño (17 puntos ≈ 1 luna por tick)
                    blobbu.sleep(17);
                } else if (!isPomodoroActive) {
                    // Despierto y sin pomodoro: degradación normal de todas las stats
                    statsDegradation();
                }
                // En pomodoro activo: no se hace nada con las stats

                if (onTickCallback != null) onTickCallback.run();
            }

            // Programa el siguiente tick independientemente del estado
            handler.postDelayed(this, TICK_INTERVAL);
        }
    };

    /**
     * @param blobbu Blobbu sobre el que se aplicarán los cambios de stats
     */
    public StatsDegradationManager(Blobbu blobbu) {
        this.blobbu = blobbu;
    }

    // ─── Lógica de stats ─────────────────────────────────────────────────────

    /**
     * Degrada todas las stats del Blobbu con el paso del tiempo.
     * Delega en Blobbu.passTime() que reduce hambre, sueño y felicidad en 1.
     */
    public void statsDegradation() {
        blobbu.passTime();
    }

    // ─── Configuración ───────────────────────────────────────────────────────

    /**
     * Registra el callback que se ejecuta tras cada tick.
     * El Fragment lo usa para refrescar las barras de stats en la UI.
     */
    public void setOnTickCallback(Runnable callback) {
        this.onTickCallback = callback;
    }

    /**
     * Reemplaza el Blobbu activo. Necesario tras una evolución o eclosión
     * para que el manager opere sobre la instancia correcta.
     */
    public void setBlobbu(Blobbu blobbu) {
        this.blobbu = blobbu;
    }

    /**
     * Activa o desactiva el modo pomodoro.
     * Mientras está activo, las stats no se degradan.
     */
    public void setPomodoroActive(boolean active) {
        this.isPomodoroActive = active;
    }

    /**
     * Activa o desactiva el modo sueño.
     * Mientras duerme, solo se recupera el sueño y el resto de stats se congela.
     */
    public void setSleeping(boolean sleeping) {
        this.isSleeping = sleeping;
    }

    // ─── Control del timer ───────────────────────────────────────────────────

    /**
     * Inicia el ticker si no estaba ya en marcha.
     * Llamar desde onResume() del Fragment.
     */
    public void start() {
        if (!running) {
            running = true;
            handler.post(tickRunnable);
        }
    }

    /**
     * Pausa la degradación sin eliminar los callbacks pendientes.
     * El ticker sigue programado pero no ejecuta lógica de stats.
     */
    public void pause() {
        running = false;
    }

    /**
     * Reanuda la degradación tras una pausa.
     */
    public void resume() {
        running = true;
    }

    /**
     * Detiene el ticker completamente y elimina los callbacks pendientes.
     * Llamar desde onPause() del Fragment para evitar fugas de memoria.
     */
    public void stop() {
        running = false;
        handler.removeCallbacks(tickRunnable);
    }
}