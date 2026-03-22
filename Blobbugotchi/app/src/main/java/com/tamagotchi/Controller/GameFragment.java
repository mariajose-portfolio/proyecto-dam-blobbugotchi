package com.tamagotchi.Controller;

import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.tamagotchi.Model.Blobbu.Blobbu;
import com.tamagotchi.Model.Blobbu.BlobbuAction;
import com.tamagotchi.Model.Blobbu.BlobbuState;
import com.tamagotchi.Model.Blobbu.Egg;
import com.tamagotchi.Model.Blobbu.EvolutionType;
import com.tamagotchi.Model.Stats.HungerIconsView;
import com.tamagotchi.Model.Stats.HappyProgressBar;
import com.tamagotchi.Model.Stats.SleepIconsView;
import com.tamagotchi.R;

public class GameFragment extends Fragment {
    private EvolutionType currentPhase;
    private Egg egg;
    private Blobbu blobbu;
    private ImageView petImage;
    private ImageView screenBackground;
    private HappyProgressBar happyBar;
    private HungerIconsView hungerBar;
    private SleepIconsView sleepBar;

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isSleeping = false; // Controla si está durmiendo
    private StatsDegradationManager degradationManager;

    // Color del filtro nocturno: 0x55 = opacidad ~33%, 2B4BFF = azul
    // Ajusta los dos primeros dígitos para más (77) o menos (33) intensidad
    private static final int NIGHT_FILTER_COLOR = 0x552B4BFF;

    public GameFragment() {
        super(R.layout.fragment_game);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        petImage = view.findViewById(R.id.petImage);
        screenBackground = requireActivity().findViewById(R.id.screenImage);

        happyBar = requireActivity().findViewById(R.id.happyBar);
        hungerBar = requireActivity().findViewById(R.id.hungerBar);
        sleepBar = requireActivity().findViewById(R.id.sleepBar);

        // Obtiene el manager desde GameController y le asigna el callback de UI
        degradationManager = GameController.getInstance(requireContext()).getDegradationManager();
        degradationManager.setOnTickCallback(() -> {
            if (blobbu == null) return;

            // Si el pomodoro está activo, mantener animación de pomodoro
            GameController gc = GameController.getInstance(requireContext());
            if (gc.isPomodoroStarted()) {
                blobbu.setState(BlobbuState.POMODORO);
            }

            if (!isSleeping) renderBlobbu(blobbu);
        });

        startGame();
    }

    // CICLO DE VIDA — pausar/reanudar el timer
    @Override
    public void onResume() {
        super.onResume();
        degradationManager.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        degradationManager.stop();
    }

    private void startGame() {
        egg = new Egg();
        blobbu = null;
        currentPhase = EvolutionType.EGG;
        isSleeping = false;

        playEggIdleAnimation();
        startEggTimer();
    }

    private void startEggTimer() {
        handler.postDelayed(this::hatchEgg, 30_000);
    }

    private void hatchEgg() {
        playEggHatchAnimation();
        handler.postDelayed(() -> {
            egg.hatch();
            blobbu = Blobbu.createBaby();
            currentPhase = EvolutionType.BABY;
            isSleeping = false;
            render();
        }, 2000);
    }

    public void performAction(BlobbuAction action) {
        if (blobbu == null || currentPhase == EvolutionType.EGG) return;

        switch (action) {
            case FEED:
                performFeed();
                break;
            case PLAY:
                blobbu.play(20);
                renderBlobbu(blobbu);
                break;
            case SLEEP:
                performSleep();
                break;
            case POMODORO:
                blobbu.passTime();
                renderBlobbu(blobbu);
                break;
        }
    }

    /**
     * Alimentar: muestra animación de comer, espera a que termine
     * y luego vuelve al estado normal
     */
    private void performFeed() {
        blobbu.eat(20);
        blobbu.setState(BlobbuState.EATING);

        // Carga la animación de comer
        petImage.setImageResource(R.drawable.anim_baby_eating);
        AnimationDrawable eatAnim = (AnimationDrawable) petImage.getDrawable();

        // Calcula la duración total sumando todos los frames del XML
        int totalDuration = 0;

        for (int i = 0; i < eatAnim.getNumberOfFrames(); i++) {
            totalDuration += eatAnim.getDuration(i);
        }

        // Reproduce la animación de comer
        eatAnim.start();

        // Espera exactamente lo que dura la animación según el XML
        handler.postDelayed(() -> {
            if (blobbu != null) {
                blobbu.updateState();
                renderBlobbu(blobbu);
            }
        }, totalDuration);
    }

