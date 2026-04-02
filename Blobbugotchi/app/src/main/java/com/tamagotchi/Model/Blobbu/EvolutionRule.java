package com.tamagotchi.Model.Blobbu;

public class EvolutionRule {

    private final EvolutionType type;
    private final EvolutionCondition condition;

    public EvolutionRule(EvolutionType type, EvolutionCondition condition) {
        this.type = type;
        this.condition = condition;
    }

    public EvolutionType getType() {
        return type;
    }

    public boolean isMet(Blobbu blobbu) {
        return condition.isMet(blobbu);
    }
}