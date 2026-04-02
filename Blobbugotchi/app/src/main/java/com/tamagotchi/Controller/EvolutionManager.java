package com.tamagotchi.Controller;

import com.tamagotchi.DataLayer.DatabaseHelper;
import com.tamagotchi.Model.Blobbu.Blobbu;
import com.tamagotchi.Model.Blobbu.EvolutionRule;
import com.tamagotchi.Model.Blobbu.ImplEvolutionRule;
import com.tamagotchi.Model.Blobbu.EvolutionType;

import java.util.List;

public class EvolutionManager {

    private static final long BABY_TO_TEEN_MS  = 2L * 24 * 60 * 60 * 1000;
    private static final long TEEN_TO_ADULT_MS = 5L * 24 * 60 * 60 * 1000;

    private final DatabaseHelper dbHelper;
    private OnEvolutionListener listener;

    public interface OnEvolutionListener {
        void onEvolution(EvolutionType newType);
    }

    public EvolutionManager(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void setOnEvolutionListener(OnEvolutionListener listener) {
        this.listener = listener;
    }

    public boolean checkEvolution(Blobbu blobbu) {
        switch (blobbu.getEvolutionType()) {
            case BABY:
                return checkTimeAndEvolve(blobbu, BABY_TO_TEEN_MS,
                        ImplEvolutionRule.getTeenRules());
            case TEEN_MEW:
            case TEEN_ART:
            case TEEN_TIDES:
                return checkTimeAndEvolve(blobbu, TEEN_TO_ADULT_MS,
                        ImplEvolutionRule.getAdultRules());
            default:
                return false;
        }
    }

    private boolean checkTimeAndEvolve(Blobbu blobbu, long requiredMs, List<EvolutionRule> rules) {
        long now = System.currentTimeMillis();
        if (now - dbHelper.getLastEvolutionTimestamp() < requiredMs) return false;

        EvolutionType next = resolveEvolution(blobbu, rules);
        applyEvolution(blobbu, next);
        return true;
    }

    private EvolutionType resolveEvolution(Blobbu blobbu, List<EvolutionRule> rules) {
        for (EvolutionRule rule : rules) {
            if (rule.isMet(blobbu)) return rule.getType();
        }

        // Fallback de seguridad: última regla de la lista
        return rules.get(rules.size() - 1).getType();
    }

    private void applyEvolution(Blobbu blobbu, EvolutionType newType) {
        blobbu.evolve(newType);
        dbHelper.saveLastEvolutionTimestamp(System.currentTimeMillis());
        dbHelper.updateBlobbu(blobbu);

        // Desbloquear en galería usando el ordinal del enum
        dbHelper.unlockCreature(newType.ordinal());

        if (listener != null) listener.onEvolution(newType);
    }
}