    /**
     * Dormir: si está despierto lo pone a dormir cambiando el fondo
     * y aplicando un filtro azul a la carcasa y al blobbu.
     * Si ya está durmiendo, lo despierta y restaura todo.
     */
    private void performSleep() {
        if (isSleeping) {
            // ---- DESPERTAR ----
            isSleeping = false;

            // Se reproduce la música normal y se quita la de dormir
            SoundManager.getInstance(requireContext()).playMainBGM();

            // Restaurar fondo de día
            screenBackground.setImageResource(R.drawable.screen_background);

            // Quitar filtro nocturno del blobbu
            petImage.clearColorFilter();

            blobbu.updateState();
            renderBlobbu(blobbu);
        }
        else {
            // ---- DORMIR ----
            isSleeping = true;
            blobbu.sleep(20);
            blobbu.setState(BlobbuState.SLEEPING);
            SoundManager.getInstance(requireContext()).playSleepBGM(); // Música de dormir

            // Cambiar fondo a versión nocturna
            screenBackground.setImageResource(R.drawable.screen_background_night);

            // Aplicar filtro azul al blobbu para efecto nocturno
            petImage.setColorFilter(NIGHT_FILTER_COLOR, PorterDuff.Mode.SRC_ATOP);

            playBabyAnimation();
            renderBlobbu(blobbu);
        }
    }

    private void render() {
        if (currentPhase == EvolutionType.EGG) {
            playEggIdleAnimation();
        } else {
            playBabyAnimation();
            renderBlobbu(blobbu);
        }
    }

    private void renderBlobbu(Blobbu blobbu) {
        if (happyBar  != null) happyBar.setProgress(blobbu.getHappyLvl());
        if (hungerBar != null) hungerBar.setHunger(scaleToIcons(blobbu.getHungryLvl()));
        if (sleepBar  != null) sleepBar.setSleep(scaleToIcons(blobbu.getSleepinessLvl()));

        // Actualiza la animación según el estado actual
        if (currentPhase != EvolutionType.EGG) {
            playBabyAnimation();
        }
    }

    /**
     * Convierte un valor 0-100 a una escala de 0-6 para los iconos
     * Ej: 100 → 6, 50 → 3, 0 → 0
     */
    private int scaleToIcons(int value) {
        return Math.round(value * 6f / 100f);
    }

    private void playEggIdleAnimation() {
        petImage.setImageResource(R.drawable.anim_egg_idle);
        startAnimation();
    }

    private void playEggHatchAnimation() {
        // Reproduce el sonido de nacer del huevo
        SoundManager.getInstance(requireContext()).playSfxHatch();
        petImage.setImageResource(R.drawable.anim_egg_hatching);
        startAnimation();
    }

    /**
     * Selecciona y reproduce la animación del bebé según su estado actual
     */
    private void playBabyAnimation() {
        int animRes;
        BlobbuState state = blobbu.getCurrentState();

        switch (state) {
            case HAPPY: animRes = R.drawable.anim_baby_happy; break;
            case SAD: animRes = R.drawable.anim_baby_sad; break;
            case HUNGRY: animRes = R.drawable.anim_baby_hungry; break;
            case EATING: animRes = R.drawable.anim_baby_eating; break;
            case SLEEPING: animRes = R.drawable.anim_baby_sleep; break;
            case POMODORO: animRes = R.drawable.anim_baby_reading; break;
            default: animRes = R.drawable.anim_baby_neutral;
        }

        petImage.setImageResource(animRes);
        startAnimation();
    }

    private void startAnimation() {
        AnimationDrawable animation = (AnimationDrawable) petImage.getDrawable();
        animation.start();
    }

    public boolean isBlobbuAlive() {
        return currentPhase != EvolutionType.EGG && blobbu != null;
    }

    public boolean isSleeping() {
        return isSleeping;
    }

    public void startPomodoroAnimation() {
        if (blobbu == null || currentPhase == EvolutionType.EGG) return;
        blobbu.setState(BlobbuState.POMODORO);
        playBabyAnimation();
    }

    public void stopPomodoroAnimation() {
        if (blobbu == null || currentPhase == EvolutionType.EGG) return;
        blobbu.updateState();
        renderBlobbu(blobbu);
    }
}