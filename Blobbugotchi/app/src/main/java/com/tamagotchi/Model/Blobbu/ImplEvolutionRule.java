package com.tamagotchi.Model.Blobbu;

import static com.tamagotchi.Model.Blobbu.ImplEvolutionCondition.all;
import static com.tamagotchi.Model.Blobbu.ImplEvolutionCondition.minHappy;
import static com.tamagotchi.Model.Blobbu.ImplEvolutionCondition.minScore;
import static com.tamagotchi.Model.Blobbu.ImplEvolutionCondition.minTimeTogether;
import static com.tamagotchi.Model.Blobbu.ImplEvolutionCondition.noCareMistakes;
import static com.tamagotchi.Model.Blobbu.ImplEvolutionCondition.previousEvolution;

import java.util.Arrays;
import java.util.List;

public class ImplEvolutionRule {

    /**
     * BABY → TEEN
     * Orden importante: MEW y TIDES primero, ART es el fallback (happyLvl >= 5).
     */
    public static List<EvolutionRule> getTeenRules() {
        return Arrays.asList(
                new EvolutionRule(EvolutionType.TEEN_MEW,   minScore(100)),
                new EvolutionRule(EvolutionType.TEEN_TIDES, minTimeTogether(1.0)),
                new EvolutionRule(EvolutionType.TEEN_ART,   minHappy(5))   // fallback
        );
    }

    /**
     * TEEN → ADULT
     * God of Tides va antes que MerFishie porque sus condiciones son un superconjunto.
     */
    public static List<EvolutionRule> getAdultRules() {
        return Arrays.asList(
                new EvolutionRule(EvolutionType.ADULT_SECRET,
                        all(minTimeTogether(2.0), noCareMistakes())),

                new EvolutionRule(EvolutionType.ADULT_MER,
                        minTimeTogether(2.0)),

                new EvolutionRule(EvolutionType.ADULT_MEW,
                        previousEvolution(EvolutionType.TEEN_MEW)),

                new EvolutionRule(EvolutionType.ADULT_ART,
                        previousEvolution(EvolutionType.TEEN_ART))
        );
    }
}