package com.tamagotchi.Controller;

import com.tamagotchi.Model.Blobbu.Blobbu;
import com.tamagotchi.Model.Blobbu.EvolutionRule;
import com.tamagotchi.Model.Blobbu.EvolutionType;

import java.util.List;

public class EvolutionManager {

    /**
     * Devuelve el tipo de evolución que se debe aplicar
     * o null si no hay ninguna válida
     */
    public EvolutionType checkEvolution(List<EvolutionRule> rules, Blobbu blobbu) {
        for (EvolutionRule rule : rules) {
            if (rule.isMet(blobbu)) {
                return rule.getType();
            }
        }

        return null;
    }
}