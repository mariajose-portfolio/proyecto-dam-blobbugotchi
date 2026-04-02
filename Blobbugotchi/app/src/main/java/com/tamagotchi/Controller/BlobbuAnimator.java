package com.tamagotchi.Controller;

import com.tamagotchi.Model.Blobbu.BlobbuState;
import com.tamagotchi.Model.Blobbu.EvolutionType;
import com.tamagotchi.R;

public class BlobbuAnimator {

    public static int getAnimationRes(EvolutionType phase, BlobbuState state) {
        switch (phase) {
            case BABY:   return getBabyAnim(state);
            case TEEN_MEW:   return getMewAnim(state);
            case TEEN_ART:   return getArtAnim(state);
            case TEEN_TIDES: return getTidesAnim(state);
            case ADULT_MEW:  return getAdultMewAnim(state);
            case ADULT_ART:  return getAdultArtAnim(state);
            case ADULT_MER:  return getAdultMerAnim(state);
            //case ADULT_SECRET: return getAdultSecretAnim(state);
            default: return R.drawable.anim_baby_neutral;
        }
    }

    private static int getBabyAnim(BlobbuState state) {
        switch (state) {
            case HAPPY:    return R.drawable.anim_baby_happy;
            case SAD:      return R.drawable.anim_baby_sad;
            case HUNGRY:   return R.drawable.anim_baby_hungry;
            case EATING:   return R.drawable.anim_baby_eating;
            case SLEEPING: return R.drawable.anim_baby_sleep;
            case POMODORO: return R.drawable.anim_baby_reading;
            default:       return R.drawable.anim_baby_neutral;
        }
    }

    private static int getMewAnim(BlobbuState state) {
        switch (state) {
            case HAPPY:    return R.drawable.anim_mew_happy;
            case SAD:      return R.drawable.anim_mew_sad;
            case HUNGRY:   return R.drawable.anim_mew_hungry;
            case EATING:   return R.drawable.anim_mew_eating;
            case SLEEPING: return R.drawable.anim_mew_sleep;
            case POMODORO: return R.drawable.anim_mew_purr;
            default:       return R.drawable.anim_mew_neutral;
        }
    }

    private static int getArtAnim(BlobbuState state) {
        switch (state) {
            case HAPPY:    return R.drawable.anim_artsy_happy;
            case SAD:      return R.drawable.anim_artsy_sad;
            case HUNGRY:   return R.drawable.anim_artsy_hungry;
            case EATING:   return R.drawable.anim_artsy_eating;
            case SLEEPING: return R.drawable.anim_artsy_sleep;
            case POMODORO: return R.drawable.anim_artsy_painting;
            default:       return R.drawable.anim_artsy_neutral;
        }
    }

    private static int getTidesAnim(BlobbuState state) {
        switch (state) {
            case HAPPY:    return R.drawable.anim_tides_happy;
            case SAD:      return R.drawable.anim_tides_sad;
            case HUNGRY:   return R.drawable.anim_tides_hungry;
            case EATING:   return R.drawable.anim_tides_eating;
            case SLEEPING: return R.drawable.anim_tides_sleep;
            case POMODORO: return R.drawable.anim_tides_sing;
            default:       return R.drawable.anim_tides_neutral;
        }
    }

    private static int getAdultMewAnim(BlobbuState state) {
        switch (state) {
            case HAPPY:    return R.drawable.anim_mew_adult_happy;
            case SAD:      return R.drawable.anim_mew_adult_sad;
            case HUNGRY:   return R.drawable.anim_mew_adult_hungry;
            case EATING:   return R.drawable.anim_mew_adult_eating;
            case SLEEPING: return R.drawable.anim_mew_adult_sleep;
            case POMODORO: return R.drawable.anim_mew_adult_hunt;
            default:       return R.drawable.anim_mew_adult_neutral;
        }
    }

    private static int getAdultArtAnim(BlobbuState state) {
        switch (state) {
            case HAPPY:    return R.drawable.anim_adult_artsy_happy;
            case SAD:      return R.drawable.anim_adult_artsy_sad;
            case HUNGRY:   return R.drawable.anim_adult_artsy_hungry;
            case EATING:   return R.drawable.anim_adult_artsy_eating;
            case SLEEPING: return R.drawable.anim_adult_artsy_sleep;
            case POMODORO: return R.drawable.anim_adult_artsy_painting;
            default:       return R.drawable.anim_adult_artsy_neutral;
        }
    }

    private static int getAdultMerAnim(BlobbuState state) {
        switch (state) {
            case HAPPY:    return R.drawable.anim_mer_adult_happy;
            case SAD:      return R.drawable.anim_mer_adult_sad;
            case HUNGRY:   return R.drawable.anim_mer_adult_hungry;
            case EATING:   return R.drawable.anim_mer_adult_eating;
            case SLEEPING: return R.drawable.anim_mer_adult_sleep;
            case POMODORO: return R.drawable.anim_mer_adult_sing;
            default:       return R.drawable.anim_mer_adult_neutral;
        }
    }

    /*private static int getAdultSecretAnim(BlobbuState state) {
        switch (state) {
            case HAPPY:    return R.drawable.anim_secret_adult_happy;
            case SAD:      return R.drawable.anim_secret_adult_sad;
            case HUNGRY:   return R.drawable.anim_secret_adult_hungry;
            case EATING:   return R.drawable.anim_secret_adult_eating;
            case SLEEPING: return R.drawable.anim_secret_adult_sleep;
            case POMODORO: return R.drawable.anim_secret_adult_reading;
            default:       return R.drawable.anim_secret_adult_neutral;
        }
    }*/
}