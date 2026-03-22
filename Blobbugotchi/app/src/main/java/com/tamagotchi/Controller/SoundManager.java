package com.tamagotchi.Controller;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

import com.tamagotchi.R;

public class SoundManager {

    private static SoundManager instance;

    // --- BGM (música de fondo, solo una a la vez) ---
    private MediaPlayer mediaPlayer;

    // --- SFX (efectos de sonido, pueden solaparse) ---
    private SoundPool soundPool;
    private int sfxAccept;
    private int sfxCoin;
    private int sfxDeath;
    private int sfxHatch;
    private int sfxJump;

    // --- Volúmenes (0.0 - 1.0) ---
    private float masterVolume = 1f;
    private float bgmVolume    = 1f;
    private float sfxVolume    = 1f;

    private Context context;

    // =====================================================
    // SINGLETON
    // =====================================================
    private SoundManager(Context context) {
        this.context = context.getApplicationContext();
        initSoundPool();
    }

    public static SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context.getApplicationContext());
        }
        return instance;
    }

    // =====================================================
    // INICIALIZACIÓN
    // =====================================================

    /**
     * Carga todos los SFX en el SoundPool para reproducción inmediata
     */
    private void initSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(attributes)
                .build();

        sfxAccept = soundPool.load(context, R.raw.sfx_accept, 1);
        sfxCoin   = soundPool.load(context, R.raw.sfx_coin,   1);
        sfxDeath  = soundPool.load(context, R.raw.sfx_death,  1);
        sfxHatch  = soundPool.load(context, R.raw.sfx_hatch,  1);
        sfxJump   = soundPool.load(context, R.raw.sfx_jump,   1);
    }

    // =====================================================
    // BGM — música de fondo
    // =====================================================

    /**
     * Reproduce una pista de música de fondo en bucle.
     * Si ya hay una en reproducción la detiene primero.
     */
    private void playBGM(int resId) {
        stopBGM();
        mediaPlayer = MediaPlayer.create(context, resId);
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(true);
            float vol = masterVolume * bgmVolume;
            mediaPlayer.setVolume(vol, vol);
            mediaPlayer.start();
        }
    }

    /** Detiene y libera el MediaPlayer actual */
    public void stopBGM() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    /** Pausa la música de fondo (ej: al ir a segundo plano) */
    public void pauseBGM() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    /** Reanuda la música de fondo */
    public void resumeBGM() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    // =====================================================
    // BGM por pantalla — métodos de conveniencia
    // =====================================================

    /** Música de la pantalla principal */
    public void playMainBGM() {
        playBGM(R.raw.bgm_main);
    }

    /** Música del pomodoro */
    public void playPomodoroBGM() {
        playBGM(R.raw.bgm_pomodoro);
    }

    /** Música del minijuego */
    public void playMinigameBGM() {
        playBGM(R.raw.bgm_minigame);
    }

    /** Música nocturna cuando el blobbu duerme */
    public void playSleepBGM() {
        playBGM(R.raw.bgm_sleep);
    }

    // =====================================================
    // SFX — efectos de sonido
    // =====================================================

    /** Sonido de confirmación / aceptar */
    public void playSfxAccept() {
        playSfx(sfxAccept);
    }

    /** Sonido de moneda (ej: recompensa en minijuego) */
    public void playSfxCoin() {
        playSfx(sfxCoin);
    }

    /** Sonido de muerte / game over */
    public void playSfxDeath() {
        playSfx(sfxDeath);
    }

    /** Sonido de eclosión del huevo */
    public void playSfxHatch() {
        playSfx(sfxHatch);
    }

    /** Sonido de salto (minijuego) */
    public void playSfxJump() {
        playSfx(sfxJump);
    }

    private void playSfx(int soundId) {
        float vol = masterVolume * sfxVolume;
        soundPool.play(soundId, vol, vol, 1, 0, 1f);
    }

    // =====================================================
    // VOLÚMENES
    // =====================================================

    /**
     * Actualiza el volumen maestro y lo aplica inmediatamente a la BGM
     * @param volume valor entre 0.0 y 1.0
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = volume;
        applyBGMVolume();
    }

    /**
     * Actualiza el volumen de música y lo aplica inmediatamente
     * @param volume valor entre 0.0 y 1.0
     */
    public void setBgmVolume(float volume) {
        this.bgmVolume = volume;
        applyBGMVolume();
    }

    /**
     * Actualiza el volumen de efectos de sonido
     * @param volume valor entre 0.0 y 1.0
     */
    public void setSfxVolume(float volume) {
        this.sfxVolume = volume;
    }

    /** Aplica el volumen actual al MediaPlayer si está activo */
    private void applyBGMVolume() {
        if (mediaPlayer != null) {
            float vol = masterVolume * bgmVolume;
            mediaPlayer.setVolume(vol, vol);
        }
    }

    // Getters para cargar los valores guardados en ConfigEntity
    public float getMasterVolume() { return masterVolume; }
    public float getBgmVolume()    { return bgmVolume; }
    public float getSfxVolume()    { return sfxVolume; }

    // =====================================================
    // LIMPIEZA
    // =====================================================

    /**
     * Libera todos los recursos de audio.
     * Llamar desde onDestroy() de la Activity principal.
     */
    public void release() {
        stopBGM();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        instance = null;
    }
}