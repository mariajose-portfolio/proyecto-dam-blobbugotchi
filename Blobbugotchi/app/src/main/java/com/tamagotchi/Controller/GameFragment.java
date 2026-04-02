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

    // MODELO
    private EvolutionType currentPhase;
    private Egg egg;
    private Blobbu blobbu;

    // UI
    private ImageView petImage;
    private ImageView screenBackground;
    private HappyProgressBar happyBar;
    private HungerIconsView hungerBar;
    private SleepIconsView sleepBar;

    // ESTADO
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isSleeping = false;
    private StatsDegradationManager degradationManager;
    private GameController gameController;

    private static final int NIGHT_FILTER_COLOR = 0x552B4BFF;

    public GameFragment() {
        super(R.layout.fragment_game);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gameController = GameController.getInstance(requireContext());
        degradationManager = gameController.getDegradationManager();

        petImage = view.findViewById(R.id.petImage);
        screenBackground = requireActivity().findViewById(R.id.screenImage);
        happyBar = requireActivity().findViewById(R.id.happyBar);
        hungerBar = requireActivity().findViewById(R.id.hungerBar);
        sleepBar = requireActivity().findViewById(R.id.sleepBar);

        // Callback del tick — solo actualiza la UI
        degradationManager.setOnTickCallback(() -> {
            if (blobbu == null) return;

            if (gameController.isPomodoroStarted()) {
                blobbu.setState(BlobbuState.POMODORO);
            }

            if (!isSleeping) renderBlobbu(blobbu);
        });

        Blobbu blobbu = gameController.dbHelper.getBlobbu();
        if (blobbu != null && blobbu.getEvolutionType() != EvolutionType.EGG) {
            // El blobbu ya ha pasado del huevo, nos aseguramos de que esté desbloqueado
            gameController.dbHelper.unlockCreature(blobbu.getEvolutionType().ordinal());
        }

        startGame();

        if (gameController.checkEvolution()) {
            // Reproducir animación de evolución
        }
    }

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

    // INICIO DEL JUEGO
    private void startGame() {
        isSleeping = false;

        // Comprobar si ya hay un Blobbu guardado en la BD
        Blobbu savedBlobbu = gameController.getBlobbu();

        if (savedBlobbu != null) {
            // En caso de habernacido el Blobbu en la sesión anterior
            // restaura el estado directamente sin pasar por el huevo
            blobbu = savedBlobbu;

            // TODO: usar el evolutionType real cuando haya más fases
            currentPhase = EvolutionType.BABY;
            render();
        }
        else {
            // Primera vez — empezar desde el huevo
            egg = new Egg();
            blobbu = null;
            currentPhase = EvolutionType.EGG;
            playEggIdleAnimation();
            startEggTimer();
        }
    }

    // FASE HUEVO, nace al pasar 20 segundos
    private void startEggTimer() {
        handler.postDelayed(this::hatchEgg, 20_000);
    }

    private void hatchEgg() {
        playEggHatchAnimation();

        handler.postDelayed(() -> {
            egg.hatch();

            // El GameController ya crea y guarda el Blobbu en BD
            blobbu = gameController.getBlobbu();
            currentPhase = EvolutionType.BABY;
            isSleeping = false;

            // Guardar que el Blobbu ya nació
            gameController.saveProgress();

            // Cuando el huevo eclosiona:
            blobbu.evolve(EvolutionType.BABY);

            // Desbloquea en la galería al Blobbu
            gameController.dbHelper.unlockCreature(EvolutionType.BABY.ordinal());
            gameController.dbHelper.updateBlobbu(blobbu);

            render();
        }, 2000);
    }

    // ACCIONES DEL USUARIO — solo gestiona UI y audio
    public void performAction(BlobbuAction action) {
        if (blobbu == null || currentPhase == EvolutionType.EGG) return;

        switch (action) {
            case FEED:
                performFeedUI();
                gameController.feedBlobbu();
                break;
            case PLAY:
                gameController.playWithBlobbu();
                renderBlobbu(blobbu);
                break;
            case SLEEP:
                performSleepUI();
                break;
            case POMODORO:
                gameController.checkEvolution();
                renderBlobbu(blobbu);
                break;
        }
    }

    /**
     * UI de alimentar: muestra animación de comer y espera a que termine
     */
    private void performFeedUI() {
        blobbu.setState(BlobbuState.EATING);
        petImage.setImageResource(R.drawable.anim_baby_eating);
        AnimationDrawable eatAnim = (AnimationDrawable) petImage.getDrawable();

        // Calcula duración total desde el XML
        int totalDuration = 0;
        for (int i = 0; i < eatAnim.getNumberOfFrames(); i++) {
            totalDuration += eatAnim.getDuration(i);
        }
        eatAnim.start();

        // Al terminar la animación actualiza la UI con el estado real
        handler.postDelayed(() -> {
            if (blobbu != null) renderBlobbu(blobbu);
        }, totalDuration);
    }

    /**
     * UI de dormir: cambia fondo, aplica filtro nocturno y gestiona audio.
     * La lógica de stats la delega en GameController.
     */
    private void performSleepUI() {
        if (isSleeping) {
            // ---- DESPERTAR ----
            isSleeping = false;
            SoundManager.getInstance(requireContext()).playMainBGM();
            screenBackground.setImageResource(R.drawable.screen_background);
            petImage.clearColorFilter();
            gameController.sleepBlobbu();
            renderBlobbu(blobbu);
        }
        else {
            // ---- DORMIR ----
            isSleeping = true;
            gameController.sleepBlobbu();
            blobbu.setState(BlobbuState.SLEEPING);
            SoundManager.getInstance(requireContext()).playSleepBGM();
            screenBackground.setImageResource(R.drawable.screen_background_night);
            petImage.setColorFilter(NIGHT_FILTER_COLOR, PorterDuff.Mode.SRC_ATOP);
            playBabyAnimation();
            renderBlobbu(blobbu);
        }
    }

    // RENDER — solo responsabilidad de actualizar la UI
    private void render() {
        if (currentPhase == EvolutionType.EGG) {
            playEggIdleAnimation();
        } else {
            playBabyAnimation();
            renderBlobbu(blobbu);
        }
    }

    private void renderBlobbu(Blobbu blobbu) {
        if (happyBar != null) happyBar.setProgress(blobbu.getHappyLvl());
        if (hungerBar != null) hungerBar.setHunger(scaleToIcons(blobbu.getHungryLvl()));
        if (sleepBar != null) sleepBar.setSleep(scaleToIcons(blobbu.getSleepinessLvl()));
        if (currentPhase != EvolutionType.EGG) playBabyAnimation();
    }

    private int scaleToIcons(int value) {
        return Math.round(value * 6f / 100f);
    }

    // ANIMACIONES
    private void playEggIdleAnimation() {
        petImage.setImageResource(R.drawable.anim_egg_idle);
        startAnimation();
    }

    private void playEggHatchAnimation() {
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