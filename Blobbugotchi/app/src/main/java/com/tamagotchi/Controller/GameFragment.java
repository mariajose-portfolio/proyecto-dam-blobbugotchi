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

        gameController.getEvolutionManager().setOnEvolutionListener(newType -> {
            currentPhase = newType;
            gameController.dbHelper.unlockCreature(newType.ordinal());
            requireActivity().runOnUiThread(() -> playBlobbuAnimation());
        });

        gameController.dbHelper.ensureGalleryRows();

        this.blobbu = gameController.dbHelper.getBlobbu();
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
        Blobbu savedBlobbu = gameController.getBlobbu();

        if (savedBlobbu != null && savedBlobbu.getEvolutionType() != EvolutionType.EGG) {
            blobbu = savedBlobbu;
            currentPhase = blobbu.getEvolutionType(); // ← usa el tipo real, no hardcodeado
            render();
        }
        else {
            // Primera vez o viene del huevo — empezar desde el huevo
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

            Blobbu newBlobbu = Blobbu.createBaby();
            gameController.dbHelper.insertBlobbu(newBlobbu); // inserta fresco
            newBlobbu.evolve(EvolutionType.BABY);
            gameController.dbHelper.updateBlobbu(newBlobbu); // guarda BABY
            gameController.initBlobbu(newBlobbu); // actualiza el singleton
            gameController.dbHelper.unlockCreature(EvolutionType.BABY.ordinal());

            blobbu = newBlobbu;
            currentPhase = EvolutionType.BABY;
            isSleeping = false;
            gameController.saveProgress();
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
            playBlobbuAnimation();
            renderBlobbu(blobbu);
        }
    }

    // RENDER — solo responsabilidad de actualizar la UI
    private void render() {
        if (currentPhase == EvolutionType.EGG) {
            playEggIdleAnimation();
        }
        else {
            playBlobbuAnimation();
            renderBlobbu(blobbu);
        }
    }

    private void renderBlobbu(Blobbu blobbu) {
        if (happyBar != null) happyBar.setProgress(blobbu.getHappyLvl());
        if (hungerBar != null) hungerBar.setHunger(scaleToIcons(blobbu.getHungryLvl()));
        if (sleepBar != null) sleepBar.setSleep(scaleToIcons(blobbu.getSleepinessLvl()));
        if (currentPhase != EvolutionType.EGG) playBlobbuAnimation();
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
    private void playBlobbuAnimation() {
        if (blobbu == null) return;
        int animRes = BlobbuAnimator.getAnimationRes(currentPhase, blobbu.getCurrentState());
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
        playBlobbuAnimation();
    }

    public void stopPomodoroAnimation() {
        if (blobbu == null || currentPhase == EvolutionType.EGG) return;
        blobbu.updateState();
        renderBlobbu(blobbu);
    }
}