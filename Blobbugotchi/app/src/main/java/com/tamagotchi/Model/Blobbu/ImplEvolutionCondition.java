package com.tamagotchi.Model.Blobbu;

public class ImplEvolutionCondition {

    public static EvolutionCondition minScore(int min) {
        return blobbu -> blobbu.getMaxScore() >= min;
    }

    public static EvolutionCondition minTimeTogether(double hours) {
        return blobbu -> blobbu.getTimeTogether() >= hours;
    }

    public static EvolutionCondition minHappy(int min) {
        return blobbu -> blobbu.getHappyLvl() >= min;
    }

    public static EvolutionCondition noCareMistakes() {
        return blobbu -> blobbu.getCareMistakes() == 0;
    }

    public static EvolutionCondition previousEvolution(EvolutionType type) {
        return blobbu -> blobbu.getPreviousEvolutionType() == type;
    }

    public static EvolutionCondition all(EvolutionCondition... conditions) {
        return blobbu -> {
            for (EvolutionCondition c : conditions) {
                if (!c.isMet(blobbu)) return false;
            }
            return true;
        };
    }
}