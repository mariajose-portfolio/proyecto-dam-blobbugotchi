package com.example.blobbugotchi.Controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
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

import com.example.blobbugotchi.Model.Blobbu.Blobbu;
import com.example.blobbugotchi.Model.Blobbu.BlobbuAction;
import com.example.blobbugotchi.Model.Blobbu.BlobbuState;
import com.example.blobbugotchi.Model.Blobbu.Egg;
import com.example.blobbugotchi.Model.Blobbu.EvolutionType;
import com.example.blobbugotchi.Model.Stats.HungerIconsView;
import com.example.blobbugotchi.Model.Stats.HappyProgressBar;
import com.example.blobbugotchi.Model.Stats.SleepIconsView;
import com.example.blobbugotchi.R;


public class GameFragment extends Fragment {

    private EvolutionType currentPhase; // Fase evolutiva actual del Blobbu
    private Egg egg; // Huevo previo a la eclosión
    private Blobbu blobbu; // Criatura activa

    private ImageView petImage; // Imagen animada del Blobbu
    private ImageView screenBackground; // Fondo de la pantalla (día/noche)
    private HappyProgressBar happyBar; // Barra de felicidad
    private HungerIconsView hungerBar; // Iconos de hambre
    private SleepIconsView sleepBar; // Iconos de sueño
    private View evolutionFlash;

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isSleeping = false; // Si el Blobbu está durmiendo
    private StatsDegradationManager degradationManager;
    private GameController gameController;

    // Color del filtro nocturno aplicado al sprite al dormir
    private static final int NIGHT_FILTER_COLOR = 0x552B4BFF;

