package com.tamagotchi.Model.Blobbu;

public class EvolutionRule implements EvolutionCondition{
    private EvolutionType type;

    private int minHappy;
    private int minHungry;
    private int minSleep;
    private int maxCareMistakes;
    private double minTimeTogether;
    private int maxScoreMinigame;

    public EvolutionRule(EvolutionType type, int minHappy, int minHungry, int minSleep,
            int maxCareMistakes, double minTimeTogether, int maxScoreMinigame) {
        this.type = type;
        this.minHappy = minHappy;
        this.minHungry = minHungry;
        this.minSleep = minSleep;
        this.maxCareMistakes = maxCareMistakes;
        this.minTimeTogether = minTimeTogether;
        this.maxScoreMinigame = maxScoreMinigame;
    }

    public EvolutionType getType() {
        return type;
    }

    /**
     * Comprueba si el blobbu cumple las condiciones para esta evolución
     */
    @Override
    public boolean isMet(Blobbu blobbu) {
        return blobbu.getHappyLvl() >= minHappy
                && blobbu.getHungryLvl() >= minHungry
                && blobbu.getSleepinessLvl() >= minSleep
                && blobbu.getCareMistakes() <= maxCareMistakes
                && blobbu.getTimeTogether() >= minTimeTogether;
    }
}

/**
 * ---- EJEMPLOS DE DECLARACIÓN DE REGLAS DE EVOLUCIÓN: ----
 * List<EvolutionRule> teenRules = List.of(
 *     new EvolutionRule(EvolutionType.TEEN_MEW, 70, 70, 70, 1, 10),
 *     new EvolutionRule(EvolutionType.TEEN_ART, 50, 80, 50, 2, 8),
 *     new EvolutionRule(EvolutionType.TEEN_TIDES, 80, 50, 50, 0, 12),
 *     new EvolutionRule(EvolutionType.TEEN_BAD, 40, 40, 40, 3, 6)
 * );
 *
 * List<EvolutionRule> adultRules = List.of(
 *     new EvolutionRule(EvolutionType.ADULT_MEW, 80, 80, 80, 1, 20),
 *     new EvolutionRule(EvolutionType.ADULT_ART, 30, 30, 30, 4, 15),
 *     new EvolutionRule(EvolutionType.ADULT_MER, 60, 60, 60, 2, 18),
 *     new EvolutionRule(EvolutionType.ADULT_SECRET, 90, 90, 70, 0, 25),
 *     new EvolutionRule(EvolutionType.ADULT_BAD, 95, 95, 95, 0, 30)
 * );
 *
 * PARA ACOMPROBAR SI LA CONDICIÓN SECRETA SE CUMPLE:
 * if (!secretUnlocked) {
 *     adultRules.removeIf(
 *         rule -> rule.getType() == EvolutionType.ADULT_SECRET
 *     );
 * }
 */