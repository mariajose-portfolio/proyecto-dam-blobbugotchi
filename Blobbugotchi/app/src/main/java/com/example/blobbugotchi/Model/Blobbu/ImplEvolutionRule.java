package com.example.blobbugotchi.Model.Blobbu;

import static com.example.blobbugotchi.Model.Blobbu.ImplEvolutionCondition.all;
import static com.example.blobbugotchi.Model.Blobbu.ImplEvolutionCondition.minHappy;
import static com.example.blobbugotchi.Model.Blobbu.ImplEvolutionCondition.minScore;
import static com.example.blobbugotchi.Model.Blobbu.ImplEvolutionCondition.minTimeTogether;
import static com.example.blobbugotchi.Model.Blobbu.ImplEvolutionCondition.noCareMistakes;
import static com.example.blobbugotchi.Model.Blobbu.ImplEvolutionCondition.previousEvolution;

import java.util.Arrays;
import java.util.List;

public class ImplEvolutionRule {

    public static List<EvolutionRule> getTeenRules() {
        return Arrays.asList(
                new EvolutionRule(EvolutionType.TEEN_MEW, minScore(100)),
                new EvolutionRule(EvolutionType.TEEN_TIDES, minTimeTogether(1.0)),
                new EvolutionRule(EvolutionType.TEEN_ART,  blobbu -> true) // En caso de no cumplir ninguna, consigue este
        );
    }

    public static List<EvolutionRule> getAdultRules() {
        return Arrays.asList(
                new EvolutionRule(EvolutionType.ADULT_ART, previousEvolution(EvolutionType.TEEN_ART)),
                new EvolutionRule(EvolutionType.ADULT_MEW, previousEvolution(EvolutionType.TEEN_MEW)),
                new EvolutionRule(EvolutionType.ADULT_MER, previousEvolution(EvolutionType.TEEN_TIDES))
                //new EvolutionRule(EvolutionType.ADULT_SECRET, all(minTimeTogether(2.0), noCareMistakes())),
        );
    }
}