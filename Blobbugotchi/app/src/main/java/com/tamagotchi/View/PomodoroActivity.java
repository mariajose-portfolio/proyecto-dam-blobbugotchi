package com.tamagotchi.View;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.tamagotchi.Controller.GameController;
import com.tamagotchi.Controller.GameFragment;
import com.tamagotchi.Controller.SoundManager;
import com.tamagotchi.Model.Games.Pomodoro;
import com.tamagotchi.R;

public class PomodoroActivity extends BaseActivity {

    private static final int DEFAULT_MINUTES = 15; // 15 por defect

    private CircularTimerView circularTimer;
    private TextView tvTimer;
    private ImageButton btnPlayPause;
    private ImageButton btnStop;

    private CountDownTimer countDownTimer;
    private boolean isRunning = false;
    private long totalTimeMillis;
    private long timeLeftMillis;

    private GameController gameController;
    private Pomodoro pomodoro;

    private PopupWindow selectorPopup;
    private int selectedMinutes = DEFAULT_MINUTES;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);

        gameController = GameController.getInstance(this);
        pomodoro = new Pomodoro();

        circularTimer = findViewById(R.id.circularTimer);
        tvTimer = findViewById(R.id.tvTimer);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnStop = findViewById(R.id.btnStop);

        totalTimeMillis = DEFAULT_MINUTES * 60 * 1000L;
        timeLeftMillis = totalTimeMillis;

        updateTimerDisplay(timeLeftMillis);
        circularTimer.setProgress(0f);

        btnPlayPause.setOnClickListener(v -> {
            if (isRunning) pauseTimer();
            else startTimer(selectedMinutes);
        });

        btnStop.setOnClickListener(v -> cancelTimer());

        // Arrancar la animación del blobbu
        ImageView ivMascot = findViewById(R.id.ivMascot);
        ivMascot.setImageResource(R.drawable.anim_baby_reading);
        ivMascot.post(() -> {
            AnimationDrawable anim = (AnimationDrawable) ivMascot.getDrawable();
            anim.start();
        });

        // Mostrar el selector automáticamente al entrar
        circularTimer.post(() -> showDurationSelector());
    }

    // --- POPUP SELECTOR DE DURACIÓN ---
    private void showDurationSelector() {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_pomodoro_selector, null);

        int popupWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
        selectorPopup = new PopupWindow(popupView, popupWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT, false);
        selectorPopup.setElevation(10f);

        // Mapa de valor → ID de vista, usando los timeOptions del modelo
        int[] timeOptions = pomodoro.getTimeOptions();
        int[] optionIds   = { R.id.opt_5, R.id.opt_10, R.id.opt_15,
                R.id.opt_20, R.id.opt_25, R.id.opt_30 };

        TextView[] optionViews = new TextView[timeOptions.length];

        for (int i = 0; i < timeOptions.length; i++) {
            optionViews[i] = popupView.findViewById(optionIds[i]);
            final int minutes = timeOptions[i];
            final int index   = i;

            optionViews[i].setOnClickListener(v -> {
                updateSelection(optionViews, index);
                selectedMinutes = minutes;
            });
        }

        // Marcar la opción por defecto visualmente
        int defaultIndex = findIndex(timeOptions, DEFAULT_MINUTES);
        if (defaultIndex >= 0) updateSelection(optionViews, defaultIndex);

        popupView.findViewById(R.id.btn_cancel_pomodoro).setOnClickListener(v -> {
            selectorPopup.dismiss();
            finish();
        });

        popupView.findViewById(R.id.btn_confirm_pomodoro).setOnClickListener(v -> {
            selectorPopup.dismiss();
            totalTimeMillis = selectedMinutes * 60 * 1000L;
            timeLeftMillis  = totalTimeMillis;
            updateTimerDisplay(timeLeftMillis);
            circularTimer.setProgress(1f);
            startTimer(selectedMinutes);
        });

        selectorPopup.showAtLocation(circularTimer, Gravity.CENTER, 0, 0);
    }

    /**
     * Actualiza el estilo visual de las opciones, marcando la seleccionada
     */
    private void updateSelection(TextView[] views, int selectedIndex) {
        for (TextView tv : views) {
            tv.setBackgroundResource(R.drawable.bg_time_option_normal);
            tv.setTextColor(getColor(R.color.black));
        }

        views[selectedIndex].setBackgroundResource(R.drawable.bg_time_option_selected);
        views[selectedIndex].setTextColor(getColor(R.color.dark_blue));
    }

    /**
     * Encuentra el índice de un valor en un array, devuelve -1 si no existe
     */
    private int findIndex(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) return i;
        }
        return -1;
    }

    // --- TIMER ---
    public void startTimer(int minutes) {
        if (isRunning) return;

        long newTotalMillis = minutes * 60 * 1000L;
        if (newTotalMillis != totalTimeMillis) {
            totalTimeMillis = newTotalMillis;
            timeLeftMillis  = totalTimeMillis;
        }

        isRunning = true;
        pomodoro.startTimer(minutes);
        btnPlayPause.setImageResource(R.drawable.ic_pause);
        gameController.startPomodoro(minutes);

        countDownTimer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;
                updateTimerDisplay(millisUntilFinished);
                circularTimer.setProgress((float) millisUntilFinished / totalTimeMillis);
            }

            @Override
            public void onFinish() {
                timeLeftMillis = 0;
                updateTimerDisplay(0);
                circularTimer.setProgress(0f);
                onTimerFinish();
            }
        }.start();

        notifyStudyingState(true);

        // Activar animación de pomodoro en el blobbu
        gameController.setPomodoroState(true);
    }

    /**
     * Notifica al GameFragment para cambiar la animación del blobbu
     * según si el pomodoro está activo o no
     */
    private void notifyStudyingState(boolean studying) {
        GameFragment gameFragment = (GameFragment) getSupportFragmentManager()
                .findFragmentById(R.id.gameFragmentContainer);

        if (gameFragment != null) {
            if (studying) gameFragment.startPomodoroAnimation();
            else gameFragment.stopPomodoroAnimation();
        }
    }

    public void cancelTimer() {
        if (!isRunning) {
            if (countDownTimer != null) countDownTimer.cancel();
            pomodoro.cancelTimer();
            finish();

            return;
        }

        int minutesLeft = (int) (timeLeftMillis / 1000) / 60;
        int secondsLeft = (int) (timeLeftMillis / 1000) % 60;
        String timeLeft = String.format("%02d:%02d", minutesLeft, secondsLeft);

        if (countDownTimer != null) countDownTimer.cancel();
        isRunning = false;
        btnPlayPause.setImageResource(R.drawable.ic_play);

        new AlertDialog.Builder(this)
                .setMessage("¿Seguro que quieres salir? Te quedan " + timeLeft + " para completar el pomodoro.")
                .setCancelable(false)
                .setPositiveButton("CONFIRMAR", (dialog, which) -> {
                    // Calcular tiempo transcurrido antes de cancelar
                    long elapsedMillis = totalTimeMillis - timeLeftMillis;
                    showResultPopup(elapsedMillis, false);
                })
                .setNegativeButton("CANCELAR", (dialog, which) -> {
                    dialog.dismiss();
                    startTimer(selectedMinutes);
                })
                .show();
    }

    public void onTimerFinish() {
        isRunning = false;
        pomodoro.cancelTimer();
        btnPlayPause.setImageResource(R.drawable.ic_play);

        // Mostrar popup de resumen — tiempo completo = totalTimeMillis
        showResultPopup(totalTimeMillis, true);
    }

    private void pauseTimer() {
        if (!isRunning) return;
        if (countDownTimer != null) countDownTimer.cancel();
        isRunning = false;
        btnPlayPause.setImageResource(R.drawable.ic_play);
    }

    private void updateTimerDisplay(long millisUntilFinished) {
        int minutes = (int) (millisUntilFinished / 1000) / 60;
        int seconds = (int) (millisUntilFinished / 1000) % 60;
        tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
    }

    /**
     * Muestra el popup de resumen al terminar o cancelar el pomodoro.
     * @param elapsedMillis tiempo que ha transcurrido realmente
     * @param completed true si terminó el tiempo, false si se canceló
     */
    private void showResultPopup(long elapsedMillis, boolean completed) {
        // Calcular tiempo transcurrido
        int elapsedSeconds = (int) (elapsedMillis / 1000);
        int minutes = elapsedSeconds / 60;
        int seconds = elapsedSeconds % 60;

        // Calcular horas transcurridas para registrar el tiempo junto al Blobbu
        double hoursSpent = elapsedMillis / 3_600_000.0;

        // Inflar el layout
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_pomodoro_result, null);
        int popupWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);

        PopupWindow resultPopup = new PopupWindow(popupView, popupWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT, false);
        resultPopup.setElevation(10f);

        // Rellenar datos
        TextView tvTime = popupView.findViewById(R.id.tv_result_time);
        TextView tvMessage = popupView.findViewById(R.id.tv_result_message);

        tvTime.setText(String.format("%02d min %02d s", minutes, seconds));
        tvMessage.setText(completed
                ? "¡Buen trabajo! Tu Blobbu está feliz de haber estudiado contigo."
                : "Puedes conseguir tus objetivos la próxima vez.");

        // Botón salir — registra el tiempo y cierra
        popupView.findViewById(R.id.btn_exit_pomodoro).setOnClickListener(v -> {
            resultPopup.dismiss();
            if (completed) gameController.completePomodoro(hoursSpent);
            else gameController.cancelPomodoro(hoursSpent);
            finish();
        });

        // Botón otro pomodoro — registra el tiempo y abre de nuevo el selector de duración
        popupView.findViewById(R.id.btn_repeat).setOnClickListener(v -> {
            resultPopup.dismiss();
            if (completed) gameController.completePomodoro(hoursSpent);
            else gameController.cancelPomodoro(hoursSpent);

            // Resetear el timer visualmente
            timeLeftMillis  = totalTimeMillis;
            updateTimerDisplay(timeLeftMillis);
            circularTimer.setProgress(0f);

            // Mostrar el selector de duración de nuevo
            showDurationSelector();
        });

        resultPopup.showAtLocation(circularTimer, Gravity.CENTER, 0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SoundManager.getInstance(this).playPomodoroBGM();
    }

    @Override
    protected void onPause() {
        super.onPause(); // guarda el progreso via BaseActivity
        SoundManager.getInstance(this).pauseBGM(); // Pausa la música
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}