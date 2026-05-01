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

    private static final long TICK_INTERVAL = 5_000;
    private static final int CARE_MISTAKE_TICKS = 24; // 24 ticks × 5s = 2 minutos
    private static final int GRACE_PERIOD_TICKS = 36; // 3 minutos de gracia tras nacer
    private int graceTicks = 0; // Ticks restantes de período de gracia
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Blobbu blobbu;
    private boolean running = false;
    private boolean isPomodoroActive = false;
    private boolean isSleeping = false;
    private Runnable onTickCallback;

    // Contadores de ticks en estado crítico (stat = 0) por cada necesidad
    private int hungryZeroTicks = 0;
    private int sleepZeroTicks = 0;
    private int happyZeroTicks = 0;

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (blobbu != null && running) {
                if (isSleeping) {
                    blobbu.sleep(17);
                    // Al dormir, el sueño se recupera: reseteamos su contador
                    sleepZeroTicks = 0;
                }
                else if (!isPomodoroActive) {
                    statsDegradation();

                    if (graceTicks > 0) {
                        graceTicks--; // Consume un tick de gracia
                    }
                    else {
                        checkCareMistakes(); // Solo comprueba errores fuera del período de gracia
                    }
                }

                if (onTickCallback != null) {
                    onTickCallback.run();
                }
            }

            handler.postDelayed(this, TICK_INTERVAL);
        }
    };

    // --- Lógica de errores de cuidado ---

    /**
     * Comprueba si alguna stat lleva demasiado tiempo a 0.
     * Si supera el umbral de 2 minutos sin ser atendida, añade un error de cuidado.
     * Si la stat ya no está a 0, resetea su contador.
     */
    private void checkCareMistakes() {
        hungryZeroTicks = checkStat(blobbu.getHungryLvl(), hungryZeroTicks);
        sleepZeroTicks = checkStat(blobbu.getSleepinessLvl(), sleepZeroTicks);
        happyZeroTicks = checkStat(blobbu.getHappyLvl(), happyZeroTicks);
    }

    /** Activa el período de gracia tras la eclosión del huevo. */
    public void startGracePeriod() {
        graceTicks = GRACE_PERIOD_TICKS;
        resetCareMistakeCounters();
    }

    /**
     * Lógica para una stat individual.
     * @param statValue Valor actual de la stat (0–100)
     * @param tickCount Ticks acumulados a 0 para esta stat
     * @return El nuevo valor del contador de ticks
     */
    private int checkStat(int statValue, int tickCount) {
        if (statValue <= 0) {
            tickCount++;
            if (tickCount >= CARE_MISTAKE_TICKS) {
                blobbu.addCareMistake();
                return 0; // Resetea el contador tras registrar el error
            }
        }
        else {
            return 0; // La stat fue atendida: resetea el contador
        }
        return tickCount;
    }

    public StatsDegradationManager(Blobbu blobbu) {
        this.blobbu = blobbu;
    }

    public void statsDegradation() {
        blobbu.passTime();
    }

    public void setOnTickCallback(Runnable callback) {
        this.onTickCallback = callback;
    }

    public void setBlobbu(Blobbu blobbu) {
        this.blobbu = blobbu;
        // Al cambiar de Blobbu (evolución/eclosión), resetea todos los contadores
        resetCareMistakeCounters();
    }

    public void setPomodoroActive(boolean active) {
        this.isPomodoroActive = active;
    }

    public void setSleeping(boolean sleeping) {
        this.isSleeping = sleeping;
    }

    public void start() {
        if (!running) {
            running = true;
            handler.post(tickRunnable);
        }
    }

    public void pause() {
        running = false;
    }
    public void resume() {
        running = true;
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(tickRunnable);
    }

    /** Resetea todos los contadores de cuidado (tras evolución o eclosión) */
    public void resetCareMistakeCounters() {
        hungryZeroTicks = 0;
        sleepZeroTicks  = 0;
        happyZeroTicks  = 0;
    }
}