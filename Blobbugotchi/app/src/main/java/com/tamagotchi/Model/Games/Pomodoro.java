package com.tamagotchi.Model.Games;

public class Pomodoro {
    private int[] timeOptions = {5, 10, 15, 20, 25, 30};
    private int selectedTime = 15; // 15 minutos por defecto
    private double totalTime;
    private boolean isRunning;

    public void startTimer(int time) {
        this.totalTime = time;
        this.isRunning = true;
    }

    public void cancelTimer() {
        this.isRunning = false;
    }

    public void setSelectedTime(int minutes) {
        this.selectedTime = minutes;
    }

    public int getSelectedTime() {
        return selectedTime;
    }

    public int[] getTimeOptions() {
        return timeOptions;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public boolean isRunning() {
        return isRunning;
    }
}