package com.example.blobbugotchi.Model.Games;

public class Minigame {
    private int maxScore;
    private int currentScore;

    public void updateScore(int score){
        if (score > maxScore){
            maxScore = score;
            currentScore = score;
        }
        else{
            currentScore = score;
        }
    }

    public int getMaxScore(){
        return maxScore;
    }
}