    public GameFragment() {
        super(R.layout.fragment_game);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        gameController = GameController.getInstance(requireContext());
        degradationManager = gameController.getDegradationManager();

        // Enlazar vistas
        petImage = view.findViewById(R.id.petImage);
        evolutionFlash = requireActivity().findViewById(R.id.evolutionFlash);
        screenBackground = requireActivity().findViewById(R.id.screenImage);
        happyBar = requireActivity().findViewById(R.id.happyBar);
        hungerBar = requireActivity().findViewById(R.id.hungerBar);
        sleepBar = requireActivity().findViewById(R.id.sleepBar);

        // Cada tick del degradationManager (cada 5s) actualiza la UI.
        // Si el Blobbu ha recuperado el sueño al máximo, se despierta solo.
        degradationManager.setOnTickCallback(() -> {
            if (blobbu == null) {
                return;
            }

            if (gameController.isPomodoroStarted()) {
                blobbu.setState(BlobbuState.POMODORO);
            }

            if (isSleeping && blobbu.getSleepinessLvl() >= 100) {
                performWakeUp(); // Se despierta automáticamente al llegar a 100
                return;
            }

            renderBlobbu(blobbu); // Solo actualiza barras, no interrumpe animaciones
        });

        // Al evolucionar, actualiza la fase y lanza la nueva animación
        gameController.getEvolutionManager().setOnEvolutionListener(newType -> {
            requireActivity().runOnUiThread(() -> {
                playPokemonEvolutionAnimation(
                        () -> {
                            // Swap: actualiza la fase y carga el nuevo sprite
                            currentPhase = newType;
                            gameController.unlockCreature(newType);
                            playBlobbuAnimation(); // Carga el nuevo AnimationDrawable
                        },
                        () -> {
                            // Al terminar: actualiza las barras de stats
                            renderBlobbu(blobbu);
                        }
                );

            });
        });

        gameController.ensureGalleryRows();

        // Carga el Blobbu existente y desbloquea su entrada en la galería
        this.blobbu = gameController.getBlobbu();
        if (blobbu != null && blobbu.getEvolutionType() != EvolutionType.EGG) {
            gameController.unlockCreature(blobbu.getEvolutionType());
        }

        startGame();

        if (gameController.checkEvolution()) {
            // TODO: reproducir animación de evolución
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        degradationManager.start(); // Reanuda la degradación al volver a la pantalla
    }

    @Override
    public void onPause() {
        super.onPause();
        degradationManager.stop(); // Pausa la degradación al salir de la pantalla
    }

    // --- Inicio del juego ---

    /**
     * Determina si hay una partida guardada para continuar o si hay que
     * empezar desde el huevo.
     */
    private void startGame() {
        isSleeping = false;
        Blobbu savedBlobbu = gameController.getBlobbu();
        degradationManager.setBlobbu(blobbu);

        if (savedBlobbu != null && savedBlobbu.getEvolutionType() != EvolutionType.EGG) {
            // Partida existente: cargar el Blobbu guardado
            blobbu = savedBlobbu;
            currentPhase = blobbu.getEvolutionType();
            render();
        }
        else {
            // Primera vez: mostrar el huevo y esperar a que eclosione
            egg = new Egg();
            blobbu = null;
            currentPhase = EvolutionType.EGG;
            playEggIdleAnimation();
            startEggTimer();
        }
    }

    // --- Fase huevo ---

    /** Inicia el temporizador de eclosión (20 segundos). */
    private void startEggTimer() {
        handler.postDelayed(this::hatchEgg, 20_000);
    }

    /**
     * Eclosión del huevo: reproduce la animación y después crea el Blobbu
     * bebé, guardándolo en la base de datos.
     */
    private void hatchEgg() {
        playEggHatchAnimation();

        handler.postDelayed(() -> {
            egg.hatch();

            Blobbu newBlobbu = Blobbu.createBaby();
            newBlobbu.evolve(EvolutionType.BABY);

            // Inserta en BD si es la primera vez, si no actualiza el registro existente
            if (gameController.loadBlobbuFromDb() == null) {
                gameController.insertBlobbu(newBlobbu);
            }
            else {
                gameController.updateBlobbu(newBlobbu);
            }

            // Actualiza la instancia en memoria y sincroniza el degradationManager
            gameController.initBlobbu(newBlobbu);

            // Da una tregua para saciar las necesidades antes de registrar un error de cuidado
            degradationManager.startGracePeriod();
            gameController.unlockCreature(EvolutionType.BABY);

            blobbu = newBlobbu;
            currentPhase = EvolutionType.BABY;
            isSleeping = false;
            playEvolutionAnimation(() -> render());
        }, 2000);
    }

    // --- Acciones del usuario ---

    /**
     * Punto de entrada para todas las acciones del usuario.
     * Gestiona las restricciones de estado antes de ejecutar cada acción.
     */
    public void performAction(BlobbuAction action) {
        if (blobbu == null || currentPhase == EvolutionType.EGG) return;

        switch (action) {
            case FEED:
                if (isSleeping) {
                    return; // No come mientras duerme
                }

                if (blobbu.getHungryLvl() >= 100) {
                    return; // No come si está lleno
                }

                performFeedUI();
                gameController.feedBlobbu();
                break;
            case PLAY:
                if (isSleeping) {
                    performWakeUp(); // Despierta antes de jugar
                }

                renderBlobbu(blobbu);
                break;
            case SLEEP:
                if (isSleeping) {
                    performSleepUI(); // Si duerme, lo despierta
                }
                else {
                    if (blobbu.getSleepinessLvl() >= 100) return; // No duerme si no tiene sueño
                    performSleepUI();
                }
                break;
            case POMODORO:
                if (isSleeping) {
                    performWakeUp();
                }

                // Sincroniza la fase por si ha evolucionado
                if (blobbu.getEvolutionType() != null) {
                    currentPhase = blobbu.getEvolutionType();
                }

                gameController.checkEvolution();
                renderBlobbu(blobbu);
                playBlobbuAnimation();
                break;
        }
    }

    // --- UI de acciones ---

    /**
     * Reproduce la animación de comer y, al terminar, vuelve a la animación
     * que corresponda según el estado actualizado del Blobbu.
     */
    private void performFeedUI() {
        blobbu.setState(BlobbuState.EATING);
        int animRes = BlobbuAnimator.getAnimationRes(currentPhase, BlobbuState.EATING);
        petImage.setImageResource(animRes);
        AnimationDrawable eatAnim = (AnimationDrawable) petImage.getDrawable();

        // Calcula la duración total de la animación de comer
        int totalDuration = 0;

        for (int i = 0; i < eatAnim.getNumberOfFrames(); i++) {
            totalDuration += eatAnim.getDuration(i);
        }
        eatAnim.start();

        // Al terminar la animación, recalcula el estado y vuelve a la animación normal
        handler.postDelayed(() -> {
            if (blobbu != null) {
                blobbu.updateState();
                renderBlobbu(blobbu);
                playBlobbuAnimation();
            }
        }, totalDuration);
    }

    /**
     * Activa el modo sueño: cambia el fondo a nocturno, aplica el filtro
     * de color al sprite y reproduce la música de dormir.
     */
    private void performSleepUI() {
        if (isSleeping) {
            performWakeUp();
        }
        else {
            isSleeping = true;
            degradationManager.setSleeping(true);
            blobbu.setState(BlobbuState.SLEEPING);
            SoundManager.getInstance(requireContext()).playSleepBGM();
            screenBackground.setImageResource(R.drawable.screen_background_night);
            petImage.setColorFilter(NIGHT_FILTER_COLOR, PorterDuff.Mode.SRC_ATOP);
            playBlobbuAnimation();
        }
    }

    /**
     * Despierta al Blobbu: restaura el fondo diurno, elimina el filtro
     * nocturno y vuelve a la música principal.
     * Se llama tanto al pulsar el botón de dormir como automáticamente
     * al completar el sueño, o al iniciar un pomodoro o minijuego.
     */
    private void performWakeUp() {
        isSleeping = false;
        degradationManager.setSleeping(false);
        SoundManager.getInstance(requireContext()).playMainBGM();
        screenBackground.setImageResource(R.drawable.screen_background);
        petImage.clearColorFilter();
        gameController.sleepBlobbu();
        blobbu.updateState(); // Recalcula el estado tras haber dormido
        renderBlobbu(blobbu);
        playBlobbuAnimation();
    }

    // --- Render ---
    /** Lanza el render completo según la fase actual. */
    private void render() {
        if (currentPhase == EvolutionType.EGG) {
            playEggIdleAnimation();
        }
        else {
            playBlobbuAnimation();
            renderBlobbu(blobbu);
        }
    }

    /**
     * Actualiza únicamente las barras de stats.
     * No toca la animación del sprite para no interrumpirla.
     */
    private void renderBlobbu(Blobbu blobbu) {
        if (happyBar != null) {
            happyBar.setProgress(blobbu.getHappyLvl());
        }

        if (hungerBar != null) {
            hungerBar.setHunger(scaleToIcons(blobbu.getHungryLvl()));
        }

        if (sleepBar != null) {
            sleepBar.setSleep(scaleToIcons(blobbu.getSleepinessLvl()));
        }
    }

    /**
     * Convierte un valor de 0–100 a una escala de 0–6 para los iconos
     * de hambre y sueño.
     */
    private int scaleToIcons(int value) {
        return Math.round(value * 6f / 100f);
    }

    // --- Animaciones ---
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
     * Selecciona la animación correcta según la fase evolutiva y el estado
     * actual del Blobbu, y la reproduce
     */
    private void playBlobbuAnimation() {
        if (blobbu == null) return;

        syncPhase(); // Garantiza que siempre use la fase correcta

        int animRes = BlobbuAnimator.getAnimationRes(currentPhase, blobbu.getCurrentState());
        petImage.setImageResource(animRes);
        startAnimation();
    }

    // Sincroniza currentPhase con la evolución real del blobbu
    private void syncPhase() {
        if (blobbu != null && blobbu.getEvolutionType() != null
                && blobbu.getEvolutionType() != EvolutionType.EGG) {
            currentPhase = blobbu.getEvolutionType();
        }
    }

    private void startAnimation() {
        AnimationDrawable animation = (AnimationDrawable) petImage.getDrawable();
        animation.start();
    }

    /** Indica si hay un Blobbu vivo (no en fase huevo). */
    public boolean isBlobbuAlive() {
        return currentPhase != EvolutionType.EGG && blobbu != null;
    }

    public boolean isSleeping() {
        return isSleeping;
    }

    /** Activa la animación de pomodoro al iniciar una sesión. */
    public void startPomodoroAnimation() {
        if (blobbu == null || currentPhase == EvolutionType.EGG) {
            return;
        }

        blobbu.setState(BlobbuState.POMODORO);
        playBlobbuAnimation();
    }

    /** Restaura la animación normal al terminar una sesión de pomodoro. */
    public void stopPomodoroAnimation() {
        if (blobbu == null || currentPhase == EvolutionType.EGG) {
            return;
        }

        blobbu.updateState();
        renderBlobbu(blobbu);
    }

    /**
     * Parpadeo suave de la pantalla del tamagotchi al evolucionar o nacer.
     * 1. Flash blanco rápido
     * 2. Tres parpadeos del fondo
     * 3. Muestra la nueva animación
     */
    private void playEvolutionAnimation(Runnable onFinished) {
        evolutionFlash.setVisibility(View.VISIBLE);

        // --- Fase 1: Flash blanco (150ms) ---
        ObjectAnimator flashIn = ObjectAnimator.ofFloat(evolutionFlash, "alpha", 0f, 1f);
        flashIn.setDuration(150);

        ObjectAnimator flashOut = ObjectAnimator.ofFloat(evolutionFlash, "alpha", 1f, 0f);
        flashOut.setDuration(150);

        // --- Fase 2: Tres parpadeos del screenBackground ---
        // Cada parpadeo: fade out a fade in de screenBackground
        AnimatorSet blink1 = buildBlink(screenBackground, 180);
        AnimatorSet blink2 = buildBlink(screenBackground, 180);
        AnimatorSet blink3 = buildBlink(screenBackground, 180);

        // --- Secuencia completa ---
        AnimatorSet sequence = new AnimatorSet();
        sequence.playSequentially(flashIn, flashOut, blink1, blink2, blink3);

        sequence.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                evolutionFlash.setVisibility(View.INVISIBLE);
                if (onFinished != null) onFinished.run();
            }
        });

        sequence.start();
    }

    /** Genera un parpadeo (fade out + fade in) sobre cualquier View. */
    private AnimatorSet buildBlink(View target, long durationMs) {
        ObjectAnimator out = ObjectAnimator.ofFloat(target, "alpha", 1f, 0f);
        out.setDuration(durationMs);

        ObjectAnimator in = ObjectAnimator.ofFloat(target, "alpha", 0f, 1f);
        in.setDuration(durationMs);

        AnimatorSet blink = new AnimatorSet();
        blink.playSequentially(out, in);
        return blink;
    }

    /**
     * Animación de evolución estilo Pokémon:
     * el sprite se tiñe de negro, encoge, cambia al nuevo sprite y vuelve a crecer.
     * @param onSpriteSwap  Se ejecuta en el momento del swap (cuando el sprite es invisible).
     *                      Aquí dentro llama a playBlobbuAnimation() para cargar el nuevo sprite.
     * @param onFinished    Se ejecuta al terminar toda la animación.
     */
    private void playPokemonEvolutionAnimation(Runnable onSpriteSwap, Runnable onFinished) {
        // --- Fase 1: Tiñe el sprite de negro (300ms) ---
        ObjectAnimator tintIn = ObjectAnimator.ofArgb(
                petImage,
                "colorFilter",
                Color.TRANSPARENT,
                Color.BLACK
        );
        tintIn.setEvaluator(new ArgbEvaluator());
        tintIn.setDuration(300);

        // --- Fase 2: Encoge hasta desaparecer (400ms) ---
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(petImage, "scaleX", 1f, 0f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(petImage, "scaleY", 1f, 0f);
        scaleDownX.setDuration(400);
        scaleDownY.setDuration(400);
        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(scaleDownX, scaleDownY);

        // --- Fase 3: Crece de vuelta (400ms) ---
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(petImage, "scaleX", 0f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(petImage, "scaleY", 0f, 1f);
        scaleUpX.setDuration(400);
        scaleUpY.setDuration(400);
        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(scaleUpX, scaleUpY);

        // --- Fase 4: Quita el tinte negro (500ms) ---
        ObjectAnimator tintOut = ObjectAnimator.ofArgb(petImage, "colorFilter",
                Color.BLACK, Color.TRANSPARENT);
        tintOut.setEvaluator(new ArgbEvaluator());
        tintOut.setDuration(500);

        // --- Secuencia completa ---
        AnimatorSet sequence = new AnimatorSet();
        sequence.playSequentially(tintIn, scaleDown, scaleUp, tintOut);

        // Cuando scaleDown termina (el sprite es invisible), hacemos el swap
        scaleDown.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onSpriteSwap != null) onSpriteSwap.run();
            }
        });

        sequence.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Limpia el tinte por si acaso y restaura escala
                petImage.clearColorFilter();
                petImage.setScaleX(1f);
                petImage.setScaleY(1f);
                if (onFinished != null) onFinished.run();
            }
        });

        sequence.start();
    }